package nl.dantevg.dynmapexport

import nl.dantevg.dynmapexport.location.TileCoords
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.IOException
import java.time.Instant
import javax.imageio.ImageIO

private const val PIXELS_PER_TILE = 128

class TileCombiner(
	private val plugin: DynmapExport,
	private val config: ExportConfig,
	private val map: DynmapWebAPI.Map,
	private val instant: Instant,
) {
	private val minMaxTile = config.toMinMaxTileCoords(map)
	private val minTile = minMaxTile.first
	private val maxTile = minMaxTile.second
	
	fun combineAndSave(): Boolean {
		val result: BufferedImage = combine() ?: return false
		val file = Paths.getLocalCombinedFile(plugin, config, instant)
		try {
			ImageIO.write(result, "png", file)
			return true
		} catch (e: IOException) {
			plugin.logger.error("Could not save combined image to $file", e)
			return false
		}
	}
	
	private fun combine(): BufferedImage? {
		val width = ((maxTile.x - minTile.x) / (1 shl config.zoom) + 1) * PIXELS_PER_TILE
		val height = ((maxTile.y - minTile.y) / (1 shl config.zoom) + 1) * PIXELS_PER_TILE
		
		plugin.logger.debug("Creating a ${width}x$height image from $minTile to $maxTile")
		
		val output = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
		val graphics = output.createGraphics()
		
		for (tile in config.toTileLocations(map)) {
			if (!drawTile(graphics, tile)) return null
		}
		
		return output
	}
	
	private fun drawTile(graphics: Graphics2D, tile: TileCoords): Boolean {
		val tileFile = Paths.getLocalTileFile(plugin, config, instant, tile)
		val tileImage = try {
			ImageIO.read(tileFile)
		} catch (e: IOException) {
			plugin.logger.error("Cannot read image from file $tileFile", e)
			return false
		}
		
		val (x, y) = tileCoordsToPixel(tile)
		graphics.drawImage(tileImage, x, y, null)
		return true
	}
	
	private fun tileCoordsToPixel(tile: TileCoords): Pair<Int, Int> =
		Pair(
			(tile.x - minTile.x) / (1 shl config.zoom) * PIXELS_PER_TILE,
			(maxTile.y - tile.y) / (1 shl config.zoom) * PIXELS_PER_TILE
		)
}
