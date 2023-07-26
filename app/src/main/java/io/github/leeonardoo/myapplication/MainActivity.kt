package io.github.leeonardoo.myapplication

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class MainActivity : AppCompatActivity() {

    val file by lazy { File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FileApp") }

    val videoFolder by lazy { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) }

    val videoFile by lazy { File(videoFolder, "talvezdeboa.mp4") }

    val videoLauncher =
        registerForActivityResult(
            ActivityResultContracts.CaptureVideo()
        ) {
            Log.d("Batata", "$it")
        }

    val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        Log.d("Cenoura", "$it")
    }

    var videoUri: Uri? = null

    val otherButton by lazy { findViewById<MaterialButton>(R.id.otherActivity) }

    val editText by lazy { findViewById<TextInputEditText>(R.id.editText) }
    val saveButton by lazy { findViewById<MaterialButton>(R.id.saveButton) }
    val textValue by lazy { findViewById<TextView>(R.id.textValue) }
    val getValueButton by lazy { findViewById<MaterialButton>(R.id.getValueButton) }
    val recordVideo by lazy { findViewById<MaterialButton>(R.id.recordVideo) }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionLauncher.launch(android.Manifest.permission.CAMERA)

        otherButton.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        val resolver = applicationContext.contentResolver

        val videoCollection = MediaStore.Video.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )

        recordVideo.setOnClickListener {
            /* val videoDetails = ContentValues().apply {
                 put(MediaStore.Video.Media.DISPLAY_NAME, "nome qualquer.mp4")
             }

             val uri = resolver.insert(videoCollection, videoDetails)

             videoUri = uri*/

            val uri = FileProvider.getUriForFile(
                this,
                "io.github.leeonardoo.myapplication.fileprovider",
                videoFile
            )

            videoLauncher.launch(uri)
        }

//        saveButton.setOnClickListener {
//            editText.text?.toString()?.let {
//                save(it)
//            }
//        }
//
//        getValueButton.setOnClickListener {
//            textValue.text = getSaveValue()
//        }

    }

    /*    fun save(text: String) {
            try {
                file.outputStream().use {
                    it.write(text.toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun getSaveValue(): String? {
            return try {
                file.inputStream().use {
                    String(it.readAllBytes())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }*/

    /* fun save(text: String) {
         try {
             tempFile?.delete()
             tempFile = File.createTempFile("externalFile", "sufixFile", this.externalCacheDir)
         } catch (e: Exception) {
             e.printStackTrace()
         }

         try {
             tempFile?.outputStream()?.use {
                 it.write(text.toByteArray())
             }
         } catch (e: Exception) {
             e.printStackTrace()
         }
     }

     fun getSaveValue(): String? {
         val myBytes = mutableListOf<Byte>()

         return try {
             tempFile?.inputStream()?.use {
                 var actualByte = it.read()
                 while (actualByte != -1) {
                     myBytes.add(actualByte.toByte())
                     actualByte = it.read()
                 }
             }

             String(myBytes.toByteArray())
         } catch (e: Exception) {
             e.printStackTrace()
             null
         }
     }*/
}