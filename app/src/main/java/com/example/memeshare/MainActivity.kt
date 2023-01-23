package com.example.memeshare

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.view.drawToBitmap
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.ViewTarget
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    var currentImageUrl : String? = null
    var image : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadMeme()
    }
    fun loadMeme(){
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val memeImage: ImageView = findViewById(R.id.memeImage)
        progressBar.visibility = View.VISIBLE
        val url = "https://meme-api.com/gimme"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                currentImageUrl = response.getString("url")


                Glide.with(this).load(currentImageUrl).listener(object: RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                }).into(memeImage)
            },
            Response.ErrorListener { error ->
                Toast.makeText(this,"Something went wrong",Toast.LENGTH_LONG).show()
            }
        )

// Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    fun shareMeme(view: View) {
        val memeImage: ImageView = findViewById(R.id.memeImage)
        val bitmapDrawable : BitmapDrawable = memeImage.drawable as BitmapDrawable
        val bitmap : Bitmap = bitmapDrawable.bitmap
        val uri: Uri = getImageToShare(bitmap)
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT,"Hey, checkout this cool meme I got from reddit $currentImageUrl")
        intent.putExtra(Intent.EXTRA_STREAM,uri)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setType("image/*")
        //val chooser = Intent.createChooser(intent,"Share this meme using...")
        startActivity(Intent.createChooser(intent, "Share image using"))
    }

    private fun getImageToShare(bitmap: Bitmap): Uri {
        val folder : File = File(cacheDir,"images")


        folder.mkdirs()
        val file: File = File(folder,"shared_image.jpg")
        val fileOutputStream: FileOutputStream = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        val uri: Uri = FileProvider.getUriForFile(applicationContext,"com.example.memeshare",file)
        return uri

    }

    fun nextMeme(view: View) {
        loadMeme()
    }

}