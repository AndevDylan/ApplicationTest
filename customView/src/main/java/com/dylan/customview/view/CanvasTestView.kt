package com.dylan.customview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.MainThread
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.postDelayed
import com.dylan.customview.R
import kotlin.math.min

/**
 * Creator: Dylan.
 * Date: 2022/7/13.
 * desc: canvas与paint基本用法
 * @link: https://rengwuxian.com/ui-1-1/
 */
class CanvasTestView : View {
    private val paint = Paint()

    // 画布旋转相关
    private var degree = 0f
    private var degreeStarted = false

    // 画线相关
    private var lineStartXY = 100f
    private var lineEndXY = 280f

    // 线是否是伸展状态
    private var spreaded = true


    // 画路径相关
    private val path = Path()

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    init {
        setOnClickListener {
            degreeStarted = !degreeStarted
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制背景
        canvas.drawColor(Color.parseColor("#eeddaa"))

        // 开启抗锯齿（默认即为开启状态）
        paint.isAntiAlias = true
        paint.color = Color.parseColor("#ee0000")
        // 画线模式
        paint.style = Paint.Style.STROKE
        val center = min(width.toFloat() / 2, height.toFloat() / 2)
        // 画圆
        paint.strokeWidth = 130f
        val radius = min(width, height).toFloat() / 2 * 0.8.toFloat()
        canvas.drawCircle(center, center, radius, paint)

        // 画矩形
        paint.color = Color.parseColor("#00ee00")
        // 设置填充模式（仅对封闭图形有影响，如圆、矩形、椭圆等，对线没有影响）
        paint.style = Paint.Style.FILL
        val rectLeftTop = width.toFloat() / 4
        // 旋转画布
        canvas.rotate(degree, center, center)
        canvas.drawRect(rectLeftTop, rectLeftTop, width - rectLeftTop, height - rectLeftTop, paint)
        if (degreeStarted) {
            degree = (degree + 1) % 360
            postDelayed(1L) {
                invalidate()
            }
        }

        // 画线
        paint.color = Color.parseColor("#0000ee")
        paint.strokeWidth = 20f
        if (spreaded) {
            if (lineEndXY < 280) {
                lineEndXY++
            } else {
                spreaded = false
            }
        } else {
            if (lineEndXY > lineStartXY) {
                lineEndXY--
            } else {
                spreaded = true
            }
        }
        canvas.drawLine(lineStartXY, lineStartXY, lineEndXY, lineEndXY, paint)
        canvas.drawLine(width - lineStartXY, lineStartXY, width - lineEndXY, lineEndXY, paint)

        // 画点
        val pointLeftXY = 400f
        paint.color = Color.parseColor("#000000")
        // 设置点宽度
        paint.strokeWidth = 50f
        // 设置点的形状
        paint.strokeCap = Paint.Cap.ROUND
        /*canvas.drawPoint(pointLeftXY, pointLeftXY, paint)
        canvas.drawPoint(width - pointLeftXY, pointLeftXY, paint)*/
        canvas.drawPoints(floatArrayOf(pointLeftXY, pointLeftXY, width - pointLeftXY, pointLeftXY), paint)

        // 画椭圆
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#ffffff")
        val ovalLeftLeft = pointLeftXY - 100
        val ovalLeftRight = pointLeftXY + 100
        val ovalLeftTop = pointLeftXY - 70
        val ovalLeftBottom = pointLeftXY + 70
        // 画左边眼镜框
        canvas.drawOval(ovalLeftLeft, ovalLeftTop, ovalLeftRight, ovalLeftBottom, paint)
        val ovalRightLeft = width - pointLeftXY - 100
        val ovalRightRight = width - ovalLeftLeft
        // 画右边眼镜框
        canvas.drawOval(ovalRightLeft, ovalLeftTop, ovalRightRight, ovalLeftBottom, paint)

        // 画圆角矩形
        paint.color = Color.parseColor("#aaaaaa")
        val roundRectLeft = width.toFloat() / 3
        val roundRectTop = height.toFloat() / 3 * 2
        val roundRectRight = width.toFloat() / 3 * 2
        val roundRectBottom = height.toFloat() / 4 * 3
        // 画嘴巴
        canvas.drawRoundRect(roundRectLeft, roundRectTop, roundRectRight, roundRectBottom, 20f, 20f, paint)

        // 画扇形
        paint.color = Color.parseColor("#0000aa")
        paint.style = Paint.Style.FILL
        val arcCenterX = width.toFloat() / 2
        val arcCenterY = height.toFloat() / 2 + 20
        // 画鼻子
        canvas.drawArc(arcCenterX - 30, arcCenterY - 30, arcCenterX + 30, arcCenterY + 30, 45f, 90f, true, paint)

        // 画路径：drawPath()，可画任意图形
        path.reset()
        // 画外部环绕细边圆
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.color = Color.GRAY
        // 添加圆
        path.addCircle(width.toFloat() / 2, height.toFloat() / 2, width.toFloat() / 2, Path.Direction.CW)
        // 移动线的起始位置
        path.moveTo(0f, height * 0.9.toFloat())
        // 画四条线，形成倒立梯形
        path.rLineTo(100f, 100f)
        path.rLineTo(width.toFloat() - 200, 0f)
        path.lineTo(width.toFloat(), height * 0.9.toFloat())
        path.rLineTo(-width.toFloat(), 0f)
        // 画弧线（与扇形参数有区别）(鼻子上的半圆弧)
        path.arcTo(arcCenterX - 60, arcCenterY - 60, arcCenterX + 60, arcCenterY + 60, 180f, 180f, true)
        // 封闭当前子图形（相当于用lineTo方法连接子图形的首尾）
        // 这里封闭的是path.arcTo()绘制的图形
//        path.close()
        // 绘制路径
        canvas.drawPath(path, paint)

        // 画bitmap
//        canvas.drawBitmap(AppCompatResources.getDrawable(context, R.drawable.test)!!.toBitmap(), 100f, 100f, paint)

        // 画文字
        paint.color = Color.RED
        paint.textSize = 108f
        canvas.drawText("小丑", width.toFloat() / 2, height.toFloat() / 2, paint)

    }

    @MainThread
    fun reset() {
        degreeStarted = false
        degree = 0f

        spreaded = true
        lineEndXY = 280f
        invalidate()
    }

}