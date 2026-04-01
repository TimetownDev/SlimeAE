package me.ddggdd135.slimeae.core.commands.subcommands;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.SubCommand;
import me.ddggdd135.slimeae.api.database.v3.RepairTask;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RepairCommand extends SubCommand {
    @Override
    @Nonnull
    public String getName() {
        return "repair";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "检测并修复数据库损坏";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae repair [--level=N] [--dry-run]";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("slimeae.admin")) {
            sender.sendMessage(CMIChatColor.translate("&c你没有权限使用这个命令"));
            return false;
        }
        int level = 4;
        boolean dryRun = false;
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("--level=")) {
                try {
                    level = Integer.parseInt(args[i].substring(8));
                } catch (NumberFormatException ignored) {
                }
            } else if ("--dry-run".equals(args[i])) {
                dryRun = true;
            }
        }
        sender.sendMessage(CMIChatColor.translate("&e开始修复检查 (level=" + level + ", dry-run=" + dryRun + ")..."));
        var mgr = SlimeAEPlugin.getV3DatabaseManager();
        RepairTask task = new RepairTask(
                mgr.getConnectionManager(), mgr.getSchemaManager().getDDL(), level, dryRun);
        List<String> report = task.run();
        for (String line : report) {
            sender.sendMessage(CMIChatColor.translate("&7" + line));
        }
        sender.sendMessage(CMIChatColor.translate("&a修复检查完成"));
        return true;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (args.length == 2)
            return List.of("--level=0", "--level=1", "--level=2", "--level=3", "--level=4", "--dry-run");
        if (args.length == 3) return List.of("--dry-run");
        return List.of();
    }
}
