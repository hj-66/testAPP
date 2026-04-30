package com.back.repository

import com.back.model.Memo
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText

class MemoRepository(
    private val storagePath: Path = defaultStoragePath(),
) {
    fun load(): List<Memo> {
        if (!storagePath.exists()) return emptyList()
        return runCatching { JsonMemoCodec.decode(storagePath.readText()) }.getOrDefault(emptyList())
    }

    fun save(memos: List<Memo>) {
        storagePath.parent?.createDirectories()
        storagePath.writeText(JsonMemoCodec.encode(memos))
    }

    fun loadTextFile(path: Path): Memo {
        val content = path.readText()
        val now = java.time.Instant.now()
        return Memo(
            title = path.fileName.toString().removeSuffix(".txt").ifBlank { "제목 없음" },
            content = content,
            filePath = path.toAbsolutePath().toString(),
            createdAt = now,
            updatedAt = now,
        )
    }

    fun saveTextFile(path: Path, memo: Memo) {
        path.parent?.createDirectories()
        path.writeBytes(memo.content.toByteArray(StandardCharsets.UTF_8))
    }

    companion object {
        fun defaultStoragePath(): Path {
            val home = System.getProperty("user.home")
            return Path.of(home, ".memoapp", "memos.json")
        }
    }
}
