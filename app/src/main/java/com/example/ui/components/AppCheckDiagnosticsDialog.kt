package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.AppCheckTestUiState
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioObsidian
import com.example.util.AppCheckDiagnostics

@Composable
fun AppCheckDiagnosticsDialog(
    diagnostics: AppCheckDiagnostics?,
    testState: AppCheckTestUiState,
    onSaveAndTest: (modeOverride: String, customDebugToken: String, customSiteKey: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedMode by remember(diagnostics?.modeOverride) {
        mutableStateOf(diagnostics?.modeOverride ?: "auto")
    }
    var debugTokenInput by remember(diagnostics?.customDebugToken, diagnostics?.deviceDebugUuid) {
        mutableStateOf(
            diagnostics?.customDebugToken?.takeIf { it.isNotBlank() }
                ?: diagnostics?.deviceDebugUuid.orEmpty()
        )
    }
    var customSiteKeyInput by remember(diagnostics?.customSiteKey) {
        mutableStateOf(diagnostics?.customSiteKey.orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioCard,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CinemaGold
                )
                Text(
                    text = "تشخيص وإعداد Firebase App Check",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (diagnostics != null) {
                    Surface(
                        color = StudioElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DiagnosticRow("حالة Firebase:", if (diagnostics.isFirebaseInitialized) "مهيأ (متصل)" else "غير مهيأ")
                            DiagnosticRow("معرف المشروع:", diagnostics.projectId)
                            DiagnosticRow("اسم الحزمة:", diagnostics.applicationId)
                            DiagnosticRow("المزوّد النشط:", diagnostics.activeModeLabelAr)
                            DiagnosticRow("مفتاح reCAPTCHA:", diagnostics.siteKeyPreview)

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            CopyableFingerprintRow(
                                label = "بصمة التوقيع SHA-1:",
                                value = diagnostics.signingSha1,
                                onCopy = { copyToClipboard(context, "SHA-1", diagnostics.signingSha1) }
                            )
                            CopyableFingerprintRow(
                                label = "بصمة التوقيع SHA-256:",
                                value = diagnostics.signingSha256,
                                onCopy = { copyToClipboard(context, "SHA-256", diagnostics.signingSha256) }
                            )
                        }
                    }

                    if (diagnostics.hasProjectMismatchWarning) {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "تنبيه: ملف google-services.json المدمج يخص المشروع (${diagnostics.projectId}) بينما مفتاح reCAPTCHA الافتراضي يخص مشروعاً آخر. أدخل مفتاح reCAPTCHA Android الخاص بمشروعك أو استخدم رمز Debug Token.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "طريقة التحقق من App Check:",
                    style = MaterialTheme.typography.labelLarge,
                    color = CinemaGold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedMode == "auto",
                        onClick = { selectedMode = "auto" },
                        label = { Text("تلقائي") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CinemaGold.copy(alpha = 0.22f),
                            selectedLabelColor = CinemaGold
                        ),
                        modifier = Modifier.testTag("appcheck_mode_auto")
                    )
                    FilterChip(
                        selected = selectedMode == "debug",
                        onClick = { selectedMode = "debug" },
                        label = { Text("Debug Token") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioCyan.copy(alpha = 0.22f),
                            selectedLabelColor = StudioCyan
                        ),
                        modifier = Modifier.testTag("appcheck_mode_debug")
                    )
                    FilterChip(
                        selected = selectedMode == "recaptcha",
                        onClick = { selectedMode = "recaptcha" },
                        label = { Text("reCAPTCHA") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioEmerald.copy(alpha = 0.22f),
                            selectedLabelColor = StudioEmerald
                        ),
                        modifier = Modifier.testTag("appcheck_mode_recaptcha")
                    )
                }

                if (selectedMode == "debug") {
                    OutlinedTextField(
                        value = debugTokenInput,
                        onValueChange = { debugTokenInput = it },
                        label = { Text("رمز التصحيح App Check Debug UUID") },
                        placeholder = { Text("اتركه فارغاً لتوليد رمز تلقائي أو الصق رمز Firebase Console") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioCyan,
                            unfocusedBorderColor = StudioElevated
                        ),
                        trailingIcon = {
                            if (debugTokenInput.isNotBlank()) {
                                IconButton(
                                    onClick = { copyToClipboard(context, "App Check Debug Token", debugTokenInput) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "نسخ الرمز",
                                        tint = StudioCyan
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("appcheck_debug_token_input")
                    )
                    Text(
                        text = "لتشغيل AI على نسخة Debug مجمعة عبر GitHub: انسخ رمز UUID أعلاه وأضفه في Firebase Console > App Check > Apps > Manage debug tokens.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedMode == "recaptcha") {
                    OutlinedTextField(
                        value = customSiteKeyInput,
                        onValueChange = { customSiteKeyInput = it },
                        label = { Text("مفتاح reCAPTCHA Enterprise Site Key (اختياري)") },
                        placeholder = { Text("اتركه فارغاً لاستخدام المفتاح المدمج في التطبيق") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CinemaGold,
                            unfocusedBorderColor = StudioElevated
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("appcheck_site_key_input")
                    )
                }

                when (testState) {
                    is AppCheckTestUiState.Testing -> {
                        Surface(
                            color = StudioElevated,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = CinemaGold,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "جاري فحص رمز App Check مع خوادم Google...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    is AppCheckTestUiState.Success -> {
                        Surface(
                            color = StudioEmerald.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, StudioEmerald.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StudioEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = testState.messageAr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StudioEmerald
                                )
                            }
                        }
                    }
                    is AppCheckTestUiState.Failed -> {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = testState.messageAr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    AppCheckTestUiState.Idle -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveAndTest(selectedMode, debugTokenInput, customSiteKeyInput)
                },
                enabled = testState !is AppCheckTestUiState.Testing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaGold,
                    contentColor = StudioObsidian
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("appcheck_save_and_test_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "حفظ وفحص الاتصال",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("appcheck_dialog_close_btn")
            ) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.White,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun CopyableFingerprintRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = CinemaGold
            )
            TextButton(
                onClick = onCopy,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.defaultMinSize(minHeight = 36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "نسخ",
                    tint = StudioCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("نسخ", style = MaterialTheme.typography.labelSmall, color = StudioCyan)
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = Color(0xFFCBD5E1)
        )
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "تم نسخ $label", Toast.LENGTH_SHORT).show()
}
