package com.example.aimbot.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.aimbot.model.AimSettings
import com.example.aimbot.util.MotionCalculator
import java.util.concurrent.atomic.AtomicBoolean

/**
 * خدمة إمكانية الوصول لتحريك المؤشر
 * 
 * هذه الخدمة مسؤولة عن تحريك المؤشر تلقائياً نحو الأهداف المكتشفة.
 * تستخدم واجهة برمجة تطبيقات إمكانية الوصول لتنفيذ حركات اللمس على الشاشة.
 */
class AimAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "AimAccessibilityService"
        
        // الحد الأدنى للمسافة لتحريك المؤشر (بالبكسل)
        private const val MIN_MOVE_DISTANCE = 5
        
        // الفاصل الزمني بين حركات المؤشر (بالمللي ثانية)
        private const val MOVE_INTERVAL = 50L
        
        // مدة حركة المؤشر (بالمللي ثانية)
        private const val GESTURE_DURATION = 100L
        
        // عدد النقاط في مسار الحركة
        private const val PATH_POINTS = 10
    }
    
    // أداة حساب الحركة
    private val motionCalculator = MotionCalculator()
    
    // متغيرات الحالة
    private val isEnabled = AtomicBoolean(false)
    private val isMoving = AtomicBoolean(false)
    private var targetX = 0
    private var targetY = 0
    private var screenWidth = 0
    private var screenHeight = 0
    
    // إعدادات التصويب
    private var aimSettings: AimSettings = AimSettings()
    
    // هاندلر للتنفيذ المتأخر
    private val handler = Handler(Looper.getMainLooper())
    
    // مثيل واحد من الخدمة (Singleton)
    private var instance: AimAccessibilityService? = null
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
        
        // تخزين مثيل الخدمة
        instance = this
        
        // الحصول على أبعاد الشاشة
        val displayMetrics = resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Accessibility service destroyed")
        
        // إيقاف أي حركات قيد التنفيذ
        isEnabled.set(false)
        isMoving.set(false)
        handler.removeCallbacksAndMessages(null)
        
        // إزالة مثيل الخدمة
        instance = null
    }
    
    /**
     * تمكين/تعطيل خدمة تحريك المؤشر
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled.set(enabled)
        Log.d(TAG, "Aim service ${if (enabled) "enabled" else "disabled"}")
        
        if (!enabled) {
            // إيقاف أي حركات قيد التنفيذ
            isMoving.set(false)
            handler.removeCallbacksAndMessages(null)
        }
    }
    
    /**
     * تحديث موقع الهدف
     * 
     * @param x الإحداثي الأفقي للهدف
     * @param y الإحداثي الرأسي للهدف
     */
    fun updateTargetPosition(x: Int, y: Int) {
        // تحويل إحداثيات الهدف من دقة التقاط الشاشة إلى دقة الشاشة الكاملة
        val captureScale = 0.5f  // يجب أن يتطابق مع CAPTURE_SCALE في ScreenCaptureService
        
        targetX = (x / captureScale).toInt()
        targetY = (y / captureScale).toInt()
        
        // التحقق من صحة الإحداثيات
        targetX = targetX.coerceIn(0, screenWidth)
        targetY = targetY.coerceIn(0, screenHeight)
        
        // تحريك المؤشر إذا كانت الخدمة ممكّنة
        if (isEnabled.get() && !isMoving.get()) {
            movePointerToTarget()
        }
    }
    
    /**
     * تحريك المؤشر نحو الهدف
     */
    private fun movePointerToTarget() {
        if (!isEnabled.get() || isMoving.get()) {
            return
        }
        
        // الحصول على مركز الشاشة (موقع التصويب الحالي)
        val currentX = screenWidth / 2
        val currentY = screenHeight / 2
        
        // حساب المسافة إلى الهدف
        val dx = targetX - currentX
        val dy = targetY - currentY
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toInt()
        
        // تجاهل الحركات الصغيرة جداً
        if (distance < MIN_MOVE_DISTANCE) {
            return
        }
        
        // تعيين علامة الحركة
        isMoving.set(true)
        
        try {
            // حساب المسار والسرعة المناسبة
            val path = motionCalculator.calculatePath(
                currentX, currentY, targetX, targetY,
                aimSettings.speed,
                aimSettings.smoothness,
                PATH_POINTS
            )
            
            // إنشاء مسار الحركة
            val gestureBuilder = GestureDescription.Builder()
            val pathBuilder = Path()
            
            // إضافة نقاط المسار
            pathBuilder.moveTo(path[0].x, path[0].y)
            for (i in 1 until path.size) {
                pathBuilder.lineTo(path[i].x, path[i].y)
            }
            
            // إنشاء وصف الحركة
            val strokeDescription = GestureDescription.StrokeDescription(
                pathBuilder,
                0,
                GESTURE_DURATION
            )
            
            // إضافة الحركة إلى وصف الإيماءة
            val gesture = gestureBuilder
                .addStroke(strokeDescription)
                .build()
            
            // تنفيذ الحركة
            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    // إعادة تعيين علامة الحركة بعد اكتمال الحركة
                    isMoving.set(false)
                    
                    // جدولة الحركة التالية بعد فترة زمنية
                    if (isEnabled.get()) {
                        handler.postDelayed({
                            movePointerToTarget()
                        }, MOVE_INTERVAL)
                    }
                }
                
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    // إعادة تعيين علامة الحركة في حالة إلغاء الحركة
                    isMoving.set(false)
                }
            }, null)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error moving pointer", e)
            isMoving.set(false)
        }
    }
    
    /**
     * تحديث إعدادات التصويب
     */
    fun updateSettings(settings: AimSettings) {
        this.aimSettings = settings
        Log.d(TAG, "Aim settings updated: $settings")
    }
    
    /**
     * الحصول على مثيل الخدمة
     */
    fun getInstance(): AimAccessibilityService? {
        return instance
    }
}
