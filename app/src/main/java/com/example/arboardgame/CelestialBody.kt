package com.example.arboardgame

import android.content.Context
import android.view.MotionEvent
import android.widget.TextView
import com.google.android.filament.Filament.init
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable

class CelestialBody(


    val context: Context,
    val celestialName: String,
    val planetScale: Float = 1.0f,
    val tilt: Float = 0.0f,
    val clockwise: Boolean = false,
    val rotationSpeedMultipler: Float = 1.0f,
    val rotationPerSecond: Float = 90.0f,
    val renderable: ModelRenderable,
    val infoCallback: InfoCallback?
) : Node(),
    Node.OnTapListener {


    override fun onTap(p0: HitTestResult?, p1: MotionEvent?) {
        if (infoCallback != null) {
            infoCallback.showInfo()
        }
    }

    private var infoCard: Node? = null
    private
    val INFO_CARD_Y_POS_COEFFICIENT = 0.55f
    private
    var planetNode: RotatingNode? = null


    init {
        setOnTapListener(this)
    }

    override fun onActivate() {
        if (infoCard == null) {
            infoCard = Node()
            infoCard!!.setParent(this)
            infoCard!!.isEnabled = false
            infoCard!!.localPosition = Vector3(
                0.0f,
                planetScale * INFO_CARD_Y_POS_COEFFICIENT,
                0.0f
            )
            ViewRenderable.builder()
                .setView(context, R.layout.celestial_card_view)
                .build()
                .thenAccept { renderable ->
                    infoCard!!.renderable = renderable
                    val textView = renderable.view as TextView
                    textView.text = celestialName
                    infoCard!!.isEnabled = !infoCard!!.isEnabled
                }
        }
        if (planetNode == null) {
            planetNode = RotatingNode(
                clockwise = clockwise,
                axisTiltDeg = tilt,
                rotationSpeedMultipler = rotationSpeedMultipler,
                degreesPerSecond = rotationPerSecond
            )
            planetNode!!.setParent(this)
            planetNode!!.renderable = renderable
            planetNode!!.localScale =
                Vector3(planetScale, planetScale, planetScale)
        }
    }

    override fun onUpdate(p0: FrameTime?) {
        super.onUpdate(p0)
        if (scene == null || infoCard == null) {
            return
        }
        val cameraPosition = scene!!.camera.worldPosition
        val cardPosition = infoCard!!.worldPosition
        val direction = Vector3.subtract(cameraPosition, cardPosition)
        val lookRotation = Quaternion.lookRotation(direction, Vector3.up())
        infoCard!!.worldRotation = lookRotation
    }
}