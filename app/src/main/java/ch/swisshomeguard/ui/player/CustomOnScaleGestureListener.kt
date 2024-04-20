package ch.swisshomeguard.ui.player

import android.view.ScaleGestureDetector
import android.widget.RelativeLayout

public class CustomOnScaleGestureListener(

) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    private var scaleFactor = 1.0f

    override fun onScale(
        detector: ScaleGestureDetector
    ): Boolean {
        scaleFactor *= detector.scaleFactor
        scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 6.0f))
        return true
    }

    override fun onScaleBegin(
        detector: ScaleGestureDetector
    ): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        super.onScaleEnd(detector)
    }
}