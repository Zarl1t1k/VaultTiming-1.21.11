package de.johnadept.mixin;

import de.johnadept.VaultTiming;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
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
            if (!isValidKey(config, stack)) {
                VaultBlockEntity.Server.playFailedUnlockSound(world, serverData, pos,
                        net.minecraft.sound.SoundEvents.BLOCK_VAULT_REJECT_REWARDED_PLAYER);
                return;
            }

            if (serverData.hasRewardedPlayer(player)) {
                VaultBlockEntity.Server.playFailedUnlockSound(world, serverData, pos,
                        net.minecraft.sound.SoundEvents.BLOCK_VAULT_REJECT_REWARDED_PLAYER);
                return;
            }

            boolean isOminous = state.get(net.minecraft.block.VaultBlock.OMINOUS);
            List<ItemStack> lootItems = new ArrayList<>();

            if (isOminous) {
                ci.cancel();
                lootItems.add(sharedData.getDisplayItem().copy());
                if (world.getGameRules().getBoolean(VaultTiming.SHOULD_ALSO_DROP_DEFAULT_LOOT)) {
                    lootItems.addAll(VaultBlockEntity.Server.generateLoot(world, config, pos, player));
                }
            } else {
                if (!world.getGameRules().getBoolean(VaultTiming.SHOULD_MODIFY_NON_OMINOUS_VAULTS)) {
                    return;
                }
                ci.cancel();
                lootItems.add(sharedData.getDisplayItem().copy());
                if (world.getGameRules().getBoolean(VaultTiming.SHOULD_ALSO_DROP_DEFAULT_LOOT)) {
                    lootItems.addAll(VaultBlockEntity.Server.generateLoot(world, config, pos, player));
                }
            }

            stack.decrement(config.keyItem().getCount());
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
