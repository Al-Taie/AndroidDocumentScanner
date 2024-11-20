package com.scanner.library.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


@Composable
fun <T> rememberMutableStateOf(value: T, key: Any? = null): MutableState<T> =
    remember(key) { mutableStateOf(value) }

@Composable
fun <T> rememberDerivedStateOf(calculation: () -> T): State<T> =
    remember { derivedStateOf(calculation) }
