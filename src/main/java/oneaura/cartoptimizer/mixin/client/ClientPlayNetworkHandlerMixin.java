package oneaura.cartoptimizer.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
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

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
        if (serverInfo != null) {
            String serverAddress = serverInfo.address;
            oneaura.cartoptimizer.ServerBlacklist.getInstance().setCurrentServer(serverAddress);
            LOGGER.info("[Cart Optimizer] Joined server: " + serverAddress);

            if (oneaura.cartoptimizer.ServerBlacklist.getInstance().isOnBlacklistedServer()) {
                LOGGER.info("[Cart Optimizer] Server is blacklisted - mod disabled.");

                // Send chat message to player
                net.minecraft.client.network.ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    player.sendMessage(
                            net.minecraft.text.Text.of("§c[Cart Optimizer] Cart Optimizer is disabled on this server."),
                            false);
                }
            }
        } else {
            oneaura.cartoptimizer.ServerBlacklist.getInstance().clearServer();
        }
    }

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

    @Inject(method = "onInventory", at = @At("HEAD"))
    private void onInventory(net.minecraft.network.packet.s2c.play.InventoryS2CPacket packet, CallbackInfo ci) {
        if (!oneaura.cartoptimizer.ModConfig.getInstance().isEffectivelyEnabled())
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
