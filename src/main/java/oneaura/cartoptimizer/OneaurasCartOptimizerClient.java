package oneaura.cartoptimizer;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneaurasCartOptimizerClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("oneauras-cart-optimizer-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("oneaura's Cart Optimizer Client initialized");
        CartOptimizerCommand.register();
    }
}
