package com.martmists.compose.codeviewer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun CodeViewer(
    tree: CodeTree,
    style: TextStyle = TextStyle.Default.copy(fontFamily = FontFamily.Monospace),
    codeBackground: Color = MaterialTheme.colorScheme.background,
    softWrap: Boolean = false,
    modifier: Modifier = Modifier
) {
    var content by remember { mutableStateOf<AnnotatedString?>(null) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    LaunchedEffect(tree) {
        content = tree.load()
    }

    Box(modifier) {
        content?.let { annotated ->
            val lineStartOffsets = remember(annotated) {
                buildList {
                    add(0)
                    annotated.indices.forEach { i ->
                        if (annotated[i] == '\n') add(i + 1)
                    }
                }
            }

            val vScroll = rememberScrollState()
            val hScroll = rememberScrollState()

            Row(Modifier.fillMaxSize().verticalScroll(vScroll)) {
                Box(Modifier.padding(end = 8.dp)) {
                    textLayoutResult?.let { layout ->
                        lineStartOffsets.forEachIndexed { i, offset ->
                            val visualLine = layout.getLineForOffset(offset)
                            Text(
                                text = (i + 1).toString().padStart(lineStartOffsets.size.toString().length, ' '),
                                style = style,
                                color = Color.Gray,
                                modifier = Modifier.offset(y = with(LocalDensity.current) {
                                    layout.getLineTop(visualLine).toDp()
                                })
                            )
                        }
                    }
                }

                Box(Modifier.weight(1f).background(color = codeBackground)) {
                    val mod = if (softWrap) Modifier else Modifier.horizontalScroll(hScroll)
                    SelectionContainer(mod) {
                        Text(
                            text = annotated,
                            style = style,
                            softWrap = softWrap,
                            onTextLayout = { textLayoutResult = it }
                        )
                    }
                }
            }
            HorizontalScrollbar(rememberScrollbarAdapter(hScroll), modifier = Modifier.align(Alignment.BottomCenter))
            VerticalScrollbar(rememberScrollbarAdapter(hScroll), modifier = Modifier.align(Alignment.CenterEnd))
        }?: run {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}
