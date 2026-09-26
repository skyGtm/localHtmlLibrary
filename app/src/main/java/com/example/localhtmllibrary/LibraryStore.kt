package com.example.localhtmllibrary

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryStore(private val context: Context) {
    private val fileName = "library.json"
    private fun dataRoot(): DocumentFile? = AppPrefs.data(context)?.let { DocumentFile.fromTreeUri(context, Uri.parse(it)) }

    suspend fun all(): MutableList<HtmlFile> = withContext(Dispatchers.IO) {
        val root = dataRoot() ?: return@withContext mutableListOf()
        val f = root.findFile(fileName) ?: return@withContext mutableListOf()
        val text = context.contentResolver.openInputStream(f.uri)?.use { it.readBytes().toString(Charsets.UTF_8) } ?: return@withContext mutableListOf()
        val a = JSONArray(text)
        MutableList(a.length()) { i -> HtmlFile.fromJson(a.getJSONObject(i)) }
    }

    suspend fun save(items: List<HtmlFile>) = withContext(Dispatchers.IO) {
        val root = dataRoot() ?: return@withContext
        val old = root.findFile(fileName)
        old?.delete()
        val f = root.createFile("application/json", fileName) ?: return@withContext
        val a = JSONArray(); items.forEach { a.put(it.toJson()) }
        context.contentResolver.openOutputStream(f.uri)?.use { it.write(a.toString(2).toByteArray()) }
    }
}


