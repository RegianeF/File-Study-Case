package io.github.leeonardoo.myapplication

import android.content.ContentValues
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

class MediaStoreActivity : AppCompatActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Log.d("Cenoura", "$it")
        }

    private var videoFile: File? = null

    private val videoLauncher =
        registerForActivityResult(ActivityResultContracts.CaptureVideo()) { result ->
            if (result) {
                saveFileIntoGallery()
            } else {
                Toast.makeText(
                    this,
                    "Algo de errado deu certo. Tente novamente!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val recordVideo by lazy { findViewById<MaterialButton>(R.id.recordVideo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_store)

        permissionLauncher.launch(android.Manifest.permission.CAMERA)

        recordVideo.setOnClickListener {
            videoFile = File(filesDir, "${System.currentTimeMillis()}.mp4")

            val uri = FileProvider.getUriForFile(
                this,
                "io.github.leeonardoo.myapplication.fileprovider",
                videoFile!!
            )

            videoLauncher.launch(uri)
        }

    }

    private fun saveFileIntoGallery() {
        GlobalScope.launch(Dispatchers.IO) {
            val videoCollection =
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val videoValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, videoFile?.name)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }

            val uri = contentResolver.insert(videoCollection, videoValues) //caminho pra um arquivo que ainda não existe

            contentResolver.openOutputStream(uri!!)?.use { outputStream ->
                FileInputStream(videoFile).use { fileInputStream ->
                    val buffer = ByteArray(8192)

                    while (true) {
                        val bytesRead = fileInputStream.read(buffer)

                        if (bytesRead == -1) {
                            break
                        }

                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            }

//            contentResolver.openOutputStream(uri!!)?.buffered()?.use { outputStream ->
//                FileInputStream(videoFile).buffered().use {
//                    it.copyTo(outputStream)
//                }
//            }

            videoValues.clear()
            videoValues.put(MediaStore.Video.Media.IS_PENDING, 0)

            contentResolver.update(
                uri,
                videoValues,
                null,
                null
            )
        }
    }

}