package top.prefersmin.banitem;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import top.prefersmin.banitem.common.CommandRegister;
import top.prefersmin.banitem.config.BanItemConfig;

@Mod(BanItem.MODID)
public class BanItem {

    public static final String MODID = "banitem";

    private static final Logger LOGGER = LogUtils.getLogger();

    public BanItem() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        MinecraftForge.EVENT_BUS.register(this);
        // 注册命令
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        // 初始化配置
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BanItemConfig.SPEC);
    }

    public static boolean shouldDelete(ItemStack stack) {
        BanItemEvent event = new BanItemEvent(stack);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DEFAULT) {
            return BanItemConfig.ITEM_BLACKLIST.get().contains(stack.getItem().getDescriptionId().replaceFirst("^(item|block)\\.", "").replaceFirst("\\.", ":"));
        } else {
            return event.getResult() == Event.Result.DENY;
        }
    }

    /**
     * 注册命令
     * @param event 事件
     */
    private void registerCommands(RegisterCommandsEvent event) {
        CommandRegister.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity) {
            if (shouldDelete(((ItemEntity) event.getEntity()).getItem())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if (shouldDelete(event.getStack())) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            } else {
                LOGGER.warn("BanItem：取消玩家拾取事件失败");
            }
        }
    }

    @SubscribeEvent
    public void onPlayerContainerOpen(PlayerContainerEvent event) {
        for (int i = 0; i < event.getContainer().slots.size(); ++i) {
            if (shouldDelete(event.getContainer().getItems().get(i))) {
                event.getContainer().getItems().set(i, ItemStack.EMPTY);
            }
        }
    }

}
