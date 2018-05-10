package app.android.com.musichistory.customViews

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import app.android.com.musichistory.R
import kotlin.properties.Delegates

/**
 * Created by akash
 * on 27/4/18.
 */
class ShadowImageView(context: Context, attributes: AttributeSet? = null) : AppCompatImageView(context, attributes) {

    companion object {
        private const val DEFAULT_RADIUS = 0.7f
        private const val DEFAULT_COLOR = -1
        private const val BRIGHTNESS = -25f
        private const val SATURATION = 1.3f
        private const val TOP_OFFSET = 2f
        private const val PADDING = 20f
        internal const val DOWNSCALE_FACTOR = 0.25f
    }

    var radiusOffset by Delegates.vetoable(DEFAULT_RADIUS, { _, _, newValue ->
        newValue > 0F || newValue <= 1
    })

    var shadowColor = DEFAULT_COLOR

    init {
        BlurShadow.init(context.applicationContext)
        cropToPadding = true
        super.setScaleType(ScaleType.CENTER_CROP)
        val padding = dpToPx(PADDING)
        setPadding(padding, padding, padding, padding)
        val typedArray = context.obtainStyledAttributes(attributes, R.styleable.ShadowView, 0, 0)
        shadowColor = typedArray.getColor(R.styleable.ShadowView_shadowColor, DEFAULT_COLOR)
        radiusOffset = typedArray.getFloat(R.styleable.ShadowView_radiusOffset, DEFAULT_RADIUS)
        typedArray.recycle()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun setImageBitmap(bm: Bitmap?) {
        setBlurShadow { super.setImageDrawable(BitmapDrawable(resources, bm)) }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun setImageResource(resId: Int) {
        setBlurShadow { super.setImageDrawable(ContextCompat.getDrawable(context, resId)) }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun setImageResource(resId: Int, withShadow: Boolean) {
        if (withShadow) {
            setImageResource(resId)
        } else {
            background = null
            super.setImageResource(resId)
        }
    }

//    fun setImageBitmap(bm: Bitmap?, withShadow: Boolean) {
//        if (withShadow) {
//            setImageResource(resId)
//        } else {
//            background = null
//            super.setImageResource(resId)
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun setImageDrawable(drawable: Drawable?, withShadow: Boolean) {
        if (withShadow) {
            setImageDrawable(drawable)
        } else {
            background = null
            super.setImageDrawable(drawable)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun setImageDrawable(drawable: Drawable?) {
        setBlurShadow { super.setImageDrawable(drawable) }
    }

    override fun setScaleType(scaleType: ScaleType?) {
        super.setScaleType(ScaleType.CENTER_CROP)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun setBlurShadow(setImage: () -> Unit = {}) {
        background = null
        if (height != 0 || measuredHeight != 0) {
            setImage()
            makeBlurShadow()
        } else {
            val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    viewTreeObserver.removeOnPreDrawListener(this)
                    setImage()
                    makeBlurShadow()
                    return false
                }
            }
            viewTreeObserver.addOnPreDrawListener(preDrawListener)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun makeBlurShadow() {
        var radius = 4.toFloat()
        radius *= 2 * radiusOffset
        val blur = BlurShadow.blur(this, width, height - dpToPx(TOP_OFFSET), radius)
        //brightness -255..255 -25 is default
        val colorMatrix = ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, BRIGHTNESS,
                0f, 1f, 0f, 0f, BRIGHTNESS,
                0f, 0f, 1f, 0f, BRIGHTNESS,
                0f, 0f, 0f, 1f, 0f)).apply { setSaturation(SATURATION) }

        background = BitmapDrawable(resources, blur).apply {
            this.colorFilter = ColorMatrixColorFilter(colorMatrix)
            applyShadowColor(this)
        }
        //super.setImageDrawable(null)
    }

    private fun applyShadowColor(bitmapDrawable: BitmapDrawable) {
        if (shadowColor != DEFAULT_COLOR) {
            bitmapDrawable.colorFilter = PorterDuffColorFilter(shadowColor, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun View.dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
    }
}