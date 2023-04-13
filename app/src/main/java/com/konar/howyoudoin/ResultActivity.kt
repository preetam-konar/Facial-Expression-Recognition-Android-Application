package com.konar.howyoudoin

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.konar.howyoudoin.databinding.ActivityResultBinding
import com.konar.howyoudoin.ml.Model300
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
//    private var labels = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        try {
//            val bufferedReader = BufferedReader(InputStreamReader(assets.open("labels.text")))
//            var line = bufferedReader.readLine()
//            while (line != null) {
//                labels.add(line)
//                line = bufferedReader.readLine()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

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
        val bitmap = binding.ivImage.drawable.toBitmap()

        predict(bitmap)

        binding.btnTryAgain.setOnClickListener {
            predict(bitmap)
        }

//        binding.btnTryAgain.setOnClickListener {
//            predict(bitmap)
//        }
    }

    private fun predict(bitmap: Bitmap) {

        val model = Model300.newInstance(this@ResultActivity)
        val labels = arrayOf(
            "Fear",
            "Angry",
            "Neutral",
            "Sad",
            "Fear",
            "Happy",
            "Surprise"
        )

// Creates inputs for reference.
        try {

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(48, 48, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(NormalizeOp(0f, 255f))
                .build()

            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)


// Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 48, 48, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(tensorImage.buffer)

// Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

// Releases model resources if no longer used.
            model.close()

            val index = outputFeature0.floatArray.maxOrNull()?.toInt()

            if (index != null) {
                binding.tvResult.text = labels[index].toString()
            } else {
                binding.tvResult.text = "No results found!!"
            }
            Log.e("Inp Shape", inputFeature0.buffer.toString())
            Log.e("Shape", tensorImage.buffer.toString())


// Releases model resources if no longer used.
            model.close()
        } catch (e: Exception) {
            Log.e("Preetam", e.message.toString())

        }
    }
}