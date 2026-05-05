package com.yepanywhere.app.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    serverUrl: String,
    password: String,
    onBackToConfig: () -> Unit
) {
    val savePassword = remember { password }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }

    // Auto-reconnect state
    var reconnecting by remember { mutableStateOf(false) }
    var retryCount by remember { mutableIntStateOf(0) }
    val maxRetries = 5
    var lastErrorUrl by remember { mutableStateOf("") }

    // Reset and retry when error changes
    LaunchedEffect(retryCount) {
        if (reconnecting && retryCount in 1..maxRetries) {
            val delaySec = when {
                retryCount <= 1 -> 1000L
                retryCount == 2 -> 2000L
                retryCount == 3 -> 4000L
                retryCount == 4 -> 8000L
                else -> 16000L
            }
            delay(delaySec)
            webView?.loadUrl(if (lastErrorUrl.isNotBlank()) lastErrorUrl else serverUrl)
        }
    }

    var pendingFileCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val fileUploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { arrayOf(it) }
        } else null
        pendingFileCallback?.onReceiveValue(uris)
        pendingFileCallback = null
    }

    BackHandler(enabled = canGoBack) {
        if (reconnecting) {
            reconnecting = false
            errorMessage = "连接已中断"
        } else {
            webView?.let {
                if (it.canGoBack()) it.goBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Yep Anywhere",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (reconnecting) {
                            Text(
                                "重连中 ($retryCount/$maxRetries)...",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (isLoading && !reconnecting) {
                            LinearProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        webView?.let {
                            if (it.canGoBack()) it.goBack()
                            else onBackToConfig()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onBackToConfig) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Error state
            if (errorMessage != null && !reconnecting) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("❌", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "无法连接",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        errorMessage ?: "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                reconnecting = true
                                retryCount = 0
                                retryCount = 1
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("重试")
                        }
                        OutlinedButton(
                            onClick = onBackToConfig,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("返回设置")
                        }
                    }
                }
            }

            // WebView
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = false
                            setSupportZoom(false)
                            allowFileAccess = true
                            allowContentAccess = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                // Successful navigation = not an error anymore
                                if (!reconnecting) {
                                    errorMessage = null
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                canGoBack = view?.canGoBack() ?: false

                                // Auto-login if password is configured
                                if (savePassword.isNotBlank()) {
                                    view?.let { autoLogin(it, savePassword) }
                                }

                                // Page loaded successfully — stop reconnecting
                                if (reconnecting) {
                                    reconnecting = false
                                    errorMessage = null
                                    retryCount = 0
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    val url = request.url.toString()
                                    lastErrorUrl = url

                                    when (error?.errorCode) {
                                        ERROR_HOST_LOOKUP -> errorMessage = "找不到服务器"
                                        ERROR_CONNECT -> errorMessage = "连接被拒绝"
                                        ERROR_TIMEOUT -> errorMessage = "连接超时"
                                        else -> errorMessage = "连接失败"
                                    }

                                    // Start auto-reconnect
                                    if (!reconnecting && retryCount < maxRetries) {
                                        reconnecting = true
                                        retryCount = 1
                                    }
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                            }

                            override fun onShowFileChooser(
                                view: WebView?,
                                filePathCallback: ValueCallback<Array<Uri>>?,
                                params: FileChooserParams?
                            ): Boolean {
                                pendingFileCallback?.onReceiveValue(null)
                                pendingFileCallback = filePathCallback

                                val intent = params?.createIntent() ?: android.content.Intent(
                                    android.content.Intent.ACTION_GET_CONTENT
                                ).apply {
                                    addCategory(android.content.Intent.CATEGORY_OPENABLE)
                                    type = "*/*"
                                }

                                fileUploadLauncher.launch(intent)
                                return true
                            }
                        }

                        loadUrl(serverUrl)
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Reconnecting overlay
            if (reconnecting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "重连中...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "第 $retryCount/$maxRetries 次尝试",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Initial loading overlay
            if (isLoading && !reconnecting && errorMessage == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun autoLogin(webView: WebView, password: String) {
    val escapedPassword = password.replace("\\", "\\\\").replace("'", "\\'")
    val js = """
        (function() {
            var pwd = '$escapedPassword';
            var attempts = 0;
            var timer = setInterval(function() {
                attempts++;
                if (attempts > 50) { clearInterval(timer); return; }
                var passField = document.querySelector('input[type="password"]');
                if (passField) {
                    var userField = document.querySelector('input[type="text"], input:not([type]), input[name="username"]');
                    if (userField && !userField.value) {
                        userField.value = 'yep';
                        userField.dispatchEvent(new Event('input', {bubbles: true}));
                        userField.dispatchEvent(new Event('change', {bubbles: true}));
                    }
                    passField.value = pwd;
                    passField.dispatchEvent(new Event('input', {bubbles: true}));
                    passField.dispatchEvent(new Event('change', {bubbles: true}));
                    setTimeout(function() {
                        var btn = document.querySelector('button[type="submit"], input[type="submit"]');
                        if (btn) btn.click();
                    }, 200);
                    clearInterval(timer);
                }
            }, 300);
        })();
    """.trimIndent()
    try {
        webView.evaluateJavascript(js, null)
    } catch (_: Exception) { }
}
