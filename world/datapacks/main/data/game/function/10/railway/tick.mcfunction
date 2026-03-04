function game:10/railway/slime/tick

execute as @e[type=minecart,x=42,y=124,z=50,dx=-1,dy=0,dz=0,tag=!10_minecart] unless block 41 125 52 redstone_lamp[lit=true] run data merge entity @s {CustomDisplayTile:true,DisplayState:{Name:"minecraft:tnt"},Tags:["10_minecart","tnt_minecart2"],DisplayOffset:6}
execute as @e[type=minecart,x=42,y=124,z=50,dx=-1,dy=0,dz=0,tag=!10_minecart] if block 41 125 52 redstone_lamp[lit=true] run data merge entity @s {Tags:["10_minecart"]}
execute as @e[tag=10_minecart] at @s run function game:10/railway/minecart/tick

execute positioned 41 125 52 run function game:10/railway/lamp/tick