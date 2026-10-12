package com.lyf.androidmvp.core.common

import java.util.UUID

data class UiMessage(val text: String, val id: String = UUID.randomUUID().toString())
