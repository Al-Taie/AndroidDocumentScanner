package com.scanner.library

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.io.IOException
import java.lang.ClassCastException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects

/**
 * Created by jhansi on 04/04/15.
 */
class PickImageFragment : Fragment() {
    private var view: View? = null
    private var cameraButton: ImageButton? = null
    private var galleryButton: ImageButton? = null
    private var fileUri: Uri? = null
    private var scanner: IScanner? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity !is IScanner) {
            throw ClassCastException("Activity must implement IScanner")
        }
        this.scanner = activity as IScanner
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.pick_image_fragment, null)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        cameraButton = requireView().findViewById<View?>(R.id.cameraButton) as ImageButton
        cameraButton!!.setOnClickListener(CameraButtonClickListener())
        galleryButton = requireView().findViewById<View?>(R.id.selectButton) as ImageButton
        galleryButton!!.setOnClickListener(GalleryClickListener())
        if (isIntentPreferenceSet()) {
            handleIntentPreference()
        } else {
            requireActivity().finish()
        }
    }

    private fun clearTempImages() {
        try {
            val tempFolder = File(ScanConstants.IMAGE_PATH)
            for (f in Objects.requireNonNull<Array<File>?>(tempFolder.listFiles())) f.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleIntentPreference() {
        val preference = getIntentPreference()
        if (preference == ScanConstants.OPEN_CAMERA) {
            openCamera()
        } else if (preference == ScanConstants.OPEN_MEDIA) {
            openMediaContent()
        }
    }

    private fun isIntentPreferenceSet(): Boolean {
        val preference = requireArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0)
        return preference != 0
    }

    private fun getIntentPreference(): Int {
        return requireArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0)
    }


    private inner class CameraButtonClickListener : View.OnClickListener {
        override fun onClick(v: View?) {
            openCamera()
        }
    }

    private inner class GalleryClickListener : View.OnClickListener {
        override fun onClick(view: View?) {
            openMediaContent()
        }
    }

    fun openMediaContent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("image/*")
        startActivityForResult(intent, ScanConstants.PICKFILE_REQUEST_CODE)
    }

    fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val file = createImageFile()
            val isDirectoryCreated = Objects.requireNonNull<File?>(file.getParentFile()).mkdirs()
            Log.d("", "openCamera: isDirectoryCreated: " + isDirectoryCreated)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val tempFileUri = FileProvider.getUriForFile(
                    requireActivity().applicationContext,
                    "com.scanlibrary.provider",  // As defined in Manifest
                    file
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri)
            } else {
                val tempFileUri = Uri.fromFile(file)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri)
            }
            startActivityForResult(cameraIntent, ScanConstants.START_CAMERA_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.CAMERA),
                MY_CAMERA_REQUEST_CODE
            )
        }
    }

    private fun createImageFile(): File {
        clearTempImages()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val file = File(
            ScanConstants.IMAGE_PATH, "IMG_" + timeStamp +
                    ".jpg"
        )
        fileUri = Uri.fromFile(file)
        return file
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("", "onActivityResult" + resultCode)
        var bitmap: Bitmap? = null
        if (resultCode == Activity.RESULT_OK) {
            try {
                when (requestCode) {
                    ScanConstants.START_CAMERA_REQUEST_CODE -> bitmap = getBitmap(fileUri!!)
                    ScanConstants.PICKFILE_REQUEST_CODE -> bitmap = getBitmap(data?.data!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            requireActivity().finish()
        }
        if (bitmap != null) {
            postImagePick(bitmap)
        }
    }

    protected fun postImagePick(bitmap: Bitmap) {
        val uri = Utils.getUri(requireActivity(), bitmap)
        bitmap.recycle()
        scanner!!.onBitmapSelect(uri)
    }

    @Throws(IOException::class)
    private fun getBitmap(selectedimg: Uri): Bitmap? {
        val options = BitmapFactory.Options()
        options.inSampleSize = 3
        var fileDescriptor: AssetFileDescriptor? = null
        fileDescriptor =
            requireActivity().contentResolver.openAssetFileDescriptor(selectedimg, "r")
        checkNotNull(fileDescriptor)
        return BitmapFactory.decodeFileDescriptor(
            fileDescriptor.fileDescriptor, null, options
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(activity, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val MY_CAMERA_REQUEST_CODE = 100
    }
}
