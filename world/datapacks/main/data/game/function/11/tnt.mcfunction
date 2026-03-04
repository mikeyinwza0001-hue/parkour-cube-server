execute if block 22 152 42 tnt run summon tnt 22 152 42 {Motion:[0.0,0.25,0.0]}
execute if block 22 152 42 tnt run setblock 22 152 42 air

execute as @e[type=tnt,x=22,y=152,z=42,distance=..4] store result score @s ingame_timer run data get entity @s fuse

execute as @e[type=tnt,x=22,y=152,z=42,distance=..4] if score @s ingame_timer matches ..10 run function game:11/explosion/destroy
execute as @e[type=tnt,x=22,y=152,z=42,distance=..4] if score @s ingame_timer matches ..10 run kill @s