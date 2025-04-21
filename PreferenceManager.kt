package com.example.aimbot.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.aimbot.model.AimSettings
import com.example.aimbot.model.ColorDetectionSettings
import com.example.aimbot.model.GameProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * مدير التفضيلات
 * 
 * هذه الفئة مسؤولة عن حفظ واسترجاع إعدادات التطبيق وملفات تعريف الألعاب.
 */
class PreferenceManager(context: Context) {
    
    companion object {
        private const val TAG = "PreferenceManager"
        private const val PREF_NAME = "aimbot_preferences"
        
        // مفاتيح التفضيلات
        private const val KEY_AIM_SETTINGS = "aim_settings"
        private const val KEY_COLOR_DETECTION_SETTINGS = "color_detection_settings"
        private const val KEY_GAME_PROFILES = "game_profiles"
        private const val KEY_CURRENT_PROFILE = "current_profile"
        private const val KEY_FIRST_RUN = "first_run"
        private const val KEY_AUTO_START = "auto_start"
        private const val KEY_OVERLAY_ENABLED = "overlay_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    }
    
    // كائن التفضيلات المشتركة
    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    // محول Gson لتحويل الكائنات إلى JSON والعكس
    private val gson = Gson()
    
    /**
     * حفظ إعدادات التصويب
     */
    fun saveAimSettings(settings: AimSettings) {
        val json = gson.toJson(settings)
        preferences.edit().putString(KEY_AIM_SETTINGS, json).apply()
        Log.d(TAG, "Aim settings saved")
    }
    
    /**
     * استرجاع إعدادات التصويب
     */
    fun getAimSettings(): AimSettings {
        val json = preferences.getString(KEY_AIM_SETTINGS, null)
        return if (json != null) {
            try {
                gson.fromJson(json, AimSettings::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing aim settings", e)
                AimSettings()
            }
        } else {
            AimSettings()
        }
    }
    
    /**
     * حفظ إعدادات اكتشاف الألوان
     */
    fun saveColorDetectionSettings(settings: ColorDetectionSettings) {
        val json = gson.toJson(settings)
        preferences.edit().putString(KEY_COLOR_DETECTION_SETTINGS, json).apply()
        Log.d(TAG, "Color detection settings saved")
    }
    
    /**
     * استرجاع إعدادات اكتشاف الألوان
     */
    fun getColorDetectionSettings(): ColorDetectionSettings {
        val json = preferences.getString(KEY_COLOR_DETECTION_SETTINGS, null)
        return if (json != null) {
            try {
                gson.fromJson(json, ColorDetectionSettings::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing color detection settings", e)
                ColorDetectionSettings()
            }
        } else {
            ColorDetectionSettings()
        }
    }
    
    /**
     * حفظ ملفات تعريف الألعاب
     */
    fun saveGameProfiles(profiles: List<GameProfile>) {
        val json = gson.toJson(profiles)
        preferences.edit().putString(KEY_GAME_PROFILES, json).apply()
        Log.d(TAG, "Game profiles saved: ${profiles.size} profiles")
    }
    
    /**
     * استرجاع ملفات تعريف الألعاب
     */
    fun getGameProfiles(): List<GameProfile> {
        val json = preferences.getString(KEY_GAME_PROFILES, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<GameProfile>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing game profiles", e)
                createDefaultProfiles()
            }
        } else {
            createDefaultProfiles()
        }
    }
    
    /**
     * حفظ ملف التعريف الحالي
     */
    fun saveCurrentProfile(profileName: String) {
        preferences.edit().putString(KEY_CURRENT_PROFILE, profileName).apply()
        Log.d(TAG, "Current profile saved: $profileName")
    }
    
    /**
     * استرجاع ملف التعريف الحالي
     */
    fun getCurrentProfile(): GameProfile {
        val profileName = preferences.getString(KEY_CURRENT_PROFILE, null)
        val profiles = getGameProfiles()
        
        return if (profileName != null) {
            profiles.find { it.name == profileName } ?: profiles.firstOrNull() ?: GameProfile.createPubgMobileProfile()
        } else {
            profiles.firstOrNull() ?: GameProfile.createPubgMobileProfile()
        }
    }
    
    /**
     * التحقق مما إذا كان هذا هو التشغيل الأول للتطبيق
     */
    fun isFirstRun(): Boolean {
        return preferences.getBoolean(KEY_FIRST_RUN, true)
    }
    
    /**
     * تعيين أن التطبيق قد تم تشغيله
     */
    fun setFirstRunCompleted() {
        preferences.edit().putBoolean(KEY_FIRST_RUN, false).apply()
    }
    
    /**
     * حفظ إعداد التشغيل التلقائي
     */
    fun saveAutoStart(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_START, enabled).apply()
    }
    
    /**
     * استرجاع إعداد التشغيل التلقائي
     */
    fun getAutoStart(): Boolean {
        return preferences.getBoolean(KEY_AUTO_START, false)
    }
    
    /**
     * حفظ إعداد تمكين طبقة العرض فوق الشاشة
     */
    fun saveOverlayEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
    }
    
    /**
     * استرجاع إعداد تمكين طبقة العرض فوق الشاشة
     */
    fun getOverlayEnabled(): Boolean {
        return preferences.getBoolean(KEY_OVERLAY_ENABLED, true)
    }
    
    /**
     * حفظ إعداد تمكين الاهتزاز
     */
    fun saveVibrationEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }
    
    /**
     * استرجاع إعداد تمكين الاهتزاز
     */
    fun getVibrationEnabled(): Boolean {
        return preferences.getBoolean(KEY_VIBRATION_ENABLED, false)
    }
    
    /**
     * إنشاء ملفات تعريف افتراضية
     */
    private fun createDefaultProfiles(): List<GameProfile> {
        return listOf(
            GameProfile.createPubgMobileProfile(),
            GameProfile.createFreeFireProfile(),
            GameProfile.createCodMobileProfile()
        )
    }
}
