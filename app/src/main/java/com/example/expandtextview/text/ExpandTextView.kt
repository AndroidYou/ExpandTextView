package com.example.expandtextview.text;

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.expandtextview.R
import com.example.expandtextview.dp2px
import com.example.expandtextview.getWidth
import java.util.*

/**
 * 自定义控件，长文本展开收起TextView
 */
@SuppressLint("AppCompatCustomView", "ResourceAsColor")
class ExpandTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet?=null, defStyleAttr: Int=0): TextView (context,attrs,defStyleAttr){
    private var TO_EXPAND_HINT_COLOR_BG_PRESSED = context.resources.getColor(R.color.color_0091ff) //展开颜色
    private var TO_SHRINK_HINT_COLOR_BG_PRESSED = context.resources.getColor(R.color.color_0091ff) //收起颜色
    private var originText // 原始内容文本
            : String? = null
    private var initWidth = 0 // TextView可展示宽度
    private var mMaxLines = 2 // TextView最大行数
    private var SPAN_CLOSE: SpannableString? = null // 收起的文案(颜色处理)
    private var SPAN_EXPAND: SpannableString? = null // 展开的文案(颜色处理)
    private var TEXT_EXPAND = "展开"
    private var TEXT_CLOSE = "收起"
    private var isExpend = true //true 默认支持展开后收缩 false 展开后 不能收缩
    private var atUserList: List<String>? = null

   // private var atUserList: List<AtUserModel>? = null

   init {
       val obtain =
           context.obtainStyledAttributes(attrs, R.styleable.ExpandTextView)
       //最多显示几行
       mMaxLines = obtain.getInt(R.styleable.ExpandTextView_maxLines,mMaxLines)
       TEXT_EXPAND = if (obtain.getString(R.styleable.ExpandTextView_expandHint)==null) TEXT_EXPAND else obtain.getString(R.styleable.ExpandTextView_expandHint).toString()
       TEXT_CLOSE = if (obtain.getString(R.styleable.ExpandTextView_shrinkHint)==null) TEXT_CLOSE else obtain.getString(R.styleable.ExpandTextView_shrinkHint).toString()
       TO_SHRINK_HINT_COLOR_BG_PRESSED = obtain.getColor(R.styleable.ExpandTextView_shrinkHintColor,context.resources.getColor(R.color.color_0091ff))
       TO_EXPAND_HINT_COLOR_BG_PRESSED = obtain.getColor(R.styleable.ExpandTextView_expandHintColor,context.resources.getColor(R.color.color_0091ff))
       isExpend = obtain.getBoolean(R.styleable.ExpandTextView_isExpend,true)
       obtain.recycle()
      // initCloseEnd()
       maxLines = mMaxLines
       movementMethod = LinkMovementMethod.getInstance()
   }

    private var mOnExpandListener: OnExpandListener? = null

    fun setExpandListener(listener: OnExpandListener) {
        mOnExpandListener = listener
    }

    interface OnExpandListener {
        fun onExpand(view: ExpandTextView?)
    }

    /**
     * 设置TextView可显示的最大行数
     * @param maxLines 最大行数
     */
    override fun setMaxLines(maxLines: Int) {
        mMaxLines = maxLines
        super.setMaxLines(maxLines)
    }

    /**
     * 初始化TextView的可展示宽度
     * @param width
     */
    fun initWidth(width: Int) {
        initWidth = width
    }
    fun setExpendTextColor (@ColorRes color:Int){
        this.TO_SHRINK_HINT_COLOR_BG_PRESSED = color
    }

    /**
     * 展开的文案(颜色处理)初始化
     */
    private fun initCloseEnd() {
        val content = TEXT_EXPAND
        SPAN_CLOSE = SpannableString(content)
        val span = ButtonSpan(context, OnClickListener {
             if (mOnExpandListener != null){
                mOnExpandListener!!.onExpand(this)
            }else {
                 super@ExpandTextView.setMaxLines(Int.MAX_VALUE)
                 setExpandText(originText)
             }
        },TO_EXPAND_HINT_COLOR_BG_PRESSED)
        SPAN_CLOSE!!.setSpan(span, 0, content.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    /**
     * 收起的文案(颜色处理)初始化
     */
    private fun initExpandEnd() {

            val content = TEXT_CLOSE

            val span = ButtonSpan(context, OnClickListener {
                super@ExpandTextView.setMaxLines(mMaxLines)
                setCloseText(originText)
            }, TO_SHRINK_HINT_COLOR_BG_PRESSED)
        if (isExpend){
            SPAN_EXPAND = SpannableString(content)
            SPAN_EXPAND!!.setSpan(span, 0, content.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }else{
            SPAN_EXPAND = SpannableString("")
        }



    }

    fun setCloseText(text: CharSequence?) {

        Log.i("ExpandTextView", "CharSequence: $text")
        if (SPAN_CLOSE == null) {
            initCloseEnd()
        }
        var appendShowAll = false // true 不需要展开收起功能， false 需要展开收起功能

        originText = text.toString()

        // SDK >= 16 可以直接从xml属性获取最大行数
        var maxLines = 0
        maxLines = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getMaxLines()
        } else {
            mMaxLines
        }
        var workingText = StringBuilder(originText!!).toString()
        Log.i("ExpandTextView", "setCloseText: $workingText")
        if (maxLines != -1) {
            val layout = createWorkingLayout(workingText)
            if (layout.lineCount > maxLines) {
                //获取一行显示字符个数，然后截取字符串数
                workingText = originText!!.substring(0, layout.getLineEnd(maxLines - 1))
                    .trim { it <= ' ' } // 收起状态原始文本截取展示的部分
                val showText = originText!!.substring(0, layout.getLineEnd(maxLines - 1))
                    .trim { it <= ' ' } + "..." + SPAN_CLOSE
                var layout2 = createWorkingLayout(showText)
                // 对workingText进行-1截取，直到展示行数==最大行数，并且添加 SPAN_CLOSE 后刚好占满最后一行
                while (layout2.lineCount > maxLines) {
                    val lastSpace = workingText.length - 1
                    if (lastSpace == -1) {
                        break
                    }
                    workingText = workingText.substring(0, lastSpace)
                    layout2 = createWorkingLayout("$workingText...$SPAN_CLOSE")
                }
                Log.i("ExpandTextView", "workingText2: $workingText")
                appendShowAll = true
                workingText = "$workingText..."
            }
        }
           setText(transformContentText(atUserList,workingText){
            })
        if (appendShowAll) {
            // 必须使用append，不能在上面使用+连接，否则spannable会无效
            append(SPAN_CLOSE)
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    @SuppressLint("SetTextI18n")
    fun setExpandText(text: String?, topic:String="") {
        if (SPAN_EXPAND == null) {
            initExpandEnd()
        }
        val layout1 = createWorkingLayout(text)
        val layout2 = createWorkingLayout(text + TEXT_CLOSE)
        // 展示全部原始内容时 如果 TEXT_CLOSE 需要换行才能显示完整，则直接将TEXT_CLOSE展示在下一行
       // if (layout2.lineCount > layout1.lineCount) {
              setText("$originText")
      //  } else {
        //    setText("$originText")
                setText(transformContentText(atUserList, originText ?: "") {

               })
     //   }
        append(SPAN_EXPAND)
        movementMethod = LinkMovementMethod.getInstance()
    }

    //返回textview的显示区域的layout，该textview的layout并不会显示出来，只是用其宽度来比较要显示的文字是否过长
    @SuppressLint("RestrictedApi")
    private fun createWorkingLayout(workingText: String?): Layout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            StaticLayout(
                workingText,
                paint, getWidth(context)- paddingLeft - paddingRight- dp2px(context,40),
                Layout.Alignment.ALIGN_NORMAL,
                lineSpacingMultiplier, lineSpacingExtra, false
            )
        } else {
            StaticLayout(
                workingText, paint, getWidth(context)- paddingLeft - paddingRight-dp2px(context,40),
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false
            )
        }
    }

    fun setAtIds(idList: List<String>) {
        atUserList = idList
    }

private fun transformContentText(
        aitList: List<String>?,
        source: String,
        onClickSpan: ((String) -> Unit)? = null
    ): SpannableStringBuilder {
    Log.i("ExpandTextView", "onClick: ${source}")
    val spannableString = SpannableStringBuilder(source)
    try {
        aitList?.forEach {

           var index =  source.indexOf(it)
            if (index!=-1){
                spannableString.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onClickSpan?.invoke(it)
                    }
                }, index, index+it.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(object : UnderlineSpan(){
                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = getColor(R.color.color_0091ff);//设置颜色
                        ds.isUnderlineText = false;//去掉下划线
                        ds.isFakeBoldText = true

                    } },
                    index, index+it.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

        }
    }catch (e:Exception){

    }





        return spannableString
    }


    fun getColor(resId: Int): Int {
        return ContextCompat.getColor(context,resId)
    }
}