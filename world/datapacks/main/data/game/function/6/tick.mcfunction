function game:6/boot_item
function game:6/remove_boot/tick
function game:6/boat

execute as @a[tag=!has_snow_boot] if items entity @s armor.feet *[custom_data={snow_boot:true}] run tag @s add has_snow_boot
execute as @a[tag=has_snow_boot] unless items entity @s armor.feet *[custom_data={snow_boot:true}] run tag @s remove has_snow_boot