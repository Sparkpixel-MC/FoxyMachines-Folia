package me.gallowsdove.foxymachines.implementation.machines;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import me.gallowsdove.foxymachines.FoxyMachines;
import me.gallowsdove.foxymachines.Items;
import me.gallowsdove.foxymachines.utils.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;


public class ChunkLoader extends SlimefunItem {

    // 插件区块票证不跨重启持久化，需自行记录位置并在启动时重新加票
    public static final List<SimpleLocation> LOADER_LOCATIONS = new CopyOnWriteArrayList<>();

    private static final String FILE_NAME = "chunkloaderdata";

    public ChunkLoader() {
        super(Items.MACHINES_ITEM_GROUP, Items.CHUNK_LOADER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                Items.REINFORCED_STRING, Items.STABILIZED_BLISTERING_BLOCK, Items.REINFORCED_STRING,
                SlimefunItems.ENRICHED_NETHER_ICE, Items.STABILIZED_BLISTERING_BLOCK, Items.WIRELESS_TRANSMITTER,
                Items.REINFORCED_STRING, Items.STABILIZED_BLISTERING_BLOCK, Items.REINFORCED_STRING
        });
    }

    public static void addLoader(@Nonnull Block b) {
        LOADER_LOCATIONS.add(new SimpleLocation(b, "chunkloader"));
        saveLoaderLocations();
    }

    public static void removeLoader(@Nonnull Block b) {
        LOADER_LOCATIONS.remove(new SimpleLocation(b, "chunkloader"));
        saveLoaderLocations();
    }

    public static void loadLoaderLocations() {
        try {
            Gson gson = new Gson();
            File file = new File(FoxyMachines.getInstance().folderPath + FILE_NAME);
            new File(FoxyMachines.getInstance().folderPath).mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String json = reader.readLine();
                Type type = new TypeToken<ArrayList<SimpleLocation>>() {}.getType();
                List<SimpleLocation> loaded = gson.fromJson(json, type);
                LOADER_LOCATIONS.clear();
                if (loaded != null) {
                    LOADER_LOCATIONS.addAll(loaded);
                }
            }
        } catch (IOException e) {
            FoxyMachines.getInstance().getLogger().log(Level.SEVERE, "无法读取区块加载器数据!", e);
        }
    }

    public static void saveLoaderLocations() {
        try {
            Gson gson = new Gson();
            String pluginFolder = FoxyMachines.getInstance().folderPath;
            File file = new File(pluginFolder + File.separator + FILE_NAME);
            new File(pluginFolder).mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                writer.write(gson.toJson(LOADER_LOCATIONS));
            }
        } catch (IOException e) {
            FoxyMachines.getInstance().getLogger().log(Level.SEVERE, "无法保存区块加载器数据!", e);
        }
    }

    public static void applyLoaderTickets() {
        for (SimpleLocation loc : new ArrayList<>(LOADER_LOCATIONS)) {
            World w = Bukkit.getServer().getWorld(UUID.fromString(loc.getWorldUUID()));
            if (w == null) {
                LOADER_LOCATIONS.remove(loc);
                continue;
            }

            Location l = new Location(w, loc.getX(), loc.getY(), loc.getZ());
            Scheduler.runAtRegion(l, () -> {
                SlimefunBlockData data = StorageCacheUtils.getBlock(l.getBlock().getLocation());
                if (data == null || !data.getSfId().equals(Items.CHUNK_LOADER.getItemId())) {
                    LOADER_LOCATIONS.remove(loc);
                    return;
                }

                Block b = l.getBlock();
                w.addPluginChunkTicket(b.getX() >> 4, b.getZ() >> 4, FoxyMachines.getInstance());
            });
        }
        saveLoaderLocations();
    }

    @Override
    public void preRegister() {
        addItemHandler(onBreak(), onBlockUse(), onPlace());
    }

    @Nonnull
    private BlockBreakHandler onBreak() {
        return new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(@Nonnull BlockBreakEvent e, @Nonnull ItemStack item, @Nonnull List<ItemStack> drops) {
                Block b = e.getBlock();
                String owner = StorageCacheUtils.getData(b.getLocation(), "owner");
                if (owner != null) {
                    NamespacedKey key = new NamespacedKey(FoxyMachines.getInstance(), "chunkloaders");
                    Player p = Bukkit.getPlayer(UUID.fromString(owner));

                    if (p != null) {
                        int i = p.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 1) - 1;
                        p.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, i);
                    }

                    Slimefun.getDatabaseManager().getBlockDataController().removeBlock(b.getLocation());

                    Scheduler.runAtRegion(b.getLocation(), () -> b.setType(Material.GLASS));
                }

                b.getWorld().removePluginChunkTicket(b.getX() >> 4, b.getZ() >> 4, FoxyMachines.getInstance());
                removeLoader(b);
            }
        };
    }

    @Nonnull
    private BlockUseHandler onBlockUse() {
        return PlayerRightClickEvent::cancel;
    }

    @Nonnull
    private BlockPlaceHandler onPlace() {
        return new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent e) {
                StorageCacheUtils.setData(e.getBlock().getLocation(), "owner", e.getPlayer().getUniqueId().toString());
            }
        };
    }

}
