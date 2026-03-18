package de.johnadept;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaultTiming implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("vault-timing");

    public static GameRules.Key<GameRules.BooleanRule> SHOULD_MODIFY_NON_OMINOUS_VAULTS;
    public static GameRules.Key<GameRules.BooleanRule> SHOULD_ALSO_DROP_DEFAULT_LOOT;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Vault Timing");

        SHOULD_MODIFY_NON_OMINOUS_VAULTS = GameRules.register(
                "modifyNonOminousVaults",
                GameRules.Category.MISC,
                GameRules.BooleanRule.create(true)
        );

        SHOULD_ALSO_DROP_DEFAULT_LOOT = GameRules.register(
                "vaultsShouldAlsoDropDefaultLoot",
                GameRules.Category.MISC,
                GameRules.BooleanRule.create(false)
        );
    }
}
