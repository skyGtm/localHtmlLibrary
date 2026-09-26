package com.example.localhtmllibrary

import android.content.Context

object AppPrefs {
    private const val P = "prefs"
    private const val LIB = "library_uri"
    private const val DATA = "data_uri"

    fun library(context: Context) = context.getSharedPreferences(P, 0).getString(LIB, null)
    fun data(context: Context) = context.getSharedPreferences(P, 0).getString(DATA, null)

    fun setLibrary(context: Context, value: String) =
        context.getSharedPreferences(P, 0).edit().putString(LIB, value).apply()

    fun setData(context: Context, value: String) =
        context.getSharedPreferences(P, 0).edit().putString(DATA, value).apply()

    fun configured(context: Context) = library(context) != null && data(context) != null
}
