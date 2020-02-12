package com.abbie.minipaint.view.paint

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.abbie.minipaint.R

class ColorPickerAdapter(private val context: Context, private var colors: List<String>, private val listener: CanvasListener) :
    RecyclerView.Adapter<ColorPickerAdapter.ColorPickerVH>() {

    private var currentPosition = 0

    class ColorPickerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivColor: ImageView = itemView.findViewById(R.id.iv_color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorPickerVH {
        val view = LayoutInflater.from(context).inflate(R.layout.item_color_picker, parent, false)
        return ColorPickerVH(view)
    }

    override fun getItemCount(): Int = colors.size

    override fun onBindViewHolder(holder: ColorPickerVH, position: Int) {
        holder.ivColor.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colors[position]))
        holder.ivColor.setOnClickListener{
            listener.onPenColorChanged(Color.parseColor(colors[position]))
            notifyItemChanged(position)
            notifyItemChanged(currentPosition)
            currentPosition = position
        }
        if (position == currentPosition) {
            holder.ivColor.setImageResource(R.drawable.paint_checked)
        } else {
            holder.ivColor.setImageDrawable(null)
        }
    }
}