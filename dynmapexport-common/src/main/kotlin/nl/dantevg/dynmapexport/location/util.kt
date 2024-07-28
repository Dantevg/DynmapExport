package nl.dantevg.dynmapexport.location

import kotlin.math.ceil
import kotlin.math.floor

fun zoomedFloor(value: Double, zoom: Int): Int = floor(value / (1 shl zoom)).toInt() * (1 shl zoom)

fun zoomedCeil(value: Double, zoom: Int): Int = ceil(value / (1 shl zoom)).toInt() * (1 shl zoom)
