package com.galaxy.airviewdictionary.ui.common

import android.net.Uri
import androidx.core.net.toUri
import android.widget.VideoView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MP4Player(resourceId: Int, width: Dp = 300.dp, height: Dp = 300.dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // 비디오 URI 설정
    var videoUri = Uri.EMPTY
    var showErrorMessage = false

    val videoView = remember { VideoView(context) }

    try {
        // 비디오 URI 설정
        val resourceUri = "android.resource://${context.packageName}/$resourceId".toUri()
        // 올바른 resourceId일 경우 videoUri 설정
        videoUri = resourceUri
    } catch (e: Exception) {
        // 리소스가 없거나 오류가 발생했을 경우 오류 메시지 표시 설정
        showErrorMessage = true
    }

    // Box로 비디오 표시
    Box(
        modifier = modifier
            .width(width)
            .height(height)
    ) {
        if (showErrorMessage) {
            Text(
                text = "Video resource not found.",
                fontSize = 20.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // AndroidView를 사용하여 VideoView 렌더링
            AndroidView(
                factory = { _ ->
                    videoView.apply {
                        // VideoView가 준비되면 자동으로 반복 재생 설정
                        setOnPreparedListener { mediaPlayer ->
                            mediaPlayer.isLooping = true // 반복 재생
                        }
                    }
                },
                update = { videoView ->
                    // resourceId가 변경될 때마다 새 비디오 URI로 업데이트
                    videoView.setVideoURI(videoUri)
                    videoView.start() // 비디오 재생 시작
                },
                modifier = Modifier.matchParentSize()
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // VideoView의 재생 중지 및 리소스 해제
            videoView.stopPlayback() // 비디오 정지
        }
    }
}

