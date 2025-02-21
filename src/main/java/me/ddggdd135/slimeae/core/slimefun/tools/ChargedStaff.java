package me.ddggdd135.slimeae.core.slimefun.tools;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.Rechargeable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import java.util.List;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ChargedStaff extends SlimefunItem implements Rechargeable {

    private static final float MAX_CHARGE = 8000F;
    private static final float COST_PER_USE = 300F;
    private static final double DAMAGE = 6.0;
    private static final int RANGE = 10;
    private static final double HITBOX_RADIUS = 0.3;

    public ChargedStaff(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(createUseHandler());
    }

    private ItemUseHandler createUseHandler() {
        return event -> {
            Player player = event.getPlayer();
            ItemStack staff = event.getItem();
            event.cancel();

            if (!removeItemCharge(staff, COST_PER_USE)) {
                player.sendMessage(ChatColors.color("&c能量不足! 需要 &e" + COST_PER_USE + "J"));
                return;
            }

            Location eyeLoc = player.getEyeLocation();
            Vector direction = eyeLoc.getDirection().normalize().clone(); // 修复向量修改问题

            boolean hit = false;
            for (int i = 0; i < RANGE; i++) {
                Location checkLoc = eyeLoc.clone().add(direction.clone().multiply(i));

                // 方块碰撞检测
                if (checkLoc.getBlock().getType().isSolid()) {
                    playMissEffect(checkLoc);
                    break;
                }

                List<Entity> targets = (List<Entity>) checkLoc.getWorld()
                        .getNearbyEntities(
                                checkLoc, HITBOX_RADIUS, HITBOX_RADIUS, HITBOX_RADIUS, e -> isValidTarget(e, player));

                if (!targets.isEmpty()) {
                    LivingEntity target = (LivingEntity) targets.get(0);
                    applyDamage(target, player);
                    playAttackEffects(checkLoc);
                    hit = true;
                    break;
                }
            }

            if (!hit) {
                playMissEffect(eyeLoc);
            }
        };
    }

    private boolean isValidTarget(Entity e, Player user) {
        return e instanceof LivingEntity && !e.getUniqueId().equals(user.getUniqueId()) && !e.isInvulnerable();
    }

    private void applyDamage(LivingEntity target, Player damager) {
        // 创建自定义伤害事件（绕过护甲计算）
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(damager, target, DamageCause.CUSTOM, DAMAGE);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            double finalHealth = target.getHealth() - DAMAGE;
            target.setHealth(Math.max(finalHealth, 0));

            // 击退效果
            Vector knockback =
                    damager.getEyeLocation().getDirection().multiply(1.2).setY(0.4);
            target.setVelocity(knockback);

            // 伤害特效
            spawnDamageParticles(target);
        }

        target.setNoDamageTicks(1);
    }

    private void spawnDamageParticles(LivingEntity target) {
        World world = target.getWorld();
        Location particleLoc = target.getEyeLocation().add(0, 0.5, 0);
        world.spawnParticle(Particle.DAMAGE_INDICATOR, particleLoc, 12, 0.3, 0.5, 0.3, 0.15);
    }

    private void playAttackEffects(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.SPELL_WITCH, loc, 20, 0.2, 0.2, 0.2, 0.3);
        world.playSound(loc, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0F, 1.5F);
    }

    private void playMissEffect(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.SMOKE_NORMAL, loc, 10, 0.5, 0.5, 0.5, 0.1);
        world.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.8F, 1.2F);
    }

    @Override
    public float getMaxItemCharge(ItemStack itemStack) {
        return MAX_CHARGE;
    }
}
