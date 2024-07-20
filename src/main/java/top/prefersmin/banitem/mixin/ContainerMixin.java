package top.prefersmin.banitem.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.prefersmin.banitem.BanItem;
import top.prefersmin.banitem.config.BanItemConfig;

/**
 * 容器接口Mixin
 *
 * @author PrefersMin
 * @version 1.0
 */
@Mixin(AbstractContainerMenu.class)
public abstract class ContainerMixin {

    @Shadow @Final public NonNullList<Slot> slots;

    /**
     * 方法映射，用于获取容器内的所有物品
     * @return 容器内物品列表
     */
    @Shadow public abstract NonNullList<ItemStack> getItems();

    /**
     * 容器内物品变化时过滤黑名单物品
     */
    @Inject(at = @At(value = "HEAD"), method = "broadcastChanges")
    public void onDetectAndSendChanges(CallbackInfo ci) {
        // 遍历容器槽位
        for(int i = 0; i < this.slots.size(); ++i) {
            // 判断是否黑名单物品
            if(BanItem.shouldDelete(this.getItems().get(i))) {
                // 将槽位置空以删除物品
                this.getItems().set(i, ItemStack.EMPTY);
            }
        }
    }

    /**
     * 关闭容器时过滤黑名单物品，同时判断玩家物品栏内是否存在黑名单物品
     */
    @Inject(at = @At(value = "HEAD"), method = "removed")
    public void onContainerClosed(Player playerIn, CallbackInfo ci) {
        // 遍历容器内槽位
        for(int i = 0; i < this.slots.size(); ++i) {
            // 判断是否黑名单物品
            if(BanItem.shouldDelete(this.getItems().get(i))) {
                // 将槽位置空以删除物品
                this.getItems().set(i, ItemStack.EMPTY);
            }
        }
        // 遍历玩家物品栏内槽位
        for(int i = 0; i < playerIn.getInventory().getContainerSize(); ++i) {
            // 仅服务端，判断是否允许OP持有黑名单物品，判断该槽位是否黑名单物品
            if(playerIn instanceof ServerPlayer && !BanItemConfig.ALLOW_OPERATOR_BYPASS.get() && BanItem.shouldDelete(playerIn.getInventory().getItem(i))) {
                // 将槽位置空以删除物品
                playerIn.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
