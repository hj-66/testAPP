package com.back

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.back.ui.MemoAppScreen
import com.back.viewmodel.MemoViewModel

@Composable
@Preview
fun App(viewModel: MemoViewModel = remember { MemoViewModel() }) {
    MemoAppScreen(viewModel)
}
