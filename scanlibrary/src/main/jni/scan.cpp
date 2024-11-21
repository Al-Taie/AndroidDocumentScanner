#include "com_scanner_library_NativeScanner.h"
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <string>
#include <vector>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include <android/bitmap.h>

#define LOG_TAG "DEBUGGING"

using namespace cv;
using namespace std;

bool FILTER_ENABLED = true;
bool APPLY_CLAHE = true;
double SCALE_FACTOR = 2.0;
double CONTRAST_VALUE = 1.25;
double CONTRAST_LIMIT_THRESHOLD = 1.5;

// Helper function to calculate the angle between three points
double angle(const Point &pt1, const Point &pt2, const Point &pt0) {
    const double dx1 = pt1.x - pt0.x;
    const double dy1 = pt1.y - pt0.y;
    const double dx2 = pt2.x - pt0.x;
    const double dy2 = pt2.y - pt0.y;
    return (dx1 * dx2 + dy1 * dy2) /
           sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
}

// Helper function to sort points in clockwise order
vector<Point> sortPointsClockwise(const vector<Point> &pts) {
    // Compute the centroid of the points
    Point2f centroid(0, 0);
    for (const Point &pt : pts) {
        centroid.x += pt.x;
        centroid.y += pt.y;
    }
    centroid.x /= pts.size();
    centroid.y /= pts.size();

    // Sort points by angle from the centroid
    vector<Point> sortedPts = pts;
    sort(sortedPts.begin(), sortedPts.end(), [&centroid](const Point &a, const Point &b) {
        double angleA = atan2(a.y - centroid.y, a.x - centroid.x);
        double angleB = atan2(b.y - centroid.y, b.x - centroid.x);
        return angleA < angleB;
    });

    return sortedPts;
}

// Find the largest square in an image
vector<Point> findLargestSquare(const Mat &image, const double minArea = 3500.0,
                                const double maxAspectRatioDiff = 0.5) {
    Mat gray, blurred, edged;
    cvtColor(image, gray, COLOR_BGR2GRAY);
    GaussianBlur(gray, blurred, Size(5, 5), 0);
    Canny(blurred, edged, 75, 200);

    vector<vector<Point>> contours;
    findContours(edged, contours, RETR_LIST, CHAIN_APPROX_SIMPLE);

    vector<Point> largestSquare;
    double maxArea = 0;

    for (const auto &contour: contours) {
        vector<Point> approx;
        approxPolyDP(contour, approx, arcLength(contour, true) * 0.02, true);

        if (approx.size() == 4 && isContourConvex(approx) && contourArea(approx) > minArea) {
            double maxCosine = 0;
            for (int j = 2; j < 5; j++) {
                double cosine = fabs(angle(approx[j % 4], approx[j - 2], approx[j - 1]));
                maxCosine = MAX(maxCosine, cosine);
            }

            if (maxCosine < maxAspectRatioDiff) {
                const double area = contourArea(approx);
                if (area > maxArea) {
                    maxArea = area;
                    largestSquare = sortPointsClockwise(approx); // Sort the points!
                }
            }
        }
    }

    __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "Scanning size() %zu", largestSquare.size());

    if (largestSquare.empty()) {
        const int width = image.cols;
        const int height = image.rows;
        return {Point(0, 0), Point(width, 0), Point(0, height), Point(width, height)};
    }

    return largestSquare;
}

auto matToBitmap(JNIEnv *env, const Mat &src, const bool needPremultiplyAlpha, jobject bitmap_config) {
    const auto java_bitmap_class = env->FindClass("android/graphics/Bitmap");
    jmethodID mid = env->GetStaticMethodID(java_bitmap_class, "createBitmap",
                                           "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    auto bitmap = env->CallStaticObjectMethod(java_bitmap_class, mid, src.size().width,
                                              src.size().height, bitmap_config);
    AndroidBitmapInfo bitmapInfo;
    void *pixels = nullptr;

    try {
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) >= 0);
        CV_Assert(src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);

        if (bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            Mat tmp(bitmapInfo.height, bitmapInfo.width, CV_8UC4, pixels);
            if (src.type() == CV_8UC1) {
                cvtColor(src, tmp, CV_GRAY2RGBA);
            } else if (src.type() == CV_8UC3) {
                cvtColor(src, tmp, CV_RGB2RGBA);
            } else if (src.type() == CV_8UC4) {
                if (needPremultiplyAlpha) {
                    cvtColor(src, tmp, COLOR_RGBA2mRGBA);
                } else {
                    src.copyTo(tmp);
                }
            }
        } else {
            Mat tmp(bitmapInfo.height, bitmapInfo.width, CV_8UC2, pixels);
            if (src.type() == CV_8UC1) {
                cvtColor(src, tmp, CV_GRAY2BGR565);
            } else if (src.type() == CV_8UC3) {
                cvtColor(src, tmp, CV_RGB2BGR565);
            } else if (src.type() == CV_8UC4) {
                cvtColor(src, tmp, CV_RGBA2BGR565);
            }
        }

        AndroidBitmap_unlockPixels(env, bitmap);
        return bitmap;
    } catch (Exception e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("org/opencv/core/CvException");
        if (!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return bitmap;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return bitmap;
    }
}


jobject createBitmap(JNIEnv* env, jobject bitmap, cv::Mat& dst) {
    // Get the Bitmap class and its getConfig method
    jclass java_bitmap_class = env->FindClass("android/graphics/Bitmap");
    jmethodID mid = env->GetMethodID(java_bitmap_class, "getConfig", "()Landroid/graphics/Bitmap$Config;");

    // Get the Bitmap's configuration
    jobject bitmap_config = env->CallObjectMethod(bitmap, mid);
    jobject result = matToBitmap(env, dst, false, bitmap_config);
    AndroidBitmap_unlockPixels(env, bitmap);
    return result;
}

Mat applyFilters(const Mat &src) {
    if (!FILTER_ENABLED) return src;

    Mat result = src.clone();

    if (APPLY_CLAHE && result.channels() >= 3) {
        cvtColor(result, result, COLOR_BGR2HSV);

        const Ptr<CLAHE> clahe = createCLAHE();
        clahe->setClipLimit(CONTRAST_LIMIT_THRESHOLD);

        vector<Mat> hsvChannels;
        split(result, hsvChannels);

        clahe->apply(hsvChannels[2], hsvChannels[2]);
        clahe->apply(hsvChannels[1], hsvChannels[1]);

        merge(hsvChannels, result);
        cvtColor(result, result, COLOR_HSV2BGR);
    }

    result.convertTo(result, -1, CONTRAST_VALUE, 29);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_scanner_library_NativeScanner_configureScanner(
        JNIEnv *env,
        jobject obj,
        const jboolean filterEnabled,
        const jboolean applyCLAHE,
        const jdouble scaleFactor,
        const jdouble contrastValue,
        const jdouble contrastLimitThreshold
) {
    FILTER_ENABLED = filterEnabled;
    APPLY_CLAHE = applyCLAHE;
    CONTRAST_VALUE = contrastValue;
    CONTRAST_LIMIT_THRESHOLD = contrastLimitThreshold;
    SCALE_FACTOR = scaleFactor;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_scanner_library_NativeScanner_getScannedBitmap(
        JNIEnv *env, jobject thiz,
        jobject bitmap,
        jfloat x1, jfloat y1, jfloat x2, jfloat y2,
        jfloat x3, jfloat y3, jfloat x4, jfloat y4
) {
    AndroidBitmapInfo bitmapInfo;
    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Failed to get bitmap bitmapInfo.");
        return NULL;
    }

    void *pixels = nullptr;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "Failed to lock pixels.");
        return NULL;
    }

    Mat mbgra(bitmapInfo.height, bitmapInfo.width, CV_8UC4, pixels);

    vector<Point2f> src_points = {
            Point2f(x1, y1), Point2f(x2, y2), Point2f(x3, y3), Point2f(x4, y4)
    };

    float width = norm(src_points[0] - src_points[1]);
    float height = norm(src_points[0] - src_points[3]);

    vector<Point2f> dst_points = {
            Point2f(0, 0), Point2f(width, 0), Point2f(width, height), Point2f(0, height)
    };

    Mat transform_matrix = getPerspectiveTransform(src_points, dst_points);
    Mat cropped_image;
    warpPerspective(mbgra, cropped_image, transform_matrix, Size(width, height), INTER_LANCZOS4);

    Mat filtered_image = applyFilters(cropped_image);
    Size output_size(width * SCALE_FACTOR, height * SCALE_FACTOR);
    Mat high_res_image;
    resize(filtered_image, high_res_image, output_size, 0, 0, INTER_LANCZOS4);

    return createBitmap(env, bitmap, high_res_image);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_scanner_library_NativeScanner_getMagicColorBitmap(JNIEnv *env, jobject thiz,
                                                           jobject bitmap) {
    __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "Scanning getMagicColorBitmap");
    AndroidBitmapInfo bitmapInfo;
    void *pixels = nullptr;

    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) < 0) {
        __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "AndroidBitmap_getInfo() failed!");
        return NULL;
    }

    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "Bitmap format is not RGBA_8888!");
        return NULL;
    }

    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "AndroidBitmap_lockPixels() failed!");
        return NULL;
    }

    const Mat mbgra(bitmapInfo.height, bitmapInfo.width, CV_8UC4, pixels);
    Mat dstBitmap = mbgra.clone();
    constexpr float alpha = 1.9;
    constexpr float beta = -80;
    dstBitmap.convertTo(dstBitmap, -1, alpha, beta);

    return createBitmap(env, bitmap, dstBitmap);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_scanner_library_NativeScanner_getBwBitmap(JNIEnv *env, jobject thiz, jobject bitmap) {
    __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "Scanning getBwBitmap");
    AndroidBitmapInfo bitmapInfo;
    void *pixels = nullptr;

    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) < 0) {
        __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "AndroidBitmap_getInfo() failed!");
        return NULL;
    }

    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "Bitmap format is not RGBA_8888!");
        return NULL;
    }

    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "AndroidBitmap_lockPixels() failed!");
        return NULL;
    }

    const Mat mbgra(bitmapInfo.height, bitmapInfo.width, CV_8UC4, pixels);
    Mat dstBitmap = mbgra.clone();

    cvtColor(mbgra, dstBitmap, COLOR_BGR2GRAY);
    constexpr float alpha = 2.2;
    constexpr float beta = 0;
    dstBitmap.convertTo(dstBitmap, -1, alpha, beta);
    threshold(dstBitmap, dstBitmap, 0, 255, THRESH_BINARY | THRESH_OTSU);

    return createBitmap(env, bitmap, dstBitmap);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_scanner_library_NativeScanner_getGrayBitmap(JNIEnv *env, jobject thiz, jobject bitmap) {
    __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "Scanning getGrayBitmap");
    AndroidBitmapInfo bitmapInfo;
    void *pixels = nullptr;

    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) < 0) {
        __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "AndroidBitmap_getInfo() failed!");
        return NULL;
    }

    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "Bitmap format is not RGBA_8888!");
        return NULL;
    }

    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "AndroidBitmap_lockPixels() failed!");
        return NULL;
    }

    const Mat mbgra(bitmapInfo.height, bitmapInfo.width, CV_8UC4, pixels);
    Mat dstBitmap = mbgra.clone();
    cvtColor(mbgra, dstBitmap, COLOR_BGR2GRAY);

    return createBitmap(env, bitmap, dstBitmap);
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_scanner_library_NativeScanner_getPoints(JNIEnv *env, jobject thiz, jobject bitmap) {
    __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "Scanning getPoints");
    AndroidBitmapInfo bitmapInfo;
    void *pixels = nullptr;

    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) < 0) {
        __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "AndroidBitmap_getInfo() failed!");
        return 0;
    }

    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "Bitmap format is not RGBA_8888!");
        return 0;
    }

    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "AndroidBitmap_lockPixels() failed!");
        return 0;
    }

    const Mat mbgra(bitmapInfo.height, bitmapInfo.width, CV_8UC4, pixels);
    vector<Point> img_pts = findLargestSquare(mbgra);
    jfloatArray jArray = env->NewFloatArray(8);

    if (jArray != NULL) {
        jfloat *ptr = env->GetFloatArrayElements(jArray, NULL);
        for (int i = 0, j = i + 4; j < 8; i++, j++) {
            ptr[i] = img_pts[i].x;
            ptr[j] = img_pts[i].y;
        }
        env->ReleaseFloatArrayElements(jArray, ptr, 0);
    }
    AndroidBitmap_unlockPixels(env, bitmap);
    return jArray;
}
