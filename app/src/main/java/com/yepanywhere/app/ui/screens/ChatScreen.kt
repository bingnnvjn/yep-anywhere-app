package com.yepanywhere.app.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.yepanywhere.app.ui.theme.GradientEnd
import com.yepanywhere.app.ui.theme.GradientStart

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    serverUrl: String,
    password: String,
    onBackToConfig: () -> Unit
) {
    // Use remember to create the savePassword reference that WebView can access
    val savePassword = remember { password }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }

    // File upload handler
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

    // Back button handling
    BackHandler(enabled = canGoBack) {
        webView?.let {
            if (it.canGoBack()) it.goBack()
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
                        if (isLoading) {
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
            if (errorMessage != null) {
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
                    Button(
                        onClick = onBackToConfig,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("返回设置")
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
                                errorMessage = null
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                canGoBack = view?.canGoBack() ?: false

                                // Auto-login if password is configured
                                if (savePassword.isNotBlank()) {
                                    autoLogin(view, savePassword)
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    errorMessage = when (error?.errorCode) {
                                        ERROR_HOST_LOOKUP -> "找不到服务器，请检查地址"
                                        ERROR_CONNECT -> "连接被拒绝，服务器是否在运行？"
                                        ERROR_TIMEOUT -> "连接超时"
                                        else -> "连接失败（${error?.description ?: "未知错误"}）"
                                    }
                                    isLoading = false
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

            // Loading overlay
            if (isLoading && errorMessage == null) {
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
