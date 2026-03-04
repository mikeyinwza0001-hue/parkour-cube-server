execute if entity @a[distance=..6,tag=has_snow_boot] unless block ~ ~ ~ snow run function game:6/set_snow

execute unless entity @a[distance=..6,tag=has_snow_boot] if block ~ ~ ~ snow run setblock ~ ~ ~ air