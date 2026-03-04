execute if entity @s[tag=tnt_minecart2] if entity @a[distance=..1,gamemode=!spectator] run function game:10/railway/minecart/explode

execute if entity @s[tag=tnt_minecart2] run function game:10/railway/minecart/motion
execute if entity @s[tag=!tnt_minecart2] if entity @a[distance=..1] run function game:10/railway/minecart/motion_player
execute if entity @s[tag=!tnt_minecart2] unless entity @a[distance=..1] run function game:10/railway/minecart/motion_no_player