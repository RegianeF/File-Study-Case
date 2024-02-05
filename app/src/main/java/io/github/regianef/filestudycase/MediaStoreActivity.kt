package io.github.regianef.filestudycase

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okio.buffer
import okio.sink
import okio.source
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

    private val fileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult

            copyFile(uri)
        }

    private val recordVideo by lazy { findViewById<MaterialButton>(R.id.recordVideo) }
    private val chooseFile by lazy { findViewById<MaterialButton>(R.id.chooseFile) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_store)

        permissionLauncher.launch(android.Manifest.permission.CAMERA)

        recordVideo.setOnClickListener {
            videoFile = File(filesDir, "${System.currentTimeMillis()}.mp4")

            val uri = FileProvider.getUriForFile(
                this,
                "io.github.regianef.filestudycase.fileprovider",
                videoFile!!
            )

            videoLauncher.launch(uri)
        }

        chooseFile.setOnClickListener {
            fileLauncher.launch("*/*")
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

            val uri = contentResolver.insert(
                videoCollection,
                videoValues
            ) //caminho pra um arquivo que ainda não existe

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

    private fun copyFile(uri: Uri) {
        GlobalScope.launch(Dispatchers.IO) {
            val fileCollection =
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val name = contentResolver.query(uri, null, null, null)?.use { cursor ->
                if (cursor.moveToNext()) {
                    val index = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    cursor.getString(index)
                } else {
                    null
                }
            }

            val fileValues = ContentValues().apply {
                put(MediaStore.Downloads.IS_PENDING, 1)
                put(MediaStore.Downloads.DISPLAY_NAME, name)
            }

            val emptyUri = contentResolver.insert(
                fileCollection,
                fileValues
            )

            /*        contentResolver.openOutputStream(emptyUri ?: Uri.EMPTY)?.use { outputStream ->
                        contentResolver.openInputStream(uri)?.use { uriInputStream ->
                            val buffer = ByteArray(8192)

                            while (true) {
                                val bytesRead = uriInputStream.read(buffer)

                                if (bytesRead == -1) {
                                    break
                                }

                                outputStream.write(buffer, 0, bytesRead)
                            }
                        }
                    }*/

            contentResolver.openOutputStream(emptyUri ?: Uri.EMPTY)?.use { outputStream ->
                outputStream.sink().buffer().use { sink ->
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.source().buffer().use { source ->
                            sink.writeAll(source)
                        }
                    }
                }
            }

            fileValues.clear()
            fileValues.put(MediaStore.Downloads.IS_PENDING, 0)

            contentResolver.update(
                emptyUri!!,
                fileValues,
                null,
                null
            )

        }
    }
}

//escolher um arquivo e copiar pra download com o nome correto