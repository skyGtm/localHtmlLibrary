package com.example.localhtmllibrary

import android.app.*
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile

class SetupActivity : AppCompatActivity() {
    private var lib: Uri? = null
    private var data: Uri? = null
    private val pickLib = 10
    private val pickData = 11

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_setup)
        val lb = findViewById<Button>(R.id.libraryButton)
        val db = findViewById<Button>(R.id.dataButton)
        val cont = findViewById<Button>(R.id.continueButton)
        lb.setOnClickListener { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), pickLib) }
        db.setOnClickListener { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), pickData) }
        cont.setOnClickListener {
            lib?.let { AppPrefs.setLibrary(this, it.toString()) }
            data?.let { AppPrefs.setData(this, it.toString()) }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(r: Int, c: Int, d: Intent?) {
        super.onActivityResult(r, c, d)
        if (c != RESULT_OK || d?.data == null) return
        val uri = d.data!!
        val flags = d.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        contentResolver.takePersistableUriPermission(uri, flags)
        if (r == pickLib) {
            lib = uri
            findViewById<TextView>(R.id.libraryText).text = uri.toString()
        } else {
            data = uri
            findViewById<TextView>(R.id.dataText).text = uri.toString()
        }
        findViewById<Button>(R.id.continueButton).isEnabled = lib != null && data != null
    }
}
