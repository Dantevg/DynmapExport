package nl.dantevg.dynmapexport

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import nl.dantevg.dynmapexport.log.Logger
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.URI
import java.time.Instant

const val Y_LEVEL = 64

typealias CommandFeedback = (String) -> Unit

interface DynmapExport {
	val logger: Logger
	val exportsDir: File
	
	var config: Config
	var worldConfiguration: DynmapWebAPI.Configuration?
	var imageThresholdCache: ImageThresholdCache
	var downloader: Downloader
	
	fun reload()
	fun debug() = "Dynmap world configuration:\n$worldConfiguration"
	
	fun purge(all: Boolean) {
		if (all) downloader.removeAllExports()
		else {
			for (exportConfig in config.exports) downloader.removeOldExports(exportConfig)
		}
	}
	
	fun export(commandFeedback: CommandFeedback): Int {
		var nExported = 0
		val now = Instant.now()
		for (exportConfig in config.exports) {
			nExported += export(commandFeedback, exportConfig, now)
		}
		
		logger.info("Exported $nExported configs, skipped ${config.exports.size - nExported}")
		commandFeedback("Exported $nExported configs, skipped ${config.exports.size - nExported}")
		return nExported
	}
	
	fun export(commandFeedback: CommandFeedback, exportConfig: ExportConfig, now: Instant): Int {
		val map = worldConfiguration?.getMapByName(exportConfig.world, exportConfig.map)
		if (map == null) {
			commandFeedback("${exportConfig.world}:${exportConfig.map} is not a valid world:map")
			return 0
		}
		commandFeedback("Exporting map ${exportConfig.world}:${exportConfig.map}")
		
		val downloadedTiles = downloader.downloadTiles(exportConfig, now, map)
		if (downloadedTiles < 0) {
			commandFeedback("No changes detected, skipping export")
			return 0
		} else if (downloadedTiles == 0) {
			commandFeedback("No tiles downloaded, configuration may be incorrect")
			return 0
		}
		
		if (config.autoCombine && TileCombiner(this, exportConfig, map, now).combineAndSave()) {
			downloader.removeOldExportDirs(exportConfig)
		}
		
		return 1
	}
	
	@OptIn(ExperimentalSerializationApi::class)
	fun DynmapExport.getDynmapConfiguration(): DynmapWebAPI.Configuration? {
		try {
			val url = URI("http://${config.dynmapHost}/up/configuration").toURL()
			return Json { ignoreUnknownKeys = true }.decodeFromStream(url.openStream())
		} catch (e: MalformedURLException) {
			logger.error(e.message)
		} catch (e: ConnectException) {
			logger.error("Could not connect to Dynmap, check the port in config.yml")
		} catch (e: IOException) {
			logger.error("Could not download Dynmap worlds configuration", e)
		}
		return null
	}
}
