package com.abbie.minipaint.view.paint

interface CanvasListener {
    fun onPenColorChanged(color: Int)
    fun onListPaintPathChanged(size:Int)
}