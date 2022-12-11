package nl.dantevg.dynmapexport

import com.google.common.io.Files
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.logging.Level

class ExportScheduler(private val plugin: DynmapExport) {
	private val lastExportFile: File = File(plugin.dataFolder, FILENAME)
	private var lastExport: Instant = try {
		Instant.parse(Files.toString(lastExportFile, StandardCharsets.UTF_8))
	} catch (e: IOException) {
		Instant.EPOCH
	}
	
	init {
		if (plugin.config.contains("schedule")) {
			val schedule = plugin.config.getString("schedule")!!
			try {
				startScheduledTask(Duration.parse("PT$schedule"))
			} catch (e: DateTimeParseException) {
				plugin.logger.warning("Invalid schedule format (only seconds, minutes, hours allowed!)")
			}
		}
	}
	
	private fun saveLastExport() {
		try {
			Files.write(lastExport.toString(), lastExportFile, StandardCharsets.UTF_8)
		} catch (e: IOException) {
			plugin.logger.log(Level.WARNING, "Could not save last export time", e)
		}
	}
	
	private fun startScheduledTask(duration: Duration) {
		var delay = Duration.between(Instant.now(), lastExport.plus(duration))
		if (delay.isNegative) delay = Duration.ZERO
		
		ExportTask().runTaskTimerAsynchronously(
			plugin,
			delay.seconds * 20,
			duration.seconds * 20
		)
		
		val durationStr: String = DurationFormatUtils.formatDurationWords(duration.toMillis(), true, true)
		val delayStr: String = DurationFormatUtils.formatDurationWords(delay.toMillis(), true, true)
		plugin.logger.info("Scheduled export every $durationStr starts in $delayStr")
	}
	
	private inner class ExportTask : BukkitRunnable() {
		override fun run() {
			plugin.export(null)
			lastExport = Instant.now()
			saveLastExport()
		}
	}
	
	companion object {
		private const val FILENAME = "last-export.txt"
	}
}