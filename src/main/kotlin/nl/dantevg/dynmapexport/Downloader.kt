package nl.dantevg.dynmapexport

import nl.dantevg.dynmapexport.location.TileCoords
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.logging.Level

class Downloader(private val plugin: DynmapExport) {
    fun downloadTiles(exportConfig: ExportConfig, now: Instant): Int {
        TODO()
    }

    fun downloadTile(config: ExportConfig, tileCoords: TileCoords): String? {
        val tilePath: String = Paths.getDynmapTilePath(config, tileCoords)
        val dest: File = Paths.getLocalTileFile(plugin, config, Instant.now(), tileCoords)
        return if (download(tilePath, dest)) dest.path else null
    }
    
    private fun download(path: String, dest: File): Boolean {
        try {
            val url = URL("http://${plugin.dynmapHost}/$path")
            val inputStream = url.openStream()
            dest.parentFile.mkdirs() // Make all directories on path to file
            val bytesWritten = Files.copy(inputStream, dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
            plugin.logger.log(Level.CONFIG, "Downloaded tile $path")
            if (bytesWritten == 0L) plugin.logger.log(Level.WARNING, "Tile was 0 bytes!")
            return bytesWritten > 0
        } catch (e: MalformedURLException) {
            plugin.logger.log(Level.SEVERE, e.message)
        } catch (e: IOException) {
            plugin.logger.log(Level.SEVERE, "Could not download tile $path", e)
        }
        return false
    }
}