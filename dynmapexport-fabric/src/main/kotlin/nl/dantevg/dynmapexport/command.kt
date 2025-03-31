package nl.dantevg.dynmapexport

import net.silkmc.silk.commands.command
import net.silkmc.silk.core.text.literal

val command = command(DynmapExportMod.MOD_ID) {
	literal("now") runsAsync {
		DynmapExportMod.export { source.sendMessage(it.literal) }
	}
	
	literal("reload") runs {
		DynmapExportMod.reload()
		if (source.isExecutedByPlayer) source.sendMessage("Reload complete".literal)
	}
	
	literal("export") {
		argument<String>("world") { world ->
			suggestList { DynmapExportMod.worldConfiguration?.worlds?.map { it.name } ?: emptyList() }
			
			argument<String>("map") { map ->
				suggestList { ctx ->
					DynmapExportMod.worldConfiguration?.getWorldByName(world(ctx))?.maps?.map { it.name } ?: emptyList()
				}
				
				argument<Int>("x") { x ->
					argument<Int>("z") { z ->
						argument<Int>("zoom") { zoom ->
							runsAsync {
								val path: String? = try {
									DynmapExportMod.downloader.downloadTile(world(), map(), x(), z(), zoom())
								} catch (e: IllegalArgumentException) {
									source.sendError(("Could not save tile: " + e.message).literal)
									return@runsAsync
								}
								source.sendMessage(if (path != null) "Saved tile at $path".literal else "Could not save tile (check console)".literal)
							}
						}
					}
				}
			}
		}
	}
	
	literal("purge") {
		runs {
			source.sendMessage("Warning: this will permanently delete all but the last export. To confirm, run /${DynmapExportMod.MOD_ID} purge confirm".literal)
		}
		literal("confirm") runs {
			DynmapExportMod.purge(false)
		}
		
		literal("all") {
			runs {
				source.sendMessage("Warning: this will permanently delete all exports. To confirm, run /${DynmapExportMod.MOD_ID} purge all confirm".literal)
			}
			literal("confirm") runs {
				DynmapExportMod.purge(true)
			}
		}
	}
}
