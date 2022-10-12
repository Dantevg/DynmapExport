package nl.dantevg.dynmapexport

import kotlin.collections.List

object DynmapWebAPI {
    class Configuration(val worlds: List<World>) {
        fun getWorldByName(name: String): World? =
            worlds.stream()
                .filter { it.name == name }
                .findAny()
                .orElse(null)
    }

    class World(val name: String, val maps: List<Map>) {
        fun getMapByName(name: String): Map? =
            maps.stream()
                .filter { it.name == name }
                .findAny()
                .orElse(null)
    }

    data class Map(val name: String, val prefix: String, val scale: Int, val worldtomap: DoubleArray)
}