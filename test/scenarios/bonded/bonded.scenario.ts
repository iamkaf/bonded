import { Capability, Readiness, describe, expect, test } from "@teakit/test";
import type { ScenarioDefinition } from "@teakit/test";

const bondedScenarios = [
  {
    "name": "bonded-adjacent-storage-benches",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 0"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 9 70 3 minecraft:stone replace"
      },
      {
        "action": "command",
        "command": "/fill -3 71 -3 9 76 3 minecraft:air replace"
      },
      {
        "action": "command",
        "command": "/setblock 0 71 0 bonded:repair_bench"
      },
      {
        "action": "command",
        "command": "/setblock 1 71 0 minecraft:barrel"
      },
      {
        "action": "command",
        "command": "/item replace block 1 71 0 container.0 with minecraft:iron_ingot 1"
      },
      {
        "action": "command",
        "command": "/setblock 4 71 0 bonded:tool_bench"
      },
      {
        "action": "command",
        "command": "/setblock 5 71 0 bonded:repair_bench"
      },
      {
        "action": "command",
        "command": "/setblock 6 71 0 minecraft:chest[facing=north]"
      },
      {
        "action": "command",
        "command": "/item replace block 6 71 0 container.0 with minecraft:copper_ingot 1"
      },
      {
        "action": "command",
        "command": "/setblock 7 71 0 bonded:repair_bench"
      },
      {
        "action": "command",
        "command": "/setblock 8 71 0 minecraft:copper_chest[facing=north]"
      },
      {
        "action": "command",
        "command": "/item replace block 8 71 0 container.0 with minecraft:iron_ingot 1"
      },
      {
        "action": "wait_for_block",
        "x": 0,
        "y": 71,
        "z": 0,
        "blockId": "bonded:repair_bench",
        "timeoutMs": 3000
      },
      {
        "action": "wait_for_block",
        "x": 4,
        "y": 71,
        "z": 0,
        "blockId": "bonded:tool_bench",
        "timeoutMs": 3000
      }
    ],
    "steps": [
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_pickaxe[minecraft:damage=1]"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 -1"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_pickaxe",
        "slot": 0,
        "damage": 0
      },
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_pickaxe[minecraft:damage=1]"
      },
      {
        "action": "command",
        "command": "/tp @s 8 72 -1"
      },
      {
        "action": "use_block_server",
        "x": 7,
        "y": 71,
        "z": 0,
        "direction": "east",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_pickaxe",
        "slot": 0,
        "damage": 0
      },
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:stone_pickaxe"
      },
      {
        "action": "assert_command_success",
        "command": "/bonded xp set @s 10 levels"
      },
      {
        "action": "command",
        "command": "/tp @s 4 72 -1"
      },
      {
        "action": "use_block_server",
        "x": 4,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:copper_pickaxe",
        "slot": 0,
        "count": 1
      },
      {
        "action": "wait_ms",
        "durationMs": 250
      },
      {
        "action": "screenshot",
        "name": "bonded-adjacent-storage-benches",
        "hideOverlay": true
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 9 76 3 minecraft:air replace"
      }
    ]
  },
  {
    "name": "bonded-innate-loot-bond",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode creative @s"
      }
    ],
    "steps": [
      {
        "action": "command",
        "command": "/loot give @s loot bonded:test/innate_bond_chest"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 0
      },
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/summon zombie 0 72 0 {NoAI:1b,PersistenceRequired:1b}"
      },
      {
        "action": "wait_ms",
        "durationMs": 250
      },
      {
        "action": "command",
        "command": "/item replace entity @e[type=minecraft:zombie,limit=1,sort=nearest] weapon.mainhand with minecraft:iron_shovel"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 from entity @e[type=minecraft:zombie,limit=1,sort=nearest] weapon.mainhand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "damage": 0
      },
      {
        "action": "screenshot",
        "name": "bonded-innate-loot-bond",
        "hideOverlay": true
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/kill @e[type=minecraft:zombie,distance=..16]"
      }
    ]
  },
  {
    "name": "bonded-over-repair",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode survival @s"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 0"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 4 70 3 minecraft:stone replace"
      },
      {
        "action": "command",
        "command": "/fill -3 71 -3 4 76 3 minecraft:air replace"
      },
      {
        "action": "command",
        "command": "/setblock 0 71 0 bonded:repair_bench"
      },
      {
        "action": "command",
        "command": "/setblock 1 71 0 minecraft:barrel"
      },
      {
        "action": "command",
        "command": "/item replace block 1 71 0 container.0 with minecraft:iron_ingot 1"
      },
      {
        "action": "wait_for_block",
        "x": 0,
        "y": 71,
        "z": 0,
        "blockId": "bonded:repair_bench",
        "timeoutMs": 3000
      }
    ],
    "steps": [
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "damage": 0,
        "maxDamage": 251
      },
      {
        "action": "command",
        "command": "/tp @s 2 72 -1"
      },
      {
        "action": "wait_ms",
        "durationMs": 250
      },
      {
        "action": "command",
        "command": "/setblock 2 71 0 minecraft:dirt"
      },
      {
        "action": "wait_for_block",
        "x": 2,
        "y": 71,
        "z": 0,
        "blockId": "minecraft:dirt",
        "timeoutMs": 3000
      },
      {
        "action": "wait_ms",
        "durationMs": 500
      },
      {
        "action": "break_block",
        "x": 2,
        "y": 71,
        "z": 0,
        "direction": "up",
        "timeoutMs": 5000
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "damage": 0,
        "maxDamage": 250
      },
      {
        "action": "wait_ms",
        "durationMs": 250
      },
      {
        "action": "screenshot",
        "name": "bonded-over-repair",
        "hideOverlay": true
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode creative @s"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 4 76 3 minecraft:air replace"
      }
    ]
  },
  {
    "name": "bonded-over-repair-cap",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode survival @s"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 0"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 3 70 3 minecraft:stone replace"
      },
      {
        "action": "command",
        "command": "/fill -3 71 -3 3 76 3 minecraft:air replace"
      },
      {
        "action": "command",
        "command": "/setblock 0 71 0 bonded:repair_bench"
      },
      {
        "action": "wait_for_block",
        "x": 0,
        "y": 71,
        "z": 0,
        "blockId": "bonded:repair_bench",
        "timeoutMs": 3000
      }
    ],
    "steps": [
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=1]"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with minecraft:iron_ingot 7"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 0,
        "maxDamage": 500
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_ingot",
        "slot": 1,
        "count": 1
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode creative @s"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 3 76 3 minecraft:air replace"
      }
    ]
  },
  {
    "name": "bonded-over-repair-visual",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode survival @s"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 0"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 4 70 3 minecraft:stone replace"
      },
      {
        "action": "command",
        "command": "/fill -3 71 -3 4 76 3 minecraft:air replace"
      },
      {
        "action": "command",
        "command": "/setblock 0 71 0 bonded:repair_bench"
      },
      {
        "action": "command",
        "command": "/setblock 1 71 0 minecraft:barrel"
      },
      {
        "action": "command",
        "command": "/item replace block 1 71 0 container.0 with minecraft:iron_ingot 8"
      },
      {
        "action": "wait_for_block",
        "x": 0,
        "y": 71,
        "z": 0,
        "blockId": "bonded:repair_bench",
        "timeoutMs": 3000
      }
    ],
    "steps": [
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=1]"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "damage": 0,
        "maxDamage": 449
      },
      {
        "action": "command",
        "command": "/tp @s 2 72 -1"
      },
      {
        "action": "wait_ms",
        "durationMs": 250
      },
      {
        "action": "command",
        "command": "/setblock 2 71 0 minecraft:dirt"
      },
      {
        "action": "wait_for_block",
        "x": 2,
        "y": 71,
        "z": 0,
        "blockId": "minecraft:dirt",
        "timeoutMs": 3000
      },
      {
        "action": "wait_ms",
        "durationMs": 500
      },
      {
        "action": "break_block",
        "x": 2,
        "y": 71,
        "z": 0,
        "direction": "up",
        "timeoutMs": 5000
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "damage": 1,
        "maxDamage": 449
      },
      {
        "action": "wait_ms",
        "durationMs": 250
      },
      {
        "action": "screenshot",
        "name": "bonded-over-repair-visual",
        "hideOverlay": false
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode creative @s"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 4 76 3 minecraft:air replace"
      }
    ]
  },
  {
    "name": "bonded-over-repair-max-damage-stability",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode survival @s"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 0"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 3 70 3 minecraft:stone replace"
      },
      {
        "action": "command",
        "command": "/fill -3 71 -3 3 76 3 minecraft:air replace"
      },
      {
        "action": "command",
        "command": "/setblock 0 71 0 bonded:repair_bench"
      },
      {
        "action": "wait_for_block",
        "x": 0,
        "y": 71,
        "z": 0,
        "blockId": "bonded:repair_bench",
        "timeoutMs": 3000
      }
    ],
    "steps": [
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=1,bonded:item_level={experience:0,maxExperience:1000,level:1,bond:500}]"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with minecraft:iron_ingot 1"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 0,
        "maxDamage": 359
      },
      {
        "action": "assert_command_success",
        "command": "/bonded xp query @s levels"
      },
      {
        "action": "assert_command_success",
        "command": "/bonded xp query @s levels"
      },
      {
        "action": "assert_command_success",
        "command": "/bonded xp query @s levels"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 0,
        "maxDamage": 359
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode creative @s"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 3 76 3 minecraft:air replace"
      }
    ]
  },
  {
    "name": "bonded-over-repair-upgrade-clears",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode survival @s"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 0"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 4 70 3 minecraft:stone replace"
      },
      {
        "action": "command",
        "command": "/fill -3 71 -3 4 76 3 minecraft:air replace"
      },
      {
        "action": "command",
        "command": "/setblock 0 71 0 bonded:repair_bench"
      },
      {
        "action": "command",
        "command": "/setblock 2 71 0 bonded:tool_bench"
      },
      {
        "action": "wait_for_block",
        "x": 0,
        "y": 71,
        "z": 0,
        "blockId": "bonded:repair_bench",
        "timeoutMs": 3000
      },
      {
        "action": "wait_for_block",
        "x": 2,
        "y": 71,
        "z": 0,
        "blockId": "bonded:tool_bench",
        "timeoutMs": 3000
      }
    ],
    "steps": [
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:stone_pickaxe[minecraft:damage=1,bonded:item_level={experience:0,maxExperience:50,level:10,bond:500}]"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with bonded:scrap 1"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.2 with minecraft:copper_ingot 1"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:stone_pickaxe",
        "slot": 0,
        "damage": 0,
        "maxDamage": 216
      },
      {
        "action": "use_block_server",
        "x": 2,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:copper_pickaxe",
        "slot": 0,
        "damage": 0,
        "maxDamage": 240
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode creative @s"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 4 76 3 minecraft:air replace"
      }
    ]
  },
  {
    "name": "bonded-over-repair-near-threshold-damage",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode survival @s"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 0"
      },
      {
        "action": "command",
        "command": "/fill -2 70 -2 4 75 2 minecraft:air replace"
      },
      {
        "action": "command",
        "command": "/fill -2 70 -2 4 70 2 minecraft:stone replace"
      }
    ],
    "steps": [
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=299,minecraft:max_damage=650,bonded:item_level={level:4,maxExperience:100,experience:96,bond:1116},bonded:applied_bonuses={bonuses:[\"bonded:durability_500\",\"bonded:durability_1000\"]},bonded:max_damage_modifiers={base_max_damage:250,modifiers:[{amount:50.0d,id:\"bonded:durability_500\",operation:\"add_value\"},{amount:50.0d,id:\"bonded:durability_1000\",operation:\"add_value\"},{amount:300.0d,id:\"bonded:over_repair\",operation:\"add_value\"}]}]"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 299
      },
      {
        "action": "command",
        "command": "/setblock 1 71 0 minecraft:dirt"
      },
      {
        "action": "command",
        "command": "/tp @s 1 72 -1"
      },
      {
        "action": "use_block_server",
        "x": 1,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 0
      },
      {
        "action": "command",
        "command": "/setblock 2 71 0 minecraft:dirt"
      },
      {
        "action": "command",
        "command": "/tp @s 2 72 -1"
      },
      {
        "action": "use_block_server",
        "x": 2,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 1
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode creative @s"
      },
      {
        "action": "command",
        "command": "/fill -2 70 -2 4 75 2 minecraft:air replace"
      }
    ]
  },
  {
    "name": "bonded-over-repair-bonus-reapply-preserves-damage",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode survival @s"
      }
    ],
    "steps": [
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=251,minecraft:max_damage=650,minecraft:enchantments={\"minecraft:efficiency\":5},bonded:item_level={level:4,maxExperience:100,experience:40,bond:1000},bonded:applied_bonuses={bonuses:[\"bonded:durability_500\",\"bonded:durability_1000\",\"bonded:dig_speed_1000\"]},bonded:max_damage_modifiers={base_max_damage:250,modifiers:[{amount:300.0d,id:\"bonded:over_repair\",operation:\"add_value\"},{amount:50.0d,id:\"bonded:durability_500\",operation:\"add_value\"},{amount:50.0d,id:\"bonded:durability_1000\",operation:\"add_value\"}]}]"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 251,
        "maxDamage": 650
      },
      {
        "action": "assert_command_success",
        "command": "/bonded xp add @s 1 points"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 251,
        "maxDamage": 650
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode creative @s"
      }
    ]
  },
  {
    "name": "bonded-over-repair-damage-survives-block-xp",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode survival @s"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 0"
      },
      {
        "action": "command",
        "command": "/fill -2 70 -2 2 70 2 minecraft:stone replace"
      },
      {
        "action": "command",
        "command": "/fill -2 71 -2 2 74 2 minecraft:air replace"
      }
    ],
    "steps": [
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=251,minecraft:max_damage=650,minecraft:enchantments={\"minecraft:efficiency\":5},bonded:item_level={level:4,maxExperience:100,experience:40,bond:1000},bonded:applied_bonuses={bonuses:[\"bonded:durability_500\",\"bonded:durability_1000\",\"bonded:dig_speed_1000\"]},bonded:max_damage_modifiers={base_max_damage:250,modifiers:[{amount:300.0d,id:\"bonded:over_repair\",operation:\"add_value\"},{amount:50.0d,id:\"bonded:durability_500\",operation:\"add_value\"},{amount:50.0d,id:\"bonded:durability_1000\",operation:\"add_value\"}]}]"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 251,
        "maxDamage": 650
      },
      {
        "action": "command",
        "command": "/setblock 0 71 0 minecraft:dirt"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 -1"
      },
      {
        "action": "wait_for_block",
        "x": 0,
        "y": 71,
        "z": 0,
        "blockId": "minecraft:dirt",
        "timeoutMs": 3000
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 252,
        "maxDamage": 650
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode creative @s"
      },
      {
        "action": "command",
        "command": "/fill -2 70 -2 2 74 2 minecraft:air replace"
      }
    ]
  },
  {
    "name": "bonded-over-repair-upgrade-chain-damage",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode survival @s"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 0"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 8 70 3 minecraft:stone replace"
      },
      {
        "action": "command",
        "command": "/fill -3 71 -3 8 76 3 minecraft:air replace"
      },
      {
        "action": "command",
        "command": "/setblock 0 71 0 bonded:repair_bench"
      },
      {
        "action": "command",
        "command": "/setblock 2 71 0 bonded:tool_bench"
      },
      {
        "action": "wait_for_block",
        "x": 0,
        "y": 71,
        "z": 0,
        "blockId": "bonded:repair_bench",
        "timeoutMs": 3000
      },
      {
        "action": "wait_for_block",
        "x": 2,
        "y": 71,
        "z": 0,
        "blockId": "bonded:tool_bench",
        "timeoutMs": 3000
      }
    ],
    "steps": [
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:wooden_shovel[minecraft:damage=1,bonded:item_level={experience:0,maxExperience:30,level:10,bond:500}]"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with bonded:scrap 1"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "command",
        "command": "/setblock 4 71 0 minecraft:dirt"
      },
      {
        "action": "command",
        "command": "/tp @s 4 72 -1"
      },
      {
        "action": "wait_for_block",
        "x": 4,
        "y": 71,
        "z": 0,
        "blockId": "minecraft:dirt",
        "timeoutMs": 3000
      },
      {
        "action": "use_block_server",
        "x": 4,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with minecraft:cobblestone 1"
      },
      {
        "action": "command",
        "command": "/tp @s 2 72 -1"
      },
      {
        "action": "use_block_server",
        "x": 2,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:stone_shovel",
        "slot": 0,
        "damage": 0
      },
      {
        "action": "assert_command_success",
        "command": "/bonded xp set @s 10 levels"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with bonded:scrap 1"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 -1"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "command",
        "command": "/setblock 5 71 0 minecraft:dirt"
      },
      {
        "action": "command",
        "command": "/tp @s 5 72 -1"
      },
      {
        "action": "wait_for_block",
        "x": 5,
        "y": 71,
        "z": 0,
        "blockId": "minecraft:dirt",
        "timeoutMs": 3000
      },
      {
        "action": "use_block_server",
        "x": 5,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with minecraft:copper_ingot 1"
      },
      {
        "action": "command",
        "command": "/tp @s 2 72 -1"
      },
      {
        "action": "use_block_server",
        "x": 2,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:copper_shovel",
        "slot": 0,
        "damage": 0
      },
      {
        "action": "assert_command_success",
        "command": "/bonded xp set @s 10 levels"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with bonded:scrap 1"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 -1"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "command",
        "command": "/setblock 6 71 0 minecraft:dirt"
      },
      {
        "action": "command",
        "command": "/tp @s 6 72 -1"
      },
      {
        "action": "wait_for_block",
        "x": 6,
        "y": 71,
        "z": 0,
        "blockId": "minecraft:dirt",
        "timeoutMs": 3000
      },
      {
        "action": "use_block_server",
        "x": 6,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with minecraft:iron_ingot 1"
      },
      {
        "action": "command",
        "command": "/tp @s 2 72 -1"
      },
      {
        "action": "use_block_server",
        "x": 2,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 0
      },
      {
        "action": "command",
        "command": "/setblock 7 71 0 minecraft:dirt"
      },
      {
        "action": "command",
        "command": "/tp @s 7 72 -1"
      },
      {
        "action": "wait_for_block",
        "x": 7,
        "y": 71,
        "z": 0,
        "blockId": "minecraft:dirt",
        "timeoutMs": 3000
      },
      {
        "action": "use_block_server",
        "x": 7,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 1
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode creative @s"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 8 76 3 minecraft:air replace"
      }
    ]
  },
  {
    "name": "bonded-scrap-repair-bench",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode survival @s"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 0"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 3 70 3 minecraft:stone replace"
      },
      {
        "action": "command",
        "command": "/fill -3 71 -3 3 76 3 minecraft:air replace"
      },
      {
        "action": "command",
        "command": "/setblock 0 71 0 bonded:repair_bench"
      },
      {
        "action": "wait_for_block",
        "x": 0,
        "y": 71,
        "z": 0,
        "blockId": "bonded:repair_bench",
        "timeoutMs": 3000
      }
    ],
    "steps": [
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with minecraft:dirt 1"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 49,
        "maxDamage": 250
      },
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with minecraft:iron_ingot 1"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 0,
        "maxDamage": 251
      },
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with bonded:scrap 1"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 0,
        "maxDamage": 251
      },
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=49]"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with minecraft:iron_ingot 1"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.2 with bonded:scrap 1"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_shovel",
        "slot": 0,
        "damage": 0,
        "maxDamage": 251
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:iron_ingot",
        "slot": 1,
        "count": 1
      },
      {
        "action": "wait_ms",
        "durationMs": 250
      },
      {
        "action": "screenshot",
        "name": "bonded-scrap-repair-bench",
        "hideOverlay": true
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode creative @s"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 3 76 3 minecraft:air replace"
      }
    ]
  },
  {
    "name": "bonded-scrap-world-sources",
    "setup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/gamemode survival @s"
      },
      {
        "action": "command",
        "command": "/tp @s 0 72 0"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 6 70 3 minecraft:stone replace"
      },
      {
        "action": "command",
        "command": "/fill -3 71 -3 6 76 3 minecraft:air replace"
      },
      {
        "action": "command",
        "command": "/setblock 0 71 0 minecraft:grass_block"
      },
      {
        "action": "wait_for_block",
        "x": 0,
        "y": 71,
        "z": 0,
        "blockId": "minecraft:grass_block",
        "timeoutMs": 3000
      },
      {
        "action": "command",
        "command": "/setblock 4 71 0 bonded:tool_bench"
      },
      {
        "action": "wait_for_block",
        "x": 4,
        "y": 71,
        "z": 0,
        "blockId": "bonded:tool_bench",
        "timeoutMs": 3000
      }
    ],
    "steps": [
      {
        "action": "clear_nearby_entities",
        "radius": 16,
        "entityType": "minecraft:item"
      },
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:iron_shovel[minecraft:damage=249,bonded:item_level={experience:0,maxExperience:1000,level:1,bond:250}]"
      },
      {
        "action": "use_block_server",
        "x": 0,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "wait_for_entity_count",
        "radius": 8,
        "entityType": "minecraft:item",
        "itemId": "bonded:scrap",
        "count": 1,
        "timeoutMs": 5000
      },
      {
        "action": "clear_nearby_entities",
        "radius": 16,
        "entityType": "minecraft:item"
      },
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/item replace entity @s weapon.mainhand with minecraft:stone_pickaxe"
      },
      {
        "action": "command",
        "command": "/item replace entity @s hotbar.1 with minecraft:copper_ingot 1"
      },
      {
        "action": "assert_command_success",
        "command": "/bonded xp set @s 10 levels"
      },
      {
        "action": "use_block_server",
        "x": 4,
        "y": 71,
        "z": 0,
        "direction": "up",
        "hand": "main_hand"
      },
      {
        "action": "assert_inventory_item",
        "itemId": "minecraft:copper_pickaxe",
        "slot": 0,
        "count": 1
      },
      {
        "action": "wait_for_entity_count",
        "radius": 8,
        "entityType": "minecraft:item",
        "itemId": "bonded:scrap",
        "minCount": 0,
        "maxCount": 2,
        "timeoutMs": 5000
      }
    ],
    "cleanup": [
      {
        "action": "command",
        "command": "/clear @s"
      },
      {
        "action": "command",
        "command": "/kill @e[type=minecraft:item,distance=..16]"
      },
      {
        "action": "command",
        "command": "/gamemode creative @s"
      },
      {
        "action": "command",
        "command": "/fill -3 70 -3 6 76 3 minecraft:air replace"
      }
    ]
  }
] satisfies ScenarioDefinition[];

describe.configure({
  timeout: "20m",
  readiness: [Readiness.ClientReady, Readiness.IntegratedServerReady, Readiness.PlayerSpawned],
  capabilities: [Capability.LegacyJsonScenarios, Capability.ServerCommands, Capability.ClientInput],
});

describe("Bonded", () => {
  for (const definition of bondedScenarios) {
    test(definition.name, async ({ scenario }) => {
      const result = await scenario.run(definition, { timeoutMs: 240_000 });

      expect(result.error ?? null).toBeNull();
    });
  }
});
