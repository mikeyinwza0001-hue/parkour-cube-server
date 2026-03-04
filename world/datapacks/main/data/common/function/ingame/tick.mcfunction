function common:ingame/saturation
function common:ingame/kill_item
function common:ingame/fire
function common:ingame/farmland/tick
function common:ingame/firework/tick
execute as @a at @s run function common:ingame/checkpoint/set_tag

## Elytra Position
execute positioned 10 259 51 run function common:ingame/end_elytra/tick