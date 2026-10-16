package com.lastnationstanding.game

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.net.Uri
import org.json.JSONObject
import android.widget.Toast
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import android.os.Handler
import android.os.Looper
import java.io.IOException

class MainActivity : Activity() {
    private lateinit var game: WebView

    private var pendingBackup: String? = null
    private val EXPORT_BACKUP = 2101
    private val IMPORT_BACKUP = 2102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(7, 17, 31)
        window.navigationBarColor = Color.rgb(7, 17, 31)
        game = WebView(this).apply {
            setBackgroundColor(Color.rgb(7, 17, 31))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            webChromeClient = WebChromeClient()
            addJavascriptInterface(ResultBridge(), "AndroidBridge")
            // Trusted bundled HTML only. Remote pages must never be loaded in this WebView.
            webViewClient = object : android.webkit.WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return true
                    return !url.startsWith("https://appassets.androidplatform.net/")
                }
            }
        }
        // Intro da M.B. Studios: exibida uma única vez por abertura do aplicativo.
        val root = FrameLayout(this)
        root.addView(game, FrameLayout.LayoutParams(-1, -1))
        val splash = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        val mb = TextView(this).apply {
            text = "M.B."
            setTextColor(Color.WHITE)
            textSize = 64f
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
            letterSpacing = 0.1f
        }
        val studios = TextView(this).apply {
            text = "STUDIOS"
            setTextColor(Color.WHITE)
            textSize = 28f
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
            letterSpacing = 0.3f
        }
        val credit = TextView(this).apply {
            text = "DESENVOLVIDO POR M.B. STUDIOS"
            setTextColor(Color.WHITE)
            textSize = 11f
            gravity = Gravity.CENTER
            letterSpacing = 0.12f
            setPadding(0, 40, 0, 0)
        }
        brand.addView(mb)
        brand.addView(studios)
        brand.addView(credit)
        splash.addView(brand, FrameLayout.LayoutParams(-1, -2, Gravity.CENTER))
        root.addView(splash, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
        splash.alpha = 0f
        splash.animate().alpha(1f).setDuration(350).start()
        Handler(Looper.getMainLooper()).postDelayed({
            splash.animate().alpha(0f).setDuration(350).withEndAction { root.removeView(splash) }.start()
        }, 2650L)
        // Secure origin for local HTML, with HTTPS resources loaded normally.
        val html = assets.open("index.html").bufferedReader().use { it.readText() }
        game.loadDataWithBaseURL("https://appassets.androidplatform.net/", html, "text/html", "UTF-8", null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) { pendingBackup = null; return }
        val uri = data?.data ?: return
        try {
            if (requestCode == EXPORT_BACKUP) {
                val contents = pendingBackup ?: return
                contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(contents) }
                pendingBackup = null
                Toast.makeText(this, "Backup salvo", Toast.LENGTH_SHORT).show()
            } else if (requestCode == IMPORT_BACKUP) {
                val text = contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: return
                if (text.length > 6_000_000) throw IOException("Backup muito grande")
                game.evaluateJavascript("window.LNSImportBackup(" + JSONObject.quote(text) + ")", null)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Não foi possível processar o backup", Toast.LENGTH_LONG).show()
        }
    }

    inner class ResultBridge {
        @JavascriptInterface
        fun exportBackup(json: String, filename: String) {
            if (json.length > 6_000_000 || !json.startsWith("{")) return
            runOnUiThread {
                pendingBackup = json
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, filename.take(90))
                }
                startActivityForResult(intent, EXPORT_BACKUP)
            }
        }
        @JavascriptInterface
        fun importBackup() {
            runOnUiThread {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                }
                startActivityForResult(intent, IMPORT_BACKUP)
            }
        }

        @JavascriptInterface
        fun exportImage(dataUrl: String, share: Boolean) {
            if (!dataUrl.startsWith("data:image/png;base64,") || dataUrl.length > 15_000_000) return
            Thread {
                try {
                    val bytes = Base64.decode(dataUrl.substringAfter(','), Base64.DEFAULT)
                    val filename = "Last-Nation-Standing-${System.currentTimeMillis()}.png"
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Last Nation Standing")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        ?: throw IOException("Não foi possível criar a imagem")
                    try {
                        contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                            ?: throw IOException("Falha ao gravar imagem")
                        val ready = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
                        contentResolver.update(uri, ready, null, null)
                    } catch (e: Exception) {
                        contentResolver.delete(uri, null, null)
                        throw e
                    }
                    runOnUiThread {
                        if (share) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            startActivity(Intent.createChooser(intent, "Compartilhar resultado"))
                        } else Toast.makeText(this@MainActivity, "Imagem salva na galeria", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    runOnUiThread { Toast.makeText(this@MainActivity, "Erro ao salvar imagem", Toast.LENGTH_LONG).show() }
                }
            }.start()
        }
    }

    @Deprecated("Back navigation handled by Android")
    override fun onBackPressed() {
        game.evaluateJavascript("(function(){return !!(window.LNSMenu&&window.LNSMenu.visible())})()") { result ->
            if (result == "true") {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Sair do aplicativo?")
                    .setMessage("As partidas salvas continuarão disponíveis.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Sair") { _, _ -> finish() }
                    .show()
            } else {
                game.evaluateJavascript("if(window.LNSMenu)window.LNSMenu.open()", null)
            }
        }
    }

    override fun onDestroy() {
        game.removeJavascriptInterface("AndroidBridge")
        game.destroy()
        super.onDestroy()
    }
}
