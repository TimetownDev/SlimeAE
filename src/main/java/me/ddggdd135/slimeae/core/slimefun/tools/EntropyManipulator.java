package me.ddggdd135.slimeae.core.slimefun.tools;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.Rechargeable;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.WeaponUseHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import java.util.*;
import java.util.stream.Collectors;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EntropyManipulator extends SlimefunItem implements Rechargeable, RecipeDisplayItem {

    private static final Random RANDOM = new Random();

    private static final float MAX_ENERGY = 200000.0F;
    private static final float ENERGY_COST = 1600.0F;
    private static final int PARTICLE_COUNT = 30;
    private static final int FIRE_TICKS = 100;
    private static final int POTATO_REGROW_DELAY = 10;

    private static final Map<Material, Material> HEAT_TRANSFORMATIONS = createHeatMap();
    private static final Map<Material, Material> COOL_TRANSFORMATIONS = createCoolMap();

    public EntropyManipulator(ItemGroup group, SlimefunItemStack item, RecipeType type, ItemStack[] recipe) {
        super(group, item, type, recipe);
        addItemHandler(createItemHandler(), createWeaponHandler());
    }

    @Override
    public float getMaxItemCharge(ItemStack item) {
        return MAX_ENERGY;
    }

    private ItemUseHandler createItemHandler() {
        return event -> {
            Player player = event.getPlayer();
            ItemStack tool = event.getItem();

            if (!removeItemCharge(tool, ENERGY_COST)) {
                player.sendMessage(ChatColors.color("&c能量不足! 需要 &e" + ENERGY_COST + "J"));
                return;
            }

            event.cancel();
            Block target = player.getTargetBlockExact(6);
            if (target == null) return;

            if (player.isSneaking()) applyCooling(target);
            else applyHeating(target);
        };
    }

    private WeaponUseHandler createWeaponHandler() {
        return (event, victim, tool) -> {
            if (victim instanceof LivingEntity) {
                LivingEntity entity = (LivingEntity) victim;
                entity.setFireTicks(FIRE_TICKS);
                playEffect(entity.getLocation(), Particle.SMOKE_LARGE);
                playSound(entity.getLocation(), Sound.BLOCK_FIRE_AMBIENT);
            }
        };
    }

    private void applyHeating(Block block) {
        if (tryProcessPotato(block)) return;

        Material result = HEAT_TRANSFORMATIONS.get(block.getType());
        if (result == null) {
            spawnSmoke(block.getLocation());
            return;
        }

        handleHeatTransformation(block, result);
    }

    private boolean tryProcessPotato(Block block) {
        if (block.getType() != Material.POTATOES) return false;

        Block farmlandBlock = block.getRelative(BlockFace.DOWN);
        if (!(farmlandBlock.getBlockData() instanceof Farmland)) return false;

        Farmland farmland = (Farmland) farmlandBlock.getBlockData();
        if (farmland.getMoisture() < 1) {
            showAngryVillagerParticles(block.getLocation());
            return true;
        }

        Ageable crop = (Ageable) block.getBlockData();
        if (crop.getAge() == crop.getMaximumAge()) {
            processMaturePotato(block);
        } else {
            accelerateGrowth(block, crop);
        }
        return true;
    }

    private void processMaturePotato(Block block) {
        Location center = block.getLocation().add(0.5, 0.5, 0.5);
        block.setType(Material.AIR);

        dropItemsWithVelocity(center, Material.BAKED_POTATO, RANDOM.nextInt(2) + 2);

        Bukkit.getScheduler()
                .runTaskLater(
                        SlimeAEPlugin.getInstance(),
                        () -> {
                            block.setType(Material.POTATOES);
                            Ageable newCrop = (Ageable) block.getBlockData();
                            newCrop.setAge(0);
                            block.setBlockData(newCrop);
                        },
                        POTATO_REGROW_DELAY);

        playEffect(center, Particle.FLASH);
        playSound(center, Sound.ITEM_FLINTANDSTEEL_USE, 1.2F, 0.8F);
    }

    private void accelerateGrowth(Block block, Ageable crop) {
        int boost = RANDOM.nextInt(3) + 1;
        int newAge = Math.min(crop.getAge() + boost, crop.getMaximumAge());

        crop.setAge(newAge);
        block.setBlockData(crop);

        spawnGrowthParticles(block.getLocation());
        dropItemsWithVelocity(block.getLocation().add(0.5, 0.5, 0.5), Material.BAKED_POTATO, boost > 2 ? 2 : 1);
    }

    private void handleHeatTransformation(Block block, Material result) {
        if (block.getType() == Material.CLAY) {
            dropItem(block, Material.BRICK, 4);
            return;
        }

        if (result == Material.CHARCOAL) {
            dropItem(block, Material.CHARCOAL, 1);
            return;
        }

        if (block.getType() != result) {
            block.setType(result);
            playEffect(block.getLocation(), Particle.FLAME);
        }
    }

    private void applyCooling(Block target) {
        Arrays.stream(BlockFace.values()).map(target::getRelative).forEach(this::processCoolingBlock);
    }

    private void processCoolingBlock(Block block) {
        if (isFluid(block.getType())) {
            handleFluidCooling(block);
            return;
        }

        Material result = COOL_TRANSFORMATIONS.get(block.getType());
        if (result != null) {
            block.setType(result);
            playEffect(block.getLocation(), Particle.VILLAGER_HAPPY);
        } else {
            tryIgniteBlock(block);
        }
    }

    private void handleFluidCooling(Block block) {
        switch (block.getType()) {
            case WATER:
                freezeWater(block);
                break;
            case LAVA:
                coolLava(block);
                break;
        }
        playEffect(block.getLocation(), Particle.SNOWBALL);
    }

    private void freezeWater(Block block) {
        Levelled water = (Levelled) block.getBlockData();
        if (water.getLevel() == 0) {
            block.setType(Material.ICE);
        } else {
            Block above = block.getRelative(BlockFace.UP);
            if (above.getType().isAir()) above.setType(Material.SNOW);
        }
    }

    private void coolLava(Block block) {
        Levelled lava = (Levelled) block.getBlockData();
        Material result = lava.getLevel() == 0 ? Material.OBSIDIAN : Material.BLACKSTONE;
        block.setType(result);
        playSound(block.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.0F, 1.5F);
    }

    private void tryIgniteBlock(Block block) {
        Block above = block.getRelative(BlockFace.UP);
        if (above.getType().isAir() && above.getType().isBurnable()) {
            above.setType(Material.FIRE);
        }
    }

    private void spawnGrowthParticles(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.VILLAGER_HAPPY, location.clone().add(0.5, 0.3, 0.5), 15, 0.3, 0.2, 0.3, 0.15);
        world.spawnParticle(Particle.COMPOSTER, location.clone().add(0.5, 0.5, 0.5), 8, 0.2, 0.1, 0.2, 0.1);
    }

    private void dropItemsWithVelocity(Location location, Material material, int amount) {
        World world = location.getWorld();
        if (world == null) return;

        ItemStack drop = new ItemStack(material, amount);
        Item entity = world.dropItem(location, drop);
        entity.setVelocity(
                new org.bukkit.util.Vector((RANDOM.nextDouble() - 0.5) * 0.2, 0.3, (RANDOM.nextDouble() - 0.5) * 0.2));
    }

    private boolean dropItem(Block block, Material material, int amount) {
        block.setType(Material.AIR);
        dropItems(block.getLocation(), material, amount);
        return true;
    }

    private void dropItems(Location location, Material material, int amount) {
        World world = location.getWorld();
        if (world != null) world.dropItemNaturally(location, new ItemStack(material, amount));
    }

    private void playEffect(Location location, Particle particle) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(particle, location.clone().add(0.5, 0.5, 0.5), PARTICLE_COUNT, 0.2, 0.2, 0.2, 0.1);
    }

    private void playSound(Location location, Sound sound) {
        playSound(location, sound, 1.0F, 1.0F);
    }

    private void playSound(Location location, Sound sound, float volume, float pitch) {
        World world = location.getWorld();
        if (world != null) world.playSound(location, sound, volume, pitch);
    }

    private void spawnSmoke(Location location) {
        playEffect(location, Particle.SMOKE_NORMAL);
    }

    private void showAngryVillagerParticles(Location location) {
        playEffect(location, Particle.VILLAGER_ANGRY);
    }

    private boolean isFluid(Material material) {
        return material == Material.WATER || material == Material.LAVA;
    }

    private static Map<Material, Material> createHeatMap() {
        Map<Material, Material> map = new HashMap<>();
        map.put(Material.COBBLESTONE, Material.STONE);
        map.put(Material.NETHERRACK, Material.NETHER_BRICKS);
        map.put(Material.CLAY, Material.BRICK);
        map.put(Material.SAND, Material.GLASS);
        map.put(Material.ICE, Material.WATER);
        map.put(Material.SNOW_BLOCK, Material.WATER);
        map.putAll(Tag.LOGS.getValues().stream().collect(Collectors.toMap(k -> k, v -> Material.CHARCOAL)));
        return map;
    }

    private static Map<Material, Material> createCoolMap() {
        Map<Material, Material> map = new HashMap<>();
        map.put(Material.GRASS_BLOCK, Material.DIRT);
        map.put(Material.STONE, Material.COBBLESTONE);
        map.put(Material.ANDESITE, Material.COBBLESTONE);
        map.put(Material.DIORITE, Material.COBBLESTONE);
        map.put(Material.GRANITE, Material.COBBLESTONE);
        map.put(Material.PACKED_ICE, Material.BLUE_ICE);
        return map;
    }

    @NotNull @Override
    public List<ItemStack> getDisplayRecipes() {
        List<ItemStack> items = new ArrayList<>();

        items.add(createRecipeBook(
                "基础转换",
                "&b▶ &f加热模式 &7(右键方块)",
                " &8• &7沙 &8→ &f玻璃",
                " &8• &7黏土 &8→ &f4砖块",
                " &8• &7原木 &8→ &8木炭",
                " &8• &7圆石 &8→ &f石头",
                " &8• &7冰/雪块 &8→ &b水",
                "",
                "&b▶ &f冷却模式 &7(潜行+右键)",
                " &8• &7草方块 &8→ &6泥土",
                " &8• &7石头 &8→ &8圆石",
                " &8• &7岩浆 &8→ &5黑曜石/黑石",
                " &8• &7水 &8→ &b冰/雪"));

        items.add(createRecipeBook(
                "作物处理",
                "&b▶ &f马铃薯加速生长",
                " &8• &7需要湿润耕地",
                " &8• &7成熟时掉落2-3烤马铃薯",
                " &8• &7未成熟时随机加速1-3阶段",
                " &8• &7重新种植机制",
                "",
                "&b▶ &f特殊效果",
                " &8• &c生长加速粒子效果",
                " &8• &a重新种植音效提示"));

        items.add(createRecipeBook(
                "高级功能",
                "",
                "&b▶ &f能量系统",
                " &8• &e容量: &6200,000J",
                " &8• &e单次消耗: &61,600J",
                " &8• &c低能量提示",
                "",
                "&b▶ &f作用范围",
                " &8• &7最大距离: &f6格",
                " &8• &7冷却模式范围: &f周围6格"));

        items.add(createRecipeBook(
                "特殊机制",
                "&b▶ &f流体控制",
                " &8• &b水 &7(满级) → &b冰",
                " &8• &b水 &7(非满级) → &f雪",
                " &8• &4岩浆 &7(满级) → &5黑曜石",
                " &8• &4岩浆 &7(非满级) → &8黑石",
                "",
                "&b▶ &f视听效果",
                " &8• &6火焰粒子 &7(加热时)",
                " &8• &b雪花粒子 &7(冷却时)",
                " &8• &a生长绿色粒子 &7(作物加速)",
                " &8• &c失败红雾 &7(耕地干燥)"));

        return items;
    }

    private ItemStack createRecipeBook(String title, String... content) {

        List<String> lore = new ArrayList<>();
        lore.add(ChatColors.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
        Arrays.stream(content)
                .forEach(line -> lore.add(ChatColors.color(line.startsWith(" &8•") ? "&7" + line : line)));
        lore.add(ChatColors.color("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));

        AdvancedCustomItemStack item =
                new AdvancedCustomItemStack(Material.KNOWLEDGE_BOOK, "{#3366ff>}" + title + "{#33ccf3<}", lore);

        return item;
    }
}
