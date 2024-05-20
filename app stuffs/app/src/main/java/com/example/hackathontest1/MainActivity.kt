package com.example.hackathontest1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.OutputStream
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Instrumentation
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.hackathontest1.databinding.ActivityMainBinding
import com.example.hackathontest1.ml.Model2
import com.example.hackathontest1.ml.ModelUnquant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

//import kotlin.coroutines.jvm.internal.CompletedContinuation.context


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var tvOutput: TextView
    private val GALLERY_REQUEST_CODE = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        imageView = binding.imageView
        button = binding.btnCaptureImage
        tvOutput = binding.tvOutput
        val buttonLoad = binding.loadImage

        button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            {
                takePicturePreview.launch(null)
            } else {
                requestPermission.launch(Manifest.permission.CAMERA)
            }
        }
        buttonLoad.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                onresult.launch(intent)
            }
            else {
                requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        //Output

    }


    //Request camera permission
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {granted->
        if(granted) {
            takePicturePreview.launch(null)
        }
        else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    //launch camera and take picture
    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {bitmap->
        if(bitmap!=null) {
            imageView.setImageBitmap(bitmap)
            outputGenerator(bitmap)
        }
    }

    //to get image from gallery
    private val onresult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result->
        Log.i("TAG", "This is the result: ${result.data} ${result.resultCode}")
        onResultReceived(GALLERY_REQUEST_CODE, result)
    }

    private fun onResultReceived(requestCode: Int, result: ActivityResult?) {
        when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                if (result?.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let{uri ->
                        Log.i("TAG", "onResultReceived: $uri")
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                        imageView.setImageBitmap(bitmap)
                        outputGenerator(bitmap)
                    }
                } else {
                    Log.e("TAG", "onActivityResult error in selecting image")
                }
            }
        }
    }
//working
//    private fun outputGenerator(bitmap: Bitmap) {
//        val model = ModelUnquant.newInstance(this)
//try {
//// Creates inputs for reference.
//        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
//        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
//        byteBuffer.order(ByteOrder.nativeOrder())
//        val intValues = IntArray(224 * 224)
//        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
//
//        var pixel = 0
//        for (i in 0 until 224) {
//            for (j in 0 until 224) {
//                val value = intValues[pixel++]
//                byteBuffer.putFloat(((value shr 16) and 0xFF).toFloat() * (1.0f / 255.0f))
//                byteBuffer.putFloat(((value shr 8) and 0xFF).toFloat() * (1.0f / 255.0f))
//                byteBuffer.putFloat((value and 0xFF).toFloat() * (1.0f / 255.0f))
//            }
//        }
//        inputFeature0.loadBuffer(byteBuffer)
//
//// Runs model inference and gets result.
//        val outputs = model.process(inputFeature0)
//        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//
//        val confidences = outputFeature0.getFloatArray()
//        var maxPos = 0
//        var maxConf = 0.0f
//
//        for (i in confidences.indices) {
//            if (confidences[i] > maxConf) {
//                maxConf = confidences[i]
//                maxPos = i
//            }
//        }
//
//        val classes = arrayOf("Potato_Early_Blight", "Potato_Healthy", "Potato_Late_Blight")
//        //tvOutput.text = classes[maxPos]
//        var s : String = ""
//        for(i in classes.indices) {
//            s+=String.format("${classes[i]} : ${confidences[i]*100} ")
//        }
//        tvOutput.text = s
//
//// Releases model resources if no longer used.
//} catch (e: Exception) {
//            e.printStackTrace()
//            // Handle any errors that occur during inference
//            Log.e("ModelInference", "Error during inference: ${e.message}")
//        } finally {
//            // Release model resources
//            model.close()
//        }
//working


    //@SuppressLint("SetTextI18n")
//    private fun outputGenerator(bitmap: Bitmap) {
//        val model = Model2.newInstance(this)
//
//
//        try {
//            // Preprocess the input image to match the model's input format
//            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
//            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true)
//            val byteBuffer = ByteBuffer.allocateDirect(4 * 256 * 256 * 3)
//            resizedBitmap?.let {
//                it.copyPixelsToBuffer(byteBuffer)
//                byteBuffer.rewind()
//                inputFeature0.loadBuffer(byteBuffer)
//            }
//
//            // Run model inference and get results
//            val outputs = model.process(inputFeature0)
//            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//
////             Process the inference results here
////             You can access the results in 'outputFeature0' and perform any required post-processing
////
////             Example: Log the output values
//            val resultString = outputFeature0.floatArray.joinToString(", ")
//            //val res = outputFeature0.floatArray[0]
//            Log.d("ModelInference", "Model output: $resultString")
//
//            // You can update UI elements or perform further actions based on the inference results
//            tvOutput.text = "Model output: $resultString"
//
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // Handle any errors that occur during inference
//            Log.e("ModelInference", "Error during inference: ${e.message}")
//        } finally {
//            // Release model resources
//            model.close()
//        }
//    }

    private fun outputGenerator(bitmap: Bitmap) {
        var labels = application.assets.open("labels.txt").bufferedReader().readLines()
        var imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(256,256, ResizeOp.ResizeMethod.BILINEAR))
            .build()
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)
        val model = Model2.newInstance(this)
            // Preprocess the input image to match the model's input format
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(tensorImage.buffer)

//
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        var maxIdx = 0
        outputFeature0.forEachIndexed { index, fl ->
            if(outputFeature0[maxIdx] < fl){
                maxIdx = index
            }
        }
        tvOutput.setText(labels[maxIdx])
/*    val confidences = outputFeature0.getFloatArray()
    var maxPos = 0
    var maxConf = 0.0f

    for (i in confidences.indices) {
        if (confidences[i] > maxConf) {
            maxConf = confidences[i]
            maxPos = i
        }
    }

    val classes = arrayOf("Potato_Early_Blight", "Potato_Healthy", "Potato_Late_Blight")
    //tvOutput.text = classes[maxPos]
    var s : String = ""
    for(i in classes.indices) {
        s+=String.format("${classes[i]} : ${confidences[i]*100} ")
    }
    tvOutput.text = s */
//            // Run model inference and get results
//            val outputs = model.process(inputFeature0)
//            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//
//            // Debugging: Print the input and output values
//            val inputString = inputFeature0.floatArray.joinToString(", ")
//            val resultString = outputFeature0.floatArray.joinToString(", ")
//            Log.d("ModelInference", "Input: ${inputString}")
//            Log.d("ModelInference", "Model output: ${resultString.toString()}")
//
//            // You can update UI elements or perform further actions based on the inference results
//            tvOutput.text = "${resultString.toString()}"
            model.close()
    }
//    }

}

