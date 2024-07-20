package top.prefersmin.banitem.mixin;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.prefersmin.banitem.BanItem;

/**
 * 交易菜单Mixin
 *
 * @author PrefersMin
 * @version 1.0
 */
@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin {

    /**
     * 从交易列表中过滤掉黑名单物品
     */
    @Inject(at = @At(value = "RETURN"), method = "getOffers", cancellable = true)
    private void getOffers(CallbackInfoReturnable<MerchantOffers> cir) {

        if (cir.getReturnValue() == null) {
            return;
        }

        MerchantOffers returnedOffers = new MerchantOffers(Util.make(new CompoundTag(), tag -> tag.put("Recipes", new ListTag())));
        cir.getReturnValue().forEach(offer -> {
            if (!BanItem.shouldDelete(offer.assemble())) {
                returnedOffers.add(offer);
            }
        });
        cir.setReturnValue(returnedOffers);
    }

}
