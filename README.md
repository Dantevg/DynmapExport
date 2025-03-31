# DynmapExport
This is a Spigot plugin that can automatically export dynmap tiles at a set
interval. It will only export when at least one tile in an export configuration
has changed since the last export.

## Known issues
- The in-game to tile coordinate conversion is not 100% correct. Should be fine
  for top-down maps, but may be noticeable for perspective maps.

## Config file
### `dynmap-host`
The hostname/ip and port on which dynmap is set to run. `localhost:8123` by
default.

### `auto-combine`
Whether to automatically combine the Dynmap tiles into one single image.
If you have set a large area and you encounter lag when exporting, you can try
disabling this.  Note that you will have to combine the tiles yourself.

### `exports`
A list of export configurations. Each configuration has the following structure:
- `world`: the name of the world
- `map`: the name of the map
- `zoom`: the zoom-out level, 0 is fully zoomed in.
- `area-change-threshold`: The minimum fraction of pixels to change before the
  automatic export saves the result.
- `colour-change-threshold`: The minimum difference in colour of two pixels for
  them to be considered changed. This is especially useful for maps generated
  by ChunkyMap, since they tend to have lots of unnoticeable changes in noise.
  Changes in dark colours are weighed heavier than changes in light colours.
- `from` and `to`: the in-game coordinates that specify the (inclusive) range
  of tiles to export.
  - `x`, `y` and `z`: the in-game block coordinates

## Command
- `/dynmapexport now`: export all configurations immediately, separate from the
  scheduler.
- `/dynmapexport export <world> <map> <x> <z> <zoom>`: export a single tile at
  the given **world coordinates** (not tile coordinates), at y-level 64.
- `/dynmapexport reload`: reload the plugin and config-file.
- `/dynmapexport worldtomap <world> <map> <x> <y> <z> [zoom]`: get the tile
  coordinates of the given world coordinates.
- `/dynmapexport purge [all]`: delete all old exports, or delete everything
  (also the latest export) if `all` is given.
