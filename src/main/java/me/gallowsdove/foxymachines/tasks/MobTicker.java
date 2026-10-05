package me.gallowsdove.foxymachines.tasks;

import io.github.mooy1.infinitylib.common.Scheduler;
import me.gallowsdove.foxymachines.abstracts.CustomMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MobTicker implements Runnable {
    int tick = 0;

    @Override
    public void run() {
        for (CustomMob mob : CustomMob.MOBS.values()) {
            mob.onUniqueTick(tick);
        }

        for (Map.Entry<CustomMob, Set<UUID>> entry : CustomMob.MOB_CACHE.entrySet()) {
            CustomMob customMob = entry.getKey();
            for (UUID uuid : entry.getValue()) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity == null) {
                    customMob.uncacheEntity(uuid);
                    continue;
                }

                if (!(entity instanceof LivingEntity livingEntity)) {
                    Scheduler.runAtEntity(entity, entity::remove);
                    customMob.uncacheEntity(uuid);
                    continue;
                }

                Scheduler.runAtEntity(livingEntity, () -> customMob.onMobTick(livingEntity, tick));
            }
        }

        if (tick == 100) {
            tick = 0;
        }
        tick++;
    }
}
