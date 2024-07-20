package top.prefersmin.banitem.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.prefersmin.banitem.BanItem;

import javax.annotation.Nullable;

/**
 * 区块实体类Mixin
 *
 * @author PrefersMin
 * @version 1.0
 */
@Mixin(BlockEntity.class)
public abstract class TileEntityMixin implements ICapabilityProvider {

    @Shadow
    @Nullable
    protected Level level;

    /**
     * 禁止Minecraft游戏世界保存黑名单物品
     */
    @Inject(at = @At(value = "HEAD"), method = "setChanged()V")
    public void onMarkDirty(CallbackInfo ci) {

        if (level == null) {
            return;
        }

        // 检查是否属于容器
        if (this instanceof Container) {
            // 遍历容器槽位
            for (int i = 0; i < ((Container) this).getContainerSize(); i++) {
                // 判断是否黑名单物品
                if (BanItem.shouldDelete(((Container) this).getItem(i))) {
                    // 将槽位置空以删除物品
                    ((Container) this).setItem(i, ItemStack.EMPTY);
                }
            }
        } else {
            // 通过能力系统检查是否拥有储物能力
            this.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(cap -> {
                // 检查是否拥有覆写物品槽位的能力
                if (cap instanceof IItemHandlerModifiable) {
                    // 遍历物品槽位
                    for (int i = 0; i < cap.getSlots(); i++) {
                        // 判断是否黑名单物品
                        if (BanItem.shouldDelete(cap.getStackInSlot(i))) {
                            // 将槽位置空以删除物品
                            ((IItemHandlerModifiable) cap).setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }
                }
            });
        }

    }
}
