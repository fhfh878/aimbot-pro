package com.example.aimbot.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.aimbot.R
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * خدمة التقاط الشاشة
 * 
 * هذه الخدمة مسؤولة عن التقاط محتوى الشاشة أثناء تشغيل اللعبة باستخدام MediaProjection API.
 * تقوم بالتقاط الإطارات بشكل دوري وإرسالها إلى المستمع لمعالجتها.
 */
class ScreenCaptureService : Service() {
    
    companion object {
        private const val TAG = "ScreenCaptureService"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "screen_capture_channel"
        
        // معدل التقاط الإطارات (بالمللي ثانية)
        private const val CAPTURE_RATE = 50L
        
        // دقة التقاط الشاشة (نسبة من دقة الشاشة الأصلية)
        private const val CAPTURE_SCALE = 0.5f
    }
    
    // واجهة لإرسال الإطارات الملتقطة
    interface OnFrameCapturedListener {
        fun onFrameCaptured(bitmap: Bitmap)
    }
    
    // متغيرات MediaProjection
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    
    // متغيرات الخيط والمعالج
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null
    
    // متغيرات الشاشة
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var screenDensity: Int = 0
    private var captureWidth: Int = 0
    private var captureHeight: Int = 0
    
    // متغيرات الحالة
    private val isRunning = AtomicBoolean(false)
    private var frameCapturedListener: OnFrameCapturedListener? = null
    
    // Binder للاتصال بالخدمة
    private val binder = LocalBinder()
    
    inner class LocalBinder : Binder() {
        fun getService(): ScreenCaptureService = this@ScreenCaptureService
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        
        // الحصول على أبعاد الشاشة وكثافتها
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi
        
        // حساب أبعاد التقاط الشاشة
        captureWidth = (screenWidth * CAPTURE_SCALE).toInt()
        captureHeight = (screenHeight * CAPTURE_SCALE).toInt()
        
        // إنشاء خيط وهاندلر للتقاط الإطارات
        handlerThread = HandlerThread("ScreenCaptureThread")
        handlerThread?.start()
        handler = Handler(handlerThread?.looper!!)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        
        // إنشاء قناة الإشعارات (للأندرويد 8.0 وما فوق)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Screen Capture Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        
        // إنشاء إشعار الخدمة الأمامية
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Aimbot")
            .setContentText("جاري التقاط الشاشة...")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        // بدء الخدمة في المقدمة
        startForeground(NOTIFICATION_ID, notification)
        
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind")
        return binder
    }
    
    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        stopCapture()
        super.onDestroy()
    }
    
    /**
     * تعيين مستمع للإطارات الملتقطة
     */
    fun setOnFrameCapturedListener(listener: OnFrameCapturedListener) {
        frameCapturedListener = listener
    }
    
    /**
     * بدء التقاط الشاشة
     * 
     * @param resultCode كود النتيجة من نشاط طلب إذن التقاط الشاشة
     * @param data بيانات النية من نشاط طلب إذن التقاط الشاشة
     */
    fun startCapture(resultCode: Int, data: Intent) {
        if (isRunning.get()) {
            Log.d(TAG, "Capture already running")
            return
        }
        
        Log.d(TAG, "Starting screen capture")
        
        // إنشاء MediaProjection
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        
        // إنشاء ImageReader
        imageReader = ImageReader.newInstance(
            captureWidth, captureHeight,
            PixelFormat.RGBA_8888, 2
        )
        
        // إنشاء VirtualDisplay
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            captureWidth, captureHeight, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )
        
        // تعيين مستمع للصور
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                try {
                    val bitmap = imageToBitmap(image)
                    frameCapturedListener?.onFrameCaptured(bitmap)
                } finally {
                    image.close()
                }
            }
        }, handler)
        
        // بدء التقاط الإطارات بشكل دوري
        isRunning.set(true)
        scheduleCaptureFrame()
    }
    
    /**
     * إيقاف التقاط الشاشة
     */
    fun stopCapture() {
        if (!isRunning.get()) {
            return
        }
        
        Log.d(TAG, "Stopping screen capture")
        
        isRunning.set(false)
        
        // إيقاف التقاط الإطارات
        handler?.removeCallbacksAndMessages(null)
        
        // تحرير الموارد
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }
    
    /**
     * جدولة التقاط الإطار التالي
     */
    private fun scheduleCaptureFrame() {
        if (!isRunning.get()) {
            return
        }
        
        handler?.postDelayed({
            captureFrame()
            scheduleCaptureFrame()
        }, CAPTURE_RATE)
    }
    
    /**
     * التقاط إطار من الشاشة
     */
    private fun captureFrame() {
        if (!isRunning.get()) {
            return
        }
        
        try {
            // لا نحتاج لفعل أي شيء هنا، لأن مستمع ImageReader سيتم استدعاؤه تلقائياً
            // عند توفر صورة جديدة
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing frame", e)
        }
    }
    
    /**
     * تحويل Image إلى Bitmap
     */
    private fun imageToBitmap(image: Image): Bitmap {
        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        
        // إنشاء Bitmap من البيانات
        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height, Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        
        // اقتصاص Bitmap إلى الحجم الصحيح
        return Bitmap.createBitmap(bitmap, 0, 0, width, height)
    }
}
