package com.example.aimbot.util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.ArrayList

/**
 * أداة اكتشاف الألوان
 * 
 * هذه الأداة مسؤولة عن اكتشاف الألوان المحددة في الصورة.
 */
class ColorDetector {
    
    companion object {
        private const val TAG = "ColorDetector"
    }
    
    // إعدادات اكتشاف الألوان
    private var targetHue: Int = 0        // قيمة اللون المستهدف (0-179)
    private var hueRange: Int = 10        // نطاق اللون (±)
    private var minSaturation: Int = 100  // الحد الأدنى للتشبع (0-255)
    private var minValue: Int = 100       // الحد الأدنى للقيمة (0-255)
    private var blurSize: Int = 5         // حجم التمويه (يجب أن يكون فردياً)
    private var minContourArea: Double = 100.0  // الحد الأدنى لمساحة المحيط
    
    // مصفوفات OpenCV
    private val hsvMat = Mat()
    private val maskMat = Mat()
    private val blurredMat = Mat()
    private val dilatedMat = Mat()
    
    /**
     * تحديث إعدادات اكتشاف الألوان
     */
    fun updateSettings(rgbColor: Int, hueRange: Int, minSaturation: Int, minValue: Int, blurSize: Int, minContourArea: Double) {
        // تحويل RGB إلى HSV
        val hsv = FloatArray(3)
        Color.colorToHSV(rgbColor, hsv)
        
        // تحويل نطاق HSV من [0-360, 0-1, 0-1] إلى [0-179, 0-255, 0-255]
        this.targetHue = (hsv[0] / 2).toInt()
        this.hueRange = hueRange
        this.minSaturation = minSaturation
        this.minValue = minValue
        this.blurSize = if (blurSize % 2 == 0) blurSize + 1 else blurSize  // يجب أن يكون فردياً
        this.minContourArea = minContourArea
        
        Log.d(TAG, "Updated settings: targetHue=$targetHue, hueRange=$hueRange, " +
                "minSaturation=$minSaturation, minValue=$minValue, blurSize=$blurSize, " +
                "minContourArea=$minContourArea")
    }
    
    /**
     * اكتشاف الأهداف في الصورة
     * 
     * @param bitmap الصورة المراد تحليلها
     * @return قائمة بمراكز الأهداف المكتشفة
     */
    fun detectTargets(bitmap: Bitmap): List<Target> {
        // تحويل Bitmap إلى Mat
        val rgbMat = Mat()
        Utils.bitmapToMat(bitmap, rgbMat)
        
        // تحويل من RGB إلى HSV
        Imgproc.cvtColor(rgbMat, hsvMat, Imgproc.COLOR_RGB2HSV)
        
        // حساب نطاق اللون المستهدف
        val lowerHue = (targetHue - hueRange + 180) % 180
        val upperHue = (targetHue + hueRange) % 180
        
        // إنشاء قناع للون المستهدف
        val lowerBound: Scalar
        val upperBound: Scalar
        
        if (lowerHue > upperHue) {
            // نطاق اللون يتجاوز 0 (مثل من 170 إلى 10)
            val lowerMask = Mat()
            val upperMask = Mat()
            
            lowerBound = Scalar(0.0, minSaturation.toDouble(), minValue.toDouble())
            upperBound = Scalar(upperHue.toDouble(), 255.0, 255.0)
            Core.inRange(hsvMat, lowerBound, upperBound, upperMask)
            
            lowerBound = Scalar(lowerHue.toDouble(), minSaturation.toDouble(), minValue.toDouble())
            upperBound = Scalar(179.0, 255.0, 255.0)
            Core.inRange(hsvMat, lowerBound, upperBound, lowerMask)
            
            // دمج القناعين
            Core.bitwise_or(lowerMask, upperMask, maskMat)
            
            lowerMask.release()
            upperMask.release()
        } else {
            // نطاق اللون عادي
            lowerBound = Scalar(lowerHue.toDouble(), minSaturation.toDouble(), minValue.toDouble())
            upperBound = Scalar(upperHue.toDouble(), 255.0, 255.0)
            Core.inRange(hsvMat, lowerBound, upperBound, maskMat)
        }
        
        // تطبيق التمويه لتقليل الضوضاء
        Imgproc.GaussianBlur(maskMat, blurredMat, Size(blurSize.toDouble(), blurSize.toDouble()), 0.0)
        
        // تطبيق عملية التوسيع لملء الفجوات
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(5.0, 5.0))
        Imgproc.dilate(blurredMat, dilatedMat, kernel)
        
        // البحث عن المحيطات
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(dilatedMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        
        // تحليل المحيطات واستخراج الأهداف
        val targets = ArrayList<Target>()
        
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            
            // تجاهل المحيطات الصغيرة جداً
            if (area < minContourArea) {
                continue
            }
            
            // حساب مركز المحيط
            val moments = Imgproc.moments(contour)
            val centerX = (moments.m10 / moments.m00).toInt()
            val centerY = (moments.m01 / moments.m00).toInt()
            
            // حساب نسبة الثقة بناءً على المساحة
            val confidence = Math.min(1.0, area / 5000.0)
            
            // إضافة الهدف إلى القائمة
            targets.add(Target(centerX, centerY, confidence))
            
            contour.release()
        }
        
        // تحرير الموارد
        rgbMat.release()
        hierarchy.release()
        kernel.release()
        
        return targets
    }
    
    /**
     * تحرير الموارد
     */
    fun release() {
        hsvMat.release()
        maskMat.release()
        blurredMat.release()
        dilatedMat.release()
    }
}

/**
 * فئة تمثل هدفاً مكتشفاً
 */
data class Target(
    val x: Int,          // الإحداثي الأفقي للهدف
    val y: Int,          // الإحداثي الرأسي للهدف
    val confidence: Double  // نسبة الثقة في الهدف (0.0 - 1.0)
)
