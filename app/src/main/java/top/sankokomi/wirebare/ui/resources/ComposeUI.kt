package top.sankokomi.wirebare.ui.resources

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SmallColorfulText(
    mainText: String,
    subText: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit = {}
) {
    RealColumn(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(text = mainText, color = textColor, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(1.dp))
        Text(text = subText, color = textColor, fontSize = 12.sp)
    }
}

@Composable
fun LargeColorfulText(
    mainText: String,
    subText: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit = {}
) {
    RealColumn(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(text = mainText, color = textColor, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = subText, color = textColor, fontSize = 14.sp)
    }
}

@Composable
fun RealBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier, contentAlignment, propagateMinConstraints, content)
}

@Composable
fun RealColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier, verticalArrangement, horizontalAlignment, content)
}