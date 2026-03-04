execute if entity @a[distance=..3] unless block ~ ~ ~ nether_wart_block run particle dust{color:[1.0,0.0,0.0],scale:1.0} ~ ~1 ~ 0.5 0.5 0.5 0.03 5 normal
execute if entity @a[distance=..3] unless block ~ ~ ~ nether_wart_block run playsound block.wart_block.place master @a ~ ~ ~ 0.5 1
execute if entity @a[distance=..3] unless block ~ ~ ~ nether_wart_block run setblock ~ ~ ~ nether_wart_block

execute unless entity @a[distance=..3] if entity @a[distance=4..5] unless block ~ ~ ~ nether_wart[age=3] run setblock ~ ~ ~ nether_wart[age=3]
execute unless entity @a[distance=..5] if entity @a[distance=6..7] unless block ~ ~ ~ nether_wart[age=1] run setblock ~ ~ ~ nether_wart[age=1]
execute unless entity @a[distance=..7] unless block ~ ~ ~ nether_wart[age=0] run setblock ~ ~ ~ nether_wart[age=0]