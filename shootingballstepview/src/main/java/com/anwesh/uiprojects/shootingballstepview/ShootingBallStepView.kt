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
val DELAY : Long = 20

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
    val x : Float = -wSize * (1 - sc1)
    paint.color = foreColor
    save()
    translate(0f, gap * (i + 1))
    drawRect(RectF(x, -hSize, x + wSize, hSize), paint)
    for (j in 0..(balls - 1)) {
        val sc : Float = sc2.divideScale(j, balls)
        save()
        translate(x + (w + r) * sc, 0f)
        drawCircle(0f, 0f, r, paint)
        restore()
    }
    restore()
}

class ShootingBallStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateScale(dir, 1, balls)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(DELAY)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SBSNode(var i : Int, val state : State = State()) {

        private var next : SBSNode? = null
        private var prev : SBSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SBSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSBSNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SBSNode {
            var curr : SBSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class ShootingBallStep(var i : Int) {

        private val root : SBSNode = SBSNode(0)
        private var curr : SBSNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : ShootingBallStepView) {

        private val animator : Animator = Animator(view)
        private var sbs : ShootingBallStep = ShootingBallStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            sbs.draw(canvas, paint)
            animator.animate {
                sbs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            sbs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : ShootingBallStepView {
            val view : ShootingBallStepView = ShootingBallStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}