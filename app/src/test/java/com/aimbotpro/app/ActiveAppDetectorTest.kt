package com.example.aimbot

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aimbot.model.GameProfile
import com.example.aimbot.util.ActiveAppDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * اختبار وحدة لكاشف التطبيقات النشطة
 */
@RunWith(AndroidJUnit4::class)
class ActiveAppDetectorTest {
    
    private lateinit var context: Context
    private lateinit var activeAppDetector: ActiveAppDetector
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        activeAppDetector = ActiveAppDetector(context)
    }
    
    @Test
    fun testActiveAppListener() {
        // إنشاء مستمع وهمي
        val mockListener = mock(ActiveAppDetector.ActiveAppListener::class.java)
        
        // تعيين المستمع
        activeAppDetector.setActiveAppListener(mockListener)
        
        // محاكاة تغيير التطبيق النشط
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        context.startActivity(intent)
        
        // انتظار لحظة للسماح بمعالجة التغيير
        Thread.sleep(1000)
        
        // التحقق من استدعاء المستمع
        verify(mockListener).onActiveAppChanged(any())
    }
    
    @Test
    fun testStartAndStopMonitoring() {
        // إنشاء مستمع وهمي
        val mockListener = mock(ActiveAppDetector.ActiveAppListener::class.java)
        
        // تعيين المستمع
        activeAppDetector.setActiveAppListener(mockListener)
        
        // بدء المراقبة
        activeAppDetector.startMonitoring()
        
        // انتظار لحظة للسماح ببدء المراقبة
        Thread.sleep(500)
        
        // إيقاف المراقبة
        activeAppDetector.stopMonitoring()
        
        // محاكاة تغيير التطبيق النشط بعد إيقاف المراقبة
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        context.startActivity(intent)
        
        // انتظار لحظة للسماح بمعالجة التغيير
        Thread.sleep(1000)
        
        // التحقق من عدم استدعاء المستمع بعد إيقاف المراقبة
        verify(mockListener, never()).onActiveAppChanged(any())
    }
    
    @Test
    fun testIsAppActive() {
        // اسم حزمة التطبيق الحالي
        val currentPackage = context.packageName
        
        // التحقق من أن التطبيق الحالي نشط
        assertTrue(activeAppDetector.isAppActive(currentPackage))
        
        // التحقق من أن تطبيق آخر غير نشط
        assertFalse(activeAppDetector.isAppActive("com.example.nonexistent"))
    }
    
    @Test
    fun testIsAnyGameActive() {
        // اسم حزمة التطبيق الحالي
        val currentPackage = context.packageName
        
        // قائمة تتضمن اسم حزمة التطبيق الحالي
        val gamePackages1 = listOf(currentPackage, "com.example.game1", "com.example.game2")
        
        // قائمة لا تتضمن اسم حزمة التطبيق الحالي
        val gamePackages2 = listOf("com.example.game1", "com.example.game2")
        
        // التحقق من أن إحدى الألعاب نشطة في القائمة الأولى
        assertTrue(activeAppDetector.isAnyGameActive(gamePackages1))
        
        // التحقق من أن لا توجد ألعاب نشطة في القائمة الثانية
        assertFalse(activeAppDetector.isAnyGameActive(gamePackages2))
    }
    
    @Test
    fun testActiveAppChangedCallback() {
        // إنشاء قفل عد تنازلي للانتظار
        val latch = CountDownLatch(1)
        
        // متغير لتخزين اسم الحزمة المستلم
        var receivedPackage = ""
        
        // تعيين مستمع
        activeAppDetector.setActiveAppListener(object : ActiveAppDetector.ActiveAppListener {
            override fun onActiveAppChanged(packageName: String) {
                receivedPackage = packageName
                latch.countDown()
            }
        })
        
        // بدء المراقبة
        activeAppDetector.startMonitoring()
        
        // محاكاة تغيير التطبيق النشط
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        context.startActivity(intent)
        
        // انتظار استدعاء المستمع
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        
        // التحقق من استلام اسم الحزمة
        assertTrue(receivedPackage.isNotEmpty())
        
        // إيقاف المراقبة
        activeAppDetector.stopMonitoring()
    }
    
    // دوال مساعدة لـ Mockito
    private fun <T> any(): T {
        org.mockito.Mockito.any<T>()
        return null as T
    }
    
    private fun never() = org.mockito.Mockito.never()
}
