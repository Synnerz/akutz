package com.github.synnerz.akutz.api.libs

import kotlin.math.*

object MathLib {
    @JvmStatic
    @JvmOverloads
    fun compareFloat(f1: Double, f2: Double, epsilon: Double = 1e-6): Int {
        val d = f1 - f2
        if (abs(d) < epsilon) return 0
        return sign(d).toInt()
    }

    @JvmStatic
    fun rescale(n: Double, oldMin: Double, oldMax: Double, newMin: Double, newMax: Double): Double {
        return (n - oldMin) / (oldMax - oldMin) * (newMax - newMin) + newMin
    }

    @JvmStatic
    fun lerp(oldValue: Double, newValue: Double, mult: Double): Double {
        return oldValue + (newValue - oldValue) * mult
    }

    /**
     * @return (r, m, b) (Pearson correlation coefficient, slope, intercept)
     */
    @JvmStatic
    fun linearLeastSquares(points: ArrayList<ArrayList<Double>>): List<Double> {
        val xSum = points.sumOf { it[0] }
        val ySum = points.sumOf { it[1] }
        val xMean = xSum / points.size
        val yMean = ySum / points.size
        val xStd = sqrt(points.sumOf { (it[0] - xMean).pow(2) })
        val yStd = sqrt(points.sumOf { (it[1] - yMean).pow(2) })
        val r = points.sumOf { (it[0] - xMean) * (it[1] - yMean) } / (xStd * yStd)
        if (r.isNaN()) return listOf(0.0, 0.0, 0.0)
        val m = r * yStd / xStd
        val b = yMean - m * xMean
        return listOf(r, m, b)
    }

    @JvmStatic
    fun calcStats(data: ArrayList<Double>): DataStats {
        val arr = data.sorted()
        val min = arr.firstOrNull() ?: Double.POSITIVE_INFINITY
        val max = arr.lastOrNull() ?: Double.NEGATIVE_INFINITY
        val Q1 = (arr.getOrElse(floor((arr.size + 1) * 0.25).toInt(), { Double.NaN }) +
                arr.getOrElse(ceil((arr.size + 1) * 0.75).toInt(), { Double.NaN })) / 2
        val Q3 = (arr.getOrElse(floor((arr.size + 1) * 0.25).toInt(), { Double.NaN }) +
                arr.getOrElse(ceil((arr.size + 1) * 0.75).toInt(), { Double.NaN })) / 2
        val mean = arr.sum() / arr.size
        return DataStats(
            min,
            max,
            (arr.getOrElse(arr.size / 2) { Double.NaN } + arr.getOrElse((arr.size + 1) / 2) { Double.NaN }) / 2,
            Q1,
            Q3,
            Q3 - Q1,
            mean,
            sqrt(data.sumOf { (it - mean).pow(2) } / arr.size),
            max - min
        )
    }

    /**
     * [Link](https://www.flipcode.com/archives/Fast_Approximate_Distance_Functions.shtml)
     */
    @JvmStatic
    fun fastDistance(dx: Int, dy: Int): Int {
        val x = if (dx < 0) -dx else dx
        val y = if (dy < 0) -dy else dy
        val min = if (x < y) x else y
        val max = if (x < y) y else x

        var approx = (max * 1007) + (min * 441)
        if (max < (min shl 4)) approx -= (max * 40)

        return ((approx + 512) shr 10)
    }

    data class DataStats(
        val min: Double,
        val max: Double,
        val median: Double,
        val Q1: Double,
        val Q3: Double,
        val IQR: Double,
        val mean: Double,
        val stddev: Double,
        val range: Double
    )

    @JvmField
    val vector3D = object {
        fun intersectPlaneLine(
            dx: Double,
            dy: Double,
            dz: Double,
            x: Double,
            y: Double,
            z: Double,
            nx: Double,
            ny: Double,
            nz: Double,
            px: Double,
            py: Double,
            pz: Double
        ): List<Double> {
            val a = dx * nx + dy * ny + dz * nz
            val b = (px - x) * nx + (py - y) * ny + (pz - z) * nz
            if (a == 0.0) {
                if (b == 0.0) return listOf(x, y, z)
                return listOf(Double.NaN, Double.NaN, Double.NaN)
            }
            val d = b / a
            return listOf(x + dx * d, y + dy * d, z + dz * d)
        }

        fun getNormal(
            x1: Double,
            y1: Double,
            z1: Double,
            x2: Double,
            y2: Double,
            z2: Double,
            x3: Double,
            y3: Double,
            z3: Double
        ): List<Double> {
            val x4 = x1 - x2
            val y4 = y1 - y2
            val z4 = z1 - z2
            val x5 = x1 - x3
            val y5 = y1 - y3
            val z5 = z1 - z3
            return listOf(y4 * z5 - z4 * y5, z4 * x5 - x4 * z5, x4 * y5 - y4 * x5)
        }

        fun getAngle(
            x1: Double,
            y1: Double,
            z1: Double,
            x2: Double,
            y2: Double,
            z2: Double
        ) = getAngle(x1, y1, z1, x2, y2, z2, true)

        fun getAngle(
            x1: Double,
            y1: Double,
            z1: Double,
            x2: Double,
            y2: Double,
            z2: Double,
            smallest: Boolean
        ): Double {
            val dp = x1 * x2 + y1 * y2 + z1 * z2;
            val a = acos(dp / (sqrt(x1.pow(2) + y1.pow(2) + z1.pow(2)) * sqrt(x2.pow(2) + y2.pow(2) + z2.pow(2))))
            if (!smallest) return a
            if (dp >= 0) return a
            return PI - a
        }

        /**
         * t: theta/yaw
         * p: phi/pitch
         * r: psi/roll
         */
        fun rotate(x: Double, y: Double, z: Double, t: Double, p: Double, r: Double): List<Double> {
            val ct = cos(t)
            val st = sin(t)
            val cp = cos(p)
            val sp = sin(p)
            val cr = cos(r)
            val sr = sin(r)
            return listOf(
                x * ct * cp +
                        z * (ct * sp * sr - st * cr) +
                        y * (ct * sp * cr + st * sr),
                x * st * cp + z * (st * sp * sr + ct * cr) +
                        y * (st * sp * cr - ct * sr),
                x * -sp + z * cp * sr +
                        y * cp * cr
            )
        }

        fun dot(ux: Double, uy: Double, uz: Double, vx: Double, vy: Double, vz: Double): Double =
            ux * vx + uy * vy + uz * vz

        fun cross(ux: Double, uy: Double, uz: Double, vx: Double, vy: Double, vz: Double): List<Double> =
            listOf(
                uy * vz - uz * vy,
                uz * vx - ux * vz,
                ux * vy - uy * vx
            )

        fun normalize(x: Double, y: Double, z: Double) = normalize(x, y, z, 1.0)

        fun normalize(x: Double, y: Double, z: Double, newLength: Double): List<Double> {
            var l = x.pow(2) + y.pow(2) + z.pow(2)
            if (l.isNaN()) l = 1.0
            val d = newLength / l
            return listOf(x * d, y * d, z * d)
        }
    }

    @JvmField
    val collision = object {
        // TODO: maybe https://www.realtimerendering.com/intersections.html
    }
}