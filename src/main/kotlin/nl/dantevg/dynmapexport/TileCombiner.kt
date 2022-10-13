package nl.dantevg.dynmapexport

import nl.dantevg.dynmapexport.location.TileCoords
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.IOException
import java.time.Instant
import java.util.logging.Level
import javax.imageio.ImageIO

class TileCombiner(private val plugin: DynmapExport) {
    fun combineAndSave(config: ExportConfig, instant: Instant): Boolean {
        val result: BufferedImage = combine(config, instant) ?: return false
        val file = Paths.getLocalCombinedFile(plugin, config, instant)
        try {
            ImageIO.write(result, "png", file)
            return true
        } catch (e: IOException) {
            plugin.logger.log(Level.SEVERE, "Could not save combined image to $file", e)
            return false
        }
    }

    private fun combine(config: ExportConfig, instant: Instant): BufferedImage? {
        val width = tileCoordsToPixelX(config, config.to) + PIXELS_PER_TILE
        val height = tileCoordsToPixelY(config, config.to) + PIXELS_PER_TILE

        plugin.logger.log(Level.CONFIG, "Creating a ${width}x$height image from ${config.from} to ${config.to}")

        val output = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = output.createGraphics()

        for (tile in config.toTileLocations()) {
            if (!drawTile(config, instant, graphics, tile)) return null
        }

        return output
    }

    private fun drawTile(config: ExportConfig, instant: Instant, graphics: Graphics2D, tile: TileCoords): Boolean {
        val tileFile = Paths.getLocalTileFile(plugin, config, instant, tile)
        val tileImage = try {
            ImageIO.read(tileFile)
        } catch (e: IOException) {
            plugin.logger.log(Level.SEVERE, "Cannot read image from file $tileFile", e)
            return false
        }

        val x = tileCoordsToPixelX(config, tile)
        val y = tileCoordsToPixelY(config, tile)
        graphics.drawImage(tileImage, x, y, null)
        return true
    }

    private fun tileCoordsToPixelX(config: ExportConfig, tile: TileCoords): Int =
        (tile.x - config.from.x) / (1 shl config.zoom) * PIXELS_PER_TILE

    private fun tileCoordsToPixelY(config: ExportConfig, tile: TileCoords): Int =
        (tile.y - config.from.y) / (1 shl config.zoom) * PIXELS_PER_TILE

    companion object {
        const val PIXELS_PER_TILE = 128
    }
}