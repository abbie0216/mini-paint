package com.abbie.minipaint.view.paint

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.abbie.minipaint.R
import com.abbie.minipaint.model.PaintPath
import timber.log.Timber

class CanvasView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    companion object {
        const val MAX_SAVED_STEP = 10
    }

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    private lateinit var holdCanvas: Canvas
    private lateinit var holdBitmap: Bitmap

    private lateinit var listener: CanvasListener

    private val bgColor = ResourcesCompat.getColor(resources, R.color.white_1, null)
    private var currentPenColor = ResourcesCompat.getColor(resources, R.color.white_1, null)

    private var paint = Paint().apply {
        isAntiAlias = true //設置抗鋸齒
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND //設置拐角樣式
        strokeCap = Paint.Cap.ROUND //設置線段末端線帽樣式
    }

    private var path = Path()

    private var motionEventX = 0f
    private var motionEventY = 0f

    private var currentX = 0f
    private var currentY = 0f

    enum class Tools { PEN, ERASER, CLEAN }

    private var currentTool = Tools.PEN

    private var listUndo = ArrayList<PaintPath>()
    private var listRedo = ArrayList<PaintPath>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Timber.d("onSizeChanged... $w x $h")
        super.onSizeChanged(w, h, oldw, oldh)

        if (::extraBitmap.isInitialized) extraBitmap.recycle()

        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(bgColor)

        if (::holdBitmap.isInitialized) holdBitmap.recycle()

        holdBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        holdCanvas = Canvas(holdBitmap)
        holdCanvas.drawColor(bgColor)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionEventX = event.x
        motionEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_UP -> touchEnd()
            MotionEvent.ACTION_MOVE -> touchMove()
        }
        return true
    }

    private fun touchEnd() {
        path.lineTo(currentX, currentY)
        if (motionEventX == currentX && motionEventY == currentY) {
            path.lineTo(currentX, currentY + 2)
            path.lineTo(currentX + 1, currentY + 2)
            path.lineTo(currentX + 1, currentY)
        }
        extraCanvas.drawPath(path, paint)

        if (currentTool != Tools.ERASER || listUndo.size > 0) {
            listUndo.add(PaintPath(Path(path), Paint(paint)))
            listRedo.clear()
            if (listUndo.size > MAX_SAVED_STEP) {
                val paintPath = listUndo.removeAt(0)
                holdCanvas.drawPath(paintPath.path,paintPath.paint)
            }
        }

        listener.onListPaintPathChanged(listUndo.size, listRedo.size)
        invalidate()
    }

    private fun touchMove() {
        path.quadTo(
            currentX,
            currentY,
            (motionEventX + currentX) / 2,
            (motionEventY + currentY) / 2
        )
        currentX = motionEventX
        currentY = motionEventY
        extraCanvas.drawPath(path, paint)
        invalidate()
    }

    private fun touchStart() {
        path.reset()
        path.moveTo(motionEventX, motionEventY)
        currentX = motionEventX
        currentY = motionEventY
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(extraBitmap, 0f, 0f, null)
    }

    fun changePenColor(color: Int) {
        currentTool = Tools.PEN
        currentPenColor = color
        paint.color = color
        paint.strokeWidth = 10f
    }

    fun usePen() {
        currentTool = Tools.PEN
        paint.color = currentPenColor
        paint.strokeWidth = 10f
    }

    fun useEraser() {
        currentTool = Tools.ERASER
        paint.color = Color.WHITE
        paint.strokeWidth = 40f
    }

    fun clean() {
        currentTool = Tools.CLEAN
        listUndo.clear()
        listRedo.clear()
        listener.onListPaintPathChanged(listUndo.size, listRedo.size)
        extraCanvas.drawColor(bgColor)
        holdCanvas.drawColor(bgColor)
        invalidate()
    }

    fun undo() {
        val paintPath = listUndo.removeAt(listUndo.lastIndex)
        listRedo.add(paintPath)
        listener.onListPaintPathChanged(listUndo.size, listRedo.size)

        extraCanvas.drawBitmap(holdBitmap,0f,0f,null)
        listUndo.forEach {
            extraCanvas.drawPath(it.path, it.paint)
        }
        invalidate()
    }

    fun redo(){
        val paintPath = listRedo.removeAt(listRedo.lastIndex)
        listUndo.add(paintPath)
        listener.onListPaintPathChanged(listUndo.size, listRedo.size)

        extraCanvas.drawBitmap(holdBitmap,0f,0f,null)
        listUndo.forEach {
            extraCanvas.drawPath(it.path, it.paint)
        }
        invalidate()
    }

    fun setListener(listener: CanvasListener) {
        this.listener = listener
    }

}