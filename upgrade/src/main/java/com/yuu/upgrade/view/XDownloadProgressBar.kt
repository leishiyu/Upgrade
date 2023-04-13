package com.yuu.upgrade.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import com.yuu.update.R
import java.util.*

/**
 *
 * @Author: Leisiyu
 * @Date: 2022/12/13
 * @Description:
 * 1.底层总进度
 * 2.已完成进度层
 * 3.斜线层
 * 4.属性动画
 */
class XDownloadProgressBar
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    /**
     * 最大进度
     */
    private var mMaxProgress = 100

    /**
     * 当前进度
     */
    private var currentProgress = 0

    /**
     * 总进度层背景
     */
    private var totalProgressColor: Int

    /**
     * 当前进度层背景
     */
    private var currentProgressColor: Int

    /**
     * 斜线颜色
     */
    private var slashColor: Int

    /**
     * 文本颜色
     */
    private var mTextColor: Int
    private var mTextSize: Float

    /**
     * progressBar高度
     */
    private var mHeight: Float

    /**
     * 进度文字偏移量
     */
    private var mOffset: Float

    /**
     * 绘制文字Flag
     */
    private var isDrawText = true

    /**
     * 绘制的文字
     */
    private var drawText: String = ""

    private var progressValueAnimation : ValueAnimator ?=null

    private var animationValue :Int = 0


    /**
     * 画笔
     */
    private var totalPainter: Paint? = null
    private var currentPainter: Paint? = null
    private var slashPainter: Paint? = null
    private var textPainter: Paint? = null
    private var drawTextX = 0F
    private var drawTextY = 0F
    private val totalRect = RectF(0f, 0f, 0f, 0f)
    private val currentRect = RectF(0f, 0f, 0f, 0f)
    private val floatArray = listOf(50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f).toFloatArray()


    init {
        val defaultTextColor = Color.rgb(66, 145, 241)
        val defaultCurrentColor = Color.rgb(66, 145, 241)
        val defaultTotalColor = Color.rgb(204, 204, 204)
        val defaultSlashColor = Color.rgb(204, 204, 204)

        val defaultReachedBarHeight = dp2px(1.5f)
        val defaultTextSize = sp2px(10f)
        val defaultProgressTextOffset = dp2px(3.0f)


        val attributes = context.theme.obtainStyledAttributes(
            attrs, R.styleable.XDownloadProgressBar,
            defStyleAttr, 0
        )


        //获取XML中属性值
        totalProgressColor =
            attributes.getColor(
                R.styleable.XDownloadProgressBar_progressTotalBackground,
                defaultTotalColor
            )

        currentProgressColor =
            attributes.getColor(
                R.styleable.XDownloadProgressBar_progressCurrentBackground,
                defaultCurrentColor
            )

        slashColor =
            attributes.getColor(
                R.styleable.XDownloadProgressBar_progressSlashBackground,
                defaultSlashColor
            )

        mTextColor =
            attributes.getColor(R.styleable.XDownloadProgressBar_textColor, defaultTextColor)

        mTextSize =
            attributes.getDimension(R.styleable.XDownloadProgressBar_textSize, defaultTextSize)
        mHeight =
            attributes.getDimension(
                R.styleable.XDownloadProgressBar_progressHeight,
                defaultReachedBarHeight
            )
        mOffset =
            attributes.getDimension(
                R.styleable.XDownloadProgressBar_textOffset,
                defaultProgressTextOffset
            )
        val textVisible =
            attributes.getInt(
                R.styleable.XDownloadProgressBar_textVisibility,
                PROGRESS_TEXT_VISIBLE
            )
        if (textVisible != PROGRESS_TEXT_VISIBLE) {
            isDrawText = false
        }
        setMaxProgress(attributes.getInt(R.styleable.XDownloadProgressBar_maxProgress, 100))
        setCurrentProgress(attributes.getInt(R.styleable.XDownloadProgressBar_currentProgress, 0))
        attributes.recycle()
        //初始化画笔
        initPainters()
        //初始化动画
        initAnimation()
    }

    private fun initAnimation() {
        progressValueAnimation = ValueAnimator.ofFloat(0f,1f).setDuration(500)
        progressValueAnimation?.addUpdateListener {
            val timePercent = it.animatedValue as Float
            animationValue = ((currentProgress-animationValue)*timePercent +animationValue).toInt()
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false))
    }

    override fun onDraw(canvas: Canvas?) {
        calculateDrawRectf()
        canvas?.run {
            drawRoundRect(totalRect, 50f, 50f, totalPainter!!)
            canvas.drawRoundRect(currentRect, 80f, 80f, currentPainter!!)
            canvas.drawBitmap(getSlashBitmap(), 0f, 0f, slashPainter)
            canvas.drawText(drawText!!, drawTextX, drawTextY, textPainter!!)
        }
    }


    private fun initPainters() {
        totalPainter = Paint(Paint.ANTI_ALIAS_FLAG)
        totalPainter?.color = totalProgressColor
        currentPainter = Paint(Paint.ANTI_ALIAS_FLAG)
        currentPainter?.color = currentProgressColor
        slashPainter = Paint(Paint.ANTI_ALIAS_FLAG)
        slashPainter?.color = slashColor
        textPainter = Paint(Paint.ANTI_ALIAS_FLAG)
        textPainter?.apply {
            color = mTextColor
            textSize = mTextSize
        }
    }

    private fun getSlashBitmap(): Bitmap {
        var startX = 0f
        val startY = 0f
        val endX = 0f
        var endY = 0f
        val factor = 1.732f
        val rectangle = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c1 = Canvas(rectangle)
        val p1 = Paint(Paint.ANTI_ALIAS_FLAG)
        val path = Path()
        path.addRoundRect(currentRect, floatArray, Path.Direction.CW)
        c1.clipPath(path)
        c1.drawARGB(0, 0, 0, 0)
        p1.color = slashColor
        p1.strokeWidth = 20f
        while (startX < width + 200) {
            startX += 50f
            endY = startX * factor
            c1.drawLine(startX, startY, endX, endY, p1)
        }
        return rectangle
    }


    private fun calculateDrawRectf() {
        //拼接绘制文本
        val temp = String.format(Locale.getDefault(), "%d", animationValue * 100 / mMaxProgress)
        drawText = "$temp%"
        //确定绘制文本宽度
        val mDrawTextWidth = textPainter!!.measureText(drawText)

        //确定当前进度绘制区域
        if (currentProgress == 0) {
            drawTextX = paddingLeft.toFloat()
        } else {
            //mReachedRectF 当前进度矩形
            currentRect.left = paddingLeft.toFloat()
            currentRect.top = height / 2.0f - mHeight / 2.0f
            currentRect.right =
                (width - paddingLeft - paddingRight) / (mMaxProgress * 1.0f) * animationValue - mOffset + paddingLeft
            currentRect.bottom = height / 2.0f + mHeight / 2.0f
            //文字绘制的X坐标
            drawTextX = if(currentRect.right < mDrawTextWidth){
                currentRect.left
            }else{
                currentRect.right - mDrawTextWidth
            }
        }

        //文字绘制的Y坐标
        drawTextY = (currentRect.top - textPainter!!.descent()).toInt().toFloat()


        totalRect.left = paddingLeft.toFloat()
        totalRect.right = (width - paddingRight).toFloat()
        totalRect.top = height / 2.0f + -mHeight / 2.0f
        totalRect.bottom = height / 2.0f + mHeight / 2.0f
    }

    private fun measure(measureSpec: Int, isWidth: Boolean): Int {
        var result: Int
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        val padding = if (isWidth) paddingLeft + paddingRight else paddingTop + paddingBottom
        if (mode == MeasureSpec.EXACTLY) {
            result = size
        } else {
            result = if (isWidth) suggestedMinimumWidth else suggestedMinimumHeight
            result += padding
            if (mode == MeasureSpec.AT_MOST) {
                result = if (isWidth) {
                    result.coerceAtLeast(size)
                } else {
                    result.coerceAtMost(size)
                }
            }
        }
        return result
    }


    /**
     * 设置最大进度值
     */
    fun setMaxProgress(maxProgress: Int) {
        if (maxProgress > 0) {
            mMaxProgress = maxProgress
            invalidate()
        }
    }

    /**
     * 设置当前进度值
     */
    fun setCurrentProgress(progress: Int) {
        if (progress in 0..mMaxProgress) {
            currentProgress = progress
            progressValueAnimation?.start()
        }
    }

    fun dp2px(dp: Float): Float {
        val scale = resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    fun sp2px(sp: Float): Float {
        val scale = resources.displayMetrics.scaledDensity
        return sp * scale
    }
    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState())
        bundle.putInt(INSTANCE_TEXT_COLOR, mTextColor)
        bundle.putFloat(INSTANCE_TEXT_SIZE, mTextSize)
        bundle.putFloat(INSTANCE_PROGRESS_HEIGHT, mHeight)
        bundle.putInt(INSTANCE_CURRENT_PROGRESS_COLOR, currentProgressColor)
        bundle.putInt(INSTANCE_TOTAL_PROGRESS_COLOR, totalProgressColor)
        bundle.putInt(INSTANCE_MAX, mMaxProgress)
        bundle.putInt(INSTANCE_PROGRESS, currentProgress)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val bundle = state
            mTextColor = bundle.getInt(INSTANCE_TEXT_COLOR)
            mTextSize = bundle.getFloat(INSTANCE_TEXT_SIZE)
            mHeight = bundle.getFloat(INSTANCE_PROGRESS_HEIGHT)
            currentProgressColor = bundle.getInt(INSTANCE_CURRENT_PROGRESS_COLOR)
            totalProgressColor = bundle.getInt(INSTANCE_TOTAL_PROGRESS_COLOR)
            initPainters()
            setMaxProgress(bundle.getInt(INSTANCE_MAX))
            setCurrentProgress(bundle.getInt(INSTANCE_PROGRESS))
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }


    companion object {
        private const val INSTANCE_STATE = "saved_instance"
        private const val INSTANCE_TEXT_COLOR = "text_color"
        private const val INSTANCE_TEXT_SIZE = "text_size"
        private const val INSTANCE_PROGRESS_HEIGHT = "mHeight"
        private const val INSTANCE_CURRENT_PROGRESS_COLOR = "current_color"
        private const val INSTANCE_TOTAL_PROGRESS_COLOR = "total_color"
        private const val INSTANCE_MAX = "max"
        private const val INSTANCE_PROGRESS = "progress"
        private const val PROGRESS_TEXT_VISIBLE = 0
    }
}