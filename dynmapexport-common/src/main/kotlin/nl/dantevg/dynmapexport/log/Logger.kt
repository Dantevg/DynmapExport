package nl.dantevg.dynmapexport.log

import java.util.logging.Level

interface Logger {
	fun log(level: Level, message: String?, thrown: Throwable? = null)

	fun debug(message: String?, thrown: Throwable? = null) = log(Level.CONFIG, message, thrown)
	fun info(message: String?, thrown: Throwable? = null) = log(Level.INFO, message, thrown)
	fun warn(message: String?, thrown: Throwable? = null) = log(Level.WARNING, message, thrown)
	fun error(message: String?, thrown: Throwable? = null) = log(Level.SEVERE, message, thrown)
}
