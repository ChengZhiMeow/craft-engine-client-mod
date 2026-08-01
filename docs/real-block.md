# CraftEngine `real_blocks`

`real_blocks` is a complete CraftEngine block configuration section. It accepts the same `state` / `states`,
`settings`, `behavior`, `loot` and event configuration as the native `blocks` section, plus the real registry
shape keys. A matching item uses `behavior.type: real_block_item`.

For every configured entry, the Ignite mod and Real Block Paper plugin:

1. resolves the normal CraftEngine block and all of its internal states;
2. replaces the corresponding injected server registry entry with the requested ID;
3. preserves the raw state IDs while combining multi-state CE blocks under a synthetic `ce_state` property;
4. binds every CE state back to its normal settings and behavior and installs its configured server voxel shape;
5. writes CraftEngine's generated block-state models only under the requested registry ID and removes the
   corresponding `craftengine:custom_<index>` blockstate assets from the final pack;
6. synchronizes the same ID, state mapping and shape to the NeoForge client during the configuration phase,
   before play-state chunks are decoded.

The server chunk palette and the client registry therefore both use the real ID, such as
`zako:test_block[ce_state=0]`. The companion writes
`plugins/CraftEngine/real_blocks.registry.json`; Ignite consumes that manifest before Paper loads worlds on
subsequent starts, so saved palettes can resolve the real ID before CE's optional delayed configuration pass.
The first successful start creates this manifest. Before placing real blocks in a permanent world, stop and
start the server once more and verify the log contains
`Installed persisted real block <id> before world loading`.

## Minimal example

```yaml
items:
  zako:test_block:
    material: copper_ingot
    data:
      item-name: "<!i><gold>真实线缆方块"
    model: zako:block/test_block
    behavior:
      type: real_block_item
      block: zako:test_block

lang:
  en_us:
    block.zako.test_block: Copper Cable
  zh_cn:
    block.zako.test_block: 真实线缆方块

real_blocks:
  zako:test_block:
    collision:
      - [0, 6, 6, 16, 10, 10]
    state:
      auto-state: tripwire
      model:
        path: zako:block/test_block
```

The item model path deliberately points at a small wrapper model instead of one of the 64 state models:

```json
{
  "parent": "zako:block/test_block_48"
}
```

Save it as `resourcepack/assets/zako/models/block/test_block.json`. State `48` is the horizontal
west/east cable used for inventory and hand rendering. The `lang` entries target the real registry
translation key directly, so Jade displays the configured name instead of `block.zako.test_block`.

Each box uses block-model coordinates:

```text
[min_x, min_y, min_z, max_x, max_y, max_z]
```

All coordinates must be finite and within `0..16`, and every minimum must be lower than its maximum.
Up to 64 boxes can be combined for each shape.

Optional shape keys:

- `outline`: selection/ray-trace outline; defaults to `collision`.
- `support`: face support shape; defaults to `collision`.
- `occlusion`: server and client occlusion shape; defaults to empty.

To vary the hitbox by state, use `shapes`. A selector contains one or more `property=value` pairs; omitted
properties are wildcards.

```yaml
real_blocks:
  zako:test_block:
    shapes:
      "facing=north":
        collision: [[6, 6, 0, 10, 10, 16]]
      "facing=south":
        collision: [[6, 6, 0, 10, 10, 16]]
      "facing=east":
        collision: [[0, 6, 6, 16, 10, 10]]
      "facing=west":
        collision: [[0, 6, 6, 16, 10, 10]]
      "facing=up":
        collision: [[6, 0, 6, 10, 16, 10]]
      "facing=down":
        collision: [[6, 0, 6, 10, 16, 10]]
```

Each selector may define `outline`, `support`, and `occlusion` in addition to the required `collision`. A
top-level shape can coexist with `shapes` as the fallback for unmatched states. Without a fallback, every state
must match a selector and misspelled properties are rejected during loading. When selectors overlap, the one
with more property constraints wins.

## IC2-style six-way cable

The generic `connection-shape` module can generate a center body plus one arm for every set bit in a six-bit
connection mask. The IC2-style cable is one adapter that uses `type: center-arms`; pipes, wires, and other
connectable blocks can reuse the same interface. `radius` is the half-width around block center and must be
greater than 0 and less than 8.

```yaml
real_blocks:
  zako:test_block:
    connection-shape:
      type: center-arms
      radius: 2
      property: connections
    states:
      properties:
        connections:
          type: int
          min: 0
          max: 63
          default: 0
      appearances:
        cable_0:
          auto-state: note_block
          model:
            path: zako:block/test_block_0
        # Define cable_1 through cable_63 in the same way.
      variants:
        connections=0:
          appearance: cable_0
        # Map connections=1 through connections=63.
    behavior:
      type: connectable_block
      property: connections
```

The bit order is `down`, `up`, `north`, `south`, `west`, `east`, matching Minecraft's 3D direction indices.
`connectable_block` updates the mask when an adjacent block with the same CraftEngine ID is placed or removed. The
server's native state, the client model, selection outline, support shape, and collision shape all use that same
mask. The cable example uses the larger `note_block` visual-state pool because a complete cable consumes 64
appearances and the shared `tripwire` pool only has 126 slots. The complete generated example and all 64 models are created by
`docs/examples/generate-ic2-cable-example.ps1`.

Existing `cable` shape sections and `cable_block` behaviors remain accepted as compatibility aliases. New
configurations should use `connection-shape` and `connectable_block`.

For behavior, model, loot and shape-only changes, run `/ce reload all`; connected compatible clients receive the
updated mapping and reload their active resource packs. Adding/removing a `real_blocks` entry, changing its CE state
count, changing `block.serverside-blocks`, or changing Minecraft versions requires a full server restart. When a
persisted entry changes state count, the first start writes the new topology manifest and reports that one more
restart is required. Start the server a second time before players join; Ignite then installs the new topology
before worlds load.

The server must be launched through Ignite and must contain both Real Block components:

- `mods/[默米-资源包引擎-真方块]craft-engine-real-block-ignite-<version>.jar`
- `plugins/[默米-资源包引擎-真方块]craft-engine-real-block-paper-<version>.jar`

For a 1.21.10 client connecting to the 1.21.11 server through ViaBackwards, also install:

- `plugins/[跨版本-CE支持]craft-engine-via-compatibility-<version>.jar`

Starting Paper's original JAR directly does not load the Ignite mod. On the supplied test server, use the separate
`start_1_21_11_ignite_real_block.bat`; a correct startup reports Java 21 and Ignite's discovered mod before Paper
initializes plugins.

Client `config/craftengine/config.yml` has `enable-real-block: true` by default. This option also enables
CraftEngine's extended client block-state stream, even if the older `enable-client-custom-block` option is off.

## Collision API

Add the release repository and the API-only dependency to the consuming plugin:

```kotlin
repositories {
    maven("https://repo.xmxcraft.cn/releases")
}

dependencies {
    compileOnly("net.momirealms:craft-engine-real-block-api:26.7.1-realblock.11")
}
```

Other Paper plugins can obtain the public API from Bukkit's services manager. Declare
`CraftEngineRealBlock` as a server dependency with `join-classpath: true`, then load the service:

```java
CraftEngineRealBlockApi api = Bukkit.getServicesManager()
        .load(CraftEngineRealBlockApi.class);
if (api == null) {
    throw new IllegalStateException("CraftEngineRealBlock API is unavailable");
}

api.getBlockCollision("zako:test_block").ifPresent(collision -> {
    for (BlockCollision.State state : collision.states()) {
        List<CollisionBox> boxes = state.boxes();
        // state.stateIndex() is the zero-based ce_state value.
    }
});

Optional<List<CollisionBox>> stateBoxes =
        api.getCollisionBoxes("zako:test_block", 0);

Optional<List<CollisionBox>> liveBlockBoxes =
        api.getCollisionBoxes(bukkitBlock);

Optional<List<CollisionBox>> snapshotBoxes =
        api.getCollisionBoxes(bukkitBlockState);
```

The API types are in `net.momirealms.craftengine.realblock.api`. Box coordinates use CraftEngine's
block-model coordinate system (`0..16`). An unknown block ID or state index returns `Optional.empty()`;
a known state without collision returns a present, empty list. API results are immutable snapshots and
are replaced after `/ce reload all`.
The `Block` overload reads the live world and must run on the block's owning server/region thread. The
`BlockState` overload resolves the state stored in that Bukkit snapshot.
