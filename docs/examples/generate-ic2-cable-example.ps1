param(
    [Parameter(Mandatory = $true)]
    [string]$PackRoot
)

$configurationDirectory = Join-Path $PackRoot 'configuration'
$modelDirectory = Join-Path $PackRoot 'resourcepack\assets\zako\models\block'
New-Item -ItemType Directory -Force -Path $configurationDirectory, $modelDirectory | Out-Null

$yaml = [System.Collections.Generic.List[string]]::new()
$itemName = -join ([char[]]@(0x771F, 0x5B9E, 0x7EBF, 0x7F06, 0x65B9, 0x5757))
$yaml.AddRange([string[]]@(
    'lang:',
    '  en_us:',
    '    block.zako.test_block: Copper Cable',
    '  zh_cn:',
    "    block.zako.test_block: $itemName",
    '',
    'items:',
    '  zako:test_block:',
    '    material: copper_ingot',
    '    data:',
    "      item-name: `"<!i><gold>$itemName`"",
    '    model: zako:block/test_block',
    '    behavior:',
    '      type: real_block_item',
    '      block: zako:test_block',
    '',
    'real_blocks:',
    '  zako:test_block:',
    '    connection-shape:',
    '      type: center-arms',
    '      radius: 2',
    '      property: connections',
    '    states:',
    '      properties:',
    '        connections:',
    '          type: int',
    '          min: 0',
    '          max: 63',
    '          default: 0',
    '      appearances:'
))
for ($mask = 0; $mask -lt 64; $mask++) {
    $yaml.Add("        `"cable_$mask`":")
    $yaml.Add('          auto-state: note_block')
    $yaml.Add('          model:')
    $yaml.Add("            path: zako:block/test_block_$mask")
}
$yaml.Add('      variants:')
for ($mask = 0; $mask -lt 64; $mask++) {
    $yaml.Add("        `"connections=$mask`":")
    $yaml.Add("          appearance: `"cable_$mask`"")
}
$yaml.AddRange([string[]]@(
    '    settings:',
    '      hardness: 1.5',
    '      resistance: 3.0',
    '      replaceable: false',
    '      is-redstone-conductor: false',
    '      is-suffocating: false',
    '      is-view-blocking: false',
    '      can-occlude: false',
    '      tags:',
    '        - minecraft:mineable/pickaxe',
    '      correct-tools:',
    '        - minecraft:wooden_pickaxe',
    '        - minecraft:stone_pickaxe',
    '        - minecraft:iron_pickaxe',
    '        - minecraft:golden_pickaxe',
    '        - minecraft:diamond_pickaxe',
    '        - minecraft:netherite_pickaxe',
    '      sounds:',
    '        break: minecraft:block.copper.break',
    '        fall: minecraft:block.copper.fall',
    '        hit: minecraft:block.copper.hit',
    '        place: minecraft:block.copper.place',
    '        step: minecraft:block.copper.step',
    '    behavior:',
    '      type: connectable_block',
    '      property: connections',
    '    loot:',
    '      template: default:loot_table/self'
))
[IO.File]::WriteAllLines(
    (Join-Path $configurationDirectory 'test_block.yml'),
    $yaml,
    [Text.UTF8Encoding]::new($false)
)

function New-CableElement([float[]]$From, [float[]]$To) {
    $faces = [ordered]@{}
    foreach ($face in @('down', 'up', 'north', 'south', 'west', 'east')) {
        $faces[$face] = [ordered]@{ texture = '#cable' }
    }
    return [ordered]@{
        from = $From
        to = $To
        faces = $faces
    }
}

function New-CableModel([int]$Mask) {
    $elements = [System.Collections.Generic.List[object]]::new()
    $elements.Add((New-CableElement @(6, 6, 6) @(10, 10, 10)))
    if (($Mask -band 1) -ne 0)  { $elements.Add((New-CableElement @(6, 0, 6) @(10, 6, 10))) }
    if (($Mask -band 2) -ne 0)  { $elements.Add((New-CableElement @(6, 10, 6) @(10, 16, 10))) }
    if (($Mask -band 4) -ne 0)  { $elements.Add((New-CableElement @(6, 6, 0) @(10, 10, 6))) }
    if (($Mask -band 8) -ne 0)  { $elements.Add((New-CableElement @(6, 6, 10) @(10, 10, 16))) }
    if (($Mask -band 16) -ne 0) { $elements.Add((New-CableElement @(0, 6, 6) @(6, 10, 10))) }
    if (($Mask -band 32) -ne 0) { $elements.Add((New-CableElement @(10, 6, 6) @(16, 10, 10))) }
    return [ordered]@{
        parent = 'minecraft:block/block'
        textures = [ordered]@{
            particle = 'minecraft:block/copper_block'
            cable = 'minecraft:block/copper_block'
        }
        elements = $elements
    }
}

for ($mask = 0; $mask -lt 64; $mask++) {
    $json = New-CableModel $mask | ConvertTo-Json -Depth 10
    [IO.File]::WriteAllText(
        (Join-Path $modelDirectory "test_block_$mask.json"),
        $json + [Environment]::NewLine,
        [Text.UTF8Encoding]::new($false)
    )
}

$itemModel = [ordered]@{
    parent = 'zako:block/test_block_48'
} | ConvertTo-Json
[IO.File]::WriteAllText(
    (Join-Path $modelDirectory 'test_block.json'),
    $itemModel + [Environment]::NewLine,
    [Text.UTF8Encoding]::new($false)
)
