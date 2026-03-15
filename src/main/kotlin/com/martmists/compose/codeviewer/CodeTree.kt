package com.martmists.compose.codeviewer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.lexer.KotlinLexer
import org.jetbrains.kotlin.lexer.KtTokens

class CodeTree(
    @param:Language("kt")
    val source: String,
    val colorPicker: ((current: Token, previous: Token?, next: Token?) -> Color)? = null,
) {
    private var loaded: AnnotatedString? = null
    private val loadMutex = Mutex()

    private var parenthesesColorIndex = 0
    private val parensColors = arrayOf(
        Color(0xFFFF6E6E),
        Color(0xFFFFAB6E),
        Color(0xFFFFD96E),
        Color(0xFF6EFFB4),
        Color(0xFF6EE8FF),
        Color(0xFF6E9FFF),
        Color(0xFFAE6EFF),
        Color(0xFFFF6EE0),
    )
    private fun defaultColorPicker(token: Token, previous: Token?, next: Token?): Color {
        return when (token.element) {
            in KtTokens.KEYWORDS -> Color(0xFFFF79C6)
            in KtTokens.COMMENTS -> Color(0xFF6272A4)
            in KtTokens.STRINGS, KtTokens.OPEN_QUOTE, KtTokens.CLOSING_QUOTE -> Color(0xFFF1FA8C)
            KtTokens.INTEGER_LITERAL, KtTokens.FLOAT_LITERAL -> Color(0xFFBD93F9)
            KtTokens.AT -> Color(0xFFE5C07B)
            KtTokens.IDENTIFIER -> when {
                previous != null && previous.element == KtTokens.AT -> Color(0xFFE5C07B)  // Also annotation
                token.text in KtTokens.SOFT_KEYWORDS.types.map { it.debugName } -> Color(0xFFFFB86C)  // Soft Keyword
                token.text.all { it.isUpperCase() || it.isDigit() || it == '_' } -> Color(0xFFCF9FFF)  // Const
                token.text[0].isUpperCase() -> Color(0xFF8BE9FD)  // likely a Type
                next != null && next.element in arrayOf(KtTokens.LPAR, KtTokens.LBRACE) -> Color(0xFF50FA7B)  // Function call
                else -> Color(0xFFF8F8F2)  // Identifier
            }
            in KtTokens.OPERATIONS -> Color(0xFFFF79C6)
            KtTokens.COLON, KtTokens.COMMA, KtTokens.SEMICOLON -> Color(0xFFABABBF)
            KtTokens.LPAR, KtTokens.LBRACE, KtTokens.LBRACKET -> {
                parensColors[parenthesesColorIndex++ % parensColors.size]  // Make these different colors to make it easier to differentiate
            }
            KtTokens.RPAR, KtTokens.RBRACE, KtTokens.RBRACKET -> {
                parensColors[--parenthesesColorIndex % parensColors.size]  // Match opening color
            }
            else -> Color(0xFFF8F8F2)
        }
    }

    private fun parseSource(): AnnotatedString {
        val lexer = KotlinLexer()
        lexer.start(source)

        val picker = colorPicker ?: ::defaultColorPicker

        val tokenList = mutableListOf<Token>()
        while (lexer.tokenType != null) {
            tokenList.add(Token(lexer.tokenType!!, lexer.tokenText))
            lexer.advance()
        }

        return buildAnnotatedString {
            for ((i, token) in tokenList.withIndex()) {
                val last = tokenList.subList(0, i).lastOrNull { it.element !in KtTokens.WHITESPACES }
                val next = tokenList.subList(i + 1, tokenList.size).firstOrNull { it.element !in KtTokens.WHITESPACES }

                withStyle(SpanStyle(color = picker(token, last, next))) {
                    append(token.text)
                }
            }
        }
    }

    suspend fun load(): AnnotatedString {
        loadMutex.withLock {
            if (loaded == null) {
                loaded = withContext(Dispatchers.Default) {
                    parseSource()
                }
            }
        }
        return loaded!!
    }
}
