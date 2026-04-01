package me.ddggdd135.slimeae.core.commands.subcommands;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.SubCommand;
import me.ddggdd135.slimeae.api.database.v3.RollbackV3ToV2;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RollbackCommand extends SubCommand {
    @Override
    @Nonnull
    public String getName() {
        return "rollback";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "回退v3数据到v2格式";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae rollback v2";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("slimeae.admin")) {
            sender.sendMessage(CMIChatColor.translate("&c你没有权限使用这个命令"));
            return false;
        }
        if (args.length < 2 || !"v2".equalsIgnoreCase(args[1])) {
            sender.sendMessage(CMIChatColor.translate("&c用法: /ae rollback v2"));
            return false;
        }
        sender.sendMessage(CMIChatColor.translate("&e开始执行v3→v2回退..."));
        try {
            var mgr = SlimeAEPlugin.getV3DatabaseManager();
            RollbackV3ToV2 rollback = new RollbackV3ToV2(
                    mgr.getConnectionManager(), mgr.getSchemaManager().getDDL());
            if (!rollback.canRollback()) {
                sender.sendMessage(CMIChatColor.translate("&c无法回退: v3数据表不存在"));
                return false;
            }
            rollback.rollback();
            sender.sendMessage(CMIChatColor.translate("&a回退完成! 请重启服务器以使用v2数据。"));
        } catch (Exception e) {
            sender.sendMessage(CMIChatColor.translate("&c回退失败: " + e.getMessage()));
        }
        return true;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (args.length == 2) return List.of("v2");
        return List.of();
    }
}
