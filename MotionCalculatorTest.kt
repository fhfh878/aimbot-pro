package com.example.aimbot

import android.graphics.PointF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aimbot.model.AimSettings
import com.example.aimbot.util.MotionCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * اختبار وحدة لحاسب الحركة
 */
@RunWith(AndroidJUnit4::class)
class MotionCalculatorTest {
    
    private lateinit var motionCalculator: MotionCalculator
    
    @Before
    fun setUp() {
        motionCalculator = MotionCalculator()
    }
    
    @Test
    fun testCalculatePath() {
        // إحداثيات البداية والنهاية
        val startX = 100
        val startY = 100
        val endX = 200
        val endY = 200
        
        // حساب المسار بسرعة ونعومة متوسطة
        val path = motionCalculator.calculatePath(
            startX, startY, endX, endY,
            0.5f, 0.5f, 10
        )
        
        // التحقق من عدد النقاط في المسار
        assertEquals(10, path.size)
        
        // التحقق من أن النقطة الأولى هي نقطة البداية
        assertEquals(startX.toFloat(), path[0].x, 0.001f)
        assertEquals(startY.toFloat(), path[0].y, 0.001f)
        
        // التحقق من أن النقطة الأخيرة قريبة من نقطة النهاية
        // (لا تصل تماماً بسبب معامل السرعة)
        val lastPoint = path[path.size - 1]
        assertTrue(abs(lastPoint.x - (startX + (endX - startX) * 0.5f)) < 10)
        assertTrue(abs(lastPoint.y - (startY + (endY - startY) * 0.5f)) < 10)
        
        // التحقق من أن المسار يتقدم بشكل تدريجي
        for (i in 1 until path.size) {
            val prevPoint = path[i - 1]
            val currPoint = path[i]
            
            // التحقق من أن النقطة الحالية أقرب إلى الهدف من النقطة السابقة
            val prevDistance = Math.sqrt(
                Math.pow((prevPoint.x - endX).toDouble(), 2.0) +
                Math.pow((prevPoint.y - endY).toDouble(), 2.0)
            )
            
            val currDistance = Math.sqrt(
                Math.pow((currPoint.x - endX).toDouble(), 2.0) +
                Math.pow((currPoint.y - endY).toDouble(), 2.0)
            )
            
            assertTrue(currDistance <= prevDistance)
        }
    }
    
    @Test
    fun testSpeedEffect() {
        // إحداثيات البداية والنهاية
        val startX = 100
        val startY = 100
        val endX = 300
        val endY = 300
        
        // حساب المسار بسرعة منخفضة
        val slowPath = motionCalculator.calculatePath(
            startX, startY, endX, endY,
            0.2f, 0.5f, 10
        )
        
        // حساب المسار بسرعة عالية
        val fastPath = motionCalculator.calculatePath(
            startX, startY, endX, endY,
            0.8f, 0.5f, 10
        )
        
        // التحقق من أن المسار السريع يصل أقرب إلى الهدف
        val slowLastPoint = slowPath[slowPath.size - 1]
        val fastLastPoint = fastPath[fastPath.size - 1]
        
        val slowDistance = Math.sqrt(
            Math.pow((slowLastPoint.x - endX).toDouble(), 2.0) +
            Math.pow((slowLastPoint.y - endY).toDouble(), 2.0)
        )
        
        val fastDistance = Math.sqrt(
            Math.pow((fastLastPoint.x - endX).toDouble(), 2.0) +
            Math.pow((fastLastPoint.y - endY).toDouble(), 2.0)
        )
        
        assertTrue(fastDistance < slowDistance)
    }
    
    @Test
    fun testSmoothnessEffect() {
        // إحداثيات البداية والنهاية
        val startX = 100
        val startY = 100
        val endX = 300
        val endY = 300
        
        // حساب المسار بنعومة منخفضة
        val roughPath = motionCalculator.calculatePath(
            startX, startY, endX, endY,
            0.5f, 0.2f, 10
        )
        
        // حساب المسار بنعومة عالية
        val smoothPath = motionCalculator.calculatePath(
            startX, startY, endX, endY,
            0.5f, 0.8f, 10
        )
        
        // حساب متوسط التغير في الاتجاه للمسارين
        val roughDirectionChanges = calculateAverageDirectionChange(roughPath)
        val smoothDirectionChanges = calculateAverageDirectionChange(smoothPath)
        
        // التحقق من أن المسار الناعم له تغيرات اتجاه أقل
        assertTrue(smoothDirectionChanges < roughDirectionChanges)
    }
    
    /**
     * حساب متوسط التغير في الاتجاه للمسار
     */
    private fun calculateAverageDirectionChange(path: List<PointF>): Float {
        if (path.size < 3) {
            return 0f
        }
        
        var totalChange = 0f
        
        for (i in 2 until path.size) {
            val p1 = path[i - 2]
            val p2 = path[i - 1]
            val p3 = path[i]
            
            // حساب المتجهات
            val v1x = p2.x - p1.x
            val v1y = p2.y - p1.y
            val v2x = p3.x - p2.x
            val v2y = p3.y - p2.y
            
            // حساب الزاوية بين المتجهين
            val dotProduct = v1x * v2x + v1y * v2y
            val v1Magnitude = Math.sqrt((v1x * v1x + v1y * v1y).toDouble()).toFloat()
            val v2Magnitude = Math.sqrt((v2x * v2x + v2y * v2y).toDouble()).toFloat()
            
            if (v1Magnitude > 0 && v2Magnitude > 0) {
                val cosAngle = dotProduct / (v1Magnitude * v2Magnitude)
                val angle = Math.acos(cosAngle.coerceIn(-1f, 1f))
                totalChange += angle
            }
        }
        
        return totalChange / (path.size - 2)
    }
    
    @Test
    fun testCircularPath() {
        // مركز الدائرة ونصف القطر
        val centerX = 200
        val centerY = 200
        val radius = 50f
        
        // حساب المسار الدائري
        val path = motionCalculator.calculateCircularPath(
            centerX, centerY, radius,
            0f, 6.28f, 20  // دورة كاملة (2π)
        )
        
        // التحقق من عدد النقاط في المسار
        assertEquals(20, path.size)
        
        // التحقق من أن جميع النقاط تقع على مسافة تقريبية من المركز تساوي نصف القطر
        for (point in path) {
            val distance = Math.sqrt(
                Math.pow((point.x - centerX).toDouble(), 2.0) +
                Math.pow((point.y - centerY).toDouble(), 2.0)
            )
            
            // السماح بهامش خطأ صغير
            assertTrue(abs(distance - radius) < 1)
        }
        
        // التحقق من أن المسار يشكل دائرة كاملة
        val firstPoint = path[0]
        val lastPoint = path[path.size - 1]
        
        // المسافة بين النقطة الأولى والأخيرة يجب أن تكون صغيرة
        val endDistance = Math.sqrt(
            Math.pow((firstPoint.x - lastPoint.x).toDouble(), 2.0) +
            Math.pow((firstPoint.y - lastPoint.y).toDouble(), 2.0)
        )
        
        // السماح بهامش خطأ صغير
        assertTrue(endDistance < 10)
    }
}
