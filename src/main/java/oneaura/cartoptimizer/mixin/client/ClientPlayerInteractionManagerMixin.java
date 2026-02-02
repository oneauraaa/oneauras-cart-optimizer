package oneaura.cartoptimizer.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("oneauras-cart-optimizer-cpim");

    @Inject(method = "interactBlockInternal", at = @At("HEAD"), cancellable = true)
    private void invokeInteractBlockInternal(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir) {
        if (player.getEntityWorld().isClient()) {
            ItemStack stack = player.getStackInHand(hand);

            // Handle Minecarts
            if (isMinecart(stack) && oneaura.cartoptimizer.ModConfig.getInstance().isEffectivelyEnabled()) {
                // Check if target is a rail
                if (player.getEntityWorld().getBlockState(hitResult.getBlockPos()).isIn(BlockTags.RAILS)) {
                    // Logic for placement
                    if (!player.isCreative()) {
                        stack.decrement(1);
                        LOGGER.info(
                                "[Cart Optimizer] Enforced minecart removal in CPIM. New count: " + stack.getCount());

                        // Calculate slot ID for locking
                        int slotId;
                        if (hand == Hand.MAIN_HAND) {
                            slotId = 36 + player.getInventory().getSelectedSlot();
                        } else {
                            slotId = 45; // Offhand slot in PlayerScreenHandler
                        }

                        oneaura.cartoptimizer.SlotLockManager.getInstance().lockSlot(slotId, 500); // 500ms lock
                        LOGGER.info("[Cart Optimizer] Locked slot " + slotId + " for 500ms");

                        if (oneaura.cartoptimizer.ModConfig.getInstance().isDebugMode()) {
                            player.sendMessage(net.minecraft.text.Text.of("§a[Cart Optimizer] -1 Cart"), true);
                        }
                    }
                    cir.setReturnValue(ActionResult.SUCCESS);
                }
            }
        }
    }

    private boolean isMinecart(ItemStack stack) {
        return stack.isOf(Items.MINECART) ||
                stack.isOf(Items.CHEST_MINECART) ||
                stack.isOf(Items.FURNACE_MINECART) ||
                stack.isOf(Items.TNT_MINECART) ||
                stack.isOf(Items.HOPPER_MINECART) ||
                stack.isOf(Items.COMMAND_BLOCK_MINECART);
    }
}
