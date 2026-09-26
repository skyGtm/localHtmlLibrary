package com.example.localhtmllibrary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: HtmlAdapter
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var store: LibraryStore

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (!AppPrefs.configured(this)) { startActivity(Intent(this, SetupActivity::class.java)); finish(); return }
        setContentView(R.layout.activity_main)
        store = LibraryStore(this)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.main_menu)
        toolbar.setOnMenuItemClickListener { if (it.itemId == R.id.settings) startActivity(Intent(this, SettingsActivity::class.java)); true }
        adapter = HtmlAdapter(this, mutableListOf(),
            { item -> startActivity(Intent(this, WebViewActivity::class.java).putExtra("uri", item.uri)) },
            { item -> scope.launch { val list=store.all(); val i=list.indexOfFirst { it.uri==item.uri }; if(i>=0){list[i]=list[i].copy(pinned=!list[i].pinned); store.save(list); load()} } })
        findViewById<RecyclerView>(R.id.recycler).apply { layoutManager=GridLayoutManager(this@MainActivity,2); adapter=this@MainActivity.adapter }
        load()
    }

    private fun load() = scope.launch {
        scan(); val list=store.all().sortedWith(compareByDescending<HtmlFile>{it.pinned}.thenBy{it.sortOrder}.thenBy(String.CASE_INSENSITIVE_ORDER){it.name})
        adapter.items.clear(); adapter.items.addAll(list); adapter.notifyDataSetChanged()
    }

    private suspend fun scan() = withContext(Dispatchers.IO) {
        val root=DocumentFile.fromTreeUri(this@MainActivity, Uri.parse(AppPrefs.library(this@MainActivity))) ?: return@withContext
        val old=store.all().associateBy{it.uri}; val found=mutableListOf<HtmlFile>()
        fun walk(dir:DocumentFile){ dir.listFiles().forEach{f-> if(f.isDirectory) walk(f) else if(f.name?.lowercase()?.endsWith(".html")==true || f.name?.lowercase()?.endsWith(".htm")==true){ val o=old[f.uri.toString()]; found += (o ?: HtmlFile(uri=f.uri.toString(),name=f.name?:"HTML",title=f.name?:"HTML",sortOrder=found.size.toLong())).copy(name=f.name?:"HTML",sortOrder=o?.sortOrder?:found.size.toLong()) } } }
        walk(root); store.save(found)
    }
    override fun onResume(){ super.onResume(); if(::store.isInitialized) load() }
    override fun onDestroy(){scope.cancel();super.onDestroy()}
}
