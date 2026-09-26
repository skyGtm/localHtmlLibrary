package com.example.localhtmllibrary

data class HtmlFile(
    val uri: String,
    val name: String,
    val title: String,
    val pinned: Boolean = false,
    val sortOrder: Long = 0,
    val lastOpened: Long = 0,
    val scrollY: Int = 0
) {
    fun toJson() = org.json.JSONObject().apply {
        put("uri", uri); put("name", name); put("title", title); put("pinned", pinned)
        put("sortOrder", sortOrder); put("lastOpened", lastOpened); put("scrollY", scrollY)
    }
    companion object {
        fun fromJson(o: org.json.JSONObject) = HtmlFile(
            uri=o.optString("uri"), name=o.optString("name"), title=o.optString("title"),
            pinned=o.optBoolean("pinned"), sortOrder=o.optLong("sortOrder"),
            lastOpened=o.optLong("lastOpened"), scrollY=o.optInt("scrollY")
        )
    }
}
