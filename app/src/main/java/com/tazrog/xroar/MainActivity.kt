package com.tazrog.xroar

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.content.res.AssetManager
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.appcompat.app.AppCompatDelegate
import com.tazrog.xroar.ui.theme.XroarTheme
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private var activeWebView: WebView? = null
    private lateinit var romRepository: RomRepository
    private lateinit var configRepository: ConfigRepository

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = fileChooserCallback
            fileChooserCallback = null
            callback?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            )
        }

    private val romDirectoryLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            val jsCallback = activeWebView
            if (uri == null) {
                jsCallback?.evaluateJavascript(
                    "window.onRomImportComplete && window.onRomImportComplete(0, 'Folder selection cancelled.', true);",
                    null
                )
                return@registerForActivityResult
            }

            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            runCatching {
                contentResolver.takePersistableUriPermission(uri, flags)
            }

            val result = romRepository.importTree(this, contentResolver, uri)
            val message = jsString(result.message)
            jsCallback?.evaluateJavascript(
                "window.onRomImportComplete && window.onRomImportComplete(${result.importedCount}, '$message', ${result.isError});",
                null
            )
        }

    private val configFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            val jsCallback = activeWebView
            if (uri == null) {
                jsCallback?.evaluateJavascript(
                    "window.onConfigImportComplete && window.onConfigImportComplete(false, 'Config selection cancelled.', true);",
                    null
                )
                return@registerForActivityResult
            }

            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            runCatching {
                contentResolver.takePersistableUriPermission(uri, flags)
            }

            val result = configRepository.importFile(this, contentResolver, uri)
            val message = jsString(result.message)
            jsCallback?.evaluateJavascript(
                "window.onConfigImportComplete && window.onConfigImportComplete(${result.imported}, '$message', ${result.isError});",
                null
            )
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val displayMode = readDisplayMode()
        applyDisplayMode(displayMode)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        romRepository = RomRepository(File(filesDir, "roms"))
        configRepository = ConfigRepository(File(filesDir, "config"))

        setContent {
            XroarTheme(darkTheme = displayMode != DISPLAY_MODE_LIGHT) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    XroarWebView(
                        activity = this,
                        romRepository = romRepository,
                        configRepository = configRepository,
                        onShowFileChooser = { callback, params ->
                            fileChooserCallback?.onReceiveValue(null)
                            fileChooserCallback = callback
                            val intent = runCatching { params.createIntent() }
                                .getOrElse { Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "*/*" } }
                                .apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = type ?: "*/*"
                                }
                            fileChooserLauncher.launch(intent)
                            true
                        },
                        onCreated = { webView -> activeWebView = webView },
                        onDestroyed = { webView ->
                            if (activeWebView === webView) {
                                activeWebView = null
                            }
                        },
                        onPickRomDirectory = { romDirectoryLauncher.launch(null) },
                        onPickConfigFile = {
                            configFileLauncher.launch(
                                arrayOf(
                                    "text/plain",
                                    "application/octet-stream",
                                    "*/*"
                                )
                            )
                        },
                        hasPhysicalKeyboard = { hasPhysicalKeyboardAttached() },
                        getDisplayMode = { readDisplayMode() },
                        setDisplayMode = { mode ->
                            persistDisplayMode(mode)
                            applyDisplayMode(mode)
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        fileChooserCallback?.onReceiveValue(null)
        fileChooserCallback = null
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        notifyPhysicalKeyboardChanged()
    }

    private fun notifyPhysicalKeyboardChanged() {
        val available = hasPhysicalKeyboardAttached()
        activeWebView?.evaluateJavascript(
            "window.onAndroidPhysicalKeyboardChanged && window.onAndroidPhysicalKeyboardChanged($available);",
            null
        )
    }

    private fun hasPhysicalKeyboardAttached(): Boolean {
        val configuration = resources.configuration
        return configuration.keyboard != Configuration.KEYBOARD_NOKEYS &&
            configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
    }

    private fun displayPreferences() =
        getSharedPreferences(DISPLAY_PREFS_NAME, MODE_PRIVATE)

    private fun readDisplayMode(): String {
        val mode = displayPreferences().getString(DISPLAY_MODE_KEY, DISPLAY_MODE_DARK)
        return when (mode) {
            DISPLAY_MODE_LIGHT,
            DISPLAY_MODE_DARK -> mode
            else -> DISPLAY_MODE_DARK
        }
    }

    private fun persistDisplayMode(mode: String) {
        val normalized = when (mode) {
            DISPLAY_MODE_LIGHT,
            DISPLAY_MODE_DARK -> mode
            else -> DISPLAY_MODE_DARK
        }
        displayPreferences().edit().putString(DISPLAY_MODE_KEY, normalized).apply()
    }

    private fun applyDisplayMode(mode: String) {
        val nightMode = when (mode) {
            DISPLAY_MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    companion object {
        private const val DISPLAY_PREFS_NAME = "xroar_display"
        private const val DISPLAY_MODE_KEY = "display_mode"
        private const val DISPLAY_MODE_LIGHT = "light"
        private const val DISPLAY_MODE_DARK = "dark"
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun XroarWebView(
    activity: ComponentActivity,
    romRepository: RomRepository,
    configRepository: ConfigRepository,
    onShowFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams) -> Boolean,
    onCreated: (WebView) -> Unit,
    onDestroyed: (WebView) -> Unit,
    onPickRomDirectory: () -> Unit,
    onPickConfigFile: () -> Unit,
    hasPhysicalKeyboard: () -> Boolean,
    getDisplayMode: () -> String,
    setDisplayMode: (String) -> Unit
) {
    val webViewState = remember { mutableStateOf<WebView?>(null) }

    BackHandler(enabled = webViewState.value?.canGoBack() == true) {
        webViewState.value?.goBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewState.value?.let(onDestroyed)
            webViewState.value?.destroy()
            webViewState.value = null
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    mediaPlaybackRequiresUserGesture = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                }

                addJavascriptInterface(
                    XroarAndroidBridge(
                        romRepository = romRepository,
                        configRepository = configRepository,
                        onPickRomDirectory = onPickRomDirectory,
                        onPickConfigFile = onPickConfigFile,
                        hasPhysicalKeyboard = hasPhysicalKeyboard,
                        getDisplayMode = getDisplayMode,
                        setDisplayMode = setDisplayMode,
                        onHapticFeedback = {
                            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                    ),
                    "AndroidBridge"
                )

                webViewClient = XroarAssetWebViewClient(
                    assetManager = activity.assets,
                    romRepository = romRepository,
                    onOpenExternalUrl = { uri ->
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            addCategory(Intent.CATEGORY_BROWSABLE)
                        }
                        runCatching {
                            context.startActivity(intent)
                        }.getOrElse { error ->
                            if (error !is ActivityNotFoundException) {
                                throw error
                            }
                        }
                    }
                )
                webChromeClient = object : WebChromeClient() {
                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>,
                        fileChooserParams: FileChooserParams
                    ): Boolean = onShowFileChooser(filePathCallback, fileChooserParams)

                    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                        return super.onConsoleMessage(consoleMessage)
                    }
                }

                loadUrl(XroarAssetWebViewClient.START_URL)
                webViewState.value = this
                onCreated(this)
            }
        },
        update = { webViewState.value = it }
    )
}

private class XroarAssetWebViewClient(
    private val assetManager: AssetManager,
    private val romRepository: RomRepository,
    private val onOpenExternalUrl: (Uri) -> Unit
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url ?: return false
        if (url.scheme == "https" && url.host == HOST) {
            return false
        }
        if (url.scheme == "http" || url.scheme == "https") {
            onOpenExternalUrl(url)
            return true
        }
        return false
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url ?: return null
        if (url.scheme != "https" || url.host != HOST) {
            return null
        }

        val path = url.encodedPath ?: return null
        if (!path.startsWith(ASSET_PREFIX)) {
            return null
        }

        val assetPath = path.removePrefix(ASSET_PREFIX)
            .ifBlank { "xroar/index.html" }
        return openAsset(assetPath)
    }

    private fun openAsset(assetPath: String): WebResourceResponse? {
        return try {
            val stream = assetManager.open(assetPath)
            successResponse(assetPath, stream)
        } catch (_: Exception) {
            val romName = assetPath.substringAfterLast('/')
            val romStream = romRepository.openFile(romName)
            if (romStream != null) {
                successResponse(romName, romStream)
            } else {
                WebResourceResponse(
                    "text/plain",
                    "utf-8",
                    404,
                    "Not Found",
                    emptyMap(),
                    ByteArrayInputStream("Missing asset: $assetPath".toByteArray())
                )
            }
        }
    }

    private fun successResponse(path: String, stream: InputStream): WebResourceResponse {
        return WebResourceResponse(
            guessMimeType(path),
            "utf-8",
            200,
            "OK",
            mapOf(
                "Cache-Control" to "no-cache",
                "Access-Control-Allow-Origin" to "*"
            ),
            stream
        )
    }

    private fun guessMimeType(path: String): String {
        val lower = path.lowercase(Locale.US)
        return when {
            lower.endsWith(".html") -> "text/html"
            lower.endsWith(".css") -> "text/css"
            lower.endsWith(".js") -> "application/javascript"
            lower.endsWith(".wasm") -> "application/wasm"
            lower.endsWith(".json") -> "application/json"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".svg") -> "image/svg+xml"
            else -> MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(lower.substringAfterLast('.', ""))
                ?: "application/octet-stream"
        }
    }

    companion object {
        const val HOST = "appassets.androidplatform.net"
        const val ASSET_PREFIX = "/assets/"
        const val START_URL = "https://$HOST/assets/xroar/index.html"
    }
}

private class XroarAndroidBridge(
    private val romRepository: RomRepository,
    private val configRepository: ConfigRepository,
    private val onPickRomDirectory: () -> Unit,
    private val onPickConfigFile: () -> Unit,
    private val hasPhysicalKeyboard: () -> Boolean,
    private val getDisplayMode: () -> String,
    private val setDisplayMode: (String) -> Unit,
    private val onHapticFeedback: () -> Unit
) {

    @JavascriptInterface
    fun pickRomFolder() {
        onPickRomDirectory()
    }

    @JavascriptInterface
    fun pickConfigFile() {
        onPickConfigFile()
    }

    @JavascriptInterface
    fun getImportedRoms(): String {
        return romRepository.listFilesJson()
    }

    @JavascriptInterface
    fun clearImportedRoms(): Int {
        return romRepository.clearAll()
    }

    @JavascriptInterface
    fun hasImportedConfig(): Boolean {
        return configRepository.exists()
    }

    @JavascriptInterface
    fun importedConfigName(): String {
        return configRepository.displayName()
    }

    @JavascriptInterface
    fun clearImportedConfig(): Boolean {
        return configRepository.clear()
    }

    @JavascriptInterface
    fun readImportedConfigBase64(): String {
        val data = configRepository.readBytes() ?: return ""
        return encodeBase64(data)
    }

    @JavascriptInterface
    fun readRomBase64(fileName: String): String {
        val data = romRepository.readBytes(fileName) ?: return ""
        return encodeBase64(data)
    }

    @JavascriptInterface
    fun hapticTap() {
        onHapticFeedback()
    }

    @JavascriptInterface
    fun hasPhysicalKeyboard(): Boolean {
        return hasPhysicalKeyboard.invoke()
    }

    @JavascriptInterface
    fun getDisplayMode(): String {
        return getDisplayMode.invoke()
    }

    @JavascriptInterface
    fun setDisplayMode(mode: String) {
        setDisplayMode.invoke(mode)
    }

    private fun encodeBase64(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.NO_WRAP)
    }
}

private class RomRepository(private val rootDir: File) {

    init {
        rootDir.mkdirs()
    }

    fun importTree(
        context: android.content.Context,
        contentResolver: android.content.ContentResolver,
        treeUri: Uri
    ): RomImportResult {
        val tree = DocumentFile.fromTreeUri(context, treeUri)
        return if (tree == null) {
            RomImportResult(0, "Unable to access selected folder.", true)
        } else {
            importDocumentTree(contentResolver, tree)
        }
    }

    private fun importDocumentTree(
        contentResolver: android.content.ContentResolver,
        tree: DocumentFile
    ): RomImportResult {
        clearAll()

        var imported = 0
        val names = mutableListOf<String>()

        fun walk(doc: DocumentFile?) {
            if (doc == null) return
            if (doc.isDirectory) {
                doc.listFiles().forEach(::walk)
                return
            }
            val name = doc.name ?: return
            if (!name.lowercase(Locale.US).endsWith(".rom")) {
                return
            }
            val target = File(rootDir, name)
            contentResolver.openInputStream(doc.uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
                imported += 1
                names += name
            }
        }

        walk(tree)

        return when {
            imported == 0 -> RomImportResult(0, "No .rom files were found in the selected folder.", true)
            else -> RomImportResult(imported, "Imported $imported ROM file(s): ${names.sorted().joinToString(", ")}", false)
        }
    }

    fun openFile(fileName: String): InputStream? {
        val file = File(rootDir, fileName)
        return if (file.isFile) FileInputStream(file) else null
    }

    fun readBytes(fileName: String): ByteArray? {
        val file = File(rootDir, fileName)
        return if (file.isFile) file.readBytes() else null
    }

    fun clearAll(): Int {
        val files = rootDir.listFiles() ?: return 0
        var deleted = 0
        for (file in files) {
            if (file.isFile && file.delete()) {
                deleted += 1
            }
        }
        return deleted
    }

    fun listFilesJson(): String {
        val files = rootDir.listFiles()
            ?.filter { it.isFile }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
        return buildString {
            append("[")
            files.forEachIndexed { index, name ->
                if (index > 0) append(",")
                append("\"")
                append(name.replace("\\", "\\\\").replace("\"", "\\\""))
                append("\"")
            }
            append("]")
        }
    }
}

private data class RomImportResult(
    val importedCount: Int,
    val message: String,
    val isError: Boolean
)

private class ConfigRepository(private val rootDir: File) {

    private val configFile = File(rootDir, "xroar.conf")
    private val metaFile = File(rootDir, "xroar.conf.name")

    init {
        rootDir.mkdirs()
    }

    fun importFile(
        context: android.content.Context,
        contentResolver: android.content.ContentResolver,
        uri: Uri
    ): ConfigImportResult {
        rootDir.mkdirs()
        val displayName = DocumentFile.fromSingleUri(context, uri)?.name ?: "xroar.conf"
        contentResolver.openInputStream(uri)?.use { input ->
            configFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return ConfigImportResult(false, "Unable to read selected config file.", true)
        metaFile.writeText(displayName)
        return ConfigImportResult(true, "Imported config file as xroar.conf from $displayName.", false)
    }

    fun exists(): Boolean = configFile.isFile

    fun displayName(): String {
        return if (metaFile.isFile) metaFile.readText().trim().ifBlank { "xroar.conf" } else "xroar.conf"
    }

    fun readBytes(): ByteArray? = if (configFile.isFile) configFile.readBytes() else null

    fun clear(): Boolean {
        var changed = false
        if (configFile.isFile) {
            changed = configFile.delete() || changed
        }
        if (metaFile.isFile) {
            changed = metaFile.delete() || changed
        }
        return changed
    }
}

private data class ConfigImportResult(
    val imported: Boolean,
    val message: String,
    val isError: Boolean
)

private fun jsString(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
}
