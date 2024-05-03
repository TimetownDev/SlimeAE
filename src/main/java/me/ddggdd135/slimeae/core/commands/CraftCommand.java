package me.ddggdd135.slimeae.core.commands;

import java.util.ArrayList;
import java.util.List;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.exceptions.NoEnoughMaterialsException;
import me.ddggdd135.slimeae.core.AutoCraftingSession;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import net.Zrips.CMILib.Colors.CMIChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(
            @NotNull CommandSender commandSender,
            @NotNull Command command,
            @NotNull String s,
            @NotNull String[] strings) {
        if (commandSender instanceof Player player) {
            Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
            if (info == null) player.sendMessage(CMIChatColor.translate("请站在对应网络方块上"));
            if (info.getCraftingSessions().size() >= 8) {
                player.sendMessage(CMIChatColor.translate("&c&l这个网络已经有8个合成任务了"));
                return false;
            }
            if (strings.length == 0) {
                player.sendMessage(CMIChatColor.translate("&c&l用法 /ae_craft <Amount>"));
            }
            try {
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack.getType().isAir()) {
                    player.sendMessage(CMIChatColor.translate("&c&l你是打算要合成空气吗?"));
                    return false;
                }
                new AutoCraftingSession(
                        info,
                        RecipeUtils.getRecipe(player.getInventory().getItemInMainHand()),
                        Integer.parseInt(strings[0]));
                player.sendMessage(CMIChatColor.translate("&a&l成功规划了合成任务"));
            } catch (NumberFormatException e) {
                player.sendMessage(CMIChatColor.translate("&c&l用法 /ae_craft <Amount>"));
            } catch (NoEnoughMaterialsException | StackOverflowError e) {
                player.sendMessage(CMIChatColor.translate("&e&l没有足够的材料"));
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender commandSender,
            @NotNull Command command,
            @NotNull String s,
            @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
