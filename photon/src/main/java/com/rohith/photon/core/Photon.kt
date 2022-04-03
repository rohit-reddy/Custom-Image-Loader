package com.rohith.photon.core

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.rohith.photon.async.DownloadImageTask
import com.rohith.photon.async.DownloadTask
import com.rohith.photon.cache.CacheRepository
import com.rohith.photon.cache.Config
import java.util.concurrent.Executors
import java.util.concurrent.Future

class Photon private constructor(context: Context, cacheSize: Int) {
    private val cache = CacheRepository(context, cacheSize)
    private val executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private val mRunningDownloadList:HashMap<String, Future<Bitmap?>> = hashMapOf()

    fun displayImage(url: String, imageview: ImageView, placeholder:
    Int) {
        var bitmap = cache.get(url)
        bitmap?.let {
            imageview.setImageBitmap(it)
            return
        }
            ?: run {
                imageview.tag = url
                if (placeholder != null)
                    imageview.setImageResource(placeholder)
                addDownloadImageTask( url, DownloadImageTask(url , imageview , cache)) }

    }

    fun addDownloadImageTask(url: String,downloadTask: DownloadTask<Bitmap?>) {

        mRunningDownloadList.put(url,executorService.submit(downloadTask))
    }

    fun clearcache() {
        cache.clear()
    }

    fun cancelTask(url: String){
        synchronized(this){
            mRunningDownloadList.forEach {
                if (it.key == url && !it.value.isDone)
                    it.value.cancel(true)
            }
        }
    }

    fun cancelAll() {
        synchronized (this) {
            mRunningDownloadList.forEach{
                if ( !it.value.isDone)
                    it.value.cancel(true)
            }
            mRunningDownloadList.clear()
        }
    }

    companion object {
        private val INSTANCE: Photon? = null
        @Synchronized
        fun getInstance(context: Context, cacheSize: Int = Config.defaultCacheSize): Photon {
            return INSTANCE?.let { return INSTANCE }
                ?: run {
                    return Photon(context, cacheSize)
                }
        }
    }
}