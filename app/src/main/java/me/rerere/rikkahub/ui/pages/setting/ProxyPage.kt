package me.rerere.rikkahub.ui.pages.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.rerere.ai.provider.ProviderProxy
import me.rerere.ai.provider.ProviderSetting
import me.rerere.common.platform.PlatformHttpClient
import me.rerere.common.platform.PlatformHttpProxy
import me.rerere.common.platform.PlatformHttpRequest
import me.rerere.rikkahub.R
import me.rerere.rikkahub.ui.components.ui.FormItem
import me.rerere.rikkahub.utils.UiState
import org.koin.compose.koinInject

@Composable
internal fun SettingProviderProxyPage(
    provider: ProviderSetting,
    onEdit: (ProviderSetting) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val httpClient = koinInject<PlatformHttpClient>()
    val testOkText = stringResource(R.string.setting_provider_page_proxy_test_ok)
    val testFailLabel = stringResource(R.string.setting_provider_page_proxy_test)

    val current = provider.proxy
    val enabled = current != ProviderProxy.None
    var enabledState by remember(provider.id) { mutableStateOf(enabled) }
    var typeIsSocks by remember(provider.id) { mutableStateOf(current is ProviderProxy.Socks5) }
    var address by remember(provider.id) { mutableStateOf((current as? ProviderProxy.Http)?.address ?: ((current as? ProviderProxy.Socks5)?.address ?: "")) }
    var port by remember(provider.id) { mutableStateOf((((current as? ProviderProxy.Http)?.port ?: (current as? ProviderProxy.Socks5)?.port)?.toString() ?: "")) }
    var username by remember(provider.id) { mutableStateOf((current as? ProviderProxy.Http)?.username ?: ((current as? ProviderProxy.Socks5)?.username ?: "")) }
    var password by remember(provider.id) { mutableStateOf((current as? ProviderProxy.Http)?.password ?: ((current as? ProviderProxy.Socks5)?.password ?: "")) }

    fun buildProxy(): ProviderProxy {
        if (!enabledState) return ProviderProxy.None
        val portNum = port.toIntOrNull() ?: return ProviderProxy.None
        if (address.isBlank()) return ProviderProxy.None
        val user = username.takeIf { it.isNotBlank() }
        val pass = password.takeIf { it.isNotBlank() }
        return if (typeIsSocks) ProviderProxy.Socks5(address.trim(), portNum, user, pass)
        else ProviderProxy.Http(address.trim(), portNum, user, pass)
    }

    fun commit() {
        val p = buildProxy()
        if (p != provider.proxy) onEdit(provider.copyProvider(proxy = p))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
    ) {
        Card(
            shape = me.rerere.rikkahub.ui.theme.AppShapes.CardLarge,
            colors = CardDefaults.cardColors(
                containerColor = if (me.rerere.rikkahub.ui.theme.LocalDarkMode.current) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            FormItem(
                label = { Text(stringResource(R.string.setting_provider_page_proxy)) },
                description = { Text(stringResource(R.string.setting_provider_page_proxy_desc)) }
            ) {
                Switch(
                    checked = enabledState,
                    onCheckedChange = {
                        enabledState = it
                        if (!it) onEdit(provider.copyProvider(proxy = ProviderProxy.None))
                    }
                )
            }

            if (enabledState) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = !typeIsSocks,
                        onClick = {
                            typeIsSocks = false
                            commit()
                        },
                        label = { Text(stringResource(R.string.setting_provider_page_proxy_type_http)) }
                    )
                    FilterChip(
                        selected = typeIsSocks,
                        onClick = {
                            typeIsSocks = true
                            commit()
                        },
                        label = { Text(stringResource(R.string.setting_provider_page_proxy_type_socks5)) }
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(stringResource(R.string.setting_provider_page_proxy_host)) },
                    placeholder = { Text("proxy.example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = me.rerere.rikkahub.ui.theme.AppShapes.InputField,
                )
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it.filter { ch -> ch.isDigit() } },
                    label = { Text(stringResource(R.string.setting_provider_page_proxy_port)) },
                    placeholder = { Text("8080") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = me.rerere.rikkahub.ui.theme.AppShapes.InputField,
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.setting_provider_page_proxy_username)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = me.rerere.rikkahub.ui.theme.AppShapes.InputField,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.setting_provider_page_proxy_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = me.rerere.rikkahub.ui.theme.AppShapes.InputField,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { commit() }) {
                        Text(stringResource(R.string.save))
                    }
                }

                var testState by remember { mutableStateOf<UiState<String>>(UiState.Idle) }
                Button(
                    onClick = {
                        commit()
                        val p = buildProxy()
                        if (p == ProviderProxy.None) return@Button
                        val platformProxy = when (p) {
                            is ProviderProxy.Http -> PlatformHttpProxy(p.address, p.port, p.username, p.password, "http")
                            is ProviderProxy.Socks5 -> PlatformHttpProxy(p.address, p.port, p.username, p.password, "socks5")
                            else -> null
                        } ?: return@Button
                        scope.launch {
                            testState = UiState.Loading
                            testState = withContext(Dispatchers.IO) {
                                runCatching {
                                    val resp = httpClient.execute(
                                        PlatformHttpRequest(
                                            method = "GET",
                                            url = "https://api.github.com/zen",
                                            headers = mapOf("User-Agent" to "LastNap-ProxyTest"),
                                            proxy = platformProxy,
                                        )
                                    )
                                    if (resp.statusCode in 200..299) UiState.Success(testOkText)
                                    else UiState.Error(RuntimeException("HTTP ${resp.statusCode}"))
                                }.getOrElse {
                                    UiState.Error(it)
                                }
                            }
                        }
                    },
                    enabled = enabledState && address.isNotBlank() && port.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (testState) {
                        is UiState.Loading -> {
                            LinearWavyProgressIndicator(Modifier.fillMaxWidth())
                        }
                        else -> {
                            Icon(Icons.Rounded.NetworkCheck, null)
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.setting_provider_page_proxy_test))
                        }
                    }
                }
                when (val st = testState) {
                    is UiState.Success -> Text(
                        text = st.data,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    is UiState.Error -> Text(
                        text = st.error.message ?: testFailLabel,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    else -> Unit
                }
            }
        }
    }
}
}
