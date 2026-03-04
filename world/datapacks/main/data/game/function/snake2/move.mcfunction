execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 1.. run scoreboard players add @s snake2timer 1 
execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 30.. run scoreboard players set @s snake2timer 0

execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 2..2 as @e[tag=snake2snake,distance=..1.1] at @s unless entity @s[scores={snake2timer=1..}] run scoreboard players set @s snake2timer 1

execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 2 run setblock ~ ~ ~ white_concrete
execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 4 run setblock ~ ~ ~ light_gray_concrete
execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 6 run setblock ~ ~ ~ white_concrete
execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 8 run setblock ~ ~ ~ light_gray_concrete
execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 10 run setblock ~ ~ ~ white_concrete
execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 12 run setblock ~ ~ ~ light_gray_concrete
execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 14 run setblock ~ ~ ~ white_concrete
execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 16 run setblock ~ ~ ~ light_gray_concrete
execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 18 run setblock ~ ~ ~ white_concrete
execute as @e[tag=snake2snake] at @s if score @s snake2timer matches 20 run setblock ~ ~ ~ air

execute as @e[tag=snake2snake] at @s unless score @s snake2timer matches 1.. unless block ~ ~ ~ air run setblock ~ ~ ~ air

schedule function game:snake2/move 3t