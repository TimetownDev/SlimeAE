package me.ddggdd135.slimeae.core.commands.subcommands;

import java.io.File;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.SubCommand;
import me.ddggdd135.slimeae.api.database.v3.ExportImportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ExportAllCommand extends SubCommand {
    @Override
    @Nonnull
    public String getName() {
        return "export-all";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "导出所有存储元件数据";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae export-all";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("slimeae.admin")) {
            sender.sendMessage(CMIChatColor.translate("&c你没有权限使用这个命令"));
            return false;
        }
        sender.sendMessage(CMIChatColor.translate("&e正在导出所有元件数据..."));
        try {
            var mgr = SlimeAEPlugin.getV3DatabaseManager();
            ExportImportManager eim = new ExportImportManager(
                    mgr.getConnectionManager(),
                    mgr.getTemplateRegistry(),
                    mgr.getSchemaManager().getDDL());
            File exportDir = new File(SlimeAEPlugin.getInstance().getDataFolder(), "exports");
            List<File> files = eim.exportAll(exportDir);
            sender.sendMessage(
                    CMIChatColor.translate("&a导出成功: " + files.size() + " 个元件已导出到 " + exportDir.getAbsolutePath()));
        } catch (Exception e) {
            sender.sendMessage(CMIChatColor.translate("&c导出失败: " + e.getMessage()));
        }
        return true;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        return List.of();
    }
}
