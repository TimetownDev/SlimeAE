package me.ddggdd135.slimeae.core.commands;

import java.util.ArrayList;
import java.util.List;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.core.AutoCraftingSession;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.RecipeUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestCommand implements CommandExecutor, TabCompleter {
    private AutoCraftingSession session;

    @Override
    public boolean onCommand(
            @NotNull CommandSender commandSender,
            @NotNull Command command,
            @NotNull String s,
            @NotNull String[] strings) {
        if (commandSender instanceof Player player) {
            // 得到玩家的坐标
            Location location = player.getLocation();
            // 通过坐标得到对应世界下对应的方块
            Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            NetworkInfo info = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
            if (session == null)
                session = new AutoCraftingSession(
                        info, RecipeUtils.getRecipe(player.getInventory().getItemInMainHand()), 1);
            else session.moveNext(1);
            player.sendMessage("\u00a7e执行了一次合成");
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
