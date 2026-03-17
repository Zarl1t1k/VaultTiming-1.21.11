package de.johnadept;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
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

        SHOULD_MODIFY_NON_OMINOUS_VAULTS = GameRuleRegistry.register(
                "modifyNonOminousVaults",
                GameRules.Category.MISC,
                GameRuleFactory.createBooleanRule(true)
        );

        SHOULD_ALSO_DROP_DEFAULT_LOOT = GameRuleRegistry.register(
                "vaultsShouldAlsoDropDefaultLoot",
                GameRules.Category.MISC,
                GameRuleFactory.createBooleanRule(false)
        );
    }
}
