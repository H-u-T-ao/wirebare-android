package top.sankokomi.wirebare.ui.resources

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import top.sankokomi.wirebare.ui.R

@Composable
fun AppTitleBar(
    icon: Any = R.mipmap.ic_wirebare,
    text: String = stringResource(id = R.string.app_name),
    endContent: @Composable BoxScope.() -> Unit
) {
    RealBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        RealRow(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = icon,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        RealBox(
            modifier = Modifier.align(Alignment.CenterEnd),
            content = endContent
        )
    }
}

@Composable
fun VisibleFadeInFadeOutAnimation(
    visible: Boolean = true,
    content: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        content = content
    )
}

@Composable
fun SmallColorfulText(
    mainText: String,
    subText: String,
    backgroundColor: Color,
    textColor: Color
) {
    RealColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clip(RoundedCornerShape(6.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(text = mainText, color = textColor, fontSize = 14.sp, lineHeight = 15.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = subText, color = textColor, fontSize = 12.sp, lineHeight = 13.sp)
    }
}

@Composable
fun LargeColorfulText(
    mainText: String,
    subText: String,
    backgroundColor: Color,
    textColor: Color
) {
    RealColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clip(RoundedCornerShape(6.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(text = mainText, color = textColor, fontSize = 18.sp, lineHeight = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = subText, color = textColor, fontSize = 14.sp, lineHeight = 16.sp)
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

@Composable
fun RealRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    Row(modifier, horizontalArrangement, verticalAlignment, content)
}