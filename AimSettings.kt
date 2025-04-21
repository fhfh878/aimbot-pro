package com.example.aimbot.model

/**
 * إعدادات التصويب
 * 
 * هذه الفئة تحتوي على إعدادات التصويب المستخدمة في تحريك المؤشر.
 */
data class AimSettings(
    // سرعة التصويب (0.0 - 1.0)
    var speed: Float = 0.5f,
    
    // نعومة الحركة (0.0 - 1.0)
    var smoothness: Float = 0.5f,
    
    // نمط التصويب
    var aimMode: Int = AIM_MODE_CONTINUOUS,
    
    // منطقة التصويب (نسبة من حجم الشاشة)
    var aimAreaCenterX: Float = 0.5f,  // مركز منطقة التصويب (0.0 - 1.0)
    var aimAreaCenterY: Float = 0.5f,  // مركز منطقة التصويب (0.0 - 1.0)
    var aimAreaWidth: Float = 0.3f,    // عرض منطقة التصويب (0.0 - 1.0)
    var aimAreaHeight: Float = 0.3f,   // ارتفاع منطقة التصويب (0.0 - 1.0)
    
    // تأخير التصويب (بالمللي ثانية)
    var aimDelay: Long = 0,
    
    // مدة التصويب (بالمللي ثانية، 0 للتصويب المستمر)
    var aimDuration: Long = 0,
    
    // تمكين الاهتزاز عند اكتشاف هدف
    var vibrationEnabled: Boolean = false,
    
    // تمكين التصويب على الرأس
    var headShotEnabled: Boolean = false,
    
    // تعويض الارتداد
    var recoilCompensationEnabled: Boolean = false,
    var verticalRecoilFactor: Float = 0.5f,
    var horizontalRecoilFactor: Float = 0.2f,
    
    // تمكين التتبع الذكي
    var smartTrackingEnabled: Boolean = false,
    
    // تمكين التصويب المتقطع
    var burstAimEnabled: Boolean = false,
    var burstAimInterval: Long = 500  // الفاصل الزمني بين فترات التصويب (بالمللي ثانية)
) {
    companion object {
        // أنماط التصويب
        const val AIM_MODE_CONTINUOUS = 0    // تصويب مستمر
        const val AIM_MODE_SINGLE_SHOT = 1   // تصويب طلقة واحدة
        const val AIM_MODE_BURST = 2         // تصويب متقطع
        const val AIM_MODE_TRACKING = 3      // تتبع الهدف
    }
}
