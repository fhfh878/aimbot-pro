package com.example.aimbot

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aimbot.model.AimSettings
import com.example.aimbot.model.ColorDetectionSettings
import com.example.aimbot.model.GameProfile
import com.example.aimbot.util.PreferenceManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * اختبار وحدة لمدير التفضيلات
 */
@RunWith(AndroidJUnit4::class)
class PreferenceManagerTest {
    
    private lateinit var context: Context
    private lateinit var preferenceManager: PreferenceManager
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        preferenceManager = PreferenceManager(context)
        
        // مسح التفضيلات قبل كل اختبار
        val sharedPreferences = context.getSharedPreferences("aimbot_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
    
    @After
    fun tearDown() {
        // مسح التفضيلات بعد كل اختبار
        val sharedPreferences = context.getSharedPreferences("aimbot_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
    
    @Test
    fun testSaveAndGetAimSettings() {
        // إنشاء إعدادات تصويب للاختبار
        val testSettings = AimSettings(
            speed = 0.75f,
            smoothness = 0.6f,
            aimMode = AimSettings.AIM_MODE_TRACKING,
            headShotEnabled = true,
            recoilCompensationEnabled = true
        )
        
        // حفظ الإعدادات
        preferenceManager.saveAimSettings(testSettings)
        
        // استرجاع الإعدادات
        val retrievedSettings = preferenceManager.getAimSettings()
        
        // التحقق من تطابق الإعدادات
        assertEquals(testSettings.speed, retrievedSettings.speed, 0.001f)
        assertEquals(testSettings.smoothness, retrievedSettings.smoothness, 0.001f)
        assertEquals(testSettings.aimMode, retrievedSettings.aimMode)
        assertEquals(testSettings.headShotEnabled, retrievedSettings.headShotEnabled)
        assertEquals(testSettings.recoilCompensationEnabled, retrievedSettings.recoilCompensationEnabled)
    }
    
    @Test
    fun testSaveAndGetColorDetectionSettings() {
        // إنشاء إعدادات اكتشاف الألوان للاختبار
        val testSettings = ColorDetectionSettings(
            targetColor = 0xFF0000,
            hueRange = 15,
            minSaturation = 150,
            minValue = 150,
            sensitivity = 0.8f
        )
        
        // حفظ الإعدادات
        preferenceManager.saveColorDetectionSettings(testSettings)
        
        // استرجاع الإعدادات
        val retrievedSettings = preferenceManager.getColorDetectionSettings()
        
        // التحقق من تطابق الإعدادات
        assertEquals(testSettings.targetColor, retrievedSettings.targetColor)
        assertEquals(testSettings.hueRange, retrievedSettings.hueRange)
        assertEquals(testSettings.minSaturation, retrievedSettings.minSaturation)
        assertEquals(testSettings.minValue, retrievedSettings.minValue)
        assertEquals(testSettings.sensitivity, retrievedSettings.sensitivity, 0.001f)
    }
    
    @Test
    fun testSaveAndGetGameProfiles() {
        // إنشاء قائمة ملفات تعريف للاختبار
        val testProfiles = listOf(
            GameProfile(
                name = "Test Profile 1",
                packageName = "com.test.game1",
                isHeadshotModeSupported = true
            ),
            GameProfile(
                name = "Test Profile 2",
                packageName = "com.test.game2",
                isRecoilCompensationSupported = true
            )
        )
        
        // حفظ ملفات التعريف
        preferenceManager.saveGameProfiles(testProfiles)
        
        // استرجاع ملفات التعريف
        val retrievedProfiles = preferenceManager.getGameProfiles()
        
        // التحقق من عدد ملفات التعريف
        assertEquals(testProfiles.size, retrievedProfiles.size)
        
        // التحقق من تطابق ملفات التعريف
        for (i in testProfiles.indices) {
            assertEquals(testProfiles[i].name, retrievedProfiles[i].name)
            assertEquals(testProfiles[i].packageName, retrievedProfiles[i].packageName)
            assertEquals(testProfiles[i].isHeadshotModeSupported, retrievedProfiles[i].isHeadshotModeSupported)
            assertEquals(testProfiles[i].isRecoilCompensationSupported, retrievedProfiles[i].isRecoilCompensationSupported)
        }
    }
    
    @Test
    fun testSaveAndGetCurrentProfile() {
        // إنشاء ملفات تعريف للاختبار
        val testProfiles = listOf(
            GameProfile(
                name = "Test Profile 1",
                packageName = "com.test.game1"
            ),
            GameProfile(
                name = "Test Profile 2",
                packageName = "com.test.game2"
            )
        )
        
        // حفظ ملفات التعريف
        preferenceManager.saveGameProfiles(testProfiles)
        
        // حفظ ملف التعريف الحالي
        preferenceManager.saveCurrentProfile("Test Profile 2")
        
        // استرجاع ملف التعريف الحالي
        val retrievedProfile = preferenceManager.getCurrentProfile()
        
        // التحقق من تطابق ملف التعريف
        assertEquals("Test Profile 2", retrievedProfile.name)
        assertEquals("com.test.game2", retrievedProfile.packageName)
    }
    
    @Test
    fun testDefaultProfiles() {
        // استرجاع ملفات التعريف الافتراضية
        val defaultProfiles = preferenceManager.getGameProfiles()
        
        // التحقق من وجود ملفات التعريف الافتراضية
        assertNotNull(defaultProfiles)
        assertEquals(3, defaultProfiles.size)
        
        // التحقق من وجود ملف تعريف PUBG Mobile
        val pubgProfile = defaultProfiles.find { it.name == "PUBG Mobile" }
        assertNotNull(pubgProfile)
        assertEquals("com.tencent.ig", pubgProfile?.packageName)
        
        // التحقق من وجود ملف تعريف Free Fire
        val freeFireProfile = defaultProfiles.find { it.name == "Free Fire" }
        assertNotNull(freeFireProfile)
        assertEquals("com.dts.freefireth", freeFireProfile?.packageName)
        
        // التحقق من وجود ملف تعريف Call of Duty Mobile
        val codProfile = defaultProfiles.find { it.name == "Call of Duty Mobile" }
        assertNotNull(codProfile)
        assertEquals("com.activision.callofduty.shooter", codProfile?.packageName)
    }
    
    @Test
    fun testFirstRun() {
        // التحقق من أن التطبيق في وضع التشغيل الأول افتراضياً
        assertEquals(true, preferenceManager.isFirstRun())
        
        // تعيين أن التطبيق قد تم تشغيله
        preferenceManager.setFirstRunCompleted()
        
        // التحقق من أن التطبيق ليس في وضع التشغيل الأول بعد الآن
        assertEquals(false, preferenceManager.isFirstRun())
    }
    
    @Test
    fun testAutoStart() {
        // التحقق من أن التشغيل التلقائي معطل افتراضياً
        assertEquals(false, preferenceManager.getAutoStart())
        
        // تمكين التشغيل التلقائي
        preferenceManager.saveAutoStart(true)
        
        // التحقق من أن التشغيل التلقائي ممكّن
        assertEquals(true, preferenceManager.getAutoStart())
    }
    
    @Test
    fun testOverlayEnabled() {
        // التحقق من أن طبقة العرض فوق الشاشة ممكّنة افتراضياً
        assertEquals(true, preferenceManager.getOverlayEnabled())
        
        // تعطيل طبقة العرض فوق الشاشة
        preferenceManager.saveOverlayEnabled(false)
        
        // التحقق من أن طبقة العرض فوق الشاشة معطلة
        assertEquals(false, preferenceManager.getOverlayEnabled())
    }
    
    @Test
    fun testVibrationEnabled() {
        // التحقق من أن الاهتزاز معطل افتراضياً
        assertEquals(false, preferenceManager.getVibrationEnabled())
        
        // تمكين الاهتزاز
        preferenceManager.saveVibrationEnabled(true)
        
        // التحقق من أن الاهتزاز ممكّن
        assertEquals(true, preferenceManager.getVibrationEnabled())
    }
}
