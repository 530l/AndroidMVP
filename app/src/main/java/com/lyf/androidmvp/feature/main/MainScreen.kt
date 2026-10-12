package com.lyf.androidmvp.feature.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lyf.androidmvp.feature.detail.DetailResult
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onNavigateToDetail: (Long, String, String) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    detailResult: DetailResult? = null,
    onDetailResultConsumed: (DetailResult) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    // 4 个实际内容 Tab（发布按钮作为 Modal 动作，不计入 Page）
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    var isPublishSheetVisible by rememberSaveable { mutableStateOf(false) }

    // Android 规范与小红书体验：在非首页 Tab 按返回键，自动退回首页 Tab
    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.scrollToPage(0)
        }
    }

    val currentTab = when (pagerState.currentPage) {
        0 -> MainTab.HOME
        1 -> MainTab.VIDEO
        2 -> MainTab.MESSAGE
        3 -> MainTab.PROFILE
        else -> MainTab.HOME
    }

    // 首次访问后保留页面；这会增加内存占用，不能保证零重组。
    var visitedTabs by rememberSaveable { mutableStateOf(setOf(0)) }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage !in visitedTabs) {
            visitedTabs = visitedTabs + pagerState.currentPage
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            XiaohongshuBottomBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    coroutineScope.launch {
                        pagerState.scrollToPage(tab.ordinal)
                    }
                },
                onPublishClick = {
                    isPublishSheetVisible = true
                }
            )
        }
    ) { innerPadding ->
        MainTabsPager(
            pagerState = pagerState,
            visitedTabs = visitedTabs,
            detailResult = detailResult,
            onDetailResultConsumed = onDetailResultConsumed,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToSettings = onNavigateToSettings,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        )
    }

    // 小红书 "+" 发布操作抽屉
    if (isPublishSheetVisible) {
        PublishBottomSheet(
            onDismissRequest = { isPublishSheetVisible = false }
        )
    }
}
