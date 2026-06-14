# Quill

> A desktop cache editing suite for RuneScape 2. Browse definitions, edit item and NPC data, preview 3D models, and export cache assets for use in external tools.

Built with **Java Swing**, **FlatLaf**, **Kotlin / Java**, and **Gradle**.

## Known Bugs:
- The NPC editor layout and animation previews are currently broken due to a refactor of the code. Will be updating and removing this message at some point today.

## Features

**Item Editor**: Browse and edit item definitions including name, stackable, value, equipment models, inventory options, recolor mappings, and parameters. Preview inventory sprites with cache-accurate rotation, zoom, offsets, and recolors. Preview 3D models in an interactive viewer. View, save, replace, and export individual item model data.

**NPC Editor**: Browse and edit NPC definitions: name, combat level, size, render animation, flags, options, model IDs, and recolors. Add, remove, and reorder model entries. Match worn item model IDs to item names. Preview composite NPC models with render animation transforms, recolors, textures, resize settings, and idle animation frames.

**Model Editor**: View and edit individual cache models. Supports model data inspection, face/texture manipulation, color preview, and direct model saving.

**Object Editor**: Browse and edit object definitions including size, flags, transforms, model assignments, animations, and sound effects.

**Texture Editor**: Browse all texture definitions in the cache, view texture previews, and save textures as PNG.

**Sprite Editor**: Browse sprite archives, view individual frames, edit palette colors, and save sprites as PNG.

**Model Viewer & Renderer**: Interactive preview panel with mouse rotation, wheel zoom, and reset. Software-based renderer handles cache HSL colors, face alpha, textures, render types, and basic lighting. Supports composite models, recolors, retextures, NPC model translations, render animation transforms, and animation frame transforms. LWJGL-accelerated canvas for high-performance preview.

**Model Dumper**: Batch export all cache models from index 7 as raw `.dat` or `.mqo` files.

## Getting Started

1. Launch Quill and select a cache folder when prompted.
2. Use **Tools** from the menu bar to open editors.
3. Make changes and click **Save** to write back to the cache.

> The cache path is fully configurable; no special location required.

## Tools

| Menu | Tool |
|---|---|
| `Tools > Item Editor` | Browse and edit item definitions |
| `Tools > NPC Editor` | Browse and edit NPC definitions |
| `Tools > Object Editor` | Browse and edit object definitions |
| `Tools > Model Editor` | Inspect and edit cache models |
| `Tools > Texture Editor` | View textures |
| `Tools > Sprite Editor` | View sprites |
| `Tools > Model Dumper` | Export all models |

## Project Layout

```
src/main/java/com/desecratedtree/quill
  animation/  Animation base and frame definitions
  cache/      Cache access, decoding, and definition actions
  defs/       Item, NPC, object, sequence, and render animation definitions
  io/         Cache definition encoding and stream utilities
  render/     Model decoding bridge, software renderer, model export, preview panels, OpenGL canvas
  sprite/     Indexed sprite archive container and codec
  texture/    Texture and material definitions, loading, and UV mapping
  tools/      CLI dump tools (item defs, sprites, textures)
  ui/         Swing editor windows and panels
  util/       Platform and path helpers

src/main/java/io/blurite/cache/model
  Model decoding, encoding, conversion, and merge support

src/main/java/net/rsprot/buffer/extensions
  Buffer smart encoding helpers
```

## Roadmap

- Texture definition improvements and full texture rendering fidelity
- Texture mapping and animation overhaul
- UI layout redesigns
- Queued cache writes
- Cache rebuilding (reduce file size without breaking references)
- OSRS support
- Large model rendering performance (30k+ vertices) with culling
- Map editor (RSPSi-based)
- World Map editor (labels, map rendering)
- Cross-cache packing (e.g. 718 to 667)

## FAQ

> **I lost my cache data!**
>
> **Always back up your cache before editing.** The author is not responsible for data loss.

## Development Notes

- The renderer is intentionally software-based so previews don't depend on OpenGL (LWJGL canvas available for performance).
- Item and NPC preview code uses cache definitions as source of truth; avoid duplicating definition logic in UI classes.
