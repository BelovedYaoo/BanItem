package top.prefersmin.banitem.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;
import top.prefersmin.banitem.BanItem;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.mojang.text2speech.Narrator.LOGGER;
import static top.prefersmin.banitem.BanItem.shouldDelete;

/**
 * Mod配置类
 *
 * @author PrefersMin
 * @version 1.0
 */
@Mod.EventBusSubscriber(modid = BanItem.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BanItemConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec.ConfigValue<List<String>> ITEM_BLACKLIST;
    private static final List<String> ITEM_BLACKLIST_DEFAULT = new ArrayList<>();
    private static final String ITEM_BLACKLIST_NAME = "itemBlacklist";
    private static final String ITEM_BLACKLIST_COMMENT = "\n物品黑名单";

    public static final ForgeConfigSpec SPEC;
    public static final Path path = FMLPaths.CONFIGDIR.get().resolve(BanItem.MODID + "-common.toml");

    static {
        BUILDER.push("banitem");
        ITEM_BLACKLIST = BUILDER.comment(ITEM_BLACKLIST_COMMENT).define(ITEM_BLACKLIST_NAME, ITEM_BLACKLIST_DEFAULT);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    /**
     * 自动监听配置文件重载事件，并通过重载事件重载配置文件
     *
     * @param event 事件
     */
    @SubscribeEvent
    public void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON) {
            SPEC.setConfig(event.getConfig().getConfigData());
            deleteBlacklistItemForWorld();
        }
    }

    /**
     * 手动重载配置文件
     */
    public void reloadConfig() {

        // 创建并加载配置文件
        CommentedFileConfig commentedFileConfig = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
        commentedFileConfig.load();

        // 设置新的配置文件
        SPEC.setConfig(commentedFileConfig);
        new BanItemConfig().deleteBlacklistItemForWorld();

    }

    public void deleteBlacklistItemForWorld() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        // 注册表
        RegistryAccess registryAccess = server.registryAccess();
        Registry<DimensionType> dimensionRegistry = registryAccess.registryOrThrow(Registries.DIMENSION_TYPE);

        dimensionRegistry.forEach((dimensionType) -> {
            ServerLevel serverLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionType.effectsLocation()));
            if (serverLevel == null) {
                return;
            }
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof ItemEntity itemEntity) {
                    if (shouldDelete(itemEntity.getItem())) {
                        LOGGER.info("BanItem：删除黑名单物品：" + itemEntity.getItem().getItem().getDescriptionId());
                        itemEntity.remove(Entity.RemovalReason.KILLED);
                    }
                }
            }
        });
    }

}
