package nl.dantevg.dynmapexport.log

import java.util.logging.Level

class Slf4jLogger(private val logger: org.slf4j.Logger) : Logger {
	override fun log(level: Level, message: String?, thrown: Throwable?) = when (level) {
		Level.CONFIG -> logger.debug(message, thrown)
		Level.INFO -> logger.info(message, thrown)
		Level.WARNING -> logger.warn(message, thrown)
		Level.SEVERE -> logger.error(message, thrown)
		else -> logger.info(message, thrown)
	}
}
