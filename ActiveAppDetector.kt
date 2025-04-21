package com.example.aimbot.util

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.util.Log
import java.util.concurrent.TimeUnit

/**
 * كاشف التطبيقات النشطة
 * 
 * هذه الفئة مسؤولة عن اكتشاف التطبيق النشط حالياً في المقدمة.
 * تستخدم لتمكين التشغيل التلقائي عند فتح ألعاب محددة.
 */
class ActiveAppDetector(private val context: Context) {
    
    companion object {
        private const val TAG = "ActiveAppDetector"
        
        // الفاصل الزمني للتحقق من التطبيق النشط (بالمللي ثانية)
        private const val CHECK_INTERVAL = 1000L
    }
    
    // واجهة للإشعار بتغيير التطبيق النشط
    interface ActiveAppListener {
        fun onActiveAppChanged(packageName: String)
    }
    
    private var listener: ActiveAppListener? = null
    private var isRunning = false
    private var lastDetectedPackage = ""
    private var checkThread: Thread? = null
    
    /**
     * تعيين مستمع للتطبيق النشط
     */
    fun setActiveAppListener(listener: ActiveAppListener) {
        this.listener = listener
    }
    
    /**
     * بدء مراقبة التطبيقات النشطة
     */
    fun startMonitoring() {
        if (isRunning) {
            return
        }
        
        isRunning = true
        lastDetectedPackage = ""
        
        checkThread = Thread {
            try {
                while (isRunning) {
                    val currentPackage = getForegroundPackage()
                    
                    if (currentPackage.isNotEmpty() && currentPackage != lastDetectedPackage) {
                        lastDetectedPackage = currentPackage
                        listener?.onActiveAppChanged(currentPackage)
                    }
                    
                    Thread.sleep(CHECK_INTERVAL)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in monitoring thread", e)
            }
        }
        
        checkThread?.start()
        Log.d(TAG, "Active app monitoring started")
    }
    
    /**
     * إيقاف مراقبة التطبيقات النشطة
     */
    fun stopMonitoring() {
        isRunning = false
        checkThread?.interrupt()
        checkThread = null
        Log.d(TAG, "Active app monitoring stopped")
    }
    
    /**
     * الحصول على اسم حزمة التطبيق النشط حالياً
     */
    private fun getForegroundPackage(): String {
        try {
            // استخدام UsageStatsManager للأندرويد 5.0 وما فوق
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                    ?: return ""
                
                val time = System.currentTimeMillis()
                
                // الحصول على أحداث الاستخدام في آخر 5 ثوانٍ
                val usageEvents = usageStatsManager.queryEvents(time - TimeUnit.SECONDS.toMillis(5), time)
                
                var lastEvent: UsageEvents.Event? = null
                val event = UsageEvents.Event()
                
                // البحث عن آخر حدث من نوع MOVE_TO_FOREGROUND
                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event)
                    
                    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        lastEvent = UsageEvents.Event()
                        lastEvent.copyFrom(event)
                    }
                }
                
                return lastEvent?.packageName ?: ""
                
            } else {
                // استخدام ActivityManager للإصدارات القديمة (غير موصى به)
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val tasks = activityManager.getRunningTasks(1)
                
                if (tasks.isNotEmpty()) {
                    return tasks[0].topActivity?.packageName ?: ""
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting foreground package", e)
        }
        
        return ""
    }
    
    /**
     * التحقق مما إذا كان التطبيق المحدد نشطاً حالياً
     */
    fun isAppActive(packageName: String): Boolean {
        return getForegroundPackage() == packageName
    }
    
    /**
     * التحقق مما إذا كانت أي لعبة من ملفات التعريف نشطة حالياً
     */
    fun isAnyGameActive(gamePackages: List<String>): Boolean {
        val currentPackage = getForegroundPackage()
        return gamePackages.contains(currentPackage)
    }
}
