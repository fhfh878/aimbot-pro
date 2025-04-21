package com.example.aimbot.model

/**
 * ملف تعريف اللعبة
 * 
 * هذه الفئة تحتوي على إعدادات خاصة بلعبة معينة لتحسين أداء التصويب التلقائي.
 */
data class GameProfile(
    // اسم ملف التعريف
    var name: String = "",
    
    // اسم حزمة اللعبة
    var packageName: String = "",
    
    // إعدادات اكتشاف الألوان الخاصة باللعبة
    var colorDetectionSettings: ColorDetectionSettings = ColorDetectionSettings(),
    
    // إعدادات التصويب الخاصة باللعبة
    var aimSettings: AimSettings = AimSettings(),
    
    // إعدادات خاصة باللعبة
    var isHeadshotModeSupported: Boolean = false,
    var isRecoilCompensationSupported: Boolean = false,
    var isTriggerBotSupported: Boolean = false,
    
    // مناطق اللعبة التي يجب تجاهلها (لتجنب التصويب على عناصر واجهة المستخدم)
    var ignoreAreas: List<IgnoreArea> = emptyList(),
    
    // مناطق اللعبة ذات الأولوية (للتصويب على مناطق معينة مثل الرأس)
    var priorityAreas: List<PriorityArea> = emptyList(),
    
    // إعدادات متقدمة
    var advancedSettings: Map<String, Any> = emptyMap()
) {
    companion object {
        /**
         * إنشاء ملف تعريف افتراضي للعبة PUBG Mobile
         */
        fun createPubgMobileProfile(): GameProfile {
            val colorSettings = ColorDetectionSettings(
                targetColor = android.graphics.Color.rgb(255, 0, 0),  // اللون الأحمر
                hueRange = 15,
                minSaturation = 150,
                minValue = 150,
                blurSize = 7,
                minContourArea = 150.0,
                sensitivity = 0.6f,
                targetSelectionStrategy = ColorDetectionSettings.TARGET_SELECTION_NEAREST_TO_CROSSHAIR
            )
            
            val aimSettings = AimSettings(
                speed = 0.4f,
                smoothness = 0.7f,
                aimMode = AimSettings.AIM_MODE_CONTINUOUS,
                aimAreaCenterX = 0.5f,
                aimAreaCenterY = 0.4f,  // أعلى قليلاً من المركز للتصويب على الجزء العلوي من الجسم
                aimAreaWidth = 0.4f,
                aimAreaHeight = 0.4f,
                headShotEnabled = true,
                recoilCompensationEnabled = true,
                verticalRecoilFactor = 0.6f,
                horizontalRecoilFactor = 0.2f
            )
            
            return GameProfile(
                name = "PUBG Mobile",
                packageName = "com.tencent.ig",
                colorDetectionSettings = colorSettings,
                aimSettings = aimSettings,
                isHeadshotModeSupported = true,
                isRecoilCompensationSupported = true,
                isTriggerBotSupported = true,
                ignoreAreas = listOf(
                    IgnoreArea(0.0f, 0.0f, 0.2f, 0.2f),  // الزاوية العلوية اليسرى (الخريطة)
                    IgnoreArea(0.8f, 0.8f, 1.0f, 1.0f)    // الزاوية السفلية اليمنى (أزرار التحكم)
                ),
                priorityAreas = listOf(
                    PriorityArea(0.4f, 0.2f, 0.6f, 0.4f, 2.0f)  // منطقة الرأس (أولوية مضاعفة)
                )
            )
        }
        
        /**
         * إنشاء ملف تعريف افتراضي للعبة Free Fire
         */
        fun createFreeFireProfile(): GameProfile {
            val colorSettings = ColorDetectionSettings(
                targetColor = android.graphics.Color.rgb(255, 0, 0),  // اللون الأحمر
                hueRange = 12,
                minSaturation = 160,
                minValue = 160,
                blurSize = 5,
                minContourArea = 120.0,
                sensitivity = 0.65f,
                targetSelectionStrategy = ColorDetectionSettings.TARGET_SELECTION_NEAREST_TO_CROSSHAIR
            )
            
            val aimSettings = AimSettings(
                speed = 0.45f,
                smoothness = 0.65f,
                aimMode = AimSettings.AIM_MODE_CONTINUOUS,
                aimAreaCenterX = 0.5f,
                aimAreaCenterY = 0.4f,
                aimAreaWidth = 0.45f,
                aimAreaHeight = 0.45f,
                headShotEnabled = true,
                recoilCompensationEnabled = true,
                verticalRecoilFactor = 0.5f,
                horizontalRecoilFactor = 0.15f
            )
            
            return GameProfile(
                name = "Free Fire",
                packageName = "com.dts.freefireth",
                colorDetectionSettings = colorSettings,
                aimSettings = aimSettings,
                isHeadshotModeSupported = true,
                isRecoilCompensationSupported = true,
                isTriggerBotSupported = true
            )
        }
        
        /**
         * إنشاء ملف تعريف افتراضي للعبة Call of Duty Mobile
         */
        fun createCodMobileProfile(): GameProfile {
            val colorSettings = ColorDetectionSettings(
                targetColor = android.graphics.Color.rgb(255, 0, 0),  // اللون الأحمر
                hueRange = 10,
                minSaturation = 170,
                minValue = 170,
                blurSize = 6,
                minContourArea = 130.0,
                sensitivity = 0.7f,
                targetSelectionStrategy = ColorDetectionSettings.TARGET_SELECTION_NEAREST_TO_CROSSHAIR
            )
            
            val aimSettings = AimSettings(
                speed = 0.5f,
                smoothness = 0.6f,
                aimMode = AimSettings.AIM_MODE_CONTINUOUS,
                aimAreaCenterX = 0.5f,
                aimAreaCenterY = 0.4f,
                aimAreaWidth = 0.4f,
                aimAreaHeight = 0.4f,
                headShotEnabled = true,
                recoilCompensationEnabled = true,
                verticalRecoilFactor = 0.7f,
                horizontalRecoilFactor = 0.25f
            )
            
            return GameProfile(
                name = "Call of Duty Mobile",
                packageName = "com.activision.callofduty.shooter",
                colorDetectionSettings = colorSettings,
                aimSettings = aimSettings,
                isHeadshotModeSupported = true,
                isRecoilCompensationSupported = true,
                isTriggerBotSupported = true
            )
        }
    }
}

/**
 * منطقة تجاهل في اللعبة
 * 
 * هذه الفئة تحدد منطقة في الشاشة يجب تجاهلها عند اكتشاف الأهداف.
 * تستخدم لتجنب التصويب على عناصر واجهة المستخدم مثل الخريطة وأزرار التحكم.
 */
data class IgnoreArea(
    val left: Float,    // الحد الأيسر للمنطقة (0.0 - 1.0)
    val top: Float,     // الحد العلوي للمنطقة (0.0 - 1.0)
    val right: Float,   // الحد الأيمن للمنطقة (0.0 - 1.0)
    val bottom: Float   // الحد السفلي للمنطقة (0.0 - 1.0)
)

/**
 * منطقة ذات أولوية في اللعبة
 * 
 * هذه الفئة تحدد منطقة في الشاشة ذات أولوية عند اكتشاف الأهداف.
 * تستخدم لتفضيل التصويب على مناطق معينة مثل الرأس.
 */
data class PriorityArea(
    val left: Float,       // الحد الأيسر للمنطقة (0.0 - 1.0)
    val top: Float,        // الحد العلوي للمنطقة (0.0 - 1.0)
    val right: Float,      // الحد الأيمن للمنطقة (0.0 - 1.0)
    val bottom: Float,     // الحد السفلي للمنطقة (0.0 - 1.0)
    val priorityFactor: Float  // معامل الأولوية (> 1.0 للأولوية العالية)
)
