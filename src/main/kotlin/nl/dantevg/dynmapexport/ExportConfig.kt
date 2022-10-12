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

    constructor(world: DynmapWebAPI.World, map: DynmapWebAPI.Map, zoom: Int, from: TileCoords, to: TileCoords) {
        this.world = world
        this.map = map
        this.zoom = zoom
        this.from = TileCoords(min(from.x, to.x), min(from.y, to.y)).floorToZoom(zoom)
        this.to = TileCoords(max(from.x, to.x), max(from.y, to.y)).ceilToZoom(zoom)
    }

    constructor(world: DynmapWebAPI.World, map: DynmapWebAPI.Map, zoom: Int, tile: TileCoords) :
            this(world, map, zoom, tile, tile)

    constructor(world: DynmapWebAPI.World, map: DynmapWebAPI.Map, zoom: Int, from: WorldCoords, to: WorldCoords) :
            this(world, map, zoom, from.toTileCoords(map, zoom), to.toTileCoords(map, zoom))

    constructor(world: DynmapWebAPI.World, map: DynmapWebAPI.Map, zoom: Int, coords: WorldCoords) :
            this(world, map, zoom, coords, coords)
}