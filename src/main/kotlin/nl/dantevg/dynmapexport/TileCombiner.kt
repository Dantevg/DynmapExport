package nl.dantevg.dynmapexport

import nl.dantevg.dynmapexport.location.TileCoords
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.IOException
import java.time.Instant
import java.util.logging.Level
import javax.imageio.ImageIO

class TileCombiner(private val plugin: DynmapExport, private val config: ExportConfig, private val instant: Instant) {
	fun combineAndSave(): Boolean {
		val result: BufferedImage = combine() ?: return false
		val file = Paths.getLocalCombinedFile(plugin, config, instant)
		try {
			ImageIO.write(result, "png", file)
			return true
		} catch (e: IOException) {
			plugin.logger.log(Level.SEVERE, "Could not save combined image to $file", e)
			return false
		}
	}
	
	private fun combine(): BufferedImage? {
		val width = tileCoordsToPixelX(config.to) + PIXELS_PER_TILE
		val height = tileCoordsToPixelY(config.from) + PIXELS_PER_TILE
		
		plugin.logger.log(Level.CONFIG, "Creating a ${width}x$height image from ${config.from} to ${config.to}")
		
		val output = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
		val graphics = output.createGraphics()
		
		for (tile in config.toTileLocations()) {
			if (!drawTile(graphics, tile)) return null
		}
		
		return output
	}
	
	private fun drawTile(graphics: Graphics2D, tile: TileCoords): Boolean {
		val tileFile = Paths.getLocalTileFile(plugin, config, instant, tile)
		val tileImage = try {
			ImageIO.read(tileFile)
		} catch (e: IOException) {
			plugin.logger.log(Level.SEVERE, "Cannot read image from file $tileFile", e)
			return false
		}
		
		val x = tileCoordsToPixelX(tile)
		val y = tileCoordsToPixelY(tile)
		graphics.drawImage(tileImage, x, y, null)
		return true
	}
	
	private fun tileCoordsToPixelX(tile: TileCoords): Int =
		(tile.x - config.from.x) / (1 shl config.zoom) * PIXELS_PER_TILE
	
	private fun tileCoordsToPixelY(tile: TileCoords): Int =
		(config.to.y - tile.y) / (1 shl config.zoom) * PIXELS_PER_TILE
	
	companion object {
		const val PIXELS_PER_TILE = 128
	}
}
