package com.back.model

import java.time.Instant
import java.util.UUID

data class Memo(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "새 메모",
    val content: String = "",
    val filePath: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt,
)

enum class MemoSort {
    UpdatedDesc,
    CreatedDesc,
    TitleAsc,
}
