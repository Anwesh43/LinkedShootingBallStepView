package com.anwesh.uiprojects.shootingballstepview

/**
 * Created by anweshmishra on 05/01/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val balls : Int = 4
val scDiv : Double = 0.51
val scGap : Float = 0.05f
val hSizeFactor : Float = 4.5f
val wSizeFactor : Float = 3.2f
val strokeFactor : Int = 90
val foreColor : Int = Color.parseColor("#4CAF50")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap

fun Canvas.drawSBSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val wSize = w / wSizeFactor
    val hSize = gap / hSizeFactor
    val r : Float = hSize / 2
    val x : Float = -w * (1 - sc1)
    paint.color = foreColor
    save()
    translate(0f, gap * (i + 1))
    drawRect(RectF(x, -hSize, x + wSize, hSize), paint)
    for (j in 0..(balls - 1)) {
        val sc : Float = sc2.divideScale(j, balls)
        save()
        translate(x - 2 * r + (w + r) * sc, 0f)
        drawCircle(0f, 0f, r, paint)
        restore()
    }
    restore()
}