execute if entity @s[x=9,y=3,z=36,dx=2,dy=0,dz=2] run function common:ingame/minecart/remove

execute if entity @s[x=13,y=7,z=23,dx=2,dy=0,dz=0] run tag @s remove movetype5
execute if entity @s[x=13,y=7,z=23,dx=2,dy=0,dz=0] run tag @s add movetype2

execute if entity @s[x=4,y=5,z=40,dx=2,dy=0,dz=0] run tag @s remove movetype2
execute if entity @s[x=4,y=5,z=40,dx=2,dy=0,dz=0] run tag @s add movetype1

execute if entity @s[x=14,y=3,z=50,dx=0,dy=0,dz=2] run tag @s remove movetype1
execute if entity @s[x=14,y=3,z=50,dx=0,dy=0,dz=2] run tag @s add movetype4

execute if entity @s[x=18,y=3,z=46,dx=2,dy=0,dz=0] run tag @s remove movetype4
execute if entity @s[x=18,y=3,z=46,dx=2,dy=0,dz=0] run tag @s add movetype3

function game:4/tnt_minecart/minecart/movetype

execute if entity @a[distance=..1,gamemode=!spectator] run function game:4/tnt_minecart/minecart/explode