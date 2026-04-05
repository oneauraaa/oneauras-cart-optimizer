package oneaura.cartoptimizer.mixin.client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("oneauras-cart-optimizer-cpnh");

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("HEAD"), cancellable = true)
    private void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        if (!oneaura.cartoptimizer.ModConfig.getInstance().isEffectivelyEnabled())
            return;

        // SyncId 0 is the player's inventory/container
        if (packet.getSyncId() == 0) {
            int slot = packet.getSlot();
            if (oneaura.cartoptimizer.SlotLockManager.getInstance().isLocked(slot)) {
                LOGGER.info("[Cart Optimizer] Blocked single slot server update for locked slot " + slot);
                ci.cancel();
            }
        }
    }

    private static java.lang.reflect.Method syncIdMethod;
    private static java.lang.reflect.Method contentsMethod;
    private static boolean reflectionInitialized = false;

    private static void initReflection(Class<?> packetClass) {
        if (reflectionInitialized)
            return;

        for (java.lang.reflect.Method m : packetClass.getMethods()) {
            String name = m.getName();
            if (name.equals("comp_3837") || name.equals("syncId")) {
                syncIdMethod = m;
            } else if (name.equals("comp_3838") || name.equals("contents")) {
                contentsMethod = m;
            } else if (name.equals("method_11441") || name.equals("getSyncId")) {
                if (syncIdMethod == null)
                    syncIdMethod = m;
            } else if (name.equals("method_11440") || name.equals("getContents")) {
                if (contentsMethod == null)
                    contentsMethod = m;
            }
        }

        if (syncIdMethod == null || contentsMethod == null) {
            LOGGER.error(
                    "[Cart Optimizer] Failed to find methods for InventoryS2CPacket! Version compatibility might be broken. packetClass: {}",
                    packetClass.getName());
        }

        reflectionInitialized = true;
    }

    @Inject(method = "onInventory", at = @At("HEAD"))
    private void onInventory(net.minecraft.network.packet.s2c.play.InventoryS2CPacket packet, CallbackInfo ci) {
        if (!oneaura.cartoptimizer.ModConfig.getInstance().isEffectivelyEnabled())
            return;

        try {
            initReflection(packet.getClass());
            if (syncIdMethod == null || contentsMethod == null)
                return;

            int syncId = (int) syncIdMethod.invoke(packet);
            if (syncId == 0) {
                @SuppressWarnings("unchecked")
                java.util.List<net.minecraft.item.ItemStack> stacks = (java.util.List<net.minecraft.item.ItemStack>) contentsMethod
                        .invoke(packet);
                for (int i = 0; i < stacks.size(); i++) {
                    if (oneaura.cartoptimizer.SlotLockManager.getInstance().isLocked(i)) {
                        net.minecraft.item.ItemStack stack = stacks.get(i);
                        if (!stack.isEmpty()) {
                            stack.setCount(0);
                            LOGGER.info("[Cart Optimizer] Blocked inventory mass update for locked slot " + i
                                    + " (Forced count to 0)");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[Cart Optimizer] Error accessing InventoryS2CPacket fields", e);
        }
    }
}
