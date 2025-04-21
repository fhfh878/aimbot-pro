package com.example.aimbot.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import androidx.core.app.NotificationCompat
import com.example.aimbot.R
import com.example.aimbot.view.OverlayView

/**
 * خدمة العرض فوق الشاشة
 * 
 * هذه الخدمة مسؤولة عن عرض طبقة فوق الشاشة للتحكم في التطبيق أثناء تشغيل اللعبة.
 * توفر أزرار للتحكم في تشغيل/إيقاف ميزة التصويب التلقائي وضبط الإعدادات.
 */
class OverlayService : Service() {
    
    companion object {
        private const val TAG = "OverlayService"
        private const val NOTIFICATION_ID = 2
        private const val NOTIFICATION_CHANNEL_ID = "overlay_channel"
    }
    
    // مدير النوافذ
    private var windowManager: WindowManager? = null
    
    // طبقة العرض فوق الشاشة
    private var overlayView: View? = null
    
    // معلمات العرض
    private lateinit var params: WindowManager.LayoutParams
    
    // حالة التشغيل
    private var isAimEnabled = false
    
    // واجهة للتفاعل مع النشاط الرئيسي
    interface OverlayCallback {
        fun onToggleAim(enabled: Boolean)
        fun onOpenSettings()
    }
    
    private var callback: OverlayCallback? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Overlay service created")
        
        // الحصول على مدير النوافذ
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // إنشاء قناة الإشعارات (للأندرويد 8.0 وما فوق)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        
        // إنشاء إشعار الخدمة الأمامية
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Aimbot")
            .setContentText("طبقة التحكم نشطة")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        // بدء الخدمة في المقدمة
        startForeground(NOTIFICATION_ID, notification)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Overlay service started")
        
        // بدء عرض الطبقة فوق الشاشة
        showOverlay()
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Overlay service destroyed")
        
        // إيقاف عرض الطبقة فوق الشاشة
        hideOverlay()
    }
    
    /**
     * تعيين واجهة التفاعل مع النشاط الرئيسي
     */
    fun setCallback(callback: OverlayCallback) {
        this.callback = callback
    }
    
    /**
     * عرض الطبقة فوق الشاشة
     */
    private fun showOverlay() {
        if (overlayView != null) {
            return
        }
        
        try {
            // إنشاء طبقة العرض
            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_view, null)
            
            // إعداد معلمات العرض
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            
            // تعيين موقع الطبقة (في الزاوية العلوية اليمنى)
            params.gravity = Gravity.TOP or Gravity.END
            params.x = 16
            params.y = 100
            
            // إعداد أزرار التحكم
            setupControls()
            
            // إضافة الطبقة إلى النافذة
            windowManager?.addView(overlayView, params)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing overlay", e)
        }
    }
    
    /**
     * إخفاء الطبقة فوق الشاشة
     */
    private fun hideOverlay() {
        if (overlayView == null) {
            return
        }
        
        try {
            // إزالة الطبقة من النافذة
            windowManager?.removeView(overlayView)
            overlayView = null
            
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding overlay", e)
        }
    }
    
    /**
     * إعداد أزرار التحكم
     */
    private fun setupControls() {
        // زر تشغيل/إيقاف التصويب التلقائي
        val toggleButton = overlayView?.findViewById<Button>(R.id.overlayToggleButton)
        toggleButton?.setOnClickListener {
            isAimEnabled = !isAimEnabled
            
            // تغيير لون الزر حسب الحالة
            toggleButton.setBackgroundResource(
                if (isAimEnabled) R.drawable.ic_power_on else R.drawable.ic_power
            )
            
            // إرسال الحالة إلى النشاط الرئيسي
            callback?.onToggleAim(isAimEnabled)
        }
        
        // زر الإعدادات
        val settingsButton = overlayView?.findViewById<ImageButton>(R.id.overlaySettingsButton)
        settingsButton?.setOnClickListener {
            // فتح شاشة الإعدادات
            callback?.onOpenSettings()
        }
        
        // إعداد خاصية السحب للطبقة
        setupDragging()
    }
    
    /**
     * إعداد خاصية السحب للطبقة
     */
    private fun setupDragging() {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        
        overlayView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // تسجيل موقع اللمس الأولي
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                
                MotionEvent.ACTION_MOVE -> {
                    // حساب المسافة المسحوبة
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    
                    // تحديث موقع الطبقة
                    params.x = (initialX - dx).toInt()
                    params.y = (initialY + dy).toInt()
                    
                    // تطبيق الموقع الجديد
                    windowManager?.updateViewLayout(overlayView, params)
                    true
                }
                
                else -> false
            }
        }
    }
    
    /**
     * تحديث حالة التصويب التلقائي
     */
    fun updateAimStatus(enabled: Boolean) {
        isAimEnabled = enabled
        
        // تحديث حالة زر التشغيل/الإيقاف
        val toggleButton = overlayView?.findViewById<Button>(R.id.overlayToggleButton)
        toggleButton?.setBackgroundResource(
            if (isAimEnabled) R.drawable.ic_power_on else R.drawable.ic_power
        )
    }
}
