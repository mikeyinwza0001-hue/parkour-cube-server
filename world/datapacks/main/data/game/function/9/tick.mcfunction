function game:9/balloon_explode/tick


data merge block -9 96 24 {Age:-999999999}
data merge block -9 96 25 {Age:-999999999}
data merge block -9 96 26 {Age:-999999999}
data merge block -8 96 24 {Age:-999999999}
data merge block -8 96 25 {Age:-999999999}
data merge block -8 96 26 {Age:-999999999}
data merge block -7 96 24 {Age:-999999999}
data merge block -7 96 25 {Age:-999999999}
data merge block -7 96 26 {Age:-999999999}
execute as @a[x=-9,y=96,z=24,dx=2,dy=0,dz=2] at @s run playsound entity.enderman.teleport master @s ~ ~ ~ 9999 1
execute as @a[x=-9,y=96,z=24,dx=2,dy=0,dz=2] run tp @s 6 117.9375 37 20.0 30.0