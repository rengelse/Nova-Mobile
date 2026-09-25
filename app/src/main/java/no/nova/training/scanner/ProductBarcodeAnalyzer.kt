package no.nova.training.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

class ProductBarcodeAnalyzer(private val onBarcode: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val reader = MultiFormatReader().apply {
        setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.EAN_13, BarcodeFormat.EAN_8, BarcodeFormat.UPC_A, BarcodeFormat.UPC_E)))
    }
    @Volatile private var consumed = false

    override fun analyze(image: ImageProxy) {
        try {
            if (consumed) return
            val plane = image.planes.firstOrNull() ?: return
            val width = image.width; val height = image.height; val rowStride = plane.rowStride; val buffer = plane.buffer
            val data = ByteArray(width * height); val row = ByteArray(rowStride)
            for (y in 0 until height) {
                buffer.position(y * rowStride)
                val count = minOf(rowStride, buffer.remaining())
                buffer.get(row, 0, count)
                System.arraycopy(row, 0, data, y * width, minOf(width, count))
            }
            val (rotated, rw, rh) = rotateLuma(data, width, height, image.imageInfo.rotationDegrees)
            val source = PlanarYUVLuminanceSource(rotated, rw, rh, 0, 0, rw, rh, false)
            val result = runCatching { reader.decodeWithState(BinaryBitmap(HybridBinarizer(source))) }.getOrNull()
            if (result != null) { consumed = true; onBarcode(result.text.filter(Char::isDigit)) }
        } finally { reader.reset(); image.close() }
    }

    private fun rotateLuma(src: ByteArray, width: Int, height: Int, rotation: Int): Triple<ByteArray, Int, Int> = when (rotation) {
        90 -> { val dst=ByteArray(src.size); var i=0; for(x in 0 until width) for(y in height-1 downTo 0) dst[i++]=src[y*width+x]; Triple(dst,height,width) }
        180 -> Triple(ByteArray(src.size){src[src.lastIndex-it]},width,height)
        270 -> { val dst=ByteArray(src.size); var i=0; for(x in width-1 downTo 0) for(y in 0 until height) dst[i++]=src[y*width+x]; Triple(dst,height,width) }
        else -> Triple(src,width,height)
    }
}
