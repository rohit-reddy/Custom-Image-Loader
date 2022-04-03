package com.rohith.photon.async

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.rohith.photon.cache.CacheRepository
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadImageTask(
    private val url: String,
    private val imageView: ImageView,
    private val cache: CacheRepository
) : DownloadTask<Bitmap?>() {

    override fun download(url: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val url = URL(url)
            val conn: HttpURLConnection = url.openConnection() as
                    HttpURLConnection
            val outputStream = ByteArrayOutputStream()
            conn.inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            val byteArray = outputStream.toByteArray()
            bitmap = BitmapFactory.Options().run {
                inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(byteArray,0, byteArray.size, this)

                // Calculate inSampleSize
                inSampleSize = calculateInSampleSize(this, imageView.width, imageView.height)

                // Decode bitmap with inSampleSize set
                inJustDecodeBounds = false

                BitmapFactory.decodeByteArray(byteArray,0, byteArray.size, this)
            }
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    private val uiHandler = Handler(Looper.getMainLooper())

    override fun call(): Bitmap? {
        val bitmap = download(url)
        bitmap?.let {
            if (imageView.tag == url) {
                updateImageView(imageView, it)
            }
            cache.put(url, it)
        }
        return bitmap
    }

    fun updateImageView(imageview: ImageView, bitmap: Bitmap) {
        uiHandler.post {
            imageview.setImageBitmap(bitmap)
        }
    }

    fun decodeSampledBitmapFromResource(
        res: Resources,
        resId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeResource(res, resId, this)
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}