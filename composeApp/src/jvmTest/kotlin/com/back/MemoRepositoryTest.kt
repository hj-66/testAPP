package com.back

import com.back.model.Memo
import com.back.repository.JsonMemoCodec
import com.back.repository.MemoRepository
import com.back.viewmodel.MemoViewModel
import java.nio.file.Files
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MemoRepositoryTest {
    @Test
    fun jsonRoundTripKeepsMemoFields() {
        val memo = Memo(
            title = "회의 메모",
            content = "첫 줄\n둘째 줄 \"인용\"",
            filePath = "/tmp/meeting.txt",
            isFavorite = true,
        )

        val decoded = JsonMemoCodec.decode(JsonMemoCodec.encode(listOf(memo)))

        assertEquals(1, decoded.size)
        assertEquals(memo.title, decoded.first().title)
        assertEquals(memo.content, decoded.first().content)
        assertEquals(memo.filePath, decoded.first().filePath)
        assertTrue(decoded.first().isFavorite)
    }

    @Test
    fun repositoryPersistsMemosToInjectedPath() {
        val path = Files.createTempDirectory("memo-app-test").resolve("memos.json")
        val repository = MemoRepository(path)
        val memo = Memo(title = "저장 테스트", content = "내용")

        repository.save(listOf(memo))

        assertEquals(listOf(memo), repository.load())
    }

    @Test
    fun repositoryReadsAndWritesTextFiles() {
        val path = Files.createTempDirectory("memo-app-text-test").resolve("note.txt")
        val repository = MemoRepository(path.resolveSibling("memos.json"))
        val memo = Memo(title = "note", content = "파일 내용")

        repository.saveTextFile(path, memo)
        val imported = repository.loadTextFile(path)

        assertEquals("파일 내용", path.readText())
        assertEquals("note", imported.title)
        assertEquals("파일 내용", imported.content)
        assertEquals(path.toAbsolutePath().toString(), imported.filePath)
    }

    @Test
    fun viewModelFiltersBySearchAndFavoriteThenDeletesImmediately() {
        val path = Files.createTempDirectory("memo-app-viewmodel-test").resolve("memos.json")
        val repository = MemoRepository(path)
        repository.save(
            listOf(
                Memo(title = "Kotlin", content = "Compose Desktop", isFavorite = true),
                Memo(title = "장보기", content = "우유"),
            ),
        )

        val viewModel = MemoViewModel(repository)
        viewModel.updateSearchQuery("compose")
        assertEquals("Kotlin", viewModel.visibleMemos.single().title)

        viewModel.updateSearchQuery("")
        viewModel.updateFavoritesOnly(true)
        assertEquals("Kotlin", viewModel.visibleMemos.single().title)

        viewModel.updateFavoritesOnly(false)
        viewModel.updateSearchQuery("우유")
        assertEquals("장보기", viewModel.visibleMemos.single().title)

        val selected = viewModel.selectedMemo
        assertNotNull(selected)
        viewModel.deleteSelected()
        assertTrue(viewModel.visibleMemos.none { it.title == "장보기" })
    }

    @Test
    fun viewModelImportsAndSavesSelectedTextFile() {
        val directory = Files.createTempDirectory("memo-app-save-text-test")
        val source = directory.resolve("source.txt")
        val target = directory.resolve("target")
        Files.writeString(source, "불러온 내용")
        val repository = MemoRepository(directory.resolve("memos.json"))

        val viewModel = MemoViewModel(repository)
        viewModel.importTextFile(source)
        assertEquals("source", viewModel.selectedMemo?.title)
        assertEquals("불러온 내용", viewModel.selectedMemo?.content)

        viewModel.updateSelected(content = "수정한 내용")
        viewModel.saveSelectedToTextFile(target)

        assertEquals("수정한 내용", target.resolveSibling("target.txt").readText())
        assertEquals("target", viewModel.selectedMemo?.title)
    }

    @Test
    fun viewModelInsertsMarkdownFormattingSnippets() {
        val path = Files.createTempDirectory("memo-app-markdown-test").resolve("memos.json")
        val viewModel = MemoViewModel(MemoRepository(path))

        viewModel.createMemo()
        viewModel.updateSelected(content = "기존 내용")
        viewModel.insertMarkdown("**", "**", "굵게")
        viewModel.insertMarkdown("<u>", "</u>", "밑줄")
        viewModel.insertMarkdown("<span style=\"color:red\">", "</span>", "빨간 글씨")

        assertEquals(
            "기존 내용\n**굵게**\n<u>밑줄</u>\n<span style=\"color:red\">빨간 글씨</span>",
            viewModel.selectedMemo?.content,
        )
    }

    @Test
    fun viewModelAdjustsContentFontSizeAndInsertsEmoji() {
        val path = Files.createTempDirectory("memo-app-editor-tools-test").resolve("memos.json")
        val viewModel = MemoViewModel(MemoRepository(path))

        viewModel.createMemo()
        viewModel.updateSelected(content = "기분")
        viewModel.insertEmoji("😊")
        viewModel.increaseContentFontSize()
        viewModel.increaseContentFontSize()

        assertEquals("기분😊", viewModel.selectedMemo?.content)
        assertEquals(17f, viewModel.contentFontSizeSp)

        repeat(20) { viewModel.decreaseContentFontSize() }
        assertEquals(MemoViewModel.MIN_CONTENT_FONT_SIZE_SP, viewModel.contentFontSizeSp)

        repeat(40) { viewModel.increaseContentFontSize() }
        assertEquals(MemoViewModel.MAX_CONTENT_FONT_SIZE_SP, viewModel.contentFontSizeSp)

        viewModel.resetContentFontSize()
        assertEquals(MemoViewModel.DEFAULT_CONTENT_FONT_SIZE_SP, viewModel.contentFontSizeSp)
    }
}
