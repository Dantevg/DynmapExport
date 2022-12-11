package nl.dantevg.dynmapexport

import nl.dantevg.dynmapexport.location.WorldCoords
import org.bukkit.Bukkit
import org.bukkit.command.*

class CommandDynmapExport(private val plugin: DynmapExport) : CommandExecutor, TabCompleter {
	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean =
		when {
			args.size == 1 && args[0] == "now" -> commandNow(sender)
			args.size == 1 && args[0] == "reload" -> commandReload(sender)
			args.size == 1 && args[0] == "debug" -> commandDebug(sender)
			args.size == 6 && args[0] == "export" -> try {
				commandExport(
					sender = sender,
					world = args[1],
					map = args[2],
					x = args[3].toInt(),
					z = args[4].toInt(),
					zoom = args[5].toInt()
				)
			} catch (e: NumberFormatException) {
				sender.sendMessage("Invalid number")
				false
			}
			
			args.size in 6..7 && args[0] == "worldtomap" -> try {
				commandWorldtomap(
					sender = sender,
					worldName = args[1],
					mapName = args[2],
					x = args[3].toInt(),
					y = args[4].toInt(),
					z = args[5].toInt(),
					zoom = args.getOrNull(6)?.toInt() ?: 0
				)
			} catch (e: NumberFormatException) {
				sender.sendMessage("Invalid number")
				false
			}
			
			else -> false
		}
	
	private fun commandNow(sender: CommandSender): Boolean {
		Bukkit.getScheduler().runTaskAsynchronously(
			plugin,
			Runnable { plugin.export(if (sender is ConsoleCommandSender) null else sender) }
		)
		return true
	}
	
	private fun commandReload(sender: CommandSender): Boolean {
		plugin.reload()
		if (sender !is ConsoleCommandSender) sender.sendMessage("Reload complete")
		return true
	}
	
	private fun commandDebug(sender: CommandSender): Boolean {
		sender.sendMessage(plugin.debug())
		return true
	}
	
	/**
	 * Export a single configuration.
	 */
	private fun commandExport(sender: CommandSender, world: String, map: String, x: Int, z: Int, zoom: Int): Boolean {
		val path: String? = try {
			plugin.downloader.downloadTile(world, map, x, z, zoom)
		} catch (e: IllegalArgumentException) {
			sender.sendMessage("Could not save tile: " + e.message)
			return false
		}
		sender.sendMessage(if (path != null) "Saved tile at $path" else "Could not save tile (check console)")
		return true
	}
	
	private fun commandWorldtomap(
		sender: CommandSender,
		worldName: String,
		mapName: String,
		x: Int,
		y: Int,
		z: Int,
		zoom: Int
	): Boolean {
		val worldCoords = WorldCoords(x, y, z)
		val map: DynmapWebAPI.Map = getMapFromWorldMapNames(sender, worldName, mapName) ?: return false
		val tileCoords = worldCoords.toTileCoords(map, zoom)
		sender.sendMessage(java.lang.String.format("%s is in tile %s", worldCoords, tileCoords))
		return true
	}
	
	override fun onTabComplete(
		sender: CommandSender,
		command: Command,
		label: String,
		args: Array<out String>
	): List<String>? = when {
		args.size == 1 -> listOf("now", "export", "reload", "debug", "worldtomap")
		// Suggest world
		args.size == 2 && (args[0] == "export" || args[0] == "worldtomap") ->
			plugin.worldConfiguration?.worlds?.map { it.name }
		// Suggest map
		args.size == 3 && (args[0] == "export" || args[0] == "worldtomap") ->
			plugin.worldConfiguration?.getWorldByName(args[1])?.maps?.map { it.name }.orEmpty()
		
		else -> emptyList()
	}
	
	private fun getMapFromWorldMapNames(sender: CommandSender, worldName: String, mapName: String): DynmapWebAPI.Map? {
		assert(plugin.worldConfiguration != null)
		val world = plugin.worldConfiguration?.getWorldByName(worldName)
		if (world == null) {
			sender.sendMessage("no world with name $worldName")
			return null
		}
		val map = world.getMapByName(mapName)
		if (map == null) {
			sender.sendMessage("world $worldName has no map with name $mapName")
			return null
		}
		return map
	}
}
