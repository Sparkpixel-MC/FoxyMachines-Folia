package me.gallowsdove.foxymachines.listeners;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.gallowsdove.foxymachines.FoxyMachines;
import me.gallowsdove.foxymachines.Items;
import me.gallowsdove.foxymachines.implementation.machines.ChunkLoader;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;

public class ChunkLoaderListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onChunkLoaderPlace(@Nonnull BlockPlaceEvent e) {
        if (e.getBlock().getType() != Material.BEACON) {
            return;
        }

        ItemStack item = e.getItemInHand();
        Player p = e.getPlayer();

        if (!SlimefunUtils.isItemSimilar(item, Items.CHUNK_LOADER, true, false)) {
            return;
        }

        Block b = e.getBlockPlaced();
        if (b.getWorld().getPluginChunkTickets(b.getX() >> 4, b.getZ() >> 4).contains(FoxyMachines.getInstance())) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.LIGHT_PURPLE + "该区块已经处于强制加载状态，无法放置区块加载器！");
            return;
        }

        NamespacedKey key = new NamespacedKey(FoxyMachines.getInstance(), "chunkloaders");

        int i = p.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0) + 1;
        Config cfg = new Config(FoxyMachines.getInstance());
        if (!p.hasPermission("foxymachines.bypass-chunk-loader-limit")) {
            int max = cfg.getInt("max-chunk-loaders");
            if (max != 0 && max < i) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + "已达到区块加载器最大数量限制：" + max);
                e.setCancelled(true);
                return;
            }
        }
        int currentComplexity = Slimefun.getGPSNetwork().getNetworkComplexity(p.getUniqueId());
        int requiredComplexity = cfg.getInt("gps-complexity-per-loader") * i;
        if (currentComplexity < requiredComplexity) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "你的GPS网络复杂度 " + currentComplexity + "/" + requiredComplexity + " " +
                "不满足区块加载器的放置条件。");
            e.setCancelled(true);
            return;
        }

        p.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, i);
        b.getWorld().addPluginChunkTicket(b.getX() >> 4, b.getZ() >> 4, FoxyMachines.getInstance());
        ChunkLoader.addLoader(b);
    }
}
