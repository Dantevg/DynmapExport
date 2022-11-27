package nl.dantevg.dynmapexport

import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.logging.Level
import javax.imageio.ImageIO

class ImageThresholdCache(private val plugin: DynmapExport) {
	fun anyChangedSince(since: Instant, config: ExportConfig, files: Collection<File>): Boolean =
		files.any { hasChangedSince(since, config, it) }
	
	/**
	 * Get the instant of the last export.
	 *
	 * @return the instant, or null if there was no export yet
	 */
	fun getCachedInstant(config: ExportConfig): Instant? {
		val mapDir = Paths.getLocalMapDir(plugin, config)
		if (!mapDir.isDirectory) return null
		return mapDir.listFiles()?.maxOfOrNull(Paths::getInstantFromFile)
	}
	
	private fun hasChangedSince(since: Instant, config: ExportConfig, file: File): Boolean {
		val image = try {
			ImageIO.read(file)
		} catch (e: IOException) {
			plugin.logger.log(Level.WARNING, "Could not read image from $file")
			return true
		}
		
		val cachedImageFile = File(Paths.getLocalExportDir(plugin, config, since), file.name)
		if (!cachedImageFile.exists()) return true
		
		val from = try {
			ImageIO.read(cachedImageFile)
		} catch (e: IOException) {
			plugin.logger.log(Level.WARNING, "Could not read image from $file")
			return true
		}
		return getFractionPixelsChanged(from, image) >= config.changeThreshold
	}
	
	private fun getFractionPixelsChanged(from: BufferedImage, to: BufferedImage): Double {
		val pixelsChanged = getNPixelsChanged(from, to)
		val totalPixels = to.width * to.height
		return (pixelsChanged / totalPixels).toDouble()
	}
	
	private fun getNPixelsChanged(from: BufferedImage, to: BufferedImage): Int {
		assert(from.width == to.width)
		assert(from.height == to.height)
		
		return (0..from.width).sumOf { x ->
			(0..from.height).count { y ->
				from.getRGB(x, y) != to.getRGB(x, y)
			}
		}
	}
}
