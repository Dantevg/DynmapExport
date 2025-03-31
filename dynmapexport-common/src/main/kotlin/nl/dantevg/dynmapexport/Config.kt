package nl.dantevg.dynmapexport

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.dantevg.dynmapexport.location.TileCoords
import nl.dantevg.dynmapexport.location.WorldCoords
import kotlin.math.max
import kotlin.math.min

@Serializable
data class Config(
	@SerialName("dynmap-host")
	@YamlComment(
		"The hostname/ip and port of Dynmap.",
		"(use localhost for Dynmap running on the same server)"
	)
	val dynmapHost: String = "localhost:8123",
	
	@SerialName("auto-combine")
	@YamlComment(
		"Whether to automatically combine the tiles into a single image.",
		"Disabling this can reduce server lag if you encounter it (for large images),",
		"but you'll need to combine the tiles yourself.",
	)
	val autoCombine: Boolean = true,
	
	@YamlComment(
		"A list of export configurations. Example:",
		"  exports:",
		"  - world: world",
		"    map: surface",
		"    zoom: 0",
		"    # These thresholds determine whether to keep or discard a new export:",
		"    #   area-change-threshold is the minimum fraction of changed pixels.",
		"    #   colour-change-threshold is the minimum colour difference of two pixels.",
		"    area-change-threshold: 0.1",
		"    colour-change-threshold: 0.1",
		"    from:",
		"      x: -100",
		"      z: -100",
		"    to:",
		"      x: 100",
		"      z: 100",
	)
	val exports: List<ExportConfig> = emptyList(),
)

@Serializable
data class ExportConfig(
	val world: String,
	val map: String,
	val zoom: Int = 0,
	@SerialName("area-change-threshold")
	val areaChangeThreshold: Double = 0.1,
	@SerialName("colour-change-threshold")
	val colourChangeThreshold: Double = 0.1,
	val from: WorldCoords,
	val to: WorldCoords,
) {
	constructor(
		world: DynmapWebAPI.World,
		map: DynmapWebAPI.Map,
		zoom: Int,
		areaChangeThreshold: Double,
		colourChangeThreshold: Double,
		from: WorldCoords,
		to: WorldCoords = from,
	) : this(world.name, map.name, zoom, areaChangeThreshold, colourChangeThreshold, from, to)
	
	/**
	 * Get all tile locations from this export config.
	 *
	 * @return a list of tiles that are within the range from the config
	 */
	fun toTileLocations(map: DynmapWebAPI.Map): List<TileCoords> {
		val tiles: MutableList<TileCoords> = ArrayList()
		val (fromTile, toTile) = toMinMaxTileCoords(map)
		
		for (x in fromTile.x..toTile.x step (1 shl zoom)) {
			for (y in fromTile.y..toTile.y step (1 shl zoom)) {
				tiles += TileCoords(x, y)
			}
		}
		
		return tiles
	}
	
	fun toMinMaxTileCoords(map: DynmapWebAPI.Map): Pair<TileCoords, TileCoords> {
		val fromTile = from.toTileCoords(map, zoom)
		val toTile = to.toTileCoords(map, zoom)
		return Pair(
			TileCoords(min(fromTile.x, toTile.x), min(fromTile.y, toTile.y)).floorToZoom(zoom),
			TileCoords(max(fromTile.x, toTile.x), max(fromTile.y, toTile.y)).ceilToZoom(zoom)
		)
	}
}
