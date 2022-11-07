# DynmapExport
This is a Spigot plugin that can automatically export dynmap tiles at a set
interval. It will only export when at least one tile in an export configuration
has changed since the last export.

## Config file
### `dynmap-host`
The hostname/ip and port on which dynmap is set to run. `localhost:8123` by
default.

### `change-treshold`
The minimum fraction of pixels to change before the automatic export saves the
result.

### `auto-combine`
Whether to automatically combine the Dynmap tiles into one single image.
If you have set a large area and you encounter lag when exporting, you can try
disabling this.  Note that you will have to combine the tiles yourself.

### `schedule`
A simplified ISO-8601 formatted interval at which to automatically export all
configurations.
> For example: `10m`, `1h30m`, `1d`

### `exports`
A list of export configurations. Each configuration has the following structure:
- `world`: the name of the world
- `map`: the name of the map
- `zoom`: the zoom-out level, 0 is fully zoomed in.
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
