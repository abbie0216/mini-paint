package com.abbie.minipaint.view.paint

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.abbie.minipaint.R
import com.abbie.minipaint.view.base.BaseFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.richpath.RichPathView
import com.richpathanimator.RichPathAnimator
import kotlinx.android.synthetic.main.fragment_paint.*

class PaintFragment : BaseFragment(), CanvasListener {

    override fun getLayoutId() = R.layout.fragment_paint

    private lateinit var colors: List<String>

    private lateinit var currentIV: ImageView
    private lateinit var currentAnim: RichPathView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colors = resources.getStringArray(R.array.colors).toList()

        // setup color picker list
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rl_color_list.layoutManager = layoutManager
        rl_color_list.adapter = ColorPickerAdapter(context!!, colors, this)

        // setup initial draw color
        canvas.setListener(this)
        canvas.changePenColor(Color.parseColor(colors[0]))
        iv_color_selected.imageTintList = ColorStateList.valueOf(Color.parseColor(colors[0]))

        // setup btn and animation
        setupAnimation()

        currentIV = iv_ink
        currentAnim = anim_ink

        iv_ink.visibility = INVISIBLE
        anim_eraser.visibility = INVISIBLE
        anim_trashcan.visibility = INVISIBLE

        val cleanConfirmDialog = MaterialDialog(context!!)
            .title(null, "Alert")
            .message(null, "!! The canvas will be deleted !!")
            .positiveButton(null, "Delete") {
                canvas.clean()
                inactiveBtnTemporary(iv_trashcan, anim_trashcan)
                changeCurrentBtnStatus(iv_ink, anim_ink)
                canvas.usePen()
                it.dismiss()
            }
            .negativeButton(null, "Cancel") {
                inactiveBtnTemporary(iv_trashcan, anim_trashcan)
                it.dismiss()
            }


        iv_ink.setOnClickListener {
            changeCurrentBtnStatus(iv_ink, anim_ink)
            canvas.usePen()
        }

        iv_eraser.setOnClickListener {
            changeCurrentBtnStatus(iv_eraser, anim_eraser)
            canvas.useEraser()
        }

        iv_undo.setOnClickListener {
            canvas.undo()
        }
        iv_undo.isEnabled = false

        iv_redo.setOnClickListener {
            canvas.redo()
        }
        iv_redo.isEnabled = false

        iv_trashcan.setOnClickListener {
            activeBtnTemporary(iv_trashcan, anim_trashcan)
            cleanConfirmDialog.show()
        }

    }

    private fun setupAnimation() {
        val ink = anim_ink.findRichPathByName("ink")
        RichPathAnimator.animate(ink)
            .startDelay(1000)
            .interpolator(LinearInterpolator())
            .fillColor(Color.TRANSPARENT, Color.BLACK)
            .duration(1500)
            .repeatMode(RichPathAnimator.RESTART)
            .repeatCount(RichPathAnimator.INFINITE)
            .andAnimate(ink)
            .interpolator(DecelerateInterpolator())
            .translationY(0f, 50f)
            .duration(1500)
            .repeatMode(RichPathAnimator.RESTART)
            .repeatCount(RichPathAnimator.INFINITE)
            .start()

        val eraser = anim_eraser.findRichPathByName("eraser")
        val line = anim_eraser.findRichPathByName("line")
        RichPathAnimator.animate(eraser)
            .interpolator(DecelerateInterpolator())
            .translationX(0f, -10f, 0f)
            .duration(1000)
            .repeatMode(RichPathAnimator.REVERSE)
            .repeatCount(RichPathAnimator.INFINITE)
            .andAnimate(line)
            .strokeAlpha(1f, 0f)
            .interpolator(DecelerateInterpolator())
            .duration(1000)
            .repeatMode(RichPathAnimator.REVERSE)
            .repeatCount(RichPathAnimator.INFINITE)
            .start()

        val cover = anim_trashcan.findRichPathByName("cover")
        cover?.isPivotToCenter = true
        RichPathAnimator.animate(cover)
            .interpolator(DecelerateInterpolator())
            .rotation(-10f, 0f, 10f, -5f, 5f)
            .duration(1000)
            .repeatMode(RichPathAnimator.REVERSE)
            .repeatCount(RichPathAnimator.INFINITE)
            .start()
    }

    private fun changeCurrentBtnStatus(btnIv: ImageView, btnAnim: RichPathView) {
        currentIV.visibility = VISIBLE
        currentAnim.visibility = INVISIBLE
        currentIV = btnIv
        currentAnim = btnAnim
        currentIV.visibility = INVISIBLE
        currentAnim.visibility = VISIBLE
    }

    private fun activeBtnTemporary(btnIv: ImageView, btnAnim: RichPathView) {
        currentIV.visibility = VISIBLE
        currentAnim.visibility = INVISIBLE
        btnIv.visibility = INVISIBLE
        btnAnim.visibility = VISIBLE
    }

    private fun inactiveBtnTemporary(btnIv: ImageView, btnAnim: RichPathView) {
        currentIV.visibility = INVISIBLE
        currentAnim.visibility = VISIBLE
        btnIv.visibility = VISIBLE
        btnAnim.visibility = INVISIBLE
    }

    override fun onPenColorChanged(color: Int) {
        changeCurrentBtnStatus(iv_ink, anim_ink)
        canvas.changePenColor(color)
        iv_color_selected.imageTintList = ColorStateList.valueOf(color)
    }

    override fun onListPaintPathChanged(undoCount: Int, redoCount: Int) {
        iv_undo.isEnabled = undoCount > 0
        iv_redo.isEnabled = redoCount > 0
    }
}