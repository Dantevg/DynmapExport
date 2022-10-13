package nl.dantevg.dynmapexport

import nl.dantevg.dynmapexport.location.TileCoords
import nl.dantevg.dynmapexport.location.WorldCoords
import kotlin.math.max
import kotlin.math.min

class ExportConfig {
    val world: DynmapWebAPI.World
    val map: DynmapWebAPI.Map
    val zoom: Int

    val from: TileCoords
    val to: TileCoords

    constructor(world: DynmapWebAPI.World, map: DynmapWebAPI.Map, zoom: Int, from: TileCoords, to: TileCoords = from) {
        this.world = world
        this.map = map
        this.zoom = zoom
        this.from = TileCoords(min(from.x, to.x), min(from.y, to.y)).floorToZoom(zoom)
        this.to = TileCoords(max(from.x, to.x), max(from.y, to.y)).ceilToZoom(zoom)
    }

    constructor(
        world: DynmapWebAPI.World,
        map: DynmapWebAPI.Map,
        zoom: Int,
        from: WorldCoords,
        to: WorldCoords = from
    ) : this(world, map, zoom, from.toTileCoords(map, zoom), to.toTileCoords(map, zoom))

    /**
     * Get all tile locations from this export config.
     *
     * @return a list of tiles that are within the range from the config
     */
    fun toTileLocations(): List<TileCoords> {
        val tiles: MutableList<TileCoords> = ArrayList()

        for (x in from.x..to.x step (1 shl zoom)) {
            for (y in from.y..to.y step (1 shl zoom)) {
                tiles += TileCoords(x, y)
            }
        }

        return tiles
    }
}