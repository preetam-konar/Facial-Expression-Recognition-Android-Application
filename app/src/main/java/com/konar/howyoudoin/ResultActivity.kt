package com.konar.howyoudoin

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.konar.howyoudoin.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
        val bs = intent.getByteArrayExtra("byteArray")

        try {

            val bmpImg = bs?.let { BitmapFactory.decodeByteArray(bs, 0, it.size) }
            binding.ivImage.setImageBitmap(bmpImg)

        } catch (e: Exception) {
            Log.e("IMAGE", e.message.toString())
        }
         */

        val imgUri = Uri.parse(intent.getStringExtra("Selected Image Uri"))

        binding.ivImage.setImageURI(imgUri)


    }
}