package nl.dantevg.dynmapexport

import nl.dantevg.dynmapexport.location.TileCoords
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Paths {
	/**
	 * Basic instant format without separators (which are problematic in filenames)
	 *
	 * [https://stackoverflow.com/a/39820917](https://stackoverflow.com/a/39820917)
	 */
	private val instantFormat = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'")
		.withZone(ZoneId.from(ZoneOffset.UTC))
	
	/**
	 * Get the Dynmap path to the tile, at
	 * `{world}/{map}/{regionX}_{regionZ}/{zoom}_{tileX}_{tileY}.png`
	 *
	 * See [dynmap github](https://github.com/webbukkit/dynmap/blob/f89777a0dd1ac9e17f595ef0361a030f53eff92a/DynmapCore/src/main/java/org/dynmap/storage/filetree/FileTreeMapStorage.java#L46-L53)
	 */
	fun getDynmapTilePath(config: ExportConfig, tile: TileCoords, dynmapConfig: DynmapWebAPI.Configuration): String =
		"tiles/${config.world}/${
			dynmapConfig.getMapByName(config.world, config.map)?.prefix
		}/${tile.tileGroupCoords}/${getZoomString(config.zoom)}${tile.x}_${tile.y}.png"
	
	/**
	 * Get the local map directory, at `plugins/DynmapExport/exports/{world}/{map}/`
	 */
	fun getLocalMapDir(plugin: DynmapExport, config: ExportConfig): File =
		File(plugin.exportsDir, "${config.world}/${config.map}")
	
	/**
	 * Get the local directory of a single export at a given [instant] in time,
	 * at `plugins/DynmapExport/exports/{world}/{map}/{instant}/`
	 */
	fun getLocalExportDir(plugin: DynmapExport, config: ExportConfig, instant: Instant): File =
		File(getLocalMapDir(plugin, config), instantFormat.format(instant))
	
	/**
	 * Get the local file for the image in a single export at a given [instant]
	 * in time, at the given [tile]-location, at
	 * `plugins/DynmapExport/exports/{world}/{map}/{instant}/{zoom}_{tileX}_{tileY}.png`.
	 *
	 * The instant gets formatted in ISO 8601 basic format, truncated to seconds
	 * (for example, `20220804T213215Z`).
	 */
	fun getLocalTileFile(plugin: DynmapExport, config: ExportConfig, instant: Instant, tile: TileCoords): File =
		File(getLocalExportDir(plugin, config, instant), "${getZoomString(config.zoom)}${tile.x}_${tile.y}.png")
	
	/**
	 * Get the local file for the combined image of a single export at a given
	 * [instant] in time, at `plugins/DynmapExport/exports/{world}/{map}/{instant}.png`
	 */
	fun getLocalCombinedFile(plugin: DynmapExport, config: ExportConfig, instant: Instant): File =
		File(getLocalMapDir(plugin, config), instantFormat.format(instant) + ".png")
	
	fun getInstantFromFile(file: File): Instant =
		Instant.from(instantFormat.parse(file.nameWithoutExtension))
	
	private fun getZoomString(zoom: Int): String = if (zoom > 0) "z".repeat(zoom) + "_" else ""
}
