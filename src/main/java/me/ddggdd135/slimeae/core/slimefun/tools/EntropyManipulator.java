package me.ddggdd135.slimeae.core.slimefun.tools;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.Rechargeable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.WeaponUseHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Tag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EntropyManipulator extends SlimefunItem implements Rechargeable {

    private static final float MAX_CAPACITY = 200000.0F;
    private static final float ENERGY_COST = 1600.0F;
    private static final Location PARTICLE_OFFSET = new Location(null, 0.5, 0.5, 0.5);

    private static final Map<Material, Material> HEAT_TRANSFORMATIONS = new HashMap<>();
    static {
        HEAT_TRANSFORMATIONS.put(Material.COBBLESTONE, Material.STONE);
        HEAT_TRANSFORMATIONS.put(Material.NETHERRACK, Material.NETHER_BRICKS);
        HEAT_TRANSFORMATIONS.put(Material.CLAY, Material.BRICK);
        HEAT_TRANSFORMATIONS.put(Material.SAND, Material.GLASS);
        HEAT_TRANSFORMATIONS.put(Material.ICE, Material.WATER);
        HEAT_TRANSFORMATIONS.put(Material.SNOW_BLOCK, Material.WATER);
        HEAT_TRANSFORMATIONS.put(Material.POTATO, Material.BAKED_POTATO);

        HEAT_TRANSFORMATIONS.putAll(
                Tag.LOGS.getValues().stream()
                        .collect(Collectors.toMap(k -> k, v -> Material.CHARCOAL))
        );
    }

    private static final Map<Material, Material> COOL_TRANSFORMATIONS = new HashMap<>();
    static {
        COOL_TRANSFORMATIONS.put(Material.GRASS_BLOCK, Material.DIRT);
        COOL_TRANSFORMATIONS.put(Material.STONE, Material.COBBLESTONE);
        COOL_TRANSFORMATIONS.put(Material.ANDESITE, Material.COBBLESTONE);
        COOL_TRANSFORMATIONS.put(Material.DIORITE, Material.COBBLESTONE);
        COOL_TRANSFORMATIONS.put(Material.GRANITE, Material.COBBLESTONE);
        COOL_TRANSFORMATIONS.put(Material.PACKED_ICE, Material.BLUE_ICE);
    }

    public EntropyManipulator(ItemGroup group, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(group, item, recipeType, recipe);
        addItemHandler(itemUseHandler(), weaponUseHandler());
    }

    @Override
    public float getMaxItemCharge(ItemStack item) {
        return MAX_CAPACITY;
    }

    private ItemUseHandler itemUseHandler() {
        return e -> {
            Player p = e.getPlayer();
            ItemStack tool = e.getItem();

            if (!removeItemCharge(tool, ENERGY_COST)) {
                p.sendMessage(ChatColors.color("&c能量不足! 需要 &e" + ENERGY_COST + "J &c能量"));
                return;
            }

            e.cancel();
            Block target = p.getTargetBlockExact(6);

            if (target != null) {
                if (p.isSneaking()) {
                    handleCooling(target);
                } else {
                    handleHeating(p, target);
                }
            }
        };
    }

    private WeaponUseHandler weaponUseHandler() {
        return (e, victim, tool) -> {
            if (victim instanceof LivingEntity) {
                LivingEntity entity = (LivingEntity) victim;
                entity.setFireTicks(100);
                playSound(entity.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1.0F, 1.0F);
                playEffect(entity.getLocation(), Particle.SMOKE_LARGE, null);
            }
        };
    }

    private void handleHeating(Player p, Block b) {
        Material original = b.getType();
        Material result = HEAT_TRANSFORMATIONS.get(original);

        if (result != null) {
            if (original == Material.POTATO && checkAndTransformPotato(b)) return;
            if (original == Material.CLAY && transformClayToBrick(b)) return;
            if (result == Material.CHARCOAL) {
                transformWoodToCharcoal(b);
                return;
            }

            if (b.getType() != result) {
                b.setType(result);
                playEffect(b.getLocation(), Particle.FLAME, null);
            }
        } else {
            spawnDefaultSmoke(b.getLocation());
        }
    }

    private void handleCooling(Block target) {
        Arrays.stream(BlockFace.values())
                .map(target::getRelative)
                .forEach(relative -> {
                    if (isFluid(relative.getType())) {
                        handleFluid(relative);
                    } else {
                        processSolidCooling(relative);
                    }
                });
    }

    private boolean isFluid(Material material) {
        return material == Material.WATER || material == Material.LAVA;
    }

    private void handleFluid(Block b) {
        Material original = b.getType();
        if (original == Material.WATER) {
            transformWater(b);
        } else if (original == Material.LAVA) {
            transformLava(b);
        }
        playEffect(b.getLocation(), Particle.SNOWBALL, null);
    }

    private void transformWater(Block b) {
        BlockData data = b.getBlockData();
        if (data instanceof Levelled) {
            Levelled water = (Levelled) data;
            if (water.getLevel() == 0) {
                b.setType(Material.ICE);
            } else {
                Block above = b.getRelative(BlockFace.UP);
                if (above.getType().isAir()) {
                    above.setType(Material.SNOW);
                }
            }
        }
    }

    private void transformLava(Block b) {
        BlockData data = b.getBlockData();
        if (data instanceof Levelled) {
            Levelled lava = (Levelled) data;
            Material result = lava.getLevel() == 0 ? Material.OBSIDIAN : Material.BLACKSTONE;
            b.setType(result);
            playSound(b.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0F, 1.5F);
        }
    }

    private void processSolidCooling(Block b) {
        Material original = b.getType();
        Material result = COOL_TRANSFORMATIONS.get(original);

        if (result != null) {
            b.setType(result);
            playEffect(b.getLocation(), Particle.VILLAGER_HAPPY, null);
        } else {
            setFireAbove(b);
        }
    }

    private void setFireAbove(Block b) {
        Block above = b.getRelative(BlockFace.UP);
        if (above.getType().isBurnable() && above.getType().isAir()) {
            above.setType(Material.FIRE);
        }
    }

    /**
     * 处理马铃薯转换逻辑
     */
    private boolean checkAndTransformPotato(Block b) {
        Block farmland = b.getRelative(BlockFace.DOWN);
        if (farmland.getType() == Material.FARMLAND) {
            BlockData data = b.getBlockData();
            if (data instanceof Ageable) {
                Ageable ageable = (Ageable) data;
                int age = ageable.getAge();

                switch (age) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        b.setType(Material.AIR);
                        World world = b.getWorld();
                        if (world != null) {
                            world.dropItemNaturally(b.getLocation(), new ItemStack(Material.BAKED_POTATO));
                        }
                        playEffect(b.getLocation(), Particle.SMOKE_LARGE, null);
                        return true;
                    default:
                        break;
                }
            }
        }
        return false;
    }
    private boolean transformClayToBrick(Block b) {
        b.setType(Material.AIR);
        World world = b.getWorld();
        if (world != null) {
            world.dropItemNaturally(b.getLocation(), new ItemStack(Material.BRICK, 4));
        }
        playEffect(b.getLocation(), Particle.CRIT_MAGIC, null);
        return true;
    }

    private void transformWoodToCharcoal(Block b) {
        b.setType(Material.AIR);
        World world = b.getWorld();
        if (world != null) {
            world.dropItemNaturally(b.getLocation(), new ItemStack(Material.CHARCOAL));
        }
        playEffect(b.getLocation(), Particle.LAVA, null);
    }

    private void playEffect(Location loc, Particle particle, Color color) {
        World world = loc.getWorld();
        if (world != null) {
            Location particleLoc = loc.clone().add(0.5, 0.5, 0.5);
            world.spawnParticle(
                    particle,
                    particleLoc,
                    30, 0.2, 0.2, 0.2, 0.1
            );
        }
    }
    private void playSound(Location loc, Sound sound, float volume, float pitch) {
        World world = loc.getWorld();
        if (world != null) {
            world.playSound(loc, sound, volume, pitch);
        }
    }

    private void spawnDefaultSmoke(Location loc) {
        World world = loc.getWorld();
        if (world != null) {
            world.spawnParticle(
                    Particle.SMOKE_NORMAL,
                    loc,
                    30, 0.2, 0.2, 0.2, 0.1
            );
        }
    }
}
