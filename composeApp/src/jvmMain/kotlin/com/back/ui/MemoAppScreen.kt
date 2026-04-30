package com.back.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.back.model.Memo
import com.back.model.MemoSort
import com.back.viewmodel.MemoViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MemoAppScreen(viewModel: MemoViewModel) {
    val searchFocusRequester = remember { FocusRequester() }
    val colors = if (viewModel.darkMode) memoDarkColorScheme() else lightColorScheme()

    fun saveWithDialogIfNeeded() {
        val memo = viewModel.selectedMemo ?: return
        val existingPath = memo.filePath
        if (existingPath == null) {
            chooseSavePath()?.let(viewModel::saveSelectedToTextFile)
        } else {
            viewModel.saveNow()
        }
    }

    MaterialTheme(colorScheme = colors) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown || !event.isMetaPressed) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.N -> {
                            viewModel.createMemo()
                            true
                        }
                        Key.S -> {
                            saveWithDialogIfNeeded()
                            true
                        }
                        Key.F -> {
                            searchFocusRequester.requestFocus()
                            true
                        }
                        Key.Backspace, Key.Delete -> {
                            viewModel.deleteSelected()
                            true
                        }
                        else -> false
                    }
                },
        ) {
            MemoListPanel(
                viewModel = viewModel,
                searchFocusRequester = searchFocusRequester,
                onOpenTextFile = { chooseOpenPath()?.let(viewModel::importTextFile) },
                onSaveTextFile = ::saveWithDialogIfNeeded,
            )
            MemoEditorPanel(
                viewModel = viewModel,
                onSaveTextFile = ::saveWithDialogIfNeeded,
                onSaveAsTextFile = { chooseSavePath()?.let(viewModel::saveSelectedToTextFile) },
            )
        }
    }
}

@Composable
private fun MemoListPanel(
    viewModel: MemoViewModel,
    searchFocusRequester: FocusRequester,
    onOpenTextFile: () -> Unit,
    onSaveTextFile: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(340.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "메모",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = viewModel::toggleDarkMode) {
                Text(if (viewModel.darkMode) "Light" else "Dark")
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onOpenTextFile, modifier = Modifier.weight(1f)) {
                Text("불러오기")
            }
            Button(onClick = onSaveTextFile, modifier = Modifier.weight(1f), enabled = viewModel.selectedMemo != null) {
                Text("저장")
            }
        }

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            label = { Text("검색") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(searchFocusRequester),
        )

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChipButton(
                label = "전체",
                selected = !viewModel.favoritesOnly,
                onClick = {
                    viewModel.updateFavoritesOnly(false)
                },
            )
            FilterChipButton(
                label = "즐겨찾기",
                selected = viewModel.favoritesOnly,
                onClick = { viewModel.updateFavoritesOnly(!viewModel.favoritesOnly) },
            )
        }

        Spacer(Modifier.height(8.dp))
        SortMenu(viewModel)
        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(viewModel.visibleMemos, key = { it.id }) { memo ->
                MemoListItem(
                    memo = memo,
                    selected = memo.id == viewModel.selectedMemoId,
                    onSelect = { viewModel.selectMemo(memo.id) },
                    onFavorite = { viewModel.toggleFavorite(memo.id) },
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = viewModel::createMemo,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("+ 새 메모")
        }
    }
}

@Composable
private fun SortMenu(viewModel: MemoViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                when (viewModel.sort) {
                    MemoSort.UpdatedDesc -> "최근 수정순"
                    MemoSort.CreatedDesc -> "최근 생성순"
                    MemoSort.TitleAsc -> "제목순"
                },
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortMenuItem("최근 수정순", MemoSort.UpdatedDesc, viewModel) { expanded = false }
            SortMenuItem("최근 생성순", MemoSort.CreatedDesc, viewModel) { expanded = false }
            SortMenuItem("제목순", MemoSort.TitleAsc, viewModel) { expanded = false }
        }
    }
}

@Composable
private fun SortMenuItem(label: String, sort: MemoSort, viewModel: MemoViewModel, close: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = {
            viewModel.updateSort(sort)
            close()
        },
    )
}

@Composable
private fun MemoListItem(
    memo: Memo,
    selected: Boolean,
    onSelect: () -> Unit,
    onFavorite: () -> Unit,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = memo.title.ifBlank { "제목 없음" },
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = memo.content.ifBlank { "내용 없음" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = memo.filePath?.let { Path.of(it).fileName.toString() } ?: memo.updatedAt.formatShort(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        TextButton(onClick = onFavorite, modifier = Modifier.size(width = 44.dp, height = 36.dp)) {
            Text(if (memo.isFavorite) "★" else "☆")
        }
    }
}

private fun memoDarkColorScheme() = darkColorScheme(
    primary = Color(0xFF4B5563),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF374151),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF6B7280),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4B5563),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF64748B),
    onTertiary = Color.White,
    background = Color(0xFF111827),
    onBackground = Color.White,
    surface = Color(0xFF1F2937),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF374151),
    onSurfaceVariant = Color(0xFFE5E7EB),
    outline = Color(0xFF9CA3AF),
    error = Color(0xFFFCA5A5),
    onError = Color.White,
)

@Composable
private fun MemoEditorPanel(
    viewModel: MemoViewModel,
    onSaveTextFile: () -> Unit,
    onSaveAsTextFile: () -> Unit,
) {
    val memo = viewModel.selectedMemo
    var markdownPreview by remember { mutableStateOf(false) }
    val contentFontFamily = viewModel.contentFontFamily.toComposeFontFamily()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
    ) {
        if (memo == null) {
            EmptyEditor(onCreate = viewModel::createMemo)
            return@Column
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "편집",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (memo.filePath == null) "저장 위치 없음" else memo.filePath,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = memo.title,
            onValueChange = { viewModel.updateSelected(title = it) },
            label = { Text("제목") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = memo.isFavorite,
                onCheckedChange = { viewModel.toggleFavorite() },
            )
            Text("즐겨찾기", color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            Text(
                text = "수정됨 · ${memo.updatedAt.formatShort()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }

        Spacer(Modifier.height(10.dp))

        TextEditToolbar(
            markdownPreview = markdownPreview,
            onToggleMarkdown = { markdownPreview = !markdownPreview },
            onBold = { viewModel.insertMarkdown("**", "**", "굵게") },
            onItalic = { viewModel.insertMarkdown("*", "*", "기울임") },
            onUnderline = { viewModel.insertMarkdown("<u>", "</u>", "밑줄") },
            onRed = { viewModel.insertMarkdown("<span style=\"color:red\">", "</span>", "빨간 글씨") },
            onBlue = { viewModel.insertMarkdown("<span style=\"color:blue\">", "</span>", "파란 글씨") },
            fontSizeSp = viewModel.contentFontSizeSp,
            onDecreaseFontSize = viewModel::decreaseContentFontSize,
            onIncreaseFontSize = viewModel::increaseContentFontSize,
            onResetFontSize = viewModel::resetContentFontSize,
            onEmoji = viewModel::insertEmoji,
            fontFamily = viewModel.contentFontFamily,
            fontFamilies = MemoViewModel.CONTENT_FONT_FAMILIES,
            onFontFamily = viewModel::updateContentFontFamily,
        )

        Spacer(Modifier.height(8.dp))

        if (markdownPreview) {
            MarkdownPreview(
                content = memo.content,
                fontSizeSp = viewModel.contentFontSizeSp,
                fontFamily = contentFontFamily,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        } else {
            key(viewModel.contentFontFamily) {
                OutlinedTextField(
                    value = memo.content,
                    onValueChange = { viewModel.updateSelected(content = it) },
                    label = { Text("내용") },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = viewModel.contentFontSizeSp.sp,
                        fontFamily = contentFontFamily,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onSaveTextFile) {
                Text("저장")
            }
            OutlinedButton(onClick = onSaveAsTextFile) {
                Text("다른 이름으로 저장")
            }
            OutlinedButton(
                onClick = viewModel::deleteSelected,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text("삭제")
            }
        }
    }
}

@Composable
private fun TextEditToolbar(
    markdownPreview: Boolean,
    onToggleMarkdown: () -> Unit,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onRed: () -> Unit,
    onBlue: () -> Unit,
    fontSizeSp: Float,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onResetFontSize: () -> Unit,
    onEmoji: (String) -> Unit,
    fontFamily: String,
    fontFamilies: List<String>,
    onFontFamily: (String) -> Unit,
) {
    var emojiExpanded by remember { mutableStateOf(false) }
    var fontExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = onBold, modifier = Modifier.size(width = 46.dp, height = 38.dp)) {
            Text("B", fontWeight = FontWeight.Bold)
        }
        OutlinedButton(onClick = onItalic, modifier = Modifier.size(width = 46.dp, height = 38.dp)) {
            Text("I", fontStyle = FontStyle.Italic)
        }
        OutlinedButton(onClick = onUnderline, modifier = Modifier.size(width = 46.dp, height = 38.dp)) {
            Text("U", textDecoration = TextDecoration.Underline)
        }
        OutlinedButton(onClick = onRed, modifier = Modifier.size(width = 46.dp, height = 38.dp)) {
            Text("A", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
        }
        OutlinedButton(onClick = onBlue, modifier = Modifier.size(width = 46.dp, height = 38.dp)) {
            Text("A", color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
        }
        OutlinedButton(onClick = onDecreaseFontSize, modifier = Modifier.size(width = 46.dp, height = 38.dp)) {
            Text("A-", maxLines = 1)
        }
        OutlinedButton(onClick = onResetFontSize, modifier = Modifier.size(width = 58.dp, height = 38.dp)) {
            Text("${fontSizeSp.toInt()}sp", maxLines = 1, fontSize = 12.sp)
        }
        OutlinedButton(onClick = onIncreaseFontSize, modifier = Modifier.size(width = 46.dp, height = 38.dp)) {
            Text("A+", maxLines = 1)
        }
        Box {
            OutlinedButton(onClick = { fontExpanded = true }, modifier = Modifier.size(width = 86.dp, height = 38.dp)) {
                Text(fontFamily, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
            }
            DropdownMenu(expanded = fontExpanded, onDismissRequest = { fontExpanded = false }) {
                fontFamilies.forEach { family ->
                    DropdownMenuItem(
                        text = { Text(family, fontFamily = family.toComposeFontFamily()) },
                        onClick = {
                            onFontFamily(family)
                            fontExpanded = false
                        },
                    )
                }
            }
        }
        Box {
            OutlinedButton(onClick = { emojiExpanded = true }, modifier = Modifier.size(width = 46.dp, height = 38.dp)) {
                Text("😊")
            }
            DropdownMenu(expanded = emojiExpanded, onDismissRequest = { emojiExpanded = false }) {
                listOf("😊", "👍", "⭐", "✅", "🔥", "🎉", "💡", "📌").forEach { emoji ->
                    DropdownMenuItem(
                        text = { Text(emoji, fontSize = 20.sp) },
                        onClick = {
                            onEmoji(emoji)
                            emojiExpanded = false
                        },
                    )
                }
            }
        }
        Button(onClick = onToggleMarkdown) {
            Text(if (markdownPreview) "Markdown 비적용" else "Markdown 적용")
        }
    }
}

@Composable
private fun MarkdownPreview(content: String, fontSizeSp: Float, fontFamily: FontFamily, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(content.ifBlank { "내용 없음" }.lines()) { line ->
            val trimmed = line.trimStart()
            val (displayLine, style) = when {
                trimmed.startsWith("# ") -> trimmed.removePrefix("# ") to TextStyle(
                    fontSize = (fontSizeSp + 9f).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = fontFamily,
                )
                trimmed.startsWith("## ") -> trimmed.removePrefix("## ") to TextStyle(
                    fontSize = (fontSizeSp + 5f).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = fontFamily,
                )
                trimmed.startsWith("- ") -> "• ${trimmed.removePrefix("- ")}" to TextStyle(
                    fontSize = fontSizeSp.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = fontFamily,
                )
                else -> line to TextStyle(
                    fontSize = fontSizeSp.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = fontFamily,
                )
            }
            Text(text = renderInlineMarkdown(displayLine), style = style)
        }
    }
}

private fun renderInlineMarkdown(line: String) = buildAnnotatedString {
    var index = 0
    while (index < line.length) {
        when {
            line.startsWith("**", index) -> {
                val end = line.indexOf("**", index + 2)
                if (end == -1) append(line[index++]) else {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(line.substring(index + 2, end))
                    }
                    index = end + 2
                }
            }
            line.startsWith("*", index) -> {
                val end = line.indexOf("*", index + 1)
                if (end == -1) append(line[index++]) else {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(line.substring(index + 1, end))
                    }
                    index = end + 1
                }
            }
            line.startsWith("<u>", index) -> {
                val end = line.indexOf("</u>", index + 3)
                if (end == -1) append(line[index++]) else {
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(line.substring(index + 3, end))
                    }
                    index = end + 4
                }
            }
            line.startsWith("<span style=\"color:red\">", index) -> {
                val end = line.indexOf("</span>", index)
                if (end == -1) append(line[index++]) else {
                    withStyle(SpanStyle(color = Color(0xFFC62828))) {
                        append(line.substring(index + 24, end))
                    }
                    index = end + 7
                }
            }
            line.startsWith("<span style=\"color:blue\">", index) -> {
                val end = line.indexOf("</span>", index)
                if (end == -1) append(line[index++]) else {
                    withStyle(SpanStyle(color = Color(0xFF1565C0))) {
                        append(line.substring(index + 25, end))
                    }
                    index = end + 7
                }
            }
            else -> append(line[index++])
        }
    }
}

@Composable
private fun EmptyEditor(onCreate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("선택된 메모가 없습니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onCreate) {
            Text("+ 새 메모")
        }
    }
}

@Composable
private fun FilterChipButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    OutlinedButton(
        onClick = onClick,
        colors = colors,
        contentPadding = ButtonDefaults.TextButtonContentPadding,
    ) {
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
    }
}

private fun chooseOpenPath(): Path? {
    val dialog = FileDialog(null as Frame?, "텍스트 파일 불러오기", FileDialog.LOAD)
    dialog.filenameFilter = FilenameFilter { _, name -> name.endsWith(".txt", ignoreCase = true) }
    dialog.isVisible = true
    val file = dialog.file ?: return null
    val directory = dialog.directory ?: return null
    return Path.of(directory, file)
}

private fun chooseSavePath(): Path? {
    val dialog = FileDialog(null as Frame?, "텍스트 파일 저장", FileDialog.SAVE)
    dialog.file = "memo.txt"
    dialog.isVisible = true
    val file = dialog.file ?: return null
    val directory = dialog.directory ?: return null
    return Path.of(directory, file)
}

private fun Instant.formatShort(): String {
    val formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm").withZone(ZoneId.systemDefault())
    return formatter.format(this)
}

@OptIn(ExperimentalTextApi::class)
private fun String.toComposeFontFamily(): FontFamily = when (this) {
    "고딕" -> FontFamily("Apple SD Gothic Neo")
    "명조" -> FontFamily("AppleMyungjo")
    "Apple 고딕" -> FontFamily("AppleGothic")
    "Arial Unicode" -> FontFamily("Arial Unicode MS")
    else -> FontFamily("Apple SD Gothic Neo")
}
