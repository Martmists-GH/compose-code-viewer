package com.martmists.compose.codeviewer

import com.intellij.psi.tree.IElementType

data class Token(
    val element: IElementType,
    val text: String,
)
