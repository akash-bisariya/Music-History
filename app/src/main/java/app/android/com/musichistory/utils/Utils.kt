package app.android.com.musichistory.utils

import android.graphics.*

/**
 * Created by akash bisariya on 31-05-2018.
 */
class Utils {
    companion object {
        /**
         * Created round bitmap image for floating view
         */
        fun getCroppedBitmap(bitmap: Bitmap): Bitmap {
            val output = Bitmap.createBitmap(bitmap.width,
                    bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            paint.alpha = 180
            // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            canvas.drawCircle(bitmap.width / 2.toFloat(), bitmap.height / 2f,
                    bitmap.width / 2.toFloat(), paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
            return output
        }
    }
}