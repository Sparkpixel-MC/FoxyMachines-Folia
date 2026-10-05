package me.gallowsdove.foxymachines;

import io.github.mooy1.infinitylib.common.Events;
import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.mooy1.infinitylib.core.AbstractAddon;
import io.github.mooy1.infinitylib.metrics.bukkit.Metrics;
import lombok.SneakyThrows;
import me.gallowsdove.foxymachines.abstracts.AbstractWand;
import me.gallowsdove.foxymachines.abstracts.CustomBoss;
import me.gallowsdove.foxymachines.commands.KillallCommand;
import me.gallowsdove.foxymachines.commands.ListallCommand;
import me.gallowsdove.foxymachines.commands.QuestCommand;
import me.gallowsdove.foxymachines.commands.SacrificialAltarCommand;
import me.gallowsdove.foxymachines.commands.SummonCommand;
import me.gallowsdove.foxymachines.implementation.consumables.UnbreakableRune;
import me.gallowsdove.foxymachines.implementation.machines.ForcefieldDome;
import me.gallowsdove.foxymachines.implementation.tools.BerryBushTrimmer;
import me.gallowsdove.foxymachines.listeners.ArmorListener;
import me.gallowsdove.foxymachines.listeners.BerryBushListener;
import me.gallowsdove.foxymachines.listeners.BoostedRailListener;
import me.gallowsdove.foxymachines.listeners.BowListener;
import me.gallowsdove.foxymachines.listeners.ChunkLoadListener;
import me.gallowsdove.foxymachines.listeners.ChunkLoaderListener;
import me.gallowsdove.foxymachines.listeners.ForcefieldListener;
import me.gallowsdove.foxymachines.listeners.GhostBlockListener;
import me.gallowsdove.foxymachines.listeners.PoseidonsFishingRodListener;
import me.gallowsdove.foxymachines.listeners.PositionSelectorListener;
import me.gallowsdove.foxymachines.listeners.RemoteControllerListener;
import me.gallowsdove.foxymachines.listeners.SacrificialAltarListener;
import me.gallowsdove.foxymachines.listeners.SwordListener;
import me.gallowsdove.foxymachines.tasks.GhostBlockTask;
import me.gallowsdove.foxymachines.tasks.MobTicker;
import me.gallowsdove.foxymachines.tasks.QuestTicker;
import me.gallowsdove.foxymachines.utils.QuestUtils;
import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.logging.Level;

public class FoxyMachines extends AbstractAddon {
    private static FoxyMachines instance;

    public String folderPath;

    public FoxyMachines() {
        super("SlimefunGuguProject", "FoxyMachines", "master", "auto-update");
    }

    @Override
    @SneakyThrows
    public void enable() {
        instance = this;

        if (!getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
            getLogger().log(Level.SEVERE, "本插件需要 鬼斩前置库插件(GuizhanLibPlugin) 才能运行!");
            getLogger().log(Level.SEVERE, "从此处下载: https://50L.cc/gzlib");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getConfig().getBoolean("auto-update") && getDescription().getVersion().startsWith("Build")) {
            GuizhanUpdater.start(this, getFile(), "SlimefunGuguProject", "FoxyMachines", "master");
        }

        Events.registerListener(new ChunkLoadListener());
        Events.registerListener(new ChunkLoaderListener());
        Events.registerListener(new BoostedRailListener());
        Events.registerListener(new BerryBushListener());
        Events.registerListener(new ForcefieldListener());
        Events.registerListener(new GhostBlockListener());
        Events.registerListener(new RemoteControllerListener());
        Events.registerListener(new SacrificialAltarListener());
        Events.registerListener(new SwordListener());
        Events.registerListener(new PoseidonsFishingRodListener());
        Events.registerListener(new ArmorListener());
        Events.registerListener(new BowListener());
        Events.registerListener(new PositionSelectorListener());

        QuestUtils.init();
        AbstractWand.init();
        UnbreakableRune.init();
        ItemSetup.INSTANCE.init();
        ResearchSetup.INSTANCE.init();

        this.folderPath = getDataFolder().getAbsolutePath() + File.separator + "data-storage" + File.separator;
        BerryBushTrimmer.loadTrimmedBlocks();
        ForcefieldDome.loadDomeLocations();
        Scheduler.run(() -> ForcefieldDome.INSTANCE.setupDomes());
        Scheduler.repeat(240, 10, new QuestTicker());
        Scheduler.repeat(100, new GhostBlockTask());
        if (getConfig().getBoolean("custom-mobs")) {
            Scheduler.repeat(2, new MobTicker());
        }

        new Metrics(this, 10568);

        getAddonCommand().addSub(new KillallCommand()).addSub((new QuestCommand())).
                addSub(new SacrificialAltarCommand()).addSub(new SummonCommand()).addSub(new ListallCommand());
    }

    @SneakyThrows
    @Override
    public void disable() {
        BerryBushTrimmer.saveTrimmedBlocks();
        ForcefieldDome.saveDomeLocations();
        if (getConfig().getBoolean("custom-mobs")) {
            CustomBoss.removeBossBars();
        }
    }

    @Nonnull
    public static FoxyMachines getInstance() {
        return instance;
    }
}
