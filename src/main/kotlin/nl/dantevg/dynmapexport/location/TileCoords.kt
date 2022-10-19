package nl.dantevg.dynmapexport.location

import kotlin.math.ceil
import kotlin.math.floor

data class TileCoords(val x: Int, val y: Int) {
	val tileGroupCoords: TileGroupCoords
		get() = TileGroupCoords(x shr 5, y shr 5)
	
	fun floorToZoom(zoom: Int): TileCoords =
		TileCoords(zoomedFloor(x.toDouble(), zoom), zoomedFloor(y.toDouble(), zoom))
	
	fun ceilToZoom(zoom: Int): TileCoords =
		TileCoords(zoomedCeil(x.toDouble(), zoom), zoomedCeil(y.toDouble(), zoom))
	
	override fun toString(): String = x.toString() + SEPARATOR + y
	
	companion object {
		const val SEPARATOR = ","
		
		fun zoomedFloor(value: Double, zoom: Int): Int = floor(value / (1 shl zoom)).toInt() * (1 shl zoom)
		
		fun zoomedCeil(value: Double, zoom: Int): Int = ceil(value / (1 shl zoom)).toInt() * (1 shl zoom)
		
		fun parse(str: String): TileCoords {
			val separator = str.indexOf(SEPARATOR)
			val x = Integer.parseInt(str.substring(0, separator - 1))
			val y = Integer.parseInt(str.substring(separator + 1))
			return TileCoords(x, y)
		}
	}
}
