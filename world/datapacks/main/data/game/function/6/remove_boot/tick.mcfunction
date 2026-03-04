execute as @a[x=1,y=32,z=38,dx=6,dy=4,dz=1] if items entity @s armor.feet *[custom_data={snow_boot:true}] at @s run function game:6/remove_boot/remove

execute as @a if items entity @s armor.feet *[custom_data={snow_boot:true}] unless entity @s[x=-22,y=24,z=12,dx=68,dy=39,dz=76] at @s run function game:6/remove_boot/remove