package com.example.expandtextview

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager


fun dp2px(context: Context,dp:Int)=(context.resources.displayMetrics.density*dp+0.5).toInt()


/**
 * 获取宽度
 *
 * @param mContext 上下文
 * @return 宽度值，px
 */
fun getWidth(mContext: Context): Int {
    val displayMetrics = DisplayMetrics()
    (mContext.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        .defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}