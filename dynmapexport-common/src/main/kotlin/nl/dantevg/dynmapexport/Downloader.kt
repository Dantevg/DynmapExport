package nl.dantevg.dynmapexport

import nl.dantevg.dynmapexport.location.TileCoords
import nl.dantevg.dynmapexport.location.WorldCoords
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant

class Downloader(private val dynmapExport: DynmapExport) {
	/**
	 * Download multiple tiles in the rectangle between `from` and `to` (inclusive)
	 *
	 * @param config the export configuration
	 * @return the amount of tiles downloaded, or -1 if nothing changed in the Dynmap
	 */
	fun downloadTiles(config: ExportConfig, now: Instant, map: DynmapWebAPI.Map): Int {
		val cached: Instant? = dynmapExport.imageThresholdCache.getCachedInstant(config)
		val tiles: List<TileCoords> = config.toTileLocations(map)
		val downloadedFiles: MutableMap<TileCoords, File> = HashMap()
		for (tile in tiles) {
			val tilePath = Paths.getDynmapTilePath(config, tile, dynmapExport.worldConfiguration ?: return -1)
			val dest = Paths.getLocalTileFile(dynmapExport, config, now, tile)
			downloadedFiles[tile] = dest
			download(tilePath, dest)
		}
		
		if (downloadedFiles.isNotEmpty() && cached != null
			&& !dynmapExport.imageThresholdCache.anyChangedSince(cached, config, downloadedFiles.values)
		) {
			// Not enough changes, remove tile files and directory again
			removeExportDir(config, now)
			return -1
		}
		return downloadedFiles.size
	}
	
	private fun removeExportDir(config: ExportConfig, instant: Instant) {
		val dir = Paths.getLocalExportDir(dynmapExport, config, instant)
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
		val dir = Paths.getLocalMapDir(dynmapExport, config)
		val lastExport = dynmapExport.imageThresholdCache.getCachedInstant(config)
		val exportDirs: List<File> = dir.listFiles()
			?.filter(File::isDirectory)
			?.filter { dir.list()?.contains(it.name + ".png") ?: false }
			.orEmpty()
		
		for (exportDir in exportDirs) {
			val instant = Paths.getInstantFromFile(exportDir)
			if (instant != lastExport) removeExportDir(config, instant)
		}
	}
	
	/**
	 * Remove all but the last exported image and export directory.
	 * @param config the export configuration
	 */
	fun removeOldExports(config: ExportConfig) {
		val dir = Paths.getLocalMapDir(dynmapExport, config)
		val lastExport = dynmapExport.imageThresholdCache.getCachedInstant(config)
		for (export in dir.listFiles().orEmpty()) {
			val instant = Paths.getInstantFromFile(export)
			if (instant == lastExport) continue
			if (export.isDirectory) removeExportDir(config, instant) else export.delete()
		}
	}
	
	/**
	 * Remove all exported files, including directories.
	 */
	fun removeAllExports() {
		fun delete(file: File) {
			if (file.isDirectory) for (subfile in file.listFiles().orEmpty()) delete(subfile)
			file.delete()
		}
		
		delete(dynmapExport.exportsDir)
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
		val world = dynmapExport.worldConfiguration?.getWorldByName(worldName)
			?: throw IllegalArgumentException("not a valid world")
		val map = world.getMapByName(mapName)
			?: throw IllegalArgumentException("not a valid map")
		val worldCoords = WorldCoords(x, Y_LEVEL, z)
		val config = ExportConfig(world, map, zoom, 0.0, 0.0, worldCoords)
		return downloadTile(config, worldCoords.toTileCoords(map, zoom))
	}
	
	/**
	 * Download a single tile at the given location.
	 *
	 * @param config     the export configuration
	 * @param tileCoords the tile coordinates
	 * @return the path to the downloaded file
	 */
	private fun downloadTile(config: ExportConfig, tileCoords: TileCoords): String? {
		val tilePath: String =
			Paths.getDynmapTilePath(config, tileCoords, dynmapExport.worldConfiguration ?: return null)
		val dest: File = Paths.getLocalTileFile(dynmapExport, config, Instant.now(), tileCoords)
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
			val url = URI("http://${dynmapExport.config.dynmapHost}/$path").toURL()
			val inputStream = url.openStream()
			dest.parentFile.mkdirs() // Make all directories on path to file
			val bytesWritten = Files.copy(inputStream, dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
			dynmapExport.logger.debug("Downloaded tile $path")
			if (bytesWritten == 0L) dynmapExport.logger.warn("Tile was 0 bytes!")
			return bytesWritten > 0
		} catch (e: MalformedURLException) {
			dynmapExport.logger.error(e.message)
		} catch (e: IOException) {
			dynmapExport.logger.error("Could not download tile $path", e)
		}
		return false
	}
}
