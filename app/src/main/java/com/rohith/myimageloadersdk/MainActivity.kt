package com.rohith.myimageloadersdk

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.rohith.photon.core.Photon

class MainActivity : AppCompatActivity() {
    private lateinit var imageLoader: Photon
    val URL1 = "https://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/1000MR0044631300503690E01_DXXX.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val image1 = findViewById<ImageView>(R.id.image1)
        //val listBtn = findViewById<Button>(R.id.listBtn)
        val clearBtn = findViewById<Button>(R.id.clearBtn)

        imageLoader = Photon.getInstance(this, CACHE_SIZE) //4MiB
        imageLoader.displayImage(URL1, image1, R.drawable.loading_animation)

        clearBtn.setOnClickListener {
            imageLoader.clearcache()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Photon.getInstance(this).cancelAll()
    }
}

const val CACHE_SIZE = 10 * 1024 * 1024