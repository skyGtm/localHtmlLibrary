package com.example.localhtmllibrary

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class WebViewActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private var item: HtmlFile? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var store: LibraryStore

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(b: Bundle?) {
        super.onCreate(b); setContentView(R.layout.activity_webview); store=LibraryStore(this)
        web=findViewById(R.id.webview)
        web.settings.javaScriptEnabled=true; web.settings.domStorageEnabled=true; web.settings.databaseEnabled=true
        web.settings.allowFileAccess=true; web.settings.allowContentAccess=true; web.settings.builtInZoomControls=true; web.settings.displayZoomControls=false
        scope.launch { val uri=intent.getStringExtra("uri") ?: return@launch; item=store.all().firstOrNull{it.uri==uri}; web.loadUrl(uri); item?.let{x->web.postDelayed({web.scrollTo(0,x.scrollY)},400)} }
    }
    override fun onPause(){ item?.let{x->scope.launch{val l=store.all();val i=l.indexOfFirst{it.uri==x.uri};if(i>=0){l[i]=l[i].copy(lastOpened=System.currentTimeMillis(),scrollY=web.scrollY);store.save(l)}}};super.onPause() }
    override fun onDestroy(){scope.cancel();web.destroy();super.onDestroy()}
}
