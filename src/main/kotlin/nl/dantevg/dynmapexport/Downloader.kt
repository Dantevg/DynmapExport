package nl.dantevg.dynmapexport

import nl.dantevg.dynmapexport.location.TileCoords
import nl.dantevg.dynmapexport.location.WorldCoords
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.logging.Level

class Downloader(private val plugin: DynmapExport) {
	/**
	 * Download multiple tiles in the rectangle between `from` and `to` (inclusive)
	 *
	 * @param config the export configuration
	 * @return the amount of tiles downloaded, or -1 if nothing changed in the Dynmap
	 */
	fun downloadTiles(config: ExportConfig, now: Instant): Int {
		val cached: Instant? = plugin.imageTresholdCache.getCachedInstant(config)
		val tiles: List<TileCoords> = config.toTileLocations()
		val downloadedFiles: MutableMap<TileCoords, File> = HashMap()
		for (tile in tiles) {
			val tilePath = Paths.getDynmapTilePath(config, tile)
			val dest = Paths.getLocalTileFile(plugin, config, now, tile)
			downloadedFiles[tile] = dest
			download(tilePath, dest)
		}
		
		// Not enough changes, remove tile files and directory again
		if (downloadedFiles.isNotEmpty() && cached != null
			&& !plugin.imageTresholdCache.anyChangedSince(cached, config, downloadedFiles.values)
		) {
			removeExportDir(config, now)
			return -1
		}
		return downloadedFiles.size
	}
	
	private fun removeExportDir(config: ExportConfig, instant: Instant) {
		val dir = Paths.getLocalExportDir(plugin, config, instant)
		dir.listFiles()?.forEach(File::delete)
		dir.delete()
	}
	
	/**
	 * Remove all but the last export directory, except ones that do not have
	 * an associated auto-combined image.
	 *
	 * @param config the export configuration
	 */
	fun removeOldExportDirs(config: ExportConfig) {
		val dir = Paths.getLocalMapDir(plugin, config)
		val lastExport = plugin.imageTresholdCache.getCachedInstant(config)
		val exportDirs: List<File> = dir.listFiles()
			?.filter(File::isDirectory)
			?.filter { dir.list().orEmpty().contains(it.name + ".png") }
			.orEmpty()
		
		for (exportDir in exportDirs) {
			val instant = Paths.getInstantFromFile(exportDir)
			if (instant != lastExport) removeExportDir(config, instant)
		}
	}
	
	/**
	 * Download a single tile at the given location.
	 *
	 * @param worldName the name of the world (e.g. `"world"`)
	 * @param mapName   the name of the map, or "prefix" (e.g. `"flat"`)
	 * @param x         the in-game block x-coordinate
	 * @param z         the in-game block z-coordinate
	 * @param zoom      the zoom-out level, 0 is fully zoomed in.
	 * @return the path to the downloaded file
	 */
	fun downloadTile(worldName: String, mapName: String, x: Int, z: Int, zoom: Int): String? {
		val world = plugin.worldConfiguration?.getWorldByName(worldName)
			?: throw IllegalArgumentException("not a valid world")
		val map = world.getMapByName(mapName)
			?: throw IllegalArgumentException("not a valid map")
		val tile = WorldCoords(x, DynmapExport.Y_LEVEL, z).toTileCoords(map, zoom)
		val config = ExportConfig(world, map, zoom, tile)
		return downloadTile(config, tile)
	}
	
	/**
	 * Download a single tile at the given location.
	 *
	 * @param config     the export configuration
	 * @param tileCoords the tile coordinates
	 * @return the path to the downloaded file
	 */
	private fun downloadTile(config: ExportConfig, tileCoords: TileCoords): String? {
		val tilePath: String = Paths.getDynmapTilePath(config, tileCoords)
		val dest: File = Paths.getLocalTileFile(plugin, config, Instant.now(), tileCoords)
		return if (download(tilePath, dest)) dest.path else null
	}
	
	/**
	 * Download the Dynmap tile at `path` to `dest`.
	 *
	 * @param path the Dynmap path to the tile
	 * @param dest the destination file to download to.
	 * @return whether the download succeeded
	 */
	private fun download(path: String, dest: File): Boolean {
		try {
			val url = URL("http://${plugin.dynmapHost}/$path")
			val inputStream = url.openStream()
			dest.parentFile.mkdirs() // Make all directories on path to file
			val bytesWritten = Files.copy(inputStream, dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
			plugin.logger.log(Level.CONFIG, "Downloaded tile $path")
			if (bytesWritten == 0L) plugin.logger.log(Level.WARNING, "Tile was 0 bytes!")
			return bytesWritten > 0
		} catch (e: MalformedURLException) {
			plugin.logger.log(Level.SEVERE, e.message)
		} catch (e: IOException) {
			plugin.logger.log(Level.SEVERE, "Could not download tile $path", e)
		}
		return false
	}
}
