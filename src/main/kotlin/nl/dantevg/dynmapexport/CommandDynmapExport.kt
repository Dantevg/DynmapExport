package nl.dantevg.dynmapexport

import org.bukkit.Bukkit
import org.bukkit.command.*
import java.util.*
import java.util.stream.Collectors

class CommandDynmapExport(private val plugin: DynmapExport) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size == 1 && args[0] == "now") {
            Bukkit.getScheduler().runTaskAsynchronously(
                plugin,
                Runnable { plugin.export(if (sender is ConsoleCommandSender) null else sender) })
            return true
        } else if (args.size == 1 && args[0] == "reload") {
            plugin.reload()
            if (sender !is ConsoleCommandSender) sender.sendMessage("Reload complete")
            return true
        } else if (args.size == 1 && args[0] == "debug") {
            sender.sendMessage(plugin.debug())
            return true
        } else if (args.size == 6 && args[0] == "export") {
            // Export single
            val world = args[1]
            val map = args[2]
            val x: Int
            val z: Int
            val zoom: Int
            try {
                x = args[3].toInt()
                z = args[4].toInt()
                zoom = args[5].toInt()
            } catch (e: NumberFormatException) {
                sender.sendMessage("Invalid number")
                return false
            }
            val path: String = try {
                plugin.downloader.downloadTile(world, map, x, z, zoom)
            } catch (e: IllegalArgumentException) {
                sender.sendMessage("Could not save tile: " + e.message)
                return false
            }
            sender.sendMessage("Saved tile at $path")
            return true
        } else if ((args.size == 6 || args.size == 7) && args[0] == "worldtomap") {
            val worldName = args[1]
            val mapName = args[2]
            val x: Int
            val y: Int
            val z: Int
            var zoom = 0
            try {
                x = args[3].toInt()
                y = args[4].toInt()
                z = args[5].toInt()
                if (args.size == 7) zoom = args[6].toInt()
            } catch (e: NumberFormatException) {
                sender.sendMessage("Invalid number")
                return false
            }
            val worldCoords = WorldCoords(x, y, z)
            val map: DynmapWebAPI.Map = getMapFromWorldMapNames(sender, worldName, mapName) ?: return false
            val tileCoords: TileCoords = worldCoords.toTileCoords(map, zoom)
            sender.sendMessage(java.lang.String.format("%s is in tile %s", worldCoords, tileCoords))
            return true
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (args.size == 1) {
            return Arrays.asList("now", "export", "reload", "debug", "worldtomap")
        } else if (args.size == 2 && (args[0] == "export" || args[0] == "worldtomap")) {
            // Suggest world
            return plugin.worldConfiguration.worlds.stream()
                .map { world -> world.name }
                .collect(Collectors.toList())
        } else if (args.size == 3 && (args[0] == "export" || args[0] == "worldtomap")) {
            // Suggest map
            val world: World = plugin.worldConfiguration.getWorldByName(args[1])
            return if (world != null) {
                world.maps.stream().map { map -> map.name }.collect(Collectors.toList())
            } else {
                emptyList<String>()
            }
        }
        return emptyList<String>()
    }
    
    private fun getMapFromWorldMapNames(sender: CommandSender, worldName: String, mapName: String): DynmapWebAPI.Map {
        assert(plugin.worldConfiguration != null)
        val world: World = plugin.worldConfiguration.getWorldByName(worldName)
        if (world == null) {
            sender.sendMessage("no world with name $worldName")
            return null
        }
        val map: DynmapWebAPI.Map = world.getMapByName(mapName)
        if (map == null) {
            sender.sendMessage("world $worldName has no map with name $mapName")
            return null
        }
        return map
    }

}