package com.example.aimbot.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * مساعد الاهتزاز
 * 
 * هذه الفئة مسؤولة عن إدارة اهتزاز الجهاز عند اكتشاف الأهداف.
 */
class VibrationHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "VibrationHelper"
        
        // أنماط الاهتزاز
        const val VIBRATION_PATTERN_TARGET_DETECTED = 0
        const val VIBRATION_PATTERN_TARGET_LOCKED = 1
        const val VIBRATION_PATTERN_NO_TARGET = 2
    }
    
    // كائن الاهتزاز
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    /**
     * التحقق مما إذا كان الاهتزاز متاحاً
     */
    fun hasVibrator(): Boolean {
        return vibrator.hasVibrator()
    }
    
    /**
     * اهتزاز بنمط محدد
     * 
     * @param pattern نمط الاهتزاز
     */
    fun vibrate(pattern: Int) {
        try {
            when (pattern) {
                VIBRATION_PATTERN_TARGET_DETECTED -> {
                    // اهتزاز قصير عند اكتشاف هدف
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(50)
                    }
                }
                
                VIBRATION_PATTERN_TARGET_LOCKED -> {
                    // اهتزاز مزدوج عند قفل الهدف
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 100, 50), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 50, 100, 50), -1)
                    }
                }
                
                VIBRATION_PATTERN_NO_TARGET -> {
                    // اهتزاز طويل عند فقدان الهدف
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(200)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating", e)
        }
    }
    
    /**
     * إيقاف الاهتزاز
     */
    fun cancel() {
        vibrator.cancel()
    }
}
