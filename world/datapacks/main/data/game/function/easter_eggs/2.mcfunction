execute if entity @a[x=44,y=-16,z=26,dx=1,dy=1,dz=0] if block 45 -16 26 netherrack run fill 45 -16 26 45 -15 26 air destroy
execute unless entity @a[x=44,y=-16,z=26,dx=1,dy=0,dz=0] unless block 45 -16 26 netherrack run fill 45 -16 26 45 -15 26 netherrack

data merge block 45 -16 32 {Age:-999999999}
data merge block 45 -15 32 {Age:-999999999}

execute as @a[x=45,y=-16,z=32,dx=0,dy=1,dz=0] at @s run playsound entity.enderman.teleport master @s ~ ~ ~ 9999 1
execute as @a[x=45,y=-16,z=32,dx=0,dy=1,dz=0] run tp @s 16 -8 34 -60.0 30.0