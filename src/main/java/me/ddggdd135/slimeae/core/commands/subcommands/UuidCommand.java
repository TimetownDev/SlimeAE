package me.ddggdd135.slimeae.core.commands.subcommands;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.api.MEStorageCellCache;
import me.ddggdd135.slimeae.api.SubCommand;
import me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class UuidCommand extends SubCommand {
    @Override
    @Nonnull
    public String getName() {
        return "uuid";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "获取存储元件的uuid";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae uuid";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!commandSender.hasPermission("slimeae.uuid")) {
            commandSender.sendMessage(CMIChatColor.translate("&a你没有权限使用这个命令"));
            return false;
        }

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(CMIChatColor.translate("&e只能由玩家运行这个命令"));
            return false;
        } else {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getType().isAir()) {
                commandSender.sendMessage(CMIChatColor.translate("&e你是要获取空气吗？"));
                return false;
            }

            MEStorageCellCache data = MEItemStorageCell.getStorage(itemStack);
            if (data == null) {
                commandSender.sendMessage(CMIChatColor.translate("&e你确定你拿着存储元件？"));
                return false;
            }
            TextComponent textComponent = Component.text(
                            CMIChatColor.translate("&eUUID: " + data.getUuid().toString()))
                    .clickEvent(ClickEvent.clickEvent(
                            ClickEvent.Action.COPY_TO_CLIPBOARD, data.getUuid().toString()))
                    .hoverEvent(HoverEvent.showText(Component.text(CMIChatColor.translate("&e点击复制"))));
            commandSender.sendMessage(textComponent);
        }

        return false;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return List.of();
    }
}
