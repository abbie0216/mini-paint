package com.abbie.minipaint.model

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

data class PaintPath constructor(val path: Path, val paint: Paint) {
    enum class Type { PATH, POINT }

    fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }
}