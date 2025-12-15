package me.gallowsdove.foxymachines.implementation.materials;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemDropHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import me.gallowsdove.foxymachines.listeners.SacrificialAltarListener;
import me.gallowsdove.foxymachines.utils.QuestUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class ShardMaterial extends SimpleSlimefunItem<ItemDropHandler> {
    private final ChatColor color;

    public ShardMaterial(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, ChatColor color) {
        super(itemGroup, item, recipeType, recipe);
        this.color = color;
    }

    @Nonnull
    @Override
    public ItemDropHandler getItemHandler() {
        return (e, p, item) -> {
            if (!isItem(item.getItemStack())) {
                return false;
            }

            if (Slimefun.instance() != null) {
                Bukkit.getGlobalRegionScheduler().runDelayed(Slimefun.instance(), scheduledTask -> {
                    if (!QuestUtils.hasActiveQuest(p)) {
                        p.sendMessage(this.color + "你应该先使用 " + ChatColor.LIGHT_PURPLE + "/foxy quest " + this.color +
                                "查看你的任务！");
                        return;
                    }

                    if (SacrificialAltarListener.findAltar(item.getLocation().getBlock()) == null) {
                        return;
                    }

                    p.sendMessage(this.color + "已重置任务!");
                    QuestUtils.resetQuestLine(p);
                    SacrificialAltarListener.particleAnimation(item.getLocation());

                    if (item.getItemStack().getAmount() == 1) {
                        item.remove();
                    } else {
                        ItemStack i = item.getItemStack();
                        i.setAmount(i.getAmount() - 1);
                        item.setItemStack(i);
                    }
                }, 20L);
            }

            return true;
        };
    }
}