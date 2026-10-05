package me.gallowsdove.foxymachines.tasks;

import io.github.mooy1.infinitylib.common.Scheduler;
import me.gallowsdove.foxymachines.implementation.materials.GhostBlock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.UUID;

public class GhostBlockTask implements Runnable {
    @Override
    public void run() {
        for (UUID uuid : Set.copyOf(GhostBlock.BLOCK_CACHE)) {
            Entity entity = Bukkit.getEntity(uuid);
            if (!(entity instanceof FallingBlock) || !GhostBlock.isGhostBlock(entity)) {
                GhostBlock.BLOCK_CACHE.remove(uuid);
                continue;
            }

            Scheduler.runAtEntity(entity, () -> {
                FallingBlock b = (FallingBlock) entity;
                b.setGravity(false);
                b.setVelocity(new Vector(0, 0, 0));
                b.setTicksLived(1);
            });
        }
    }
}
