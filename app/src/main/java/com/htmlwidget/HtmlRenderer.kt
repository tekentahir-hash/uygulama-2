package com.htmlwidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

object HtmlRenderer {

    /**
     * HTML sayfasını render eder.
     * zoom=100 → normal boyut, zoom=50 → yarı boyut (küçültülmüş)
     * scrollX/Y → sayfanın hangi noktasından başlanacak (zoom uygulanmadan önceki koordinatlar)
     * Sonuçta tam olarak widthPx x heightPx boyutunda bitmap döner.
     */
    fun render(
        ctx: Context,
        uri: String,
        widthPx: Int,
        heightPx: Int,
        scrollX: Int,
        scrollY: Int,
        zoom: Int,          // 10-500, yüzde
        delayMs: Long,
        onDone: (Bitmap?) -> Unit
    ) {
        Handler(Looper.getMainLooper()).post {
            try {
                val scale = zoom / 100f

                // WebView'ın görmesi gereken sanal boyut:
                // zoom=50 ise widget 2x büyük içeriği göstermeli → renderW = widthPx / scale
                val virtualW = (widthPx / scale).toInt().coerceAtLeast(100)
                val virtualH = (heightPx / scale).toInt().coerceAtLeast(100)

                // Sayfa scroll konumu da sanal koordinatlarda
                val virtualScrollX = (scrollX / scale).toInt()
                val virtualScrollY = (scrollY / scale).toInt()

                // WebView toplam render alanı
                val totalW = (virtualW + virtualScrollX + 50).coerceAtLeast(virtualW)
                val totalH = (virtualH + virtualScrollY + 50).coerceAtLeast(virtualH)

                val wv = WebView(ctx)
                wv.measure(
                    android.view.View.MeasureSpec.makeMeasureSpec(totalW, android.view.View.MeasureSpec.EXACTLY),
                    android.view.View.MeasureSpec.makeMeasureSpec(totalH, android.view.View.MeasureSpec.EXACTLY)
                )
                wv.layout(0, 0, totalW, totalH)
                wv.setBackgroundColor(Color.WHITE)

                wv.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = false
                    useWideViewPort = true
                    allowFileAccess = true
                    allowContentAccess = true
                    @Suppress("DEPRECATION") allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION") allowUniversalAccessFromFileURLs = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    // Zoom'u WebView text zoom ile değil, canvas scale ile uyguluyoruz
                    textZoom = 100
                }

                wv.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            try {
                                // Scroll uygula (sanal koordinatlarda)
                                wv.scrollTo(virtualScrollX, virtualScrollY)

                                Handler(Looper.getMainLooper()).postDelayed({
                                    try {
                                        // Tam render bitmap
                                        val fullBmp = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
                                        val fullCanvas = Canvas(fullBmp)
                                        fullCanvas.drawColor(Color.WHITE)
                                        wv.draw(fullCanvas)
                                        wv.destroy()

                                        // virtualScrollX/Y noktasından virtualW x virtualH kes
                                        val cropX = virtualScrollX.coerceAtLeast(0)
                                        val cropY = virtualScrollY.coerceAtLeast(0)
                                        val cropW = virtualW.coerceAtMost(totalW - cropX)
                                        val cropH = virtualH.coerceAtMost(totalH - cropY)

                                        val croppedBmp = Bitmap.createBitmap(fullBmp, cropX, cropY, cropW, cropH)
                                        fullBmp.recycle()

                                        // Widget boyutuna ölçekle — bu adım zoom'u yansıtır
                                        val finalBmp = Bitmap.createScaledBitmap(croppedBmp, widthPx, heightPx, true)
                                        if (croppedBmp != finalBmp) croppedBmp.recycle()

                                        onDone(finalBmp)
                                    } catch (e: Exception) {
                                        try { wv.destroy() } catch (_: Exception) {}
                                        onDone(null)
                                    }
                                }, 300)
                            } catch (e: Exception) {
                                try { wv.destroy() } catch (_: Exception) {}
                                onDone(null)
                            }
                        }, delayMs)
                    }

                    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                        try { wv.destroy() } catch (_: Exception) {}
                        onDone(null)
                    }
                }

                try {
                    val parsedUri = Uri.parse(uri)
                    val stream = ctx.contentResolver.openInputStream(parsedUri)
                    val html = stream?.bufferedReader()?.readText() ?: ""
                    stream?.close()
                    val basePath = parsedUri.path?.let {
                        val parent = java.io.File(it).parent
                        if (parent != null) "file://$parent/" else null
                    }
                    wv.loadDataWithBaseURL(basePath, html, "text/html", "UTF-8", null)
                } catch (e: Exception) {
                    try { wv.destroy() } catch (_: Exception) {}
                    onDone(null)
                }
            } catch (e: Exception) {
                onDone(null)
            }
        }
    }

    fun dpToPx(ctx: Context, dp: Int): Int =
        (dp * ctx.resources.displayMetrics.density).toInt().coerceAtLeast(50)
}
