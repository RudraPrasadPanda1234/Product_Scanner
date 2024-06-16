package com.example.productscanner

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: Button
    private lateinit var imageCapture: ImageCapture
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var retrofit: Retrofit
    private lateinit var cameraExecutor: ExecutorService

    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA
// android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
// android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btn_capture)
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()
        cameraExecutor = Executors.newSingleThreadExecutor()
        retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        btnCapture.setOnClickListener {
            capturePhoto()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // Ensure this line is present
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Toast.makeText(this, "Use case binding failed", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    // Convert ImageProxy to Bitmap
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    processPhoto(bitmap)
                    Toast.makeText(this@CameraActivity, "Photo captured successfully", Toast.LENGTH_SHORT).show()
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Photo capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
// private fun uploadPhotoToFirebase(uri: Uri) {
// val storageRef = storage.reference.child("images/${uri.lastPathSegment}")
// storageRef.putFile(uri)
// .addOnSuccessListener {
// Toast.makeText(this, "Photo uploaded to firebase storage successfully", Toast.LENGTH_SHORT).show()
// storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
// processPhoto(downloadUrl.toString())
// }
// }
// .addOnFailureListener {
// Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
// }
// }

    private fun processPhoto(bitmap: Bitmap) {
        // Convert Bitmap to Base64
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()
        val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        // Create InlineData, Part, Content, and GeminiProRequest objects
        val inlineData = InlineData(base64Image, "image/jpeg")
        val part = Part(inlineData, "give me pure and only json response of attached image in format of { product_name : String, description : String, color : String, pattern : String}")
        val content = Content(listOf(part))
        val request = GeminiProRequest(listOf(content))
        // Send Base64 image to Gemini service
        val service = retrofit.create(GeminiProService::class.java)
        // Log the request body
        Log.d("GeminiRequest", "Sending image to Gemini: ${request}") //

        service.processImage(request).enqueue(object : Callback<GeminiProResponse> {
            override fun onResponse(call: Call<GeminiProResponse>, response: Response<GeminiProResponse>) {
                if (response.isSuccessful) {
                    val productData = response.body()
                    if (productData != null) {
                        saveProductDataToFirestore(productData)
                    } else {
                        Toast.makeText(this@CameraActivity, "Gemini response body is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@CameraActivity, "Gemini request failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GeminiProResponse>, t: Throwable) {
                Toast.makeText(this@CameraActivity, "Gemini request failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProductDataToFirestore(productData: GeminiProResponse) {
        firestore.collection("products")
            .add(productData)
            .addOnSuccessListener {
                Toast.makeText(this, "Product data saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save product data", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}