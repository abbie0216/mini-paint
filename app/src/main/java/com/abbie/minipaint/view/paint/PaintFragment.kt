package com.abbie.minipaint.view.paint

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.abbie.minipaint.R
import com.abbie.minipaint.view.base.BaseFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.richpath.RichPathView
import com.richpathanimator.AnimationBuilder
import com.richpathanimator.RichPathAnimator
import kotlinx.android.synthetic.main.fragment_paint.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class PaintFragment : BaseFragment(), CanvasListener {

    override fun getLayoutId() = R.layout.fragment_paint

    private lateinit var colors: List<String>

    private lateinit var currentIV: ImageView
    private lateinit var currentAnim: RichPathView

    private lateinit var animInk: AnimationBuilder
    private lateinit var animEraser: AnimationBuilder
    private lateinit var animUpload: AnimationBuilder
    private lateinit var animCover: AnimationBuilder

    private val cleanDialog = MaterialDialog(context!!)
        .title(null, getString(R.string.alert))
        .message(null, getString(R.string.dlg_msg_canvas_delete))
        .positiveButton(null, getString(R.string.delete)) {
            canvas.clean()
            inactiveBtnTemporary(iv_trashcan, anim_trashcan)
            changeCurrentBtnStatus(iv_ink, anim_ink)
            canvas.usePen()
        }
        .negativeButton(null, getString(R.string.cancel)) {
            inactiveBtnTemporary(iv_trashcan, anim_trashcan)
        }
        .cancelOnTouchOutside(false)

    private val saveDialog = MaterialDialog(context!!)
        .title(null, getString(R.string.alert))
        .message(null, getString(R.string.dlg_msg_save_canvas))
        .positiveButton(null, getString(R.string.yes)) {
            saveCanvas(canvas.getBitmap())
            inactiveBtnTemporary(iv_upload, anim_upload)
        }
        .negativeButton(null, getString(R.string.cancel)) {
            inactiveBtnTemporary(iv_upload, anim_upload)
        }
        .cancelOnTouchOutside(false)

    val backDialog = MaterialDialog(context!!)
        .title(null, getString(R.string.alert))
        .message(null, getString(R.string.dlg_msg_save_and_close_canvas))
        .positiveButton(null, getString(R.string.yes)) {
            saveCanvas(canvas.getBitmap())
            requireActivity().finish()
        }
        .negativeButton(null, getString(R.string.just_close)) {
            requireActivity().finish()
        }
        .cancelOnTouchOutside(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colors = resources.getStringArray(R.array.colors).toList()

        // setup color picker list
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rl_color_list.layoutManager = layoutManager
        rl_color_list.adapter = ColorPickerAdapter(context!!, colors, this)
        iv_color_selected.imageTintList = ColorStateList.valueOf(Color.parseColor(colors[0]))

        // setup canvas
        canvas.setListener(this)
        canvas.changePenColor(Color.parseColor(colors[0]))

        // setup btn and animation
        setupAnimation()
        animInk.start()

        currentIV = iv_ink
        currentAnim = anim_ink

        iv_ink.visibility = INVISIBLE
        anim_eraser.visibility = INVISIBLE
        anim_trashcan.visibility = INVISIBLE
        anim_upload.visibility = INVISIBLE

        iv_ink.setOnClickListener {
            animInk.start()
            changeCurrentBtnStatus(iv_ink, anim_ink)
            canvas.usePen()
        }

        iv_eraser.setOnClickListener {
            animEraser.start()
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

        iv_upload.setOnClickListener {
            animUpload.start()
            activeBtnTemporary(iv_upload, anim_upload)
            saveDialog.show()
        }

        iv_trashcan.setOnClickListener {
            animCover.start()
            activeBtnTemporary(iv_trashcan, anim_trashcan)
            cleanDialog.show()
        }

        // setup back pressed callback
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (iv_undo.isEnabled) backDialog.show()
                else requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setupAnimation() {
        val ink = anim_ink.findRichPathByName("ink")
        animInk = RichPathAnimator.animate(ink)
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

        val eraser = anim_eraser.findRichPathByName("eraser")
        val line = anim_eraser.findRichPathByName("line")
        animEraser = RichPathAnimator.animate(eraser)
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

        val upload = anim_upload.findRichPathByName("upload")
        animUpload = RichPathAnimator.animate(upload)
            .interpolator(LinearInterpolator())
            .translationY(0f, 5f, 0f, 5f)
            .duration(1000)

        val cover = anim_trashcan.findRichPathByName("cover")
        cover?.isPivotToCenter = true
        animCover = RichPathAnimator.animate(cover)
            .interpolator(DecelerateInterpolator())
            .rotation(-10f, 0f, 10f, 0f, -5f, 0f, 5f, 0f)
            .duration(1000)
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

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun saveCanvas(bitmap: Bitmap) {
        val simpleDateFormat = SimpleDateFormat("yyyyMMddhhmmss")
        val name = simpleDateFormat.format(Date())

        val fos: OutputStream?
        fos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver: ContentResolver = context!!.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES
            )
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            resolver.openOutputStream(imageUri!!)
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            val image = File(imagesDir, "$name.jpg")
            FileOutputStream(image)
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos?.close()
    }
}