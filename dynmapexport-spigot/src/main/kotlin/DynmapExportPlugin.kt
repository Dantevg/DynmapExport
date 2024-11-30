package nl.dantevg.dynmapexport

import nl.dantevg.dynmapexport.location.WorldCoords
import nl.dantevg.dynmapexport.log.JavaLogger
import org.bukkit.plugin.java.JavaPlugin

object DynmapExportPlugin : JavaPlugin(), DynmapExport {
	private val spigotConfig by JavaPlugin::config

	override val logger = JavaLogger(this.getLogger())
	override val exportsDir = this.dataFolder.resolve("exports")

	override lateinit var config: Config
	override var worldConfiguration: DynmapWebAPI.Configuration? = null
	override var imageThresholdCache = ImageThresholdCache(this)
	override var downloader = Downloader(this)

	override fun onEnable() {
		exportsDir.mkdirs()
		saveDefaultConfig()

		val command = CommandDynmapExport(this)
		getCommand("dynmapexport")?.setExecutor(command)
		getCommand("dynmapexport")?.tabCompleter = command

		worldConfiguration = getDynmapConfiguration()
		config = loadConfig()
	}

	override fun reload() {
		logger.info("Reloading")
		isEnabled = false
		reloadConfig()
		worldConfiguration = getDynmapConfiguration()
		loadConfig()
		isEnabled = true
	}

	private fun loadConfig() = Config(
		dynmapHost = spigotConfig.getString("dynmapHost")!!,
		autoCombine = spigotConfig.getBoolean("autoCombine"),
		exports = spigotConfig.getMapList("exports").mapNotNull {
			getExportConfig(it!!)
		}
	)

	private fun getExportConfig(exportMap: Map<*, *>): ExportConfig? {
		fun <T> ensurePresent(value: T?, msg: String): T? =
			if (value == null) {
				logger.warn("$msg, ignoring this export")
				null
			} else value

		val worldName = exportMap["world"] as String?
		val mapName = exportMap["map"] as String?
		val zoom = exportMap["zoom"] as Int
		val areaChangeThreshold = exportMap["area-change-threshold"] as Double
		val colourChangeThreshold = exportMap["pixel-change-threshold"] as Double
		val fromMap =
			ensurePresent(exportMap["from"] as Map<String, Int>, "export is missing field 'from'")
				?: return null
		val toMap =
			ensurePresent(exportMap["to"] as? Map<String, Int>, "export is missing field 'to'")
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

		return ExportConfig(world, map, zoom, areaChangeThreshold, colourChangeThreshold, from, to)
	}
}
