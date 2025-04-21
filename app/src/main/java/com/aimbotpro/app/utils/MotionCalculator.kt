package com.example.aimbot.util

import android.graphics.PointF
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * أداة حساب حركة المؤشر
 * 
 * هذه الأداة مسؤولة عن حساب مسار حركة المؤشر من الموقع الحالي إلى الهدف.
 * تأخذ في الاعتبار سرعة الحركة ونعومتها لتوفير حركة طبيعية وسلسة.
 */
class MotionCalculator {
    
    /**
     * حساب مسار الحركة من الموقع الحالي إلى الهدف
     * 
     * @param currentX الإحداثي الأفقي الحالي
     * @param currentY الإحداثي الرأسي الحالي
     * @param targetX الإحداثي الأفقي للهدف
     * @param targetY الإحداثي الرأسي للهدف
     * @param speed سرعة الحركة (0.0 - 1.0)
     * @param smoothness نعومة الحركة (0.0 - 1.0)
     * @param numPoints عدد النقاط في المسار
     * @return قائمة بنقاط المسار
     */
    fun calculatePath(
        currentX: Int, currentY: Int,
        targetX: Int, targetY: Int,
        speed: Float, smoothness: Float,
        numPoints: Int
    ): List<PointF> {
        // قائمة نقاط المسار
        val path = ArrayList<PointF>()
        
        // إضافة النقطة الأولى (الموقع الحالي)
        path.add(PointF(currentX.toFloat(), currentY.toFloat()))
        
        // حساب المسافة الإجمالية
        val dx = targetX - currentX
        val dy = targetY - currentY
        val distance = sqrt((dx * dx + dy * dy).toFloat())
        
        // تعديل سرعة الحركة بناءً على المسافة
        // كلما كانت المسافة أكبر، كلما كانت الحركة أسرع
        val adjustedSpeed = speed * (0.5f + 0.5f * (distance / 1000f)).coerceAtMost(1.0f)
        
        // حساب عدد النقاط الوسيطة
        val steps = numPoints - 1
        
        // حساب معامل التقدم لكل خطوة
        val progressStep = 1.0f / steps
        
        // إنشاء مسار غير خطي باستخدام منحنى بيزييه
        for (i in 1 until numPoints) {
            // حساب معامل التقدم (0.0 - 1.0)
            val progress = i * progressStep
            
            // تطبيق دالة التخفيف لجعل الحركة أكثر طبيعية
            val easedProgress = applyEasing(progress, smoothness)
            
            // حساب الإحداثيات المتوسطة
            val intermediateX = currentX + dx * easedProgress * adjustedSpeed
            val intermediateY = currentY + dy * easedProgress * adjustedSpeed
            
            // إضافة اضطراب عشوائي للحركة لجعلها تبدو أكثر طبيعية
            val jitterAmount = (1.0f - smoothness) * 5.0f
            val jitterX = (Math.random() * 2 - 1) * jitterAmount
            val jitterY = (Math.random() * 2 - 1) * jitterAmount
            
            // إضافة النقطة إلى المسار
            path.add(PointF(
                (intermediateX + jitterX).toFloat(),
                (intermediateY + jitterY).toFloat()
            ))
        }
        
        // إضافة النقطة الأخيرة (الهدف)
        // نستخدم معامل السرعة لتحديد مدى الاقتراب من الهدف
        val finalX = currentX + dx * adjustedSpeed
        val finalY = currentY + dy * adjustedSpeed
        path.add(PointF(finalX, finalY))
        
        return path
    }
    
    /**
     * تطبيق دالة التخفيف على معامل التقدم
     * 
     * @param progress معامل التقدم (0.0 - 1.0)
     * @param smoothness نعومة الحركة (0.0 - 1.0)
     * @return معامل التقدم بعد تطبيق دالة التخفيف
     */
    private fun applyEasing(progress: Float, smoothness: Float): Float {
        // دالة التخفيف: مزيج من الدوال الخطية والتربيعية والتكعيبية
        // كلما زادت قيمة smoothness، كلما كانت الحركة أكثر نعومة
        
        // دالة خطية: progress
        val linear = progress
        
        // دالة تربيعية: progress^2
        val quadratic = progress * progress
        
        // دالة تكعيبية: progress^3
        val cubic = progress * progress * progress
        
        // مزج الدوال بناءً على معامل النعومة
        return when {
            smoothness < 0.33f -> {
                // مزيج من الخطية والتربيعية
                val blend = smoothness * 3.0f
                linear * (1.0f - blend) + quadratic * blend
            }
            smoothness < 0.66f -> {
                // مزيج من التربيعية والتكعيبية
                val blend = (smoothness - 0.33f) * 3.0f
                quadratic * (1.0f - blend) + cubic * blend
            }
            else -> {
                // تكعيبية مع إضافة تأثير الارتداد
                val blend = (smoothness - 0.66f) * 3.0f
                val bounce = sin(progress * Math.PI).toFloat() * 0.1f * blend
                cubic * (1.0f - blend) + (cubic + bounce) * blend
            }
        }
    }
    
    /**
     * حساب مسار دائري حول الهدف
     * 
     * @param centerX مركز الدائرة (الإحداثي الأفقي)
     * @param centerY مركز الدائرة (الإحداثي الرأسي)
     * @param radius نصف قطر الدائرة
     * @param startAngle زاوية البداية (بالراديان)
     * @param endAngle زاوية النهاية (بالراديان)
     * @param numPoints عدد النقاط في المسار
     * @return قائمة بنقاط المسار
     */
    fun calculateCircularPath(
        centerX: Int, centerY: Int,
        radius: Float,
        startAngle: Float, endAngle: Float,
        numPoints: Int
    ): List<PointF> {
        // قائمة نقاط المسار
        val path = ArrayList<PointF>()
        
        // حساب الفرق بين زاوية البداية وزاوية النهاية
        val angleDiff = endAngle - startAngle
        
        // حساب معامل التقدم لكل خطوة
        val angleStep = angleDiff / (numPoints - 1)
        
        // إنشاء مسار دائري
        for (i in 0 until numPoints) {
            // حساب الزاوية الحالية
            val angle = startAngle + i * angleStep
            
            // حساب الإحداثيات
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            
            // إضافة النقطة إلى المسار
            path.add(PointF(x, y))
        }
        
        return path
    }
}
