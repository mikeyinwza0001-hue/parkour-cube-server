scoreboard players set @e[tag=snake1snake] snake1timer 0
execute as @e[tag=snake1snake] at @s run setblock ~ ~ ~ air
kill @e[tag=snake1snake]
schedule clear game:snake1/move