package nl.dantevg.dynmapexport.location

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

		fun parse(str: String): TileCoords {
			val separator = str.indexOf(SEPARATOR)
			val x = Integer.parseInt(str.substring(0, separator - 1))
			val y = Integer.parseInt(str.substring(separator + 1))
			return TileCoords(x, y)
		}
	}
}
