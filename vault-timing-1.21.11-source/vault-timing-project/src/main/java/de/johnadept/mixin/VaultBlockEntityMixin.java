package de.johnadept.mixin;

import de.johnadept.VaultTiming;
import net.minecraft.block.BlockState;
import net.minecraft.block.vault.VaultBlockEntity;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(VaultBlockEntity.Server.class)
public class VaultBlockEntityMixin {

    @Inject(
            method = "tryUnlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onTryUnlock(
            ServerWorld world,
            BlockPos pos,
            BlockState state,
            VaultConfig config,
            VaultServerData serverData,
            VaultSharedData sharedData,
            PlayerEntity player,
            ItemStack stack,
            Hand hand,
            CallbackInfo ci
    ) {
        System.out.println("Executing tryUnlock");

        try {
            // Check if vault can be unlocked (has a key configured and key item is not empty)
            if (config.keyItem().isEmpty()) {
                return;
            }

            // Check if provided key matches the required key
            if (!isValidKey(config, stack)) {
                VaultBlockEntity.Server.playFailedUnlockSound(world, serverData, pos, config.failedUnlockSound());
                return;
            }

            // Check if player already received a reward from this vault
            if (serverData.hasRewardedPlayer(player)) {
                VaultBlockEntity.Server.playFailedUnlockSound(world, serverData, pos, config.failedUnlockSound());
                return;
            }

            // Check gamerule for non-ominous vaults
            boolean isOminous = state.get(net.minecraft.block.VaultBlock.OMINOUS);
            List<ItemStack> lootItems = new ArrayList<>();

            if (isOminous) {
                // Ominous vault: always apply the fix
                ci.cancel();
                ItemStack displayedItem = sharedData.getDisplayItem().copy();
                lootItems.add(displayedItem);

                if (world.getGameRules().getBoolean(VaultTiming.SHOULD_ALSO_DROP_DEFAULT_LOOT)) {
                    lootItems.addAll(VaultBlockEntity.Server.generateLoot(world, config, pos, player));
                }
            } else {
                // Non-ominous vault: only apply fix if gamerule enabled
                if (!world.getGameRules().getBoolean(VaultTiming.SHOULD_MODIFY_NON_OMINOUS_VAULTS)) {
                    return;
                }
                ci.cancel();
                ItemStack displayedItem = sharedData.getDisplayItem().copy();
                lootItems.add(displayedItem);

                if (world.getGameRules().getBoolean(VaultTiming.SHOULD_ALSO_DROP_DEFAULT_LOOT)) {
                    lootItems.addAll(VaultBlockEntity.Server.generateLoot(world, config, pos, player));
                }
            }

            // Give key damage / consume key
            stack.damage(config.keyItem().getCount(), player, net.minecraft.util.Hand.MAIN_HAND);

            // Eject items and mark player as rewarded
            VaultBlockEntity.Server.unlock(world, state, pos, config, serverData, sharedData, lootItems);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isValidKey(VaultConfig config, ItemStack stack) {
        ItemStack keyItem = config.keyItem();
        return ItemStack.areItemsAndComponentsEqual(stack, keyItem)
                && stack.getCount() >= keyItem.getCount();
    }
}
