package com.example.arboardgame

import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3

class RotatingNode(
    val clockwise: Boolean = false,
    val axisTiltDeg: Float = 0.0f,
    val rotationSpeedMultipler: Float = 1.0f,
    val degreesPerSecond: Float = 90.0f
) : Node() {


    private var animator: ObjectAnimator? = null

    override fun onActivate() {
        if (animator != null) {
            return
        }

        animator = createAnimator()
        animator!!.setTarget(this)
        animator!!.setDuration(getAnimationDuration())
        animator!!.start()
    }

    private fun createAnimator(): ObjectAnimator {
        val orientations = arrayOfNulls<Quaternion>(4)
        val baseOrientation = Quaternion.axisAngle(
            Vector3(1.0f, 0f, 0.0f),
            axisTiltDeg
        )
        for (i in orientations.indices) {
            var angle = (i * 360 / (orientations.size - 1)).toFloat()
            if (clockwise) {
                angle = 360 - angle
            }

            val orientation = Quaternion.axisAngle(
                Vector3(0.0f, 1.0f, 0.0f),
                angle
            )
            orientations[i] = Quaternion.multiply(baseOrientation, orientation)
        }

        val animator = ObjectAnimator()
        animator.setObjectValues(*orientations as Array<Any>)

        animator.setPropertyName("localRotation")
        animator.setEvaluator(QuaternionEvaluator())
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.RESTART
        animator.interpolator = LinearInterpolator()
        animator.setAutoCancel(true)
        return animator

    }


    private fun getAnimationDuration(): Long {
        return (1000 * 360 / (degreesPerSecond * rotationSpeedMultipler)).toLong()
    }

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)
        if (animator == null) {
            return
        }
        if (rotationSpeedMultipler == 0.0f) {
            animator!!.pause()
        } else {
            animator!!.resume()
            val animatedFraction = animator!!.getAnimatedFraction()
            animator!!.setDuration(getAnimationDuration())
            animator!!.setCurrentFraction(animatedFraction)
        }
    }

    override fun onDeactivate() {
        if (animator == null) {
            return
        }

        animator!!.cancel()
        animator = null
    }

}

