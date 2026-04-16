package com.gwangy.nassspandroidsample

import android.content.Context
import android.content.SharedPreferences

object AppConfig {
    private const val PREFS = "napspprefs"
    private const val KEY_MEDIA = "media_key"
    private const val KEY_PREFIX_ADUNIT = "adunit_"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun setMediaKey(context: Context, key: String) {
        prefs(context).edit().putString(KEY_MEDIA, key).apply()
    }

    fun getMediaKey(context: Context): String? = prefs(context).getString(KEY_MEDIA, null)

    fun setAdUnit(context: Context, idKey: String, value: String) {
        prefs(context).edit().putString(KEY_PREFIX_ADUNIT + idKey, value).apply()
    }

    fun getAdUnit(context: Context, idKey: String): String? =
        prefs(context).getString(KEY_PREFIX_ADUNIT + idKey, null)
}
