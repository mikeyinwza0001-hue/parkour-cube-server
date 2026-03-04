execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 1.. run scoreboard players add @s snake1timer 1 
execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 30.. run scoreboard players set @s snake1timer 0

execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 2..2 as @e[tag=snake1snake,distance=..1.1] at @s unless entity @s[scores={snake1timer=1..}] run scoreboard players set @s snake1timer 1

execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 2 run setblock ~ ~ ~ green_concrete
execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 4 run setblock ~ ~ ~ lime_concrete
execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 6 run setblock ~ ~ ~ green_concrete
execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 8 run setblock ~ ~ ~ lime_concrete
execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 10 run setblock ~ ~ ~ green_concrete
execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 12 run setblock ~ ~ ~ lime_concrete
execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 14 run setblock ~ ~ ~ green_concrete
execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 16 run setblock ~ ~ ~ lime_concrete
execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 18 run setblock ~ ~ ~ green_concrete
execute as @e[tag=snake1snake] at @s if score @s snake1timer matches 20 run setblock ~ ~ ~ air

execute as @e[tag=snake1snake] at @s unless score @s snake1timer matches 1.. unless block ~ ~ ~ air run setblock ~ ~ ~ air

schedule function game:snake1/move 3t