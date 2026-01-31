package oneaura.cartoptimizer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.text.Text;

public class CartOptimizerCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("cartoptimizer")
                    .then(ClientCommandManager.literal("debug")
                            .then(ClientCommandManager.literal("on")
                                    .executes(context -> {
                                        ModConfig.getInstance().setDebugMode(true);
                                        context.getSource()
                                                .sendFeedback(Text.of("§a[Cart Optimizer] Debug mode enabled"));
                                        return 1;
                                    }))
                            .then(ClientCommandManager.literal("off")
                                    .executes(context -> {
                                        ModConfig.getInstance().setDebugMode(false);
                                        context.getSource()
                                                .sendFeedback(Text.of("§c[Cart Optimizer] Debug mode disabled"));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("on")
                            .executes(context -> {
                                ModConfig.getInstance().setEnabled(true);
                                context.getSource().sendFeedback(Text.of("§a[Cart Optimizer] Enabled"));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("off")
                            .executes(context -> {
                                ModConfig.getInstance().setEnabled(false);
                                context.getSource().sendFeedback(Text.of("§c[Cart Optimizer] Disabled"));
                                return 1;
                            })));
        });
    }
}
