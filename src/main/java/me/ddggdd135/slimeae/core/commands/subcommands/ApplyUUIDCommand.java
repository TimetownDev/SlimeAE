package me.ddggdd135.slimeae.core.commands.subcommands;

import static me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell.UUID_KEY;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.guguslimefunlib.libraries.nbtapi.NBT;
import me.ddggdd135.slimeae.api.MEStorageCellCache;
import me.ddggdd135.slimeae.api.SubCommand;
import me.ddggdd135.slimeae.core.slimefun.MEItemStorageCell;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ApplyUUIDCommand extends SubCommand {
    @Override
    @Nonnull
    public String getName() {
        return "applyuuid";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "修改存储元件的UUID";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae applyuuid <StorageCell_UUID>";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!commandSender.hasPermission("slimeae.applyuuid")) {
            commandSender.sendMessage(CMIChatColor.translate("&a你没有权限使用这个命令"));
            return false;
        }
        if (args.length != 2) {
            commandSender.sendMessage(CMIChatColor.translate("&e用法:" + getUsage()));
            return false;
        }
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(CMIChatColor.translate("&e只能由玩家运行这个命令"));
            return false;
        } else {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getType().isAir()) {
                commandSender.sendMessage(CMIChatColor.translate("&e你是要修改空气吗？"));
                return false;
            }

            MEStorageCellCache data = MEItemStorageCell.getStorage(itemStack);
            if (data == null) {
                commandSender.sendMessage(CMIChatColor.translate("&e你确定你拿着存储元件？"));
                return false;
            }

            UUID uuid;
            try {
                uuid = UUID.fromString(args[1]);
            } catch (IllegalArgumentException e) {
                commandSender.sendMessage("UUID错误");
                return false;
            }

            itemStack = player.getInventory().getItemInMainHand();
            NBT.modify(itemStack, x -> {
                x.setUUID(UUID_KEY, uuid);
            });
            commandSender.sendMessage(CMIChatColor.translate("&e修改成功"));
        }
        return false;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return List.of();
    }
}
