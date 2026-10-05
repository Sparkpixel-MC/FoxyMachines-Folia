package me.gallowsdove.foxymachines.listeners;

import me.gallowsdove.foxymachines.implementation.materials.GhostBlock;
import io.github.mooy1.infinitylib.common.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GhostBlockListener implements Listener {
    private final Map<UUID, Location> preExplosionLocations = new ConcurrentHashMap<>();
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onHitByFishingRod(PlayerFishEvent e) {
        if (e.getCaught() instanceof FallingBlock b &&
                b.getPersistentDataContainer().has(GhostBlock.KEY, PersistentDataType.STRING)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onEntityExplode(EntityExplodeEvent e) {
        for (Entity ent : e.getLocation().getWorld().getNearbyEntities(e.getLocation(), 6, 6, 6)) {
            if (ent instanceof FallingBlock b && GhostBlock.isGhostBlock(b)) {
                preExplosionLocations.put(b.getUniqueId(), b.getLocation());
            }
        }

        Scheduler.runAtRegion(e.getLocation(), () -> {
            for (Map.Entry<UUID, Location> entry : preExplosionLocations.entrySet()) {
                preExplosionLocations.remove(entry.getKey());
                Entity ent = Bukkit.getEntity(entry.getKey());
                if (ent instanceof FallingBlock fb && GhostBlock.isGhostBlock(fb)) {
                    fb.teleportAsync(entry.getValue()).thenAccept(unused -> {
                        if (fb.isValid()) {
                            fb.setVelocity(new Vector(0, 0, 0));
                            fb.setGravity(false);
                        }
                    });
                }
            }
        });
    }
}
