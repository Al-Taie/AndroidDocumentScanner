LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
OPENCV_INSTALL_MODULES:=on
include sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := native_scanner
LOCAL_SRC_FILES := scan.cpp
LOCAL_LDFLAGS   += -ljnigraphics
LOCAL_LDLIBS    += -lm -llog -landroid
include $(BUILD_SHARED_LIBRARY)
