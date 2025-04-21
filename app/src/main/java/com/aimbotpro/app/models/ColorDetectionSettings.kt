package com.example.aimbot.model

import android.graphics.Color

/**
 * إعدادات اكتشاف الألوان
 * 
 * هذه الفئة تحتوي على إعدادات اكتشاف الألوان المستخدمة في تحليل الصور.
 */
data class ColorDetectionSettings(
    // لون الهدف (RGB)
    var targetColor: Int = Color.RED,
    
    // نطاق اللون (±)
    var hueRange: Int = 10,
    
    // الحد الأدنى للتشبع (0-255)
    var minSaturation: Int = 100,
    
    // الحد الأدنى للقيمة (0-255)
    var minValue: Int = 100,
    
    // حجم التمويه (يجب أن يكون فردياً)
    var blurSize: Int = 5,
    
    // الحد الأدنى لمساحة المحيط
    var minContourArea: Double = 100.0,
    
    // حساسية الاكتشاف (0.0 - 1.0)
    var sensitivity: Float = 0.5f,
    
    // استراتيجية اختيار الهدف
    var targetSelectionStrategy: Int = TARGET_SELECTION_NEAREST_TO_CROSSHAIR,
    
    // منطقة البحث (نسبة من حجم الشاشة)
    var searchAreaCenterX: Float = 0.5f,  // مركز منطقة البحث (0.0 - 1.0)
    var searchAreaCenterY: Float = 0.5f,  // مركز منطقة البحث (0.0 - 1.0)
    var searchAreaWidth: Float = 1.0f,    // عرض منطقة البحث (0.0 - 1.0)
    var searchAreaHeight: Float = 1.0f    // ارتفاع منطقة البحث (0.0 - 1.0)
) {
    companion object {
        // استراتيجيات اختيار الهدف
        const val TARGET_SELECTION_NEAREST_TO_CROSSHAIR = 0  // الأقرب إلى مركز الشاشة (التصويب)
        const val TARGET_SELECTION_HIGHEST_CONFIDENCE = 1    // ذو نسبة الثقة الأعلى
        const val TARGET_SELECTION_NEAREST_TO_TOP = 2        // الأقرب إلى أعلى الشاشة (للتصويب على الرأس)
    }
}
