package me.gallowsdove.foxymachines.commands;

import io.github.mooy1.infinitylib.commands.SubCommand;
import io.github.mooy1.infinitylib.common.Scheduler;
import me.gallowsdove.foxymachines.abstracts.CustomBoss;
import me.gallowsdove.foxymachines.abstracts.CustomMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class KillallCommand extends SubCommand {
    public KillallCommand() {
        super("killall", "击杀当前世界中由 FoxyMachines 召唤的所有自定义生物", "foxymachines.admin");
    }

    @Override
    protected void execute(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }

        if (args.length != 0) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "使用方法: /foxy killall");
            return;
        }

        int count = 0;
        for (Set<UUID> uuids : CustomMob.MOB_CACHE.values()) {
            for (UUID uuid : uuids) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity instanceof LivingEntity && entity.getWorld().equals(player.getWorld())) {
                    Scheduler.runAtEntity(entity, entity::remove);
                    count++;
                }
            }
        }

        CustomBoss.removeBossBars();

        player.sendMessage("已移除 %s 个实体".formatted(count));
    }

    @Override
    protected void complete(@Nonnull CommandSender commandSender, @Nonnull String[] strings, @Nonnull List<String> list) { }
}
