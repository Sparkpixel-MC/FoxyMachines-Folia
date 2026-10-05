package me.gallowsdove.foxymachines.listeners;

import io.github.thebusybiscuit.slimefun4.api.events.ExplosiveToolBreakBlocksEvent;
import io.github.mooy1.infinitylib.common.Scheduler;
import me.gallowsdove.foxymachines.FoxyMachines;
import me.gallowsdove.foxymachines.implementation.machines.ForcefieldDome;
import me.gallowsdove.foxymachines.utils.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ForcefieldListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onPlayerBreak(@Nonnull BlockBreakEvent e) {
        Block b = e.getBlock();

        if (ForcefieldDome.FORCEFIELD_BLOCKS.remove(b)) {
            Scheduler.runAtRegion(b.getLocation(), () -> b.setType(Material.BARRIER));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onExplosionBreak(@Nonnull BlockExplodeEvent e) {
        Block b = e.getBlock();

        if (ForcefieldDome.FORCEFIELD_BLOCKS.remove(b)) {
            Scheduler.runAtRegion(b.getLocation(), () -> b.setType(Material.BARRIER));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onBurnBreak(@Nonnull BlockBurnEvent e) {
        Block b = e.getBlock();

        if (ForcefieldDome.FORCEFIELD_BLOCKS.remove(b)) {
            Scheduler.runAtRegion(b.getLocation(), () -> b.setType(Material.BARRIER));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onLeavesDecay(@Nonnull LeavesDecayEvent e) {
        Block b = e.getBlock();

        if (ForcefieldDome.FORCEFIELD_BLOCKS.remove(b)) {
            Scheduler.runAtRegion(b.getLocation(), () -> b.setType(Material.BARRIER));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onFadeBreak(@Nonnull BlockFadeEvent e) {
        Block b = e.getBlock();

        if (ForcefieldDome.FORCEFIELD_BLOCKS.remove(b)) {
            Scheduler.runAtRegion(b.getLocation(), () -> b.setType(Material.BARRIER));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onBlockDropEvent(@Nonnull EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof FallingBlock)) {
            return;
        }

        Block b = e.getBlock();

        if (ForcefieldDome.FORCEFIELD_BLOCKS.remove(b)) {
            Scheduler.runAtRegion(b.getLocation(), () -> b.setType(Material.BARRIER));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onBlocksBreakByExplosiveToolEvent(@Nonnull ExplosiveToolBreakBlocksEvent e) {
        for (Block b : e.getAdditionalBlocks()) {
            if (ForcefieldDome.FORCEFIELD_BLOCKS.remove(b)) {
                Scheduler.runAtRegion(b.getLocation(), () -> b.setType(Material.BARRIER));
            }
        }
    }

    // 末影珍珠传送：Folia 上 PlayerTeleportEvent 不会触发 (PaperMC/Folia#490)，
    // 改在珍珠命中时取消，命中取消即阻止传送（Paper 与 Folia 均有效）。
    @EventHandler(ignoreCancelled = true)
    private void onEnderPearlHit(@Nonnull ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof EnderPearl pearl)) {
            return;
        }

        if (ForcefieldDome.isInsideDome(pearl.getLocation())) {
            e.setCancelled(true);
            if (pearl.getShooter() instanceof Player p) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + "你不能传送到穹顶力场内部!");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerTeleport(@Nonnull PlayerTeleportEvent e) {
        if (e.getCause() == TeleportCause.ENDER_PEARL || e.getCause() == TeleportCause.CONSUMABLE_EFFECT) {
            Location l = e.getTo();
            for (SimpleLocation loc: ForcefieldDome.domeLocations) {
                if (e.getPlayer().getWorld() == Bukkit.getServer().getWorld(UUID.fromString(loc.getWorldUUID()))) {
                    int xdif = (int) (l.getX() - loc.getX());
                    int ydif = (int) (l.getY() - loc.getY());
                    int zdif = (int) (l.getZ() - loc.getZ());
                    if (Math.floor(Math.sqrt((xdif * xdif) + (ydif * ydif) + (zdif * zdif))) <= 32) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "你不能传送到穹顶力场内部!");
                        break;
                    }
                }
            }
        }
    }
}
