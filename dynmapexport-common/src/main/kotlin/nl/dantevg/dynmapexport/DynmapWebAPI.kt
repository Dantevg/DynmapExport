package nl.dantevg.dynmapexport

import kotlinx.serialization.Serializable

object DynmapWebAPI {
	@Serializable
	class Configuration(val worlds: List<World>) {
		fun getWorldByName(name: String): World? =
			worlds.stream()
				.filter { it.name == name }
				.findAny()
				.orElse(null)
		
		fun getMapByName(world: String, map: String): Map? =
			getWorldByName(world)?.getMapByName(map)
	}
	
	@Serializable
	class World(val name: String, val maps: List<Map>) {
		fun getMapByName(name: String): Map? =
			maps.stream()
				.filter { it.name == name }
				.findAny()
				.orElse(null)
	}
	
	@Serializable
	data class Map(val name: String, val prefix: String, val scale: Int, val worldtomap: DoubleArray)
}
