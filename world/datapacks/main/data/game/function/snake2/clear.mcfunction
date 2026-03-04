scoreboard players set @e[tag=snake2snake] snake2timer 0
execute as @e[tag=snake2snake] at @s run setblock ~ ~ ~ air
kill @e[tag=snake2snake]
schedule clear game:snake2/move