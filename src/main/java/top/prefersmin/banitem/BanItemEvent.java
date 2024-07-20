package top.prefersmin.banitem;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * 每当检测物品是否应该删除时触发
 *
 * @author PrefersMin
 */
@Event.HasResult
public class BanItemEvent extends Event {

    public final ItemStack stack;

    public BanItemEvent(ItemStack stack) {
        this.stack = stack;
    }

    /**
     * 获取物品
     * @return 物品实体
     */
    public ItemStack getItem() {
        return this.stack;
    }

}
