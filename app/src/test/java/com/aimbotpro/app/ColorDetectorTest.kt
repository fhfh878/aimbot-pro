package com.example.aimbot

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aimbot.model.ColorDetectionSettings
import com.example.aimbot.util.ColorDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * اختبار وحدة لكاشف الألوان
 */
@RunWith(AndroidJUnit4::class)
class ColorDetectorTest {
    
    private lateinit var colorDetector: ColorDetector
    private lateinit var testBitmap: Bitmap
    
    @Before
    fun setUp() {
        // التأكد من تهيئة OpenCV
        val latch = CountDownLatch(1)
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, null, { status ->
            if (status) {
                latch.countDown()
            }
        })
        assertTrue("فشل تهيئة OpenCV", latch.await(10, TimeUnit.SECONDS))
        
        // إنشاء كاشف الألوان
        colorDetector = ColorDetector()
        
        // إنشاء صورة اختبار
        testBitmap = createTestBitmap()
    }
    
    /**
     * إنشاء صورة اختبار تحتوي على مناطق ملونة
     */
    private fun createTestBitmap(): Bitmap {
        val width = 300
        val height = 300
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        // ملء الصورة باللون الأسود
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, Color.BLACK)
            }
        }
        
        // إضافة منطقة حمراء في المنتصف
        val redRadius = 30
        val redCenterX = width / 2
        val redCenterY = height / 2
        for (x in (redCenterX - redRadius) until (redCenterX + redRadius)) {
            for (y in (redCenterY - redRadius) until (redCenterY + redRadius)) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    val dx = x - redCenterX
                    val dy = y - redCenterY
                    if (dx * dx + dy * dy <= redRadius * redRadius) {
                        bitmap.setPixel(x, y, Color.RED)
                    }
                }
            }
        }
        
        // إضافة منطقة زرقاء في الزاوية العلوية اليسرى
        val blueRadius = 20
        val blueCenterX = 50
        val blueCenterY = 50
        for (x in (blueCenterX - blueRadius) until (blueCenterX + blueRadius)) {
            for (y in (blueCenterY - blueRadius) until (blueCenterY + blueRadius)) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    val dx = x - blueCenterX
                    val dy = y - blueCenterY
                    if (dx * dx + dy * dy <= blueRadius * blueRadius) {
                        bitmap.setPixel(x, y, Color.BLUE)
                    }
                }
            }
        }
        
        // إضافة منطقة خضراء في الزاوية السفلية اليمنى
        val greenRadius = 25
        val greenCenterX = width - 50
        val greenCenterY = height - 50
        for (x in (greenCenterX - greenRadius) until (greenCenterX + greenRadius)) {
            for (y in (greenCenterY - greenRadius) until (greenCenterY + greenRadius)) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    val dx = x - greenCenterX
                    val dy = y - greenCenterY
                    if (dx * dx + dy * dy <= greenRadius * greenRadius) {
                        bitmap.setPixel(x, y, Color.GREEN)
                    }
                }
            }
        }
        
        return bitmap
    }
    
    @Test
    fun testDetectRedColor() {
        // إعدادات اكتشاف اللون الأحمر
        val settings = ColorDetectionSettings(
            targetColor = Color.RED,
            hueRange = 10,
            minSaturation = 100,
            minValue = 100,
            blurSize = 5,
            minContourArea = 50.0,
            sensitivity = 0.5f
        )
        
        // تحديث إعدادات كاشف الألوان
        colorDetector.updateSettings(settings)
        
        // تحويل الصورة إلى Mat
        val mat = Mat()
        org.opencv.android.Utils.bitmapToMat(testBitmap, mat)
        
        // اكتشاف اللون الأحمر
        val targets = colorDetector.detectTargets(mat)
        
        // التحقق من اكتشاف هدف واحد على الأقل
        assertNotNull(targets)
        assertTrue(targets.isNotEmpty())
        
        // التحقق من أن الهدف المكتشف قريب من مركز المنطقة الحمراء
        val target = targets[0]
        val redCenterX = testBitmap.width / 2
        val redCenterY = testBitmap.height / 2
        val distance = Math.sqrt(
            Math.pow((target.x - redCenterX).toDouble(), 2.0) +
            Math.pow((target.y - redCenterY).toDouble(), 2.0)
        )
        
        // السماح بهامش خطأ صغير
        assertTrue(distance < 10)
    }
    
    @Test
    fun testDetectBlueColor() {
        // إعدادات اكتشاف اللون الأزرق
        val settings = ColorDetectionSettings(
            targetColor = Color.BLUE,
            hueRange = 10,
            minSaturation = 100,
            minValue = 100,
            blurSize = 5,
            minContourArea = 50.0,
            sensitivity = 0.5f
        )
        
        // تحديث إعدادات كاشف الألوان
        colorDetector.updateSettings(settings)
        
        // تحويل الصورة إلى Mat
        val mat = Mat()
        org.opencv.android.Utils.bitmapToMat(testBitmap, mat)
        
        // اكتشاف اللون الأزرق
        val targets = colorDetector.detectTargets(mat)
        
        // التحقق من اكتشاف هدف واحد على الأقل
        assertNotNull(targets)
        assertTrue(targets.isNotEmpty())
        
        // التحقق من أن الهدف المكتشف قريب من مركز المنطقة الزرقاء
        val target = targets[0]
        val blueCenterX = 50
        val blueCenterY = 50
        val distance = Math.sqrt(
            Math.pow((target.x - blueCenterX).toDouble(), 2.0) +
            Math.pow((target.y - blueCenterY).toDouble(), 2.0)
        )
        
        // السماح بهامش خطأ صغير
        assertTrue(distance < 10)
    }
    
    @Test
    fun testSensitivityEffect() {
        // إعدادات اكتشاف اللون الأحمر بحساسية منخفضة
        val lowSensitivitySettings = ColorDetectionSettings(
            targetColor = Color.RED,
            hueRange = 5,  // نطاق ضيق
            minSaturation = 200,  // تشبع عالي
            minValue = 200,  // قيمة عالية
            blurSize = 5,
            minContourArea = 50.0,
            sensitivity = 0.2f  // حساسية منخفضة
        )
        
        // إعدادات اكتشاف اللون الأحمر بحساسية عالية
        val highSensitivitySettings = ColorDetectionSettings(
            targetColor = Color.RED,
            hueRange = 20,  // نطاق واسع
            minSaturation = 50,  // تشبع منخفض
            minValue = 50,  // قيمة منخفضة
            blurSize = 5,
            minContourArea = 50.0,
            sensitivity = 0.8f  // حساسية عالية
        )
        
        // تحويل الصورة إلى Mat
        val mat = Mat()
        org.opencv.android.Utils.bitmapToMat(testBitmap, mat)
        
        // اكتشاف اللون الأحمر بحساسية منخفضة
        colorDetector.updateSettings(lowSensitivitySettings)
        val lowSensitivityTargets = colorDetector.detectTargets(mat)
        
        // اكتشاف اللون الأحمر بحساسية عالية
        colorDetector.updateSettings(highSensitivitySettings)
        val highSensitivityTargets = colorDetector.detectTargets(mat)
        
        // التحقق من أن الحساسية العالية تكتشف أهدافاً أكثر
        assertTrue(highSensitivityTargets.size >= lowSensitivityTargets.size)
    }
    
    @Test
    fun testMinContourAreaEffect() {
        // إعدادات اكتشاف اللون الأحمر بمساحة محيط صغيرة
        val smallAreaSettings = ColorDetectionSettings(
            targetColor = Color.RED,
            hueRange = 10,
            minSaturation = 100,
            minValue = 100,
            blurSize = 5,
            minContourArea = 10.0,  // مساحة محيط صغيرة
            sensitivity = 0.5f
        )
        
        // إعدادات اكتشاف اللون الأحمر بمساحة محيط كبيرة
        val largeAreaSettings = ColorDetectionSettings(
            targetColor = Color.RED,
            hueRange = 10,
            minSaturation = 100,
            minValue = 100,
            blurSize = 5,
            minContourArea = 1000.0,  // مساحة محيط كبيرة
            sensitivity = 0.5f
        )
        
        // تحويل الصورة إلى Mat
        val mat = Mat()
        org.opencv.android.Utils.bitmapToMat(testBitmap, mat)
        
        // اكتشاف اللون الأحمر بمساحة محيط صغيرة
        colorDetector.updateSettings(smallAreaSettings)
        val smallAreaTargets = colorDetector.detectTargets(mat)
        
        // اكتشاف اللون الأحمر بمساحة محيط كبيرة
        colorDetector.updateSettings(largeAreaSettings)
        val largeAreaTargets = colorDetector.detectTargets(mat)
        
        // التحقق من أن مساحة المحيط الصغيرة تكتشف أهدافاً أكثر
        assertTrue(smallAreaTargets.size >= largeAreaTargets.size)
    }
    
    @Test
    fun testTargetSelectionStrategy() {
        // إعدادات اكتشاف اللون مع استراتيجية اختيار الهدف الأقرب إلى مركز الشاشة
        val nearestToCrosshairSettings = ColorDetectionSettings(
            targetColor = Color.RED,
            hueRange = 10,
            minSaturation = 100,
            minValue = 100,
            blurSize = 5,
            minContourArea = 50.0,
            sensitivity = 0.5f,
            targetSelectionStrategy = ColorDetectionSettings.TARGET_SELECTION_NEAREST_TO_CROSSHAIR
        )
        
        // تحويل الصورة إلى Mat
        val mat = Mat()
        org.opencv.android.Utils.bitmapToMat(testBitmap, mat)
        
        // اكتشاف اللون مع استراتيجية اختيار الهدف الأقرب إلى مركز الشاشة
        colorDetector.updateSettings(nearestToCrosshairSettings)
        val targets = colorDetector.detectTargets(mat)
        
        // التحقق من اكتشاف هدف واحد على الأقل
        assertNotNull(targets)
        assertTrue(targets.isNotEmpty())
        
        // التحقق من أن الهدف المكتشف هو الأقرب إلى مركز الشاشة
        val target = targets[0]
        val screenCenterX = testBitmap.width / 2
        val screenCenterY = testBitmap.height / 2
        
        // حساب المسافة من الهدف إلى مركز الشاشة
        val distance = Math.sqrt(
            Math.pow((target.x - screenCenterX).toDouble(), 2.0) +
            Math.pow((target.y - screenCenterY).toDouble(), 2.0)
        )
        
        // التحقق من أن المسافة أقل من نصف قطر الصورة
        assertTrue(distance < testBitmap.width / 2)
    }
}
