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
        if (!oneaura.cartoptimizer.ModConfig.getInstance().isEnabled())
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

    @Inject(method = "onInventory", at = @At("HEAD"))
    private void onInventory(net.minecraft.network.packet.s2c.play.InventoryS2CPacket packet, CallbackInfo ci) {
        if (!oneaura.cartoptimizer.ModConfig.getInstance().isEnabled())
            return;

        if (packet.syncId() == 0) {
            java.util.List<net.minecraft.item.ItemStack> stacks = packet.contents();
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
    }
}
