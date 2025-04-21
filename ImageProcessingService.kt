package com.example.aimbot.service

import android.graphics.Bitmap
import android.util.Log
import com.example.aimbot.model.ColorDetectionSettings
import com.example.aimbot.util.ColorDetector
import com.example.aimbot.util.Target
import org.opencv.android.OpenCVLoader

/**
 * خدمة معالجة الصور
 * 
 * هذه الخدمة مسؤولة عن تحليل الصور الملتقطة واكتشاف الأهداف باستخدام OpenCV.
 * تستقبل الإطارات من خدمة التقاط الشاشة وتحللها لاكتشاف الأهداف وإرسالها إلى المستمع.
 */
class ImageProcessingService {
    
    companion object {
        private const val TAG = "ImageProcessingService"
        
        // تهيئة OpenCV
        init {
            if (!OpenCVLoader.initDebug()) {
                Log.e(TAG, "OpenCV initialization failed")
            } else {
                Log.d(TAG, "OpenCV initialization succeeded")
            }
        }
    }
    
    // أدوات اكتشاف الأهداف
    private val colorDetector = ColorDetector()
    
    // إعدادات اكتشاف الألوان
    private var settings: ColorDetectionSettings = ColorDetectionSettings()
    
    // واجهة لإرسال الأهداف المكتشفة
    interface OnTargetDetectedListener {
        fun onTargetDetected(targetX: Int, targetY: Int, confidence: Float)
        fun onNoTargetDetected()
    }
    
    private var targetDetectedListener: OnTargetDetectedListener? = null
    
    /**
     * تعيين مستمع للأهداف المكتشفة
     */
    fun setOnTargetDetectedListener(listener: OnTargetDetectedListener) {
        targetDetectedListener = listener
    }
    
    /**
     * تحليل الإطار واكتشاف الأهداف
     * 
     * @param bitmap الإطار المراد تحليله
     */
    fun processFrame(bitmap: Bitmap) {
        try {
            // اكتشاف الأهداف في الصورة
            val targets = colorDetector.detectTargets(bitmap)
            
            if (targets.isEmpty()) {
                // لم يتم اكتشاف أي هدف
                targetDetectedListener?.onNoTargetDetected()
                return
            }
            
            // اختيار الهدف الأفضل بناءً على الأولوية
            val bestTarget = selectBestTarget(targets, bitmap.width, bitmap.height)
            
            // إرسال الهدف إلى المستمع
            targetDetectedListener?.onTargetDetected(
                bestTarget.x,
                bestTarget.y,
                bestTarget.confidence.toFloat()
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame", e)
            targetDetectedListener?.onNoTargetDetected()
        }
    }
    
    /**
     * اختيار الهدف الأفضل من بين الأهداف المكتشفة
     * 
     * @param targets قائمة الأهداف المكتشفة
     * @param screenWidth عرض الشاشة
     * @param screenHeight ارتفاع الشاشة
     * @return الهدف الأفضل
     */
    private fun selectBestTarget(targets: List<Target>, screenWidth: Int, screenHeight: Int): Target {
        // حساب مركز الشاشة
        val centerX = screenWidth / 2
        val centerY = screenHeight / 2
        
        // اختيار الهدف بناءً على استراتيجية الاختيار
        return when (settings.targetSelectionStrategy) {
            ColorDetectionSettings.TARGET_SELECTION_NEAREST_TO_CROSSHAIR -> {
                // اختيار الهدف الأقرب إلى مركز الشاشة (التصويب)
                targets.minByOrNull { 
                    val dx = it.x - centerX
                    val dy = it.y - centerY
                    dx * dx + dy * dy  // مربع المسافة
                } ?: targets[0]
            }
            
            ColorDetectionSettings.TARGET_SELECTION_HIGHEST_CONFIDENCE -> {
                // اختيار الهدف ذو نسبة الثقة الأعلى
                targets.maxByOrNull { it.confidence } ?: targets[0]
            }
            
            ColorDetectionSettings.TARGET_SELECTION_NEAREST_TO_TOP -> {
                // اختيار الهدف الأقرب إلى أعلى الشاشة (للتصويب على الرأس)
                targets.minByOrNull { it.y } ?: targets[0]
            }
            
            else -> {
                // الاختيار الافتراضي: الأقرب إلى مركز الشاشة
                targets.minByOrNull { 
                    val dx = it.x - centerX
                    val dy = it.y - centerY
                    dx * dx + dy * dy
                } ?: targets[0]
            }
        }
    }
    
    /**
     * تحديث إعدادات اكتشاف الألوان
     */
    fun updateSettings(settings: ColorDetectionSettings) {
        this.settings = settings
        
        // تحديث إعدادات اكتشاف الألوان
        colorDetector.updateSettings(
            settings.targetColor,
            settings.hueRange,
            settings.minSaturation,
            settings.minValue,
            settings.blurSize,
            settings.minContourArea
        )
        
        Log.d(TAG, "Settings updated: $settings")
    }
    
    /**
     * تحرير الموارد
     */
    fun release() {
        colorDetector.release()
    }
}
