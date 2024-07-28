package nl.dantevg.dynmapexport.location

import nl.dantevg.dynmapexport.DynmapWebAPI
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class WorldCoordsTest {
	@DisplayName("World coordinates to projection 'flat' tile coordinates")
	@ParameterizedTest(name = "zoom {2} @ {0} -> {1}")
	@MethodSource("flatCoordinateProvider")
	fun toTileCoordsFlat(worldCoords: WorldCoords, tileCoords: TileCoords?, zoom: Int) {
		Assertions.assertEquals(tileCoords, worldCoords.toTileCoords(flat, zoom))
	}

	@DisplayName("World coordinates to projection 'surface' tile coordinates")
	@ParameterizedTest(name = "zoom {2} @ {0} -> {1}")
	@MethodSource("surfaceCoordinateProvider")
	fun toTileCoordsSurface(worldCoords: WorldCoords, tileCoords: TileCoords?, zoom: Int) {
		Assertions.assertEquals(tileCoords, worldCoords.toTileCoords(surface, zoom))
	}

	companion object {
		val flat: DynmapWebAPI.Map = DynmapWebAPI.Map(
			"flat", "flat", 0, doubleArrayOf(
				4.0, 0.0, 0.0,
				0.0, 0.0, -4.0,
				0.0, 1.0, 0.0
			)
		)

		val surface: DynmapWebAPI.Map = DynmapWebAPI.Map(
			"surface", "surface", 0, doubleArrayOf(
				11.31370849898476, 0.0, -11.313708498984761,
				-5.6568542494923815, 13.856406460551018, -5.656854249492381,
				0.0, 1.0, 0.0
			)
		)

		@JvmStatic
		fun flatCoordinateProvider(): Stream<Arguments> {
			return Stream.of(
				// Zoom 0
				Arguments.of(WorldCoords(0, 0, 0), TileCoords(0, -1), 0),
				Arguments.of(WorldCoords(31, 0, 0), TileCoords(0, -1), 0),
				Arguments.of(WorldCoords(0, 0, 31), TileCoords(0, -1), 0),
				Arguments.of(WorldCoords(31, 0, 31), TileCoords(0, -1), 0),

				Arguments.of(WorldCoords(0, 0, -32), TileCoords(0, 0), 0),
				Arguments.of(WorldCoords(31, 0, -32), TileCoords(0, 0), 0),
				Arguments.of(WorldCoords(0, 0, -1), TileCoords(0, 0), 0),
				Arguments.of(WorldCoords(31, 0, -1), TileCoords(0, 0), 0),

				// Zoom 1
				Arguments.of(WorldCoords(0, 0, -32), TileCoords(0, 0), 1),
				Arguments.of(WorldCoords(63, 0, -32), TileCoords(0, 0), 1),
				Arguments.of(WorldCoords(0, 0, 31), TileCoords(0, 0), 1),
				Arguments.of(WorldCoords(63, 0, 31), TileCoords(0, 0), 1),

				Arguments.of(WorldCoords(0, 0, -96), TileCoords(0, 2), 1),
				Arguments.of(WorldCoords(63, 0, -96), TileCoords(0, 2), 1),
				Arguments.of(WorldCoords(0, 0, -33), TileCoords(0, 2), 1),
				Arguments.of(WorldCoords(63, 0, -33), TileCoords(0, 2), 1),

				Arguments.of(WorldCoords(0, 0, 32), TileCoords(0, -2), 1),
				Arguments.of(WorldCoords(63, 0, 32), TileCoords(0, -2), 1),
				Arguments.of(WorldCoords(0, 0, 95), TileCoords(0, -2), 1),
				Arguments.of(WorldCoords(63, 0, 95), TileCoords(0, -2), 1),

				Arguments.of(WorldCoords(0, 0, 96), TileCoords(0, -4), 1),
				Arguments.of(WorldCoords(63, 0, 96), TileCoords(0, -4), 1),
				Arguments.of(WorldCoords(0, 0, 159), TileCoords(0, -4), 1),
				Arguments.of(WorldCoords(63, 0, 159), TileCoords(0, -4), 1),

				Arguments.of(WorldCoords(64, 0, -32), TileCoords(2, 0), 1),
				Arguments.of(WorldCoords(127, 0, -32), TileCoords(2, 0), 1),
				Arguments.of(WorldCoords(64, 0, 31), TileCoords(2, 0), 1),
				Arguments.of(WorldCoords(127, 0, 31), TileCoords(2, 0), 1),

				// Zoom 2
				Arguments.of(WorldCoords(0, 0, -32), TileCoords(0, 0), 2),
				Arguments.of(WorldCoords(127, 0, -32), TileCoords(0, 0), 2),
				Arguments.of(WorldCoords(0, 0, 95), TileCoords(0, 0), 2),
				Arguments.of(WorldCoords(127, 0, 95), TileCoords(0, 0), 2)
			)
		}

		@JvmStatic
		fun surfaceCoordinateProvider(): Stream<Arguments> {
			return Stream.of( // Zoom 0
				Arguments.of(WorldCoords(0, 97, -75), TileCoords(6, 13), 0)
			)
		}
	}
}
