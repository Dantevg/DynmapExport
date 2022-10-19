package nl.dantevg.dynmapexport.location

data class TileGroupCoords(val x: Int, val y: Int) {
	override fun toString(): String = x.toString() + SEPARATOR + y
	
	companion object {
		const val SEPARATOR = "_"
	}
}
