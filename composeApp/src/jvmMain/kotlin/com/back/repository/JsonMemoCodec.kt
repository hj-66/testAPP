package com.back.repository

import com.back.model.Memo
import java.time.Instant

object JsonMemoCodec {
    fun encode(memos: List<Memo>): String = buildString {
        append("[\n")
        memos.forEachIndexed { index, memo ->
            append("  {\n")
            append("    \"id\": \"").append(escape(memo.id)).append("\",\n")
            append("    \"title\": \"").append(escape(memo.title)).append("\",\n")
            append("    \"content\": \"").append(escape(memo.content)).append("\",\n")
            append("    \"filePath\": ")
            memo.filePath?.let { append("\"").append(escape(it)).append("\"") } ?: append("null")
            append(",\n")
            append("    \"isFavorite\": ").append(memo.isFavorite).append(",\n")
            append("    \"createdAt\": \"").append(memo.createdAt).append("\",\n")
            append("    \"updatedAt\": \"").append(memo.updatedAt).append("\"\n")
            append("  }")
            if (index < memos.lastIndex) append(",")
            append("\n")
        }
        append("]\n")
    }

    fun decode(json: String): List<Memo> {
        if (json.isBlank()) return emptyList()
        val root = Parser(json).parse()
        val items = root as? List<*> ?: return emptyList()
        return items.mapNotNull { item ->
            val obj = item as? Map<*, *> ?: return@mapNotNull null
            val createdAt = obj.string("createdAt")?.let(::parseInstant) ?: Instant.now()
            Memo(
                id = obj.string("id") ?: return@mapNotNull null,
                title = obj.string("title").orEmpty().ifBlank { "제목 없음" },
                content = obj.string("content").orEmpty(),
                filePath = obj.string("filePath"),
                isFavorite = obj.boolean("isFavorite") ?: false,
                createdAt = createdAt,
                updatedAt = obj.string("updatedAt")?.let(::parseInstant) ?: createdAt,
            )
        }
    }

    private fun Map<*, *>.string(key: String): String? = this[key] as? String

    private fun Map<*, *>.boolean(key: String): Boolean? = this[key] as? Boolean

    private fun parseInstant(value: String): Instant = runCatching { Instant.parse(value) }.getOrDefault(Instant.now())

    private fun escape(value: String): String = buildString {
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> {
                    if (char.code < 0x20) append("\\u%04x".format(char.code)) else append(char)
                }
            }
        }
    }

    private class Parser(private val source: String) {
        private var index = 0

        fun parse(): Any? {
            val value = parseValue()
            skipWhitespace()
            return value
        }

        private fun parseValue(): Any? {
            skipWhitespace()
            return when (peek()) {
                '[' -> parseArray()
                '{' -> parseObject()
                '"' -> parseString()
                't' -> readLiteral("true", true)
                'f' -> readLiteral("false", false)
                'n' -> readLiteral("null", null)
                else -> error("Unexpected character at $index")
            }
        }

        private fun parseArray(): List<Any?> {
            expect('[')
            val values = mutableListOf<Any?>()
            skipWhitespace()
            if (consume(']')) return values
            while (true) {
                values += parseValue()
                skipWhitespace()
                if (consume(']')) return values
                expect(',')
            }
        }

        private fun parseObject(): Map<String, Any?> {
            expect('{')
            val values = linkedMapOf<String, Any?>()
            skipWhitespace()
            if (consume('}')) return values
            while (true) {
                skipWhitespace()
                val key = parseString()
                skipWhitespace()
                expect(':')
                values[key] = parseValue()
                skipWhitespace()
                if (consume('}')) return values
                expect(',')
            }
        }

        private fun parseString(): String {
            expect('"')
            val result = StringBuilder()
            while (index < source.length) {
                val char = source[index++]
                when (char) {
                    '"' -> return result.toString()
                    '\\' -> result.append(parseEscape())
                    else -> result.append(char)
                }
            }
            error("Unterminated string")
        }

        private fun parseEscape(): Char {
            val escaped = source.getOrNull(index++) ?: error("Unterminated escape")
            return when (escaped) {
                '"', '\\', '/' -> escaped
                'b' -> '\b'
                'f' -> '\u000C'
                'n' -> '\n'
                'r' -> '\r'
                't' -> '\t'
                'u' -> {
                    val hex = source.substring(index, index + 4)
                    index += 4
                    hex.toInt(16).toChar()
                }
                else -> error("Unknown escape: $escaped")
            }
        }

        private fun readLiteral(literal: String, value: Any?): Any? {
            if (!source.startsWith(literal, index)) error("Expected $literal at $index")
            index += literal.length
            return value
        }

        private fun skipWhitespace() {
            while (index < source.length && source[index].isWhitespace()) index++
        }

        private fun peek(): Char? = source.getOrNull(index)

        private fun consume(char: Char): Boolean {
            if (peek() != char) return false
            index++
            return true
        }

        private fun expect(char: Char) {
            if (!consume(char)) error("Expected $char at $index")
        }
    }
}
