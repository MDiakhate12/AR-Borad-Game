package com.example.arboardgame

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.KeyCharacterMap
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.*
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION = 42

    //Maps what we get from Firebase with an easier to work with value
    companion object {
        val SUN = "Sun"
        val MERCURY = "Mercury"
        val VENUS = "Venus"
        val EARTH = "Earth"
        val MARS = "Mars"
        val JUPITER = "Jupiter"
        val SATURN = "Saturn"
        val NEPTUNE = "Neptune"
        val URANUS = "Uranus"
    }

    //Item that we get back from Firebase
    private lateinit var celestial: String

    //Prevents stacking multiple instances of the same object
    private var hasPlacedObject = false

    //Reference to the ARSceneView declared in activity_main.xml
    private lateinit var arSceneView: ArSceneView

    //Used to intercept tap events on the screen
    private lateinit var gestureDetector: GestureDetector

    //Used to validate if the user has the AR Core app installed on their device
    private var installRequested: Boolean = false

    //Used to clear the base when a new value is available
    private var objectBase: Node? = null

    //Renderable objects for our 3D models
    private var sunRenderable: ModelRenderable? = null
    private var mercuryRenderable: ModelRenderable? = null
    private var venusRenderable: ModelRenderable? = null
    private var earthRenderable: ModelRenderable? = null
    private var marsRenderable: ModelRenderable? = null
    private var jupiterRenderable: ModelRenderable? = null
    private var saturnRenderable: ModelRenderable? = null
    private var neptuneRenderable: ModelRenderable? = null
    private var uranusRenderable: ModelRenderable? = null

    //Flag for when the 3D models have finished loading
    private var hasFinishedLoading = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestCameraPermission();

        arSceneView = findViewById(R.id.ar_scene_view)

        initData()
        initRenderables()
        initGestures()
        initSceneView()
        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (!hasCameraPermission()) {
            Toast.makeText(
                this,
                "You must grant camera permissions to use this app.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onPause() {
        super.onPause()
        arSceneView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView.destroy()
    }

    override fun onResume() {
        super.onResume()
        if (arSceneView.session == null) {
            try {
                val session = createArSession(installRequested)
                if (session == null) {
                    installRequested = hasCameraPermission()
                    return
                } else {
                    arSceneView.setupSession(session)
                }
            } catch (e: KeyCharacterMap.UnavailableException) {
                Toast.makeText(
                    this,
                    "Exception occurred.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
        try {
            arSceneView.resume()
        } catch (ex: CameraNotAvailableException) {
            finish()
            return
        }
    }

    private fun createArSession(installRequested: Boolean): Session? {
        var session: Session? = null
        if (hasCameraPermission()) {
            when (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> return null
                ArCoreApk.InstallStatus.INSTALLED -> {
                }
            }

            session = Session(this)
            val config = Config(session)
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            session.configure(config)
        }

        return session
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initData() {
//        val database = FirebaseDatabase.getInstance()
//        val myRef = database.getReference("piece/1/tag/planet")
//        myRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                celestial = dataSnapshot.getValue(String::class.java) ?: MERCURY
//                hasPlacedObject = false
//                this@MainActivity.runOnUiThread { objectBase?.setParent(null) }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.w("TEST", "Failed to read value.", error.toException())
//            }
//        })

        celestial = MERCURY
        hasPlacedObject = false
        this@MainActivity.runOnUiThread { objectBase?.setParent(null) }


    }

    private fun initRenderables() {
        val sunStage =
            ModelRenderable.builder().setSource(
                this,
                Uri.parse("Sol.sfb")
            ).build()

        val mercuryStage =
            ModelRenderable.builder().setSource(
                this,
                Uri.parse("Mercury.sfb")
            ).build()

        val venusStage =
            ModelRenderable.builder().setSource(
                this,
                Uri.parse("Venus.sfb")
            ).build()

        val earthStage =
            ModelRenderable.builder().setSource(
                this,
                Uri.parse("Earth.sfb")
            ).build()

        val marsStage =
            ModelRenderable.builder().setSource(
                this,
                Uri.parse("Mars.sfb")
            ).build()

        val jupiterStage =
            ModelRenderable.builder().setSource(
                this,
                Uri.parse("Jupiter.sfb")
            ).build()

        val saturnStage =
            ModelRenderable.builder().setSource(
                this,
                Uri.parse("Saturn.sfb")
            ).build()

        val neptuneStage =
            ModelRenderable.builder().setSource(
                this,
                Uri.parse("Neptune.sfb")
            ).build()

        val uranusStage =
            ModelRenderable.builder().setSource(
                this,
                Uri.parse("Uranus.sfb")
            ).build()

        CompletableFuture.allOf(
            sunStage,
            mercuryStage,
            venusStage,
            earthStage,
            marsStage,
            jupiterStage,
            saturnStage,
            neptuneStage,
            uranusStage
        ).handle { t, u ->
            try {
                sunRenderable = sunStage.get()
                mercuryRenderable = mercuryStage.get()
                venusRenderable = venusStage.get()
                earthRenderable = earthStage.get()
                marsRenderable = marsStage.get()
                jupiterRenderable = jupiterStage.get()
                saturnRenderable = saturnStage.get()
                neptuneRenderable = neptuneStage.get()
                uranusRenderable = uranusStage.get()
                hasFinishedLoading = true
            } catch (e: InterruptedException) {
            } catch (e: ExecutionException) {
            }
        }
    }

    private fun initSceneView() {
        arSceneView
            .scene
            .addOnUpdateListener { frameTime ->
                val frame = arSceneView.arFrame
                if (frame == null)
                    return@addOnUpdateListener
                if (frame.camera.trackingState != TrackingState.TRACKING)
                    return@addOnUpdateListener
            }
    }

    private fun initGestures() {
        gestureDetector = GestureDetector(this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent?): Boolean {
                    onSingleTap(e, celestial)
                    return super.onSingleTapUp(e)
                }

                override fun onDown(e: MotionEvent?): Boolean {
                    return true
                }
            })

        arSceneView.scene.setOnTouchListener { hitTestResult, motionEvent ->
            if (!hasPlacedObject) {
                return@setOnTouchListener gestureDetector.onTouchEvent(motionEvent)
            }
            return@setOnTouchListener false
        }
    }

    private fun onSingleTap(tap: MotionEvent?, celestialKey: String) {
        if (!hasFinishedLoading) {
            return
        }
        val frame = arSceneView.arFrame
        if (frame != null) {
            if (!hasPlacedObject && tryPlacingObject(tap, frame, celestialKey))
                hasPlacedObject = true
        }
    }

    private fun tryPlacingObject(tap: MotionEvent?, frame: Frame, celestialKey: String): Boolean {
        if (frame.camera.trackingState == TrackingState.TRACKING) {
            for (hit in frame.hitTest(tap)) {
                val trackable = hit.trackable
                if (trackable is Plane
                    && trackable.isPoseInPolygon(hit.hitPose)
                ) {
                    val anchor = hit.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arSceneView.scene)
                    objectBase = createCelestial(celestialKey)
                    anchorNode.addChild(objectBase)
                    return true
                }
            }
        }
        return false
    }

    private fun createCelestial(celestialKey: String): Node {
        val base = Node()

        val celestialObject = RotatingNode()


        celestialObject.setParent(base)
        celestialObject.localPosition = Vector3(0.0f, 0.5f, 0.0f)
        celestialObject.localScale = Vector3(0.5f, 0.5f, 0.5f)

        if (celestialKey == SUN) {
            celestialObject.renderable = sunRenderable
        } else if (celestialKey == MERCURY) {
            celestialObject.renderable = mercuryRenderable
        } else if (celestialKey == VENUS) {
            celestialObject.renderable = venusRenderable
        } else if (celestialKey == EARTH) {
            celestialObject.renderable = earthRenderable
        } else if (celestialKey == MARS) {
            celestialObject.renderable = marsRenderable
        } else if (celestialKey == JUPITER) {
            celestialObject.renderable = jupiterRenderable
        } else if (celestialKey == SATURN) {
            celestialObject.renderable = saturnRenderable
        } else if (celestialKey == NEPTUNE) {
            celestialObject.renderable = neptuneRenderable
        } else if (celestialKey == URANUS) {
            celestialObject.renderable = uranusRenderable
        }
        return base
    }
}



