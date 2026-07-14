/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.myguardianlink.myguardianwatch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults.buttonColors
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.my_guardian_link.R
import com.myguardianlink.myguardianwatch.presentation.theme.BrandNavy
import com.myguardianlink.myguardianwatch.presentation.theme.MyGuardianWatchTheme
import com.myguardianlink.myguardianwatch.presentation.theme.RoadSideOrange
import com.myguardianlink.myguardianwatch.presentation.theme.RoadSideRed
import com.myguardianlink.myguardianwatch.presentation.theme.rememberRoundScreenContentPadding
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyGuardianWatchTheme {
                AppScaffold(timeText = {}) {
                    UrgentAssistScreen(
                        sender = remember { WatchUrgentAssistSender(applicationContext) },
                    )
                }
            }
        }
    }
}

private enum class UrgentAssistUiState {
    Idle,
    Sending,
    Sent,
    NoPhone,
    Error,
}

private val CardShape = RoundedCornerShape(12.dp)

@Composable
fun UrgentAssistScreen(sender: WatchUrgentAssistSender) {
    var uiState by remember { mutableStateOf(UrgentAssistUiState.Idle) }
    var statusMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val roundPadding = rememberRoundScreenContentPadding()

    ScreenScaffold(
        timeText = {},
        scrollIndicator = {},
        modifier = Modifier.background(color = BrandNavy)
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
//                .padding(contentPadding)
                .padding(roundPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            /* Box(
                 modifier = Modifier
                     .fillMaxWidth()
                     .background(color = BrandWhite, shape = CardShape)
                     .padding(horizontal = 12.dp, vertical = 8.dp),
                 contentAlignment = Alignment.Center,
             ) {
                 Text(
                     text = stringResource(R.string.urgent_assist_title),
                     style = MaterialTheme.typography.titleSmall.copy(
                         fontWeight = FontWeight.SemiBold,
                         color = BrandBlack,
                     ),
                     textAlign = TextAlign.Center,
                     maxLines = 1,
                 )
             }*/

            Spacer(modifier = Modifier.height(6.dp))

            Image(
                painter = painterResource(id = R.drawable.img_app_icon),
                contentDescription = stringResource(R.string.urgent_assist_title),
                modifier = Modifier
                    .height(56.dp)
                    .width(56.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.my_guardian_link),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.height(1.dp).width(20.dp).background(RoadSideOrange))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (uiState) {
                    UrgentAssistUiState.Idle -> stringResource(R.string.urgent_assist_subtitle)
                    UrgentAssistUiState.Sending -> stringResource(R.string.urgent_assist_sending)
                    UrgentAssistUiState.Sent -> stringResource(R.string.urgent_assist_sent)
                    UrgentAssistUiState.NoPhone -> stringResource(R.string.urgent_assist_no_phone)
                    UrgentAssistUiState.Error -> statusMessage.ifBlank {
                        stringResource(R.string.urgent_assist_error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    lineHeight = 14.sp,
                ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState == UrgentAssistUiState.Sending) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            } else {
                Button(
                    onClick = {
                        if (uiState == UrgentAssistUiState.Sending) return@Button
                        coroutineScope.launch {
                            uiState = UrgentAssistUiState.Sending
                            when (val result = sender.sendUrgentAssist()) {
                                WatchUrgentAssistSender.SendResult.Success -> {
                                    uiState = UrgentAssistUiState.Sent
                                }

                                WatchUrgentAssistSender.SendResult.NoPhoneConnected -> {
                                    uiState = UrgentAssistUiState.NoPhone
                                }

                                is WatchUrgentAssistSender.SendResult.Error -> {
                                    statusMessage = result.message
                                    uiState = UrgentAssistUiState.Error
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp, max = 44.dp),
                    enabled = uiState != UrgentAssistUiState.Sending,
                    shape = CardShape,
                    colors = buttonColors(
                        containerColor = RoadSideRed,
                        contentColor = RoadSideRed,
                        disabledContainerColor = RoadSideRed.copy(alpha = 0.5f),
                        disabledContentColor = RoadSideRed.copy(alpha = 0.7f),
                    ),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.protect_me),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 14.sp,
                                fontSize = 12.sp,
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun DefaultPreview() {
    MyGuardianWatchTheme {
        AppScaffold(timeText = {}) {
            UrgentAssistScreen(
                sender = WatchUrgentAssistSender(LocalContext.current),
            )
        }
    }
}
