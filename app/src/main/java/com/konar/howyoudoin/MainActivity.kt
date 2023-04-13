package com.konar.howyoudoin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.konar.howyoudoin.databinding.ActivityMainBinding
import com.konar.howyoudoin.ml.FacialExpressionRecog
import kotlinx.coroutines.DelicateCoroutinesApi
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageUri: Uri

    private fun showRationaleDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }

    private val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    Toast.makeText(
                        this@MainActivity,
                        "Permission granted!!", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(this@MainActivity, "Permission denied!!", Toast.LENGTH_SHORT)
                            .show()
                    } else if (permissionName == Manifest.permission.CAMERA) {
                        Toast.makeText(this@MainActivity, "Permission denied!!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    private fun requestCameraAndStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            showRationaleDialog(
                "How you doin'",
                "How you doin' needs camera and storage permission to access images"
            )
        } else {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isCameraStorageAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /*
    private fun uriToBitmap(imgUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(imgUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            return BitmapFactory.decodeFileDescriptor(fileDescriptor)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
     */

    @OptIn(DelicateCoroutinesApi::class)
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                try {
                    val imgUri = result.data?.data
                    /*
                    bmpImg = uriToBitmap(imgUri!!)
                    if (bmpImg != null) {
                        val intent = Intent(this@MainActivity, ResultActivity::class.java)
                        val bs = ByteArrayOutputStream()
                        bmpImg!!.compress(Bitmap.CompressFormat.PNG, 90, bs)
                        if (bs.toByteArray().size > 400) {
                            val newBs = ByteArrayOutputStream()
                            var newBmp: Bitmap? = null
                            if (bmpImg!!.width > bmpImg!!.height) {
                                val aspRat = bmpImg!!.width / bmpImg!!.height
                                Log.e("AspRatio", bmpImg!!.height.toString())

                                newBmp = Bitmap.createScaledBitmap(
                                    bmpImg!!,
                                    400,
                                    400 * aspRat,
                                    false
                                )
                            } else {
                                val aspRat = bmpImg!!.height / bmpImg!!.width
                                Log.e("AspRatio", bmpImg!!.height.toString())
                                newBmp = Bitmap.createScaledBitmap(
                                    bmpImg!!,
                                    400 * aspRat,
                                    400,
                                    false
                                )
                            }


                            newBmp?.compress(Bitmap.CompressFormat.PNG, 90, newBs)
                            intent.putExtra("byteArray", newBs.toByteArray())
                            startActivity(intent)

                        } else {
                            intent.putExtra("byteArray", bs.toByteArray())
                            startActivity(intent)
                        }
                    }

                     */
                    val intent = Intent(this@MainActivity, ResultActivity::class.java)
                    intent.putExtra("Selected Image Uri", imgUri.toString())
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("IMG", e.message.toString())
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUri = createImageUri()!!

        if (!isReadStorageAllowed() && !isCameraStorageAllowed()) {
            requestCameraAndStoragePermission()
        }

        binding.btnUpload.setOnClickListener {
            if (!isReadStorageAllowed()) {
                requestPermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
            if (isReadStorageAllowed()) {
                val pickIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(pickIntent)
            }
        }

        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission.launch(arrayOf(Manifest.permission.CAMERA))
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                takePicture.launch(imageUri)
            }
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        val intent = Intent(this@MainActivity, ResultActivity::class.java)
        intent.putExtra("Selected Image Uri", imageUri.toString())

        startActivity(intent)
    }

    private fun createImageUri(): Uri? {
        val image = File(applicationContext.filesDir, "camera_photo.png")
        return FileProvider.getUriForFile(
            applicationContext,
            "com.konar.howyoudoin.fileProvider",
            image
        )
    }

}