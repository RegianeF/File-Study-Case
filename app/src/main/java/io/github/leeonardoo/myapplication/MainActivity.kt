package io.github.leeonardoo.myapplication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class MainActivity : AppCompatActivity() {

    var tempFile: File? = null

    val file by lazy { File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FileApp") }

    val otherButton by lazy { findViewById<MaterialButton>(R.id.otherActivity) }

    val editText by lazy { findViewById<TextInputEditText>(R.id.editText) }
    val saveButton by lazy { findViewById<MaterialButton>(R.id.saveButton) }
    val textValue by lazy { findViewById<TextView>(R.id.textValue) }
    val getValueButton by lazy { findViewById<MaterialButton>(R.id.getValueButton) }
    val mediaStoreButton by lazy { findViewById<MaterialButton>(R.id.mediaStoreActivity) }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        otherButton.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        mediaStoreButton.setOnClickListener {
            val intent = Intent(this, MediaStoreActivity::class.java)
            this.startActivity(intent)
        }


        saveButton.setOnClickListener {
            editText.text?.toString()?.let {
                save(it)
            }
        }

        getValueButton.setOnClickListener {
            textValue.text = getSaveValue()
        }

    }

    fun save(text: String) {
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
    }
}