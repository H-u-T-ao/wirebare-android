package top.sankokomi.wirebare.ui.resources

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.util.statusBarHeightDp

@Composable
fun AppStatusBar(color: Color = Color.Transparent) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(statusBarHeightDp)
            .background(color)
    )
}

@Composable
fun AppTitleBar(
    icon: Any = R.mipmap.ic_wirebare,
    text: String = stringResource(id = R.string.app_name),
    endContent: @Composable BoxScope.() -> Unit = {}
) {
    RealColumn(
        modifier = Modifier.shadow(2.dp)
    ) {
        AppStatusBar(Color.White)
        RealBox(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            RealRow(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = icon,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            RealBox(
                modifier = Modifier.align(Alignment.CenterEnd),
                content = endContent
            )
        }
    }
}

@Composable
fun AppCheckBoxItemMenuPopup(
    itemList: List<Pair<State<String>, MutableState<Boolean>>>,
    size: Int = itemList.size
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    Box {
        Image(
            painter = painterResource(id = R.drawable.ic_more),
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                    isMenuExpanded = true
                },
            contentDescription = null
        )
        DropdownMenu(
            expanded = isMenuExpanded,
            modifier = Modifier
                .background(Purple80)
                .padding(vertical = 2.dp, horizontal = 10.dp),
            onDismissRequest = {
                isMenuExpanded = false
            }
        ) {
            for (i in 0 until size) {
                val item = itemList[i].first
                val checked = itemList[i].second
                RealRow(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            checked.value = !checked.value
                            isMenuExpanded = false
                        }
                        .padding(vertical = 2.dp)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked.value,
                        onCheckedChange = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.value,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppNavigationBar(
    pagerState: PagerState,
    navigationItems: List<Pair<Pair<Painter, String>, Pair<Painter, String>>>
) {
    val rememberScope = rememberCoroutineScope()
    RealRow(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
            .background(Purple80)
            .padding(vertical = 2.dp)
    ) {
        for (index in navigationItems.indices) {
            val item = navigationItems[index]
            RealBox(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1F)
            ) {
                val (painter, str) = if (pagerState.currentPage != index) {
                    item.first
                } else {
                    item.second
                }
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            rememberScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                ) {
                    ImageButton(
                        painter = painter,
                        str = str
                    )
                }
            }
        }
    }
}

@Composable
fun ImageButton(
    painter: Painter,
    str: String
) {
    RealColumn(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painter,
            modifier = Modifier.size(32.dp),
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = str,
            fontSize = 14.sp,
            color = Color.Black
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
        Text(
            text = mainText,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 15.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subText,
            color = textColor,
            fontSize = 12.sp,
            lineHeight = 13.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LargeColorfulText(
    mainText: String,
    subText: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    RealColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clip(RoundedCornerShape(6.dp))
            .combinedClickable(onLongClick = onLongClick, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = mainText,
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp
        )
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