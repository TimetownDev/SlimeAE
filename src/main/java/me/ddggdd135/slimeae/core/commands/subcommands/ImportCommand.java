package me.ddggdd135.slimeae.core.commands.subcommands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.SubCommand;
import me.ddggdd135.slimeae.api.database.v3.ExportImportManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ImportCommand extends SubCommand {
    @Override
    @Nonnull
    public String getName() {
        return "import";
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "导入存储元件数据";
    }

    @Override
    @Nonnull
    public String getUsage() {
        return "/ae import <filename>";
    }

    @Override
    public boolean onCommand(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("slimeae.admin")) {
            sender.sendMessage(CMIChatColor.translate("&c你没有权限使用这个命令"));
            return false;
        }
        if (args.length < 2) {
            sender.sendMessage(CMIChatColor.translate("&c用法: /ae import <filename>"));
            return false;
        }
        String filename = args[1];
        File exportDir = new File(SlimeAEPlugin.getInstance().getDataFolder(), "exports");
        File file = new File(exportDir, filename);
        if (!file.exists()) {
            file = new File(exportDir, filename + ".json");
        }
        if (!file.exists()) {
            sender.sendMessage(CMIChatColor.translate("&c文件不存在: " + file.getAbsolutePath()));
            return false;
        }
        sender.sendMessage(CMIChatColor.translate("&e正在导入 " + file.getName() + "..."));
        try {
            var mgr = SlimeAEPlugin.getV3DatabaseManager();
            ExportImportManager eim = new ExportImportManager(
                    mgr.getConnectionManager(),
                    mgr.getTemplateRegistry(),
                    mgr.getSchemaManager().getDDL());
            int count = eim.importCell(file);
            sender.sendMessage(CMIChatColor.translate("&a导入成功: " + count + " 个物品"));
        } catch (Exception e) {
            sender.sendMessage(CMIChatColor.translate("&c导入失败: " + e.getMessage()));
        }
        return true;
    }

    @Override
    @Nullable public List<String> onTabComplete(
            @Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (args.length == 2) {
            File exportDir = new File(SlimeAEPlugin.getInstance().getDataFolder(), "exports");
            if (exportDir.isDirectory()) {
                String[] files = exportDir.list();
                if (files != null) {
                    List<String> results = new ArrayList<>();
                    for (String f : files) {
                        if (f.endsWith(".json")) results.add(f);
                    }
                    return results;
                }
            }
        }
        return List.of();
    }
}
