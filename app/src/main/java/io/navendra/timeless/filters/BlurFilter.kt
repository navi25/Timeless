package io.navendra.timeless.filters

import java.util.*

class BlurFilter constructor(
        private val pixelsIn: IntArray,
        private val height: Int,
        private val width: Int,
        private val a0: Int,
        private val a1: Int,
        private val a2: Int,
        private val a3: Int
){

    private  var sigmaFar : Float = 5.0f
    private var sigmaNear : Float = 4.5f
    private var kernel = HashMap<String, Double>()
    private var blurMap = HashMap<Double, Kernel>()
    private  var pixelsOut: IntArray = IntArray(pixelsIn.size)
    private val radius = 5

    fun sigmaFar(value:Float) : BlurFilter{
        sigmaFar = value
        return this
    }

    fun sigmaNear(value:Float) : BlurFilter{
        sigmaNear = value
        return this
    }


    //intermediate variables
    private lateinit var channelPixelsOut: DoubleArray
    private var pixelValue = 0.0
    private var gv: Double = 0.toDouble()
    private var pv: Double = 0.toDouble()
    private var rowOffset: Int = 0
    private var colOffset:Int = 0
    //endregion




    //region API functions
    fun execute(): BlurFilter {
        Pipeline()
                .copyPixels()
                .createThreeChannels()
                .calculateIndividualBlurs()
                .createPixelsFromChannels()
        return this
    }

    fun executeTogether(): BlurFilter {
        Pipeline()
                .copyPixels()
                .calculateBlur()
        return this
    }

    fun getPixelsOut(): IntArray? {
        return pixelsOut
    }
    //endregion

    //region Base Functions
    private fun getGaussianValue(sigma: Double, k: Double): Double {
        var gv = 0.0
        val gc = 1 / (sigma * Math.sqrt(2 * Math.PI))
        val gec = -1 / (2.0 * sigma * sigma)
        gv = gc * Math.exp(gec * k * k)
        return gv
    }

    internal inner class Kernel(val matrix: DoubleArray)

    private fun getKernelMatrix(sigma: Double): Kernel {
        if (blurMap.containsKey(sigma)) {
            return blurMap[sigma]!!
        }
        val size = 2 * radius.toInt() + 1
        val matrix = DoubleArray(size)

        var sum = 0.0
        var k = 0
        run {
            var i = (-radius).toInt()
            while (i < radius) {
                val `val` = getGaussianValue(sigma, i.toDouble())
                matrix[k++] = `val`
                sum += `val`
                i++
            }
        }

        //Normalisation
        for (i in matrix.indices) {
            matrix[i] /= sum
        }

        val kernel = Kernel(matrix)

        blurMap[sigma] = kernel
        return kernel
    }

    private fun getPixelValue(row: Int, col: Int, pixels: DoubleArray): Double {
        val pixelIndex = row * width + col
        return if (pixelIndex < 0 || pixelIndex >= pixels.size) {
            0.0
        } else pixels[pixelIndex]
    }

    private fun setGaussianPixels(sigma: Float, rowStart: Int, rowEnd: Int, pixels: DoubleArray) {
        val intermidiate = DoubleArray(pixels.size)
        var pixelIndex: Int
        val colStart = 0
        val colEnd = width

        val start = 0
        val end = 0

        val matrix = getKernelMatrix(sigma.toDouble()).matrix

        //First GaussianBlur Transformation
        for (row in rowStart until rowEnd) {
            for (col in colStart until colEnd) {
                pixelIndex = row * width + col
                intermidiate[pixelIndex] = getOutputPixelValue(matrix, sigma, 1, row, col, pixels)
            }
        }

        //Second GaussianBlur Transformation
        for (row in rowStart until rowEnd) {
            for (col in colStart until colEnd) {
                pixelIndex = row * width + col
                channelPixelsOut[pixelIndex] = getOutputPixelValue(matrix, sigma, 0, row, col, intermidiate)
            }
        }
    }

    private fun buildBlur(channelpixelsIn: DoubleArray?): DoubleArray {

        channelPixelsOut = Arrays.copyOf(channelpixelsIn!!, channelpixelsIn.size)
        var row = 0
        while (row < height) {

            //region SIGMA_FAR BLUR
            if (row < a0) {
                setGaussianPixels(sigmaFar, 0, a0, channelpixelsIn)
                row = a0
            } else if (row >= a0 && row < a1) {
                val newSigma = sigmaFar * (a1 - row) / (a1 - a0)
                if (newSigma < 0.6f) {
                    row = a1
                } else {
                    setGaussianPixels(newSigma, row, row + 1, channelpixelsIn)
                    row++
                }
            } else if (row >= a1 && row < a2) {
                row = a2
            } else if (row >= a2 && row < a3) {
                val newSigma = sigmaNear * (row - a2) / (a3 - a2)
                if (newSigma < 0.6f) {
                    row = a3
                } else {
                    setGaussianPixels(newSigma, row, row + 1, channelpixelsIn)
                    row++
                }
            } else if (row < height && row >= a3) {
                if (sigmaNear < 0.6f) {
                    row = height
                } else {
                    setGaussianPixels(sigmaNear, a3, height, channelpixelsIn)
                    row = height
                }
            } else {
                break
            }

        }
        return channelPixelsOut
    }
    //endregion

    //region Utility Functions
    private fun getOutputPixelValue(matrix: DoubleArray, sigma: Float, rowWise: Int, row: Int, col: Int, channelPixelIn: DoubleArray): Double {
        val r = radius.toInt()
        pixelValue = 0.0
        for (i in -r until r) {
            if (rowWise == 1) {
                rowOffset = i
                colOffset = 0
            } else {
                rowOffset = 0
                colOffset = i
            }

            gv = matrix[i + Math.abs(r)]
            pv = getPixelValue(row + rowOffset, col + colOffset, channelPixelIn)
            pixelValue += gv * pv
        }

        return pixelValue
    }

    //endregion

    //region Pipeline Class to pipeline the whole process
    private inner class Pipeline {

        internal lateinit var rout: DoubleArray
        internal lateinit var gout: DoubleArray
        internal lateinit var bout: DoubleArray
        internal var size = 0
        internal lateinit var channelR: DoubleArray
        internal lateinit var channelG: DoubleArray
        internal lateinit var channelB: DoubleArray

        internal fun copyPixels(): Pipeline {
            //            pixelsOut = pixelsIn;
            return this
        }


        internal fun createThreeChannels(): Pipeline {
            size = height * width
            channelR = DoubleArray(size)
            channelG = DoubleArray(size)
            channelB = DoubleArray(size)

            for (i in 0 until size) {
                val B = pixelsIn!![i] and 0xff
                val G = pixelsIn[i] shr 8 and 0xff
                val R = pixelsIn[i] shr 16 and 0xff
                val A = 0xff
                channelR[i] = R.toDouble()
                channelG[i] = G.toDouble()
                channelB[i] = B.toDouble()
            }

            return this
        }

        internal fun calculateBlur(): Pipeline {
            val pixels = DoubleArray(pixelsIn!!.size)
            for (i in pixelsIn.indices) {
                pixels[i] = pixelsIn[i].toDouble()
            }
            val doublePixels = buildBlur(pixels)
            pixelsOut = scale(doublePixels)
            return this
        }

        private fun scale(pixels: DoubleArray): IntArray {
            val scaledPixels = IntArray(pixels.size)
            val scale = Math.pow(2.0, 10.0)
            for (i in pixels.indices) {
                var value = (pixels[i] * scale).toLong()
                value /= scale.toLong()
                scaledPixels[i] = value.toInt()
            }
            return scaledPixels
        }

        internal fun calculateIndividualBlurs(): Pipeline {
            rout = buildBlur(channelR)
            gout = buildBlur(channelG)
            bout = buildBlur(channelB)
            return this
        }

        internal fun createPixelsFromChannels(): Pipeline {
            for (i in 0 until size) {
                val R = rout!![i]
                val G = gout!![i]
                val B = bout!![i]
                val A = 0xff

                val color = A and 0xff shl 24 or (R.toInt() and 0xff shl 16) or (G.toInt() and 0xff shl 8) or (B.toInt() and 0xff)
                pixelsOut[i] = color
            }
            return this
        }

    }
    //endregion

}