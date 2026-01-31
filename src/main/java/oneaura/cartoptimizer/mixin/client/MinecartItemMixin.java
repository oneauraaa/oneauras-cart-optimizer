package oneaura.cartoptimizer.mixin.client;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.MinecartItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This mixin immediately decrements the minecart item on the client side
 * when used on a rail, preventing the "ghost cart" issue on laggy servers.
 */
@Mixin(MinecartItem.class)
public class MinecartItemMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("oneauras-cart-optimizer-mixin");

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        // Only run on client side
        if (context.getWorld().isClient()) {
            // Check if clicking on a rail
            if (context.getWorld().getBlockState(context.getBlockPos()).isIn(BlockTags.RAILS)) {
                // Get player to access inventory directly
                net.minecraft.entity.player.PlayerEntity player = context.getPlayer();

                if (player != null) {
                    // Try to get the stack directly from the hand used
                    ItemStack stack = player.getStackInHand(context.getHand());

                    // Verify it's the right item (just in case of rapid switching desync, though
                    // unlikely in this call stack)
                    if (!stack.isEmpty() && stack.getItem() == context.getStack().getItem()) {
                        stack.decrement(1);
                        LOGGER.info("[Cart Optimizer] Force removed minecart from player hand (Client). Remaining: "
                                + stack.getCount());
                    } else {
                        // Fallback to context stack if hand is already different or empty
                        context.getStack().decrement(1);
                        LOGGER.info("[Cart Optimizer] Force removed minecart from context stack (Client).");
                    }

                    // Return SUCCESS to tell the client the action worked.
                    cir.setReturnValue(ActionResult.SUCCESS);
                }
            }
        }
    }
}
