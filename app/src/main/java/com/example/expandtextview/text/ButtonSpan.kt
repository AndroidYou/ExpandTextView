package com.example.expandtextview.text;

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

class ButtonSpan @JvmOverloads constructor(
    private val context: Context,
    var onClickListener: View.OnClickListener?,
    private val colorId: Int = Color.BLUE
) :
    ClickableSpan() {
    override fun updateDrawState(ds: TextPaint) {
        ds.color = colorId
        ds.textSize = dip2px(15f).toFloat()
        ds.isUnderlineText = false
    }


    override fun onClick(widget: View) {
        if (onClickListener != null) {
            onClickListener!!.onClick(widget)
        }
    }

    companion object {
        fun dip2px(dipValue: Float): Int {
            val scale = Resources.getSystem().displayMetrics.density
            return (dipValue * scale + 0.5f).toInt()
        }
    }
}