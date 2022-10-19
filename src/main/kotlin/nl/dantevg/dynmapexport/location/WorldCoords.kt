package nl.dantevg.dynmapexport.location

import nl.dantevg.dynmapexport.DynmapWebAPI

data class WorldCoords(val x: Int, val y: Int, val z: Int) {
	fun toTileCoords(map: DynmapWebAPI.Map, zoom: Int): TileCoords {
		val unscaledX = (map.worldtomap[0].toInt() * x
				+ map.worldtomap[1].toInt() * y
				+ map.worldtomap[2].toInt() * z)
		val tileX = TileCoords.zoomedFloor(unscaledX.toDouble() / SCALE_FACTOR, zoom)
		
		val unscaledY = (map.worldtomap[3].toInt() * x
				+ map.worldtomap[4].toInt() * y
				+ map.worldtomap[5].toInt() * z)
		val tileY = TileCoords.zoomedCeil(unscaledY.toDouble() / SCALE_FACTOR - 1, zoom)
		
		return TileCoords(tileX, tileY)
	}
	
	override fun toString(): String = x.toString() + SEPARATOR + y + SEPARATOR + z
	
	companion object {
		const val SCALE_FACTOR = 128
		const val SEPARATOR = ","
	}
}
