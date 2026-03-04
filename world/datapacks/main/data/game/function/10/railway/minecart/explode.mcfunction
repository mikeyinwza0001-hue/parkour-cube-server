execute as @a[distance=..2] run damage @s 14
particle explosion_emitter ~ ~ ~ 1 1 1 1 1
playsound entity.generic.explode master @a ~ ~ ~ 1 1
kill @s