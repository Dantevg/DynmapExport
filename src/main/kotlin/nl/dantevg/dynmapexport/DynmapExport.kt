package nl.dantevg.dynmapexport

import com.google.gson.Gson
import nl.dantevg.dynmapexport.location.WorldCoords
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.URL
import java.time.Instant
import java.util.logging.Level

const val Y_LEVEL = 64

class DynmapExport : JavaPlugin() {
	private lateinit var exportConfigs: List<ExportConfig>
	
	var worldConfiguration: DynmapWebAPI.Configuration? = null
	lateinit var imageThresholdCache: ImageThresholdCache
	lateinit var downloader: Downloader
	lateinit var dynmapHost: String
	
	override fun onEnable() {
		dataFolder.mkdirs()
		saveDefaultConfig()
		dynmapHost = config.getString("dynmap-host")!!
		
		val command = CommandDynmapExport(this)
		getCommand("dynmapexport")?.setExecutor(command)
		getCommand("dynmapexport")?.setTabCompleter(command)
		
		imageThresholdCache = ImageThresholdCache(this)
		downloader = Downloader(this)
		ExportScheduler(this)
		
		worldConfiguration = getDynmapConfiguration()
		exportConfigs = if (worldConfiguration != null) {
			config.getMapList("exports").mapNotNull { getExportConfig(it!!) }
		} else {
			ArrayList()
		}
	}
	
	fun export(commandSender: CommandSender?): Int {
		var nExported = 0
		val now = Instant.now()
		for (exportConfig in exportConfigs) {
			commandSender?.sendMessage("Exporting map ${exportConfig.world.name}:${exportConfig.map.name}")
			val downloadedTiles = downloader.downloadTiles(exportConfig, now)
			if (downloadedTiles > 0) {
				nExported++
				if (config.getBoolean("auto-combine") && TileCombiner(this, exportConfig, now).combineAndSave()) {
					downloader.removeOldExportDirs(exportConfig)
				}
			}
		}
		
		logger.log(Level.INFO, "Exported $nExported configs, skipped ${exportConfigs.size - nExported}")
		commandSender?.sendMessage("Exported $nExported configs, skipped ${exportConfigs.size - nExported}")
		return nExported
	}
	
	fun reload() {
		logger.log(Level.INFO, "Reload: disabling plugin")
		isEnabled = false
		Bukkit.getScheduler().cancelTasks(this)
		logger.log(Level.INFO, "Reload: re-enabling plugin")
		reloadConfig()
		isEnabled = true
		logger.log(Level.INFO, "Reload complete")
	}
	
	fun purge(all: Boolean) {
		if (all) downloader.removeAllExports()
		else {
			for (exportConfig in exportConfigs) downloader.removeOldExports(exportConfig)
		}
	}
	
	fun debug() = "Dynmap world configuration:\n$worldConfiguration"
	
	private fun getDynmapConfiguration(): DynmapWebAPI.Configuration? {
		try {
			val url = URL("http://$dynmapHost/up/configuration")
			val reader = InputStreamReader(url.openStream())
			return Gson().fromJson(reader, DynmapWebAPI.Configuration::class.java)
		} catch (e: MalformedURLException) {
			logger.log(Level.SEVERE, e.message)
		} catch (e: ConnectException) {
			logger.log(Level.SEVERE, "Could not connect to Dynmap, check the port in config.yml")
		} catch (e: IOException) {
			logger.log(Level.SEVERE, "Could not download Dynmap worlds configuration", e)
		}
		return null
	}
	
	private fun getExportConfig(exportMap: Map<*, *>): ExportConfig? {
		val worldName = exportMap["world"] as String?
		val mapName = exportMap["map"] as String?
		val zoom = exportMap["zoom"] as Int
		val changeThreshold = exportMap["change-threshold"] as Double
		val fromMap = ensurePresent(exportMap["from"] as Map<String, Int>, "export is missing field 'from'")
			?: return null
		val toMap = ensurePresent(exportMap["to"] as? Map<String, Int>, "export is missing field 'to'")
			?: return null
		
		val fromX = ensurePresent(fromMap["x"], "export field 'from' is missing field 'x'") ?: return null
		val fromY = fromMap["y"] ?: Y_LEVEL
		val fromZ = ensurePresent(fromMap["z"], "export field 'from' is missing field 'z'") ?: return null
		val toX = ensurePresent(toMap["x"], "export field 'to' is missing field 'x'") ?: return null
		val toY = toMap["y"] ?: Y_LEVEL
		val toZ = ensurePresent(toMap["z"], "export field 'to' is missing field 'z'") ?: return null
		
		val from = WorldCoords(fromX, fromY, fromZ)
		val to = WorldCoords(toX, toY, toZ)
		
		val world = ensurePresent(
			worldName?.let { worldConfiguration?.getWorldByName(it) },
			"$worldName is not a valid world"
		) ?: return null
		
		val map = ensurePresent(
			mapName?.let(world::getMapByName),
			"$mapName is not a valid map for world $worldName"
		) ?: return null
		
		return ExportConfig(world, map, zoom, changeThreshold, from, to)
	}
	
	private fun <T> ensurePresent(value: T?, msg: String): T? =
		if (value == null) {
			logger.warning("$msg, ignoring this export")
			null
		} else value
}
