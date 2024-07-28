package nl.dantevg.dynmapexport.log

import java.util.logging.Level

class JavaLogger(private val logger: java.util.logging.Logger) : Logger {
	override fun log(level: Level, message: String?, thrown: Throwable?) =
		logger.log(level, message, thrown)
}
