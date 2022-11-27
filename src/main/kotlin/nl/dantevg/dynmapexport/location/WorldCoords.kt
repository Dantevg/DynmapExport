package nl.dantevg.dynmapexport.location

import nl.dantevg.dynmapexport.DynmapWebAPI

data class WorldCoords(val x: Int, val y: Int, val z: Int) {
	fun toTileCoords(map: DynmapWebAPI.Map, zoom: Int): TileCoords {
		val unscaledX = (map.worldtomap[0] * x
				+ map.worldtomap[1] * y
				+ map.worldtomap[2] * z)
		val tileX = TileCoords.zoomedCeil(unscaledX / SCALE_FACTOR - 1, zoom)
		
		val unscaledY = (map.worldtomap[3] * x
				+ map.worldtomap[4] * y
				+ map.worldtomap[5] * z)
		val tileY = TileCoords.zoomedCeil(unscaledY / SCALE_FACTOR - 1, zoom)
		
		return TileCoords(tileX, tileY)
	}
	
	override fun toString(): String = x.toString() + SEPARATOR + y + SEPARATOR + z
	
	companion object {
		const val SCALE_FACTOR = 128
		const val SEPARATOR = ","
	}
}
