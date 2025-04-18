package nl.dantevg.dynmapexport

import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.time.Instant
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.pow

// Used in calculating the difference between two colours.
// A value of 1 results in colour values of 0 vs 1 being as different as 254 vs 255.
// Lower values will give more weight to changes in darker colours.
private const val COLOUR_CHANGE_LINEARITY = 0.75

class ImageThresholdCache(private val dynmapExport: DynmapExport) {
	/**
	 * Get the instant of the last export.
	 *
	 * @return the instant, or null if there was no export yet
	 */
	fun getCachedInstant(config: ExportConfig): Instant? {
		val mapDir = Paths.getLocalMapDir(dynmapExport, config)
		if (!mapDir.isDirectory) return null
		return mapDir.listFiles()?.maxOfOrNull(Paths::getInstantFromFile)
	}
	
	fun anyChangedSince(since: Instant, config: ExportConfig, files: Collection<File>): Boolean =
		files.any { hasChangedSince(since, config, it) }
	
	private fun hasChangedSince(since: Instant, config: ExportConfig, file: File): Boolean {
		val image = try {
			ImageIO.read(file)
		} catch (e: IOException) {
			dynmapExport.logger.warn("Could not read image from $file")
			return true
		}
		
		val cachedImageFile = File(Paths.getLocalExportDir(dynmapExport, config, since), file.name)
		if (!cachedImageFile.exists()) return true
		
		val from = try {
			ImageIO.read(cachedImageFile)
		} catch (e: IOException) {
			dynmapExport.logger.warn("Could not read image from $file")
			return true
		}
		return fractionPixelsChanged(from, image, config.colourChangeThreshold) >= config.areaChangeThreshold
	}
}

private fun fractionPixelsChanged(from: BufferedImage, to: BufferedImage, colourChangeThreshold: Double): Double {
	val pixelsChanged = nPixelsChanged(from, to, colourChangeThreshold)
	val totalPixels = to.width * to.height
	return pixelsChanged.toDouble() / totalPixels
}

private fun nPixelsChanged(from: BufferedImage, to: BufferedImage, colourChangeThreshold: Double): Int {
	assert(from.width == to.width)
	assert(from.height == to.height)
	
	return (0 until from.width).sumOf { x ->
		(0 until from.height).count { y ->
			colourDifference(from.getRGB(x, y), to.getRGB(x, y)) >= colourChangeThreshold
		}
	}
}

/**
 * Calculate the colour difference between two RGB colours.
 * The result is a number between 0 and 1, where 0 means the colours are the same,
 * and 1 means the colours are completely different.
 *
 * @param from the first colour
 * @param to the second colour
 * @return the colour difference on the scale `[0,1]`
 */
private fun colourDifference(from: RGB, to: RGB): Double =
	maxOf(
		channelDifference(from.red(), to.red()),
		channelDifference(from.green(), to.green()),
		channelDifference(from.blue(), to.blue()),
	)

private val lut = DoubleArray(256) { (it.toDouble() / 0xFF).pow(COLOUR_CHANGE_LINEARITY) }

private fun channelDifference(from: Int, to: Int): Double = abs(lut[from] - lut[to])

typealias RGB = Int

private fun RGB.red(): Int = (this ushr 16) and 0xFF
private fun RGB.green(): Int = (this ushr 8) and 0xFF
private fun RGB.blue(): Int = this and 0xFF
