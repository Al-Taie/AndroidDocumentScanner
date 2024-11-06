package com.scanner.demo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.scanner.library.ScanConstants
import com.scanner.library.Scanner
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var scanButton: Button? = null
    private var cameraButton: Button? = null
    private var mediaButton: Button? = null
    private var scannedImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        startScan(1)
    }

    private fun init() {
        scanButton = findViewById<View?>(R.id.scanButton) as Button
        scanButton!!.setOnClickListener(ScanButtonClickListener())
        cameraButton = findViewById<View?>(R.id.cameraButton) as Button
        cameraButton!!.setOnClickListener(ScanButtonClickListener(ScanConstants.OPEN_CAMERA))
        mediaButton = findViewById<View?>(R.id.mediaButton) as Button
        mediaButton!!.setOnClickListener(ScanButtonClickListener(ScanConstants.OPEN_MEDIA))
        scannedImageView = findViewById<View?>(R.id.scannedImage) as ImageView
    }

    private inner class ScanButtonClickListener(private val preference: Int = 0) : View.OnClickListener {
        override fun onClick(v: View?) {
            startScan(preference)
        }
    }

    protected fun startScan(preference: Int) {
        val intent = Intent(this, Scanner::class.java)
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data?.extras?.getParcelable<Uri?>(ScanConstants.SCANNED_RESULT)
            var bitmap: Bitmap? = null
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                contentResolver.delete(uri!!, null, null)
                scannedImageView!!.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun convertByteArrayToBitmap(data: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val REQUEST_CODE = 99
    }
}
