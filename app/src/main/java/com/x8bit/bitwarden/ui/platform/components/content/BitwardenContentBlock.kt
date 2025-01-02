package com.x8bit.bitwarden.ui.platform.components.content

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.x8bit.bitwarden.R
import com.x8bit.bitwarden.ui.platform.base.util.bottomDivider
import com.x8bit.bitwarden.ui.platform.components.model.ContentBlockData
import com.x8bit.bitwarden.ui.platform.components.toggle.color.bitwardenSwitchColors
import com.x8bit.bitwarden.ui.platform.components.util.rememberVectorPainter
import com.x8bit.bitwarden.ui.platform.theme.BitwardenTheme

/**
 * An overloaded version [BitwardenContentBlock] which takes a [ContentBlockData] for the
 * header text.
 */
@Composable
fun BitwardenContentBlock(
    data: ContentBlockData,
    modifier: Modifier = Modifier,
    headerTextStyle: TextStyle = BitwardenTheme.typography.titleSmall,
    subtitleTextStyle: TextStyle = BitwardenTheme.typography.bodyMedium,
    backgroundColor: Color = BitwardenTheme.colorScheme.background.secondary,
    showDivider: Boolean = true,
    shape: ContentBlockShape = ContentBlockShape.Default,
    onContentClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    BitwardenContentBlock(
        headerText = data.headerText,
        modifier = modifier,
        headerTextStyle = headerTextStyle,
        subtitleText = data.subtitleText,
        subtitleTextStyle = subtitleTextStyle,
        iconVectorResource = data.iconVectorResource,
        backgroundColor = backgroundColor,
        showDivider = showDivider,
        shape = shape,
        onContentClick = onContentClick,
        trailingContent = trailingContent,
    )
}

/**
 * A default content block which displays a header with an optional subtitle and an icon.
 * Implemented to match design component.
 */
@Composable
private fun BitwardenContentBlock(
    headerText: AnnotatedString,
    modifier: Modifier = Modifier,
    headerTextStyle: TextStyle = BitwardenTheme.typography.titleSmall,
    subtitleText: AnnotatedString? = null,
    subtitleTextStyle: TextStyle = BitwardenTheme.typography.bodyMedium,
    showDivider: Boolean = true,
    @DrawableRes iconVectorResource: Int? = null,
    backgroundColor: Color = BitwardenTheme.colorScheme.background.secondary,
    shape: ContentBlockShape = ContentBlockShape.Default,
    onContentClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    var dividerStartPadding by remember { mutableStateOf(0.dp) }
    val localDensity = LocalDensity.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = shape(),
            )
            .clickable(enabled = onContentClick != null) {
                onContentClick?.invoke()
            }
            .bottomDivider(
                enabled = showDivider && (shape !is ContentBlockShape.Bottom),
                paddingStart = dividerStartPadding,
            )
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .onGloballyPositioned {
                    dividerStartPadding = with(localDensity) {
                        it.size.width.toDp()
                    }
                },
        ) {
            iconVectorResource
                ?.let {
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        painter = rememberVectorPainter(it),
                        contentDescription = null,
                        tint = BitwardenTheme.colorScheme.icon.secondary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                }
                ?: Spacer(Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(weight = 1f, fill = true)) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = headerText,
                style = headerTextStyle,
            )
            subtitleText?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = subtitleTextStyle,
                    color = BitwardenTheme.colorScheme.text.secondary,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        trailingContent?.let {
            Spacer(Modifier.width(16.dp))
            it()
        }
        Spacer(Modifier.width(16.dp))
    }
}

/**
 * Sealed interface representing the shape of a [BitwardenContentBlock] background.
 */
sealed interface ContentBlockShape {
    /**
     * Returns the [Shape] defined by this [ContentBlockShape].
     */
    operator fun invoke(): Shape

    /**
     * The default [ContentBlockShape] implementation. A rectangle shape.
     */
    data object Default : ContentBlockShape {
        override fun invoke(): Shape = RectangleShape
    }

    /**
     * The shape of a [BitwardenContentBlock] with rounded corners at the top.
     */
    data object Top : ContentBlockShape {
        override fun invoke(): Shape = RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 8.dp,
        )
    }

    /**
     * The shape of a [BitwardenContentBlock] with rounded corners at the bottom.
     */
    data object Bottom : ContentBlockShape {
        override fun invoke(): Shape = RoundedCornerShape(
            bottomStart = 8.dp,
            bottomEnd = 8.dp,
        )
    }

    /**
     * The shape of a [BitwardenContentBlock] with rounded corners on all sides.
     */
    data object Rounded : ContentBlockShape {
        override fun invoke(): Shape = RoundedCornerShape(
            size = 8.dp,
        )
    }
}

@Preview
@Composable
private fun BitwardenContentBlock_preview() {
    BitwardenTheme {
        Column(
            modifier = Modifier
                .background(color = BitwardenTheme.colorScheme.background.primary)
                .padding(16.dp),
        ) {
            BitwardenContentBlock(
                data = ContentBlockData(
                    headerText = "Header",
                    subtitleText = "Subtitle",
                    iconVectorResource = null,
                ),
                shape = ContentBlockShape.Top,
            )
            BitwardenContentBlock(
                data = ContentBlockData(
                    headerText = "Header",
                    subtitleText = "Subtitle",
                    iconVectorResource = R.drawable.ic_number2,
                ),
            )
            BitwardenContentBlock(
                data = ContentBlockData(
                    headerText = "Header",
                    subtitleText = "Subtitle",
                    iconVectorResource = null,
                ),
                showDivider = false,
                trailingContent = {
                    Switch(
                        modifier = Modifier
                            .height(height = 56.dp)
                            .testTag(tag = "SwitchToggle"),
                        enabled = false,
                        checked = false,
                        onCheckedChange = null,
                        colors = bitwardenSwitchColors(),
                    )
                },
            )
            BitwardenContentBlock(
                data = ContentBlockData(
                    headerText = "Header",
                    subtitleText = "Subtitle",
                    iconVectorResource = null,
                ),
                shape = ContentBlockShape.Bottom,
            )
        }
    }
}
