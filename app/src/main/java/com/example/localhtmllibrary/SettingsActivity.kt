package com.example.localhtmllibrary

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class SettingsActivity : AppCompatActivity() {
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
    private val libReq=21; private val dataReq=22; private val exportReq=23; private val importReq=24
    override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_settings)
        findViewById<Button>(R.id.changeLibrary).setOnClickListener{pick(libReq)}
        findViewById<Button>(R.id.changeData).setOnClickListener{pick(dataReq)}
        findViewById<Button>(R.id.rescan).setOnClickListener{finish()}
        findViewById<Button>(R.id.backup).setOnClickListener{startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/zip").putExtra(Intent.EXTRA_TITLE,"html-library-backup.zip"),exportReq)}
        findViewById<Button>(R.id.restore).setOnClickListener{startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/zip").addCategory(Intent.CATEGORY_OPENABLE),importReq)} }
    private fun pick(r:Int)=startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),r)
    override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d);if(c!=RESULT_OK||d?.data==null)return;val u=d.data!!;val flags=d.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION);contentResolver.takePersistableUriPermission(u,flags)
        when(r){libReq->AppPrefs.setLibrary(this,u.toString());dataReq->AppPrefs.setData(this,u.toString());exportReq->scope.launch{exportBackup(u)};importReq->scope.launch{restoreBackup(u)}}}

    private fun relativeMap(root:DocumentFile):Map<String,String>{
        val m=mutableMapOf<String,String>()
        fun walk(d:DocumentFile,rel:String){d.listFiles().forEach{f->if(f.isDirectory)walk(f,"$rel${f.name}/")else m[f.uri.toString()]= "$rel${f.name}"}}
        walk(root,"");return m
    }

    private suspend fun exportBackup(dest:Uri)=withContext(Dispatchers.IO){
        val root=DocumentFile.fromTreeUri(this@SettingsActivity,Uri.parse(AppPrefs.library(this@SettingsActivity)))?:return@withContext
        val rel=relativeMap(root); val meta=LibraryStore(this@SettingsActivity).all()
        contentResolver.openOutputStream(dest)!!.use{out->ZipOutputStream(out).use{z->
            val a=JSONArray(); meta.forEach{a.put(JSONObject().apply{put("relative",rel[it.uri]?:it.name);put("name",it.name);put("title",it.title);put("pinned",it.pinned);put("sortOrder",it.sortOrder);put("lastOpened",it.lastOpened);put("scrollY",it.scrollY)})}
            z.putNextEntry(ZipEntry("metadata/library.json"));z.write(a.toString(2).toByteArray());z.closeEntry()
            fun add(d:DocumentFile,path:String){d.listFiles().forEach{f->if(f.isDirectory)add(f,"$path${f.name}/")else{z.putNextEntry(ZipEntry("library/$path${f.name}"));contentResolver.openInputStream(f.uri)?.use{it.copyTo(z)};z.closeEntry()}}}
            add(root,"")
        }}
        runOnUiThread{Toast.makeText(this@SettingsActivity,"Backup exported.",Toast.LENGTH_SHORT).show()}
    }

    private fun ensureDir(root:DocumentFile,parts:List<String>):DocumentFile?{var d=root;for(p in parts.filter{it.isNotEmpty()})d=d.findFile(p)?:d.createDirectory(p)?:return null;return d}

    private suspend fun restoreBackup(src:Uri)=withContext(Dispatchers.IO){
        val lib=DocumentFile.fromTreeUri(this@SettingsActivity,Uri.parse(AppPrefs.library(this@SettingsActivity)))?:return@withContext
        val data=DocumentFile.fromTreeUri(this@SettingsActivity,Uri.parse(AppPrefs.data(this@SettingsActivity)))?:return@withContext
        val imported=mutableMapOf<String,JSONObject>()
        contentResolver.openInputStream(src)!!.use{input->ZipInputStream(input).use{z->var e=z.nextEntry;while(e!=null){val name=e.name
            if(name=="metadata/library.json"){val a=JSONArray(z.readBytes().toString(Charsets.UTF_8));for(i in 0 until a.length()){val o=a.getJSONObject(i);imported[o.optString("relative")]=o}}
            else if(name.startsWith("library/")&&!name.endsWith("/")){val rel=name.removePrefix("library/");val parts=rel.split('/');val fn=parts.last();val dir=ensureDir(lib,parts.dropLast(1))?:run{e=z.nextEntry;continue};dir.findFile(fn)?.delete();val f=dir.createFile("application/octet-stream",fn)?:run{e=z.nextEntry;continue};contentResolver.openOutputStream(f.uri)?.use{z.copyTo(it)}}
            e=z.nextEntry}}
        }
        fun find(d:DocumentFile,rel:String):DocumentFile?{val p=rel.split('/').filter{it.isNotEmpty()};var cur=d;for(x in p){cur=cur.findFile(x)?:return null};return cur}
        val restored=imported.entries.mapIndexedNotNull{idx,(rel,o)->find(lib,rel)?.let{f->HtmlFile(f.uri.toString(),o.optString("name",f.name?:("HTML")),o.optString("title",f.name?:"HTML"),o.optBoolean("pinned"),o.optLong("sortOrder",idx.toLong()),o.optLong("lastOpened"),o.optInt("scrollY"))}}
        LibraryStore(this@SettingsActivity).save(restored)
        runOnUiThread{Toast.makeText(this@SettingsActivity,"Backup imported.",Toast.LENGTH_SHORT).show()}
    }
    override fun onDestroy(){scope.cancel();super.onDestroy()}
}
