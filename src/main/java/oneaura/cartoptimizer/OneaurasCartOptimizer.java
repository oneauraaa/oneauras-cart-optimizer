package oneaura.cartoptimizer;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneaurasCartOptimizer implements ModInitializer {
	public static final String MOD_ID = "oneauras-cart-optimizer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("oneaura's Cart Optimizer initialized");
	}
}