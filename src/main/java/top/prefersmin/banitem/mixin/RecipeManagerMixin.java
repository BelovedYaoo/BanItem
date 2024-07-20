package top.prefersmin.banitem.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.prefersmin.banitem.BanItem;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 配方管理器Mixin
 *
 * @author PrefersMin
 * @version 1.0
 */
@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    /**
     * 禁止通过配方合成黑名单物品
     */
    @Inject(at = @At(value = "RETURN"), method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", cancellable = true)
    private <C extends Container, T extends Recipe<C>> void onGetRecipe(RecipeType<T> pRecipeType, C pInventory, Level pLevel, CallbackInfoReturnable<Optional<T>> cir) {
        cir.getReturnValue().ifPresent(value ->
                cir.setReturnValue(BanItem.shouldDelete(value.getResultItem(pLevel.registryAccess())) ? Optional.empty() : Optional.of(value)));
    }

    /**
     * 过滤黑名单物品的配方
     */
    @Inject(at = @At(value = "RETURN"), method = "getRecipesFor", cancellable = true)
    private <C extends Container, T extends Recipe<C>> void onGetRecipes(RecipeType<T> pRecipeType, C pInventory, Level pLevel, CallbackInfoReturnable<List<T>> cir) {
        cir.setReturnValue(cir.getReturnValue().stream()
                .filter(entry -> !BanItem.shouldDelete(entry.assemble(pInventory, pLevel.registryAccess())))
                .collect(Collectors.toList()));
    }

}
