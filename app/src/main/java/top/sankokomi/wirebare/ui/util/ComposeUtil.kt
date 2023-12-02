package top.sankokomi.wirebare.ui.util

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.random.Random

/**
 * 用于测试 Composable 函数的重组情况，若发生了重组，背景颜色将会被改变
 * */
fun Modifier.test(): Modifier =
    this.then(background(Color(Random.nextInt())))

/**
 * 将 compose 颜色转换为 Int
 * */
val Color.androidColor: Int get() = toArgb()

/**
 * 将 Int 转换为 compose 颜色
 * */
val Int.composeColor: Color get() = Color(this)