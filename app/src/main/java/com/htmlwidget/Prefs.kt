package com.htmlwidget

import android.content.Context

object Prefs {
    private const val NAME = "hw6_prefs"
    private fun sp(ctx: Context) = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun setUri(ctx: Context, id: Int, v: String) = sp(ctx).edit().putString("uri_$id", v).apply()
    fun getUri(ctx: Context, id: Int) = sp(ctx).getString("uri_$id", null)

    fun setName(ctx: Context, id: Int, v: String) = sp(ctx).edit().putString("name_$id", v).apply()
    fun getName(ctx: Context, id: Int) = sp(ctx).getString("name_$id", "dosya.html") ?: "dosya.html"

    fun setIntervalSec(ctx: Context, id: Int, v: Long) = sp(ctx).edit().putLong("interval_$id", v).apply()
    fun getIntervalSec(ctx: Context, id: Int) = sp(ctx).getLong("interval_$id", 900L)
    fun getIntervalMs(ctx: Context, id: Int) = getIntervalSec(ctx, id) * 1000L

    fun setScrollX(ctx: Context, id: Int, v: Int) = sp(ctx).edit().putInt("sx_$id", v).apply()
    fun getScrollX(ctx: Context, id: Int) = sp(ctx).getInt("sx_$id", 0)

    fun setScrollY(ctx: Context, id: Int, v: Int) = sp(ctx).edit().putInt("sy_$id", v).apply()
    fun getScrollY(ctx: Context, id: Int) = sp(ctx).getInt("sy_$id", 0)

    fun setZoom(ctx: Context, id: Int, v: Int) = sp(ctx).edit().putInt("zoom_$id", v).apply()
    fun getZoom(ctx: Context, id: Int) = sp(ctx).getInt("zoom_$id", 100)

    fun setDelaySec(ctx: Context, id: Int, v: Float) = sp(ctx).edit().putFloat("delay_$id", v).apply()
    fun getDelaySec(ctx: Context, id: Int) = sp(ctx).getFloat("delay_$id", 1.5f)
    fun getDelayMs(ctx: Context, id: Int) = (getDelaySec(ctx, id) * 1000f).toLong()

    // Serbest dp boyutu
    fun setWidthDp(ctx: Context, id: Int, v: Int) = sp(ctx).edit().putInt("wdp_$id", v).apply()
    fun getWidthDp(ctx: Context, id: Int) = sp(ctx).getInt("wdp_$id", 148)

    fun setHeightDp(ctx: Context, id: Int, v: Int) = sp(ctx).edit().putInt("hdp_$id", v).apply()
    fun getHeightDp(ctx: Context, id: Int) = sp(ctx).getInt("hdp_$id", 148)

    // Sabit boyut seçeneği: 0=1x1, 1=1x2, 2=2x2, 3=1x4
    fun setPreset(ctx: Context, id: Int, v: Int) = sp(ctx).edit().putInt("preset_$id", v).apply()
    fun getPreset(ctx: Context, id: Int) = sp(ctx).getInt("preset_$id", 2) // default 2x2

    // Preset'ten dp boyutlarını hesapla (1 hücre = 74dp)
    fun presetToDp(preset: Int): Pair<Int, Int> = when (preset) {
        0 -> Pair(74, 74)    // 1x1
        1 -> Pair(74, 148)   // 1x2
        2 -> Pair(148, 148)  // 2x2
        3 -> Pair(74, 296)   // 1x4
        else -> Pair(148, 148)
    }

    fun remove(ctx: Context, id: Int) {
        sp(ctx).edit()
            .remove("uri_$id").remove("name_$id").remove("interval_$id")
            .remove("sx_$id").remove("sy_$id").remove("zoom_$id")
            .remove("delay_$id").remove("wdp_$id").remove("hdp_$id")
            .remove("preset_$id")
            .apply()
    }
}
