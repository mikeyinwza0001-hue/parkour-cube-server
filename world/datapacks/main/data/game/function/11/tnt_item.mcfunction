execute if entity @a[x=40,y=148,z=39,distance=..20] unless entity @e[tag=11_tnt_item] run summon item 40 148 39 {Tags:["11_tnt_item","ingame"],Item:{id:"minecraft:tnt",count:1b}}
execute unless entity @a[x=40,y=148,z=39,distance=..20] if entity @e[tag=11_tnt_item] run kill @e[tag=11_tnt_item]
execute as @e[tag=11_tnt_item] run data merge entity @s {PickupDelay:-999999999}

execute as @a[x=40,y=148,z=39,dx=0,dy=0,dz=0,nbt=!{Inventory:[{id:"minecraft:tnt",components:{"minecraft:custom_data":{11_tnt_item:true}}}]}] at @s run playsound entity.item.pickup master @s ~ ~ ~ 1 1
execute as @a[x=40,y=148,z=39,dx=0,dy=0,dz=0,nbt=!{Inventory:[{id:"minecraft:tnt",components:{"minecraft:custom_data":{11_tnt_item:true}}}]}] run item replace entity @s hotbar.4 with tnt[item_name={"text": "TNT","italic": false,"bold": true,"color": "red"},can_place_on={blocks:["redstone_block"]},custom_data={11_tnt_item:true}] 1

execute as @a[nbt={Inventory:[{id:"minecraft:tnt",components:{"minecraft:custom_data":{11_tnt_item:true}}}]}] unless entity @s[x=-6,y=157,z=8,dx=65,dy=-19,dz=64] run clear @s tnt[custom_data={11_tnt_item:true}]