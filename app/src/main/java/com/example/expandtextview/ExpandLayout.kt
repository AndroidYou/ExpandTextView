package com.example.expandtextview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.internal.ViewUtils
import org.w3c.dom.Text
import java.util.regex.Pattern

/**
 * 自定义展开文本
 * yf
 */
class ExpandLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defaultInt: Int = 0
) : ConstraintLayout(context, attributeSet, defaultInt) {
    private var originText : String? = null // 原始内容文本
    private var mMaxLines = 2 // TextView最大行数
    private var expandTextView:TextView
    private var expandButton :TextView
    private var topic:List<String>?=null
    private var isExpand = false

    /**
      *<!--话题颜色-->
     */
    private var mTopicColor =Color.BLACK
    /**
     *<!--默认文本颜色-->
     */
    private var mDefaultColor =Color.BLACK
    /**
     *<!--展开按钮颜色-->
     */
    private var mExpandBtnColor =Color.BLACK
    /**
     *<!--展开按钮是否加粗-->
     */
    private var mExpandBtnBold =true
    /**
     *<!--话题是否加粗-->
     */
    private var mTopicBold =true
    /**
     *<!--文本最大高度-->
     */
    private var mTextMaxHeight =0
    /**
     *<!--文本行数-->
     */
    private var mTextMaxLines =0
    /**
     *<!--展开文本字体大小-->
     */
    private var mExpandBtnTextSize =16f
    /**
     *<!---默认文本字体大小-->
     */
    private var mDefaultTextSize =16f
    /**
     *<!---默认文本字体大小-->
     */
    private var mExpandBtnMarginTop =0

    init {
        val attributes = context.obtainStyledAttributes(attributeSet, R.styleable.ExpandLayout)
        mDefaultColor = attributes.getColor(R.styleable.ExpandLayout_defaultColor,Color.BLACK)
        mTopicColor = attributes.getColor(R.styleable.ExpandLayout_topicColor,getColor(R.color.color_0091ff))
        mExpandBtnColor = attributes.getColor(R.styleable.ExpandLayout_expandBtnColor,getColor(R.color.color_0091ff))
        mExpandBtnBold = attributes.getBoolean(R.styleable.ExpandLayout_expandBtnBold,false)
        mTopicBold = attributes.getBoolean(R.styleable.ExpandLayout_topicBold,false)
        mTextMaxHeight = attributes.getDimensionPixelSize(R.styleable.ExpandLayout_textMaxHeight,mTextMaxHeight)
        mTextMaxLines = attributes.getInt(R.styleable.ExpandLayout_textMaxLines,mTextMaxLines)
        mExpandBtnTextSize = attributes.getDimension(R.styleable.ExpandLayout_expandBtnTextSize,mExpandBtnTextSize)
        mDefaultTextSize = attributes.getDimension(R.styleable.ExpandLayout_defaultTextSize,mDefaultTextSize)
        mExpandBtnMarginTop = attributes.getDimensionPixelSize(R.styleable.ExpandLayout_expandBtnMarginTop,mExpandBtnMarginTop)

        attributes.recycle()


        expandTextView = TextView(context).apply {
              layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT).apply {
              startToStart =LayoutParams.PARENT_ID
              topToTop = LayoutParams.PARENT_ID
          }
        }
        expandTextView.id = View.generateViewId()
        expandTextView.setTextColor(mDefaultColor)
        expandTextView.maxLines =mTextMaxLines
        expandTextView.textSize = mDefaultTextSize
        //展开按钮
        expandButton = TextView(context)
        expandButton.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            topToBottom = expandTextView.id
            topMargin = mExpandBtnMarginTop

        }
        expandButton.typeface = if (mExpandBtnBold)Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        expandButton.text = "展开"
        expandButton.textSize = mExpandBtnTextSize
        expandButton.setTextColor(mExpandBtnColor)

        addView(expandTextView)
        addView(expandButton)
        initListener()
    }

    private fun initListener() {
        expandButton.setOnClickListener {
            if (!isExpand){
                expandTextView.maxLines = Int.MAX_VALUE
                expandButton.text = "收起"
                expandTextView.text = originText?.let { it1 ->
                    transformContentText(topic, it1){
                    }

                }
                Log.i("TAGss", "initListener: ${transformContentText(topic,originText!!,{})}")
               // expandTextView.movementMethod = LinkMovementMethod.getInstance()
                Log.i("TAGss", "initListener: ${expandTextView.height}")
                //TODO  超出一定高度内容可滑动
//                if (expandTextView.height>mTextMaxHeight){
//                    expandTextView.movementMethod = ScrollingMovementMethod.getInstance()
//                    expandTextView.height = mTextMaxHeight
//                }
            }else{
                expandTextView.maxLines = mMaxLines
                expandButton.text = "展开"
                setTextContent(originText!!,topic)
            }
            isExpand = !isExpand
        }
    }

    fun  setTextContent(content:String,topic:List<String>?=null){
        this.topic = topic
         originText = content
        var  maxLines =if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            expandTextView.maxLines
        } else {
            mMaxLines
        }
        var workingText = StringBuilder(originText!!).toString()
        if (maxLines != -1) {
            val layout = createWorkingLayout(workingText)
            if (layout.lineCount > maxLines) {
                //获取一行显示字符个数，然后截取字符串数
                workingText = originText!!.substring(0, layout.getLineEnd(maxLines - 1))
                    .trim { it <= ' ' } // 收起状态原始文本截取展示的部分
                val showText = originText!!.substring(0, layout.getLineEnd(maxLines - 1))
                    .trim { it <= ' ' } + "..."
                var layout2 = createWorkingLayout(showText)
                // 对workingText进行-1截取，直到展示行数==最大行数，并且添加 SPAN_CLOSE 后刚好占满最后一行
                while (layout2.lineCount > maxLines) {
                    val lastSpace = workingText.length - 1
                    if (lastSpace == -1) {
                        break
                    }
                    workingText = workingText.substring(0, lastSpace)
                    layout2 = createWorkingLayout("$workingText...")
                }
                workingText = "$workingText..."
                expandTextView.text = transformContentText(topic,workingText){

                }
                expandButton.visibility = View.VISIBLE
              //  expandTextView.movementMethod = LinkMovementMethod.getInstance()
            }else{
                expandButton.visibility = View.GONE
                expandTextView.text = originText
            }
        }
    }

    //返回textview的显示区域的layout，该textview的layout并不会显示出来，只是用其宽度来比较要显示的文字是否过长
    @SuppressLint("RestrictedApi")
    private fun createWorkingLayout(workingText: String?): Layout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            StaticLayout(
                workingText,
                expandTextView.paint, getWidth(context)- ViewUtils.dpToPx(context, 50).toInt() - paddingLeft - paddingRight,
                Layout.Alignment.ALIGN_NORMAL,
                expandTextView.lineSpacingMultiplier, expandTextView.lineSpacingExtra, false
            )
        } else {
            StaticLayout(
                workingText, expandTextView.paint, getWidth(context)- ViewUtils.dpToPx(context, 50).toInt() - paddingLeft - paddingRight,
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false
            )
        }
    }
    private fun transformContentText(
        aitList: List<String>?,
        source: String,
        onClickSpan: ((String) -> Unit)? = null
    ): SpannableStringBuilder {
        val spannableString = SpannableStringBuilder(source)
        try {
            aitList?.forEach {
                val compile = Pattern.compile(it)
                val matcher = compile.matcher(source)
                while (matcher.find()) {
                    val s = matcher.start()
                    val e = matcher.end()
                    spannableString.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            onClickSpan?.invoke(it)
                        }
                    }, s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(
                        object : UnderlineSpan() {
                            override fun updateDrawState(ds: TextPaint) {
                                ds.color = mTopicColor//设置颜色
                                ds.isUnderlineText = false;//去掉下划线
                                ds.isFakeBoldText = mTopicBold

                            }
                        },
                        s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
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