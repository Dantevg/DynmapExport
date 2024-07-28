package nl.dantevg.dynmapexport

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import nl.dantevg.dynmapexport.log.Slf4jLogger
import org.slf4j.LoggerFactory
import java.nio.file.NoSuchFileException
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

object DynmapExportMod : ModInitializer, DynmapExport {
	const val MOD_ID = "dynmapexport"

	override val logger = Slf4jLogger(LoggerFactory.getLogger(MOD_ID))
	override val exportsDir = FabricLoader.getInstance().gameDir.resolve(MOD_ID).toFile()

	override var config = loadConfig()
	override var worldConfiguration: DynmapWebAPI.Configuration? = null
	override var imageThresholdCache = ImageThresholdCache(this)
	override var downloader = Downloader(this)

	override fun onInitialize() {
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register {
			worldConfiguration = getDynmapConfiguration()
				?.also { logger.info("Loaded dynmap configuration") }
		}

		command // auto-register command with Silk
	}

	override fun reload() {
		logger.info("Reloading")
		config = loadConfig()
		worldConfiguration = getDynmapConfiguration()
	}

	private fun loadConfig(): Config {
		FabricLoader.getInstance().configDir.createDirectories()
		val configPath = FabricLoader.getInstance().configDir.resolve("$MOD_ID.yml")
		try {
			// Try to read the config file if it exists
			return Yaml.default.decodeFromStream(configPath.inputStream())
		} catch (_: NoSuchFileException) {
			// Config file did not exist, write default one
			logger.info("Creating default config")
			Yaml.default.encodeToStream(Config(), configPath.outputStream())
			return Config()
		} catch (e: YamlException) {
			logger.error("Error in config.yml:${e.line}:${e.column} in ${e.path.toHumanReadableString()}: ${e.localizedMessage}")
			logger.error("Using default config.")
			return Config()
		}
	}
}
