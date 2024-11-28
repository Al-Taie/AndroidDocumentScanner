package com.scanner.library


sealed class DocumentState(open val distance: Float) {
    data class ComeCloser(override val distance: Float) : DocumentState(distance)
    data class Correct(override val distance: Float) : DocumentState(distance)
    data class GoFurther(override val distance: Float) : DocumentState(distance)
}
