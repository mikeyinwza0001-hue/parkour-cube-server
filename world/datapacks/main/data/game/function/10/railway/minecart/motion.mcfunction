execute if entity @s[x=37,y=122,z=50,dx=-5,dy=2,dz=0] run data merge entity @s {Motion:[-0.2, 0.0, 0.0]}
execute if entity @s[x=31,y=122,z=49,dx=0,dy=0,dz=0] run data merge entity @s {Motion:[-0.0, 0.0, -0.2]}
execute if entity @s[x=37,y=122,z=50,dx=-5,dy=2,dz=0] run data merge entity @s {Motion:[-0.2, 0.0, 0.0]}
execute if entity @s[x=30,y=122,z=48,dx=-4,dy=0,dz=0] run data merge entity @s {Motion:[-0.2, 0.0, 0.0]}
execute if entity @s[x=25,y=122,z=47,dx=0,dy=0,dz=-3] run data merge entity @s {Motion:[0.0, 0.0, -0.2]}
execute if entity @s[x=24,y=122,z=43,dx=-11,dy=0,dz=0] run data merge entity @s {Motion:[-0.2, 0.0, 0.0]}
execute if entity @s[x=12,y=122,z=42,dx=0,dy=1,dz=-16] run data merge entity @s {Motion:[0.0, 0.0, -0.2]}
execute if entity @s[x=13,y=123,z=25,dx=11,dy=0,dz=0] run data merge entity @s {Motion:[0.2, 0.0, 0.0]}
execute if entity @s[x=25,y=123,z=25,dx=17,dy=3,dz=0] run data merge entity @s {Motion:[0.5, 0.0, 0.0]}
execute if entity @s[x=43,y=125,z=23,dx=17,dy=3,dz=1] run data merge entity @s {Motion:[0.0, 0.0, -0.5]}

execute if entity @s[x=43,y=118,z=22,distance=..2] run function common:ingame/minecart/remove