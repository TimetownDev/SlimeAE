package me.ddggdd135.slimeae.core.commands.subcommands;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MigrateCommand extends SubCommand {
    @Override
    @Nonnull
    public String getName() {
        return "migrate";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "手动触发v2到v3数据迁移";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae migrate";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("slimeae.admin")) {
            sender.sendMessage(CMIChatColor.translate("&c你没有权限使用这个命令"));
            return false;
        }
        sender.sendMessage(CMIChatColor.translate("&e开始执行v2→v3迁移..."));
        try {
            SlimeAEPlugin.getV3DatabaseManager().getMigration().migrate();
            SlimeAEPlugin.getV3DatabaseManager().getSchemaManager().markSchemaVersion3();
            sender.sendMessage(CMIChatColor.translate("&a迁移完成!"));
        } catch (Exception e) {
            sender.sendMessage(CMIChatColor.translate("&c迁移失败: " + e.getMessage()));
        }
        return true;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return List.of();
    }
}
