package com.back.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.back.model.Memo
import com.back.model.MemoSort
import com.back.repository.MemoRepository
import java.time.Instant
import java.nio.file.Path

class MemoViewModel(
    private val repository: MemoRepository = MemoRepository(),
) {
    private val memos = mutableStateListOf<Memo>()

    var selectedMemoId by mutableStateOf<String?>(null)
        private set
    var searchQuery by mutableStateOf("")
        private set
    var favoritesOnly by mutableStateOf(false)
        private set
    var darkMode by mutableStateOf(false)
        private set
    var sort by mutableStateOf(MemoSort.UpdatedDesc)
        private set
    var contentFontSizeSp by mutableStateOf(DEFAULT_CONTENT_FONT_SIZE_SP)
        private set
    var contentFontFamily by mutableStateOf(DEFAULT_CONTENT_FONT_FAMILY)
        private set

    init {
        memos += repository.load()
        selectedMemoId = visibleMemos.firstOrNull()?.id
    }

    val selectedMemo: Memo?
        get() = memos.firstOrNull { it.id == selectedMemoId }

    val visibleMemos: List<Memo>
        get() {
            val normalizedQuery = searchQuery.trim().lowercase()
            return memos.asSequence()
                .filter { memo -> !favoritesOnly || memo.isFavorite }
                .filter { memo ->
                    normalizedQuery.isBlank() ||
                        memo.title.lowercase().contains(normalizedQuery) ||
                        memo.content.lowercase().contains(normalizedQuery)
                }
                .let { sequence ->
                    when (sort) {
                        MemoSort.UpdatedDesc -> sequence.sortedByDescending { it.updatedAt }
                        MemoSort.CreatedDesc -> sequence.sortedByDescending { it.createdAt }
                        MemoSort.TitleAsc -> sequence.sortedBy { it.title.lowercase() }
                    }
                }
                .toList()
        }

    fun createMemo() {
        val now = Instant.now()
        val memo = Memo(createdAt = now, updatedAt = now)
        memos.add(0, memo)
        selectedMemoId = memo.id
        persist()
    }

    fun selectMemo(id: String) {
        selectedMemoId = id
    }

    fun updateSelected(title: String? = null, content: String? = null) {
        val memo = selectedMemo ?: return
        replaceMemo(
            memo.copy(
                title = title ?: memo.title,
                content = content ?: memo.content,
                updatedAt = Instant.now(),
            ),
        )
    }

    fun toggleFavorite(id: String? = selectedMemoId) {
        val memo = memos.firstOrNull { it.id == id } ?: return
        replaceMemo(memo.copy(isFavorite = !memo.isFavorite, updatedAt = Instant.now()))
    }

    fun deleteSelected() {
        val memo = selectedMemo ?: return
        memos.removeAll { it.id == memo.id }
        selectedMemoId = visibleMemos.firstOrNull()?.id
        persist()
    }

    fun saveNow() {
        val memo = selectedMemo
        val path = memo?.filePath
        if (memo != null && path != null) {
            repository.saveTextFile(Path.of(path), memo)
        }
        persist()
    }

    fun saveSelectedToTextFile(path: Path) {
        val memo = selectedMemo ?: return
        val target = path.withTxtExtension()
        repository.saveTextFile(target, memo)
        replaceMemo(
            memo.copy(
                title = target.fileName.toString().removeSuffix(".txt").ifBlank { memo.title },
                filePath = target.toAbsolutePath().toString(),
                updatedAt = Instant.now(),
            ),
        )
    }

    fun importTextFile(path: Path) {
        val imported = repository.loadTextFile(path)
        val existingIndex = memos.indexOfFirst { it.filePath == imported.filePath }
        if (existingIndex >= 0) {
            memos[existingIndex] = imported.copy(
                id = memos[existingIndex].id,
                isFavorite = memos[existingIndex].isFavorite,
            )
            selectedMemoId = memos[existingIndex].id
        } else {
            memos.add(0, imported)
            selectedMemoId = imported.id
        }
        persist()
    }

    fun insertMarkdown(prefix: String, suffix: String = prefix, placeholder: String = "텍스트") {
        val memo = selectedMemo ?: return
        val separator = if (memo.content.isBlank() || memo.content.endsWith("\n")) "" else "\n"
        updateSelected(content = memo.content + separator + prefix + placeholder + suffix)
    }

    fun insertEmoji(emoji: String) {
        val memo = selectedMemo ?: return
        updateSelected(content = memo.content + emoji)
    }

    fun updateSearchQuery(value: String) {
        searchQuery = value
        keepSelectionVisible()
    }

    fun updateFavoritesOnly(value: Boolean) {
        favoritesOnly = value
        keepSelectionVisible()
    }

    fun toggleDarkMode() {
        darkMode = !darkMode
    }

    fun updateSort(value: MemoSort) {
        sort = value
    }

    fun increaseContentFontSize() {
        contentFontSizeSp = (contentFontSizeSp + CONTENT_FONT_SIZE_STEP_SP).coerceAtMost(MAX_CONTENT_FONT_SIZE_SP)
    }

    fun decreaseContentFontSize() {
        contentFontSizeSp = (contentFontSizeSp - CONTENT_FONT_SIZE_STEP_SP).coerceAtLeast(MIN_CONTENT_FONT_SIZE_SP)
    }

    fun resetContentFontSize() {
        contentFontSizeSp = DEFAULT_CONTENT_FONT_SIZE_SP
    }

    fun updateContentFontFamily(value: String) {
        if (value in CONTENT_FONT_FAMILIES) {
            contentFontFamily = value
        }
    }

    private fun replaceMemo(updated: Memo) {
        val index = memos.indexOfFirst { it.id == updated.id }
        if (index == -1) return
        memos[index] = updated
        persist()
    }

    private fun keepSelectionVisible() {
        val visibleIds = visibleMemos.map { it.id }.toSet()
        if (selectedMemoId !in visibleIds) selectedMemoId = visibleMemos.firstOrNull()?.id
    }

    private fun persist() {
        repository.save(memos.toList())
    }

    private fun Path.withTxtExtension(): Path {
        val filename = fileName.toString()
        return if (filename.endsWith(".txt", ignoreCase = true)) this else resolveSibling("$filename.txt")
    }

    companion object {
        const val MIN_CONTENT_FONT_SIZE_SP = 12f
        const val DEFAULT_CONTENT_FONT_SIZE_SP = 15f
        const val MAX_CONTENT_FONT_SIZE_SP = 28f
        const val DEFAULT_CONTENT_FONT_FAMILY = "기본"
        val CONTENT_FONT_FAMILIES = listOf(DEFAULT_CONTENT_FONT_FAMILY, "고딕", "명조", "Apple 고딕", "Arial Unicode")
        private const val CONTENT_FONT_SIZE_STEP_SP = 1f
    }
}
