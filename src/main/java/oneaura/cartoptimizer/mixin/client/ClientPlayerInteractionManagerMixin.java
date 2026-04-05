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
import net.minecraft.client.MinecraftClient;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("oneauras-cart-optimizer-cpim");

    @Inject(method = "interactBlockInternal", at = @At("HEAD"), cancellable = true)
    private void invokeInteractBlockInternal(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir) {
        if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().world.isClient()) {
            ItemStack stack = player.getStackInHand(hand);

            // Handle Minecarts
            if (isMinecart(stack) && oneaura.cartoptimizer.ModConfig.getInstance().isEffectivelyEnabled()) {
                // Check if target is a rail
                if (MinecraftClient.getInstance().world.getBlockState(hitResult.getBlockPos()).isIn(BlockTags.RAILS)) {
                    // Logic for placement
                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);
                        LOGGER.info(
                                "[Cart Optimizer] Enforced minecart removal in CPIM. New count: " + stack.getCount());

                        // Calculate slot ID for locking
                        int slotId;
                        if (hand == Hand.MAIN_HAND) {
                            slotId = 36 + getPlayerSelectedSlot(player.getInventory());
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

    private static java.lang.reflect.Method getSelectedSlotMethod = null;
    private static java.lang.reflect.Field selectedSlotField = null;
    private static boolean inventoryReflectionInit = false;

    private static int getPlayerSelectedSlot(net.minecraft.entity.player.PlayerInventory inventory) {
        if (!inventoryReflectionInit) {
            for (java.lang.reflect.Method m : inventory.getClass().getMethods()) {
                String name = m.getName();
                if (name.equals("getSelectedSlot") || name.equals("method_67532")) {
                    getSelectedSlotMethod = m;
                    break;
                }
            }
            if (getSelectedSlotMethod == null) {
                for (java.lang.reflect.Field f : inventory.getClass().getDeclaredFields()) {
                    String name = f.getName();
                    if (name.equals("selectedSlot") || name.equals("field_7545")) {
                        selectedSlotField = f;
                        selectedSlotField.setAccessible(true);
                        break;
                    }
                }
            }
            inventoryReflectionInit = true;
        }

        try {
            if (getSelectedSlotMethod != null) {
                return (int) getSelectedSlotMethod.invoke(inventory);
            } else if (selectedSlotField != null) {
                return (int) selectedSlotField.get(inventory);
            }
        } catch (Exception e) {
            LOGGER.error("[Cart Optimizer] Error getting selected slot from PlayerInventory", e);
        }
        return 0; // Default offhand/first slot fallback
    }
}
