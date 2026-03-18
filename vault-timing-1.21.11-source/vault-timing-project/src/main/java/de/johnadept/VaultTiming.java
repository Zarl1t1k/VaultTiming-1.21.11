package de.johnadept;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaultTiming implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("vault-timing");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Vault Timing");
    }
}
