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
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(VaultBlockEntity.Server.class)
public abstract class VaultBlockEntityMixin {

    @Invoker("playFailedUnlockSound")
    private static native void invokePlayFailedUnlockSound(ServerWorld world, VaultServerData serverData, BlockPos pos, SoundEvent sound);

    @Invoker("unlock")
    private static native void invokeUnlock(ServerWorld world, BlockState state, BlockPos pos, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData, List<ItemStack> items);

    @Invoker("generateLoot")
    private static native List<ItemStack> invokeGenerateLoot(ServerWorld world, VaultConfig config, BlockPos pos, PlayerEntity player, ItemStack stack);

    @Inject(method = "tryUnlock", at = @At("HEAD"), cancellable = true)
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
                invokePlayFailedUnlockSound(world, serverData, pos, SoundEvents.BLOCK_VAULT_REJECT_REWARDED_PLAYER);
                return;
            }

            if (!serverData.hasRewardedPlayer(player)) {
                boolean isOminous = state.get(net.minecraft.block.VaultBlock.OMINOUS);

                if (!isOminous && !world.getGameRules().getBoolean(VaultTiming.SHOULD_MODIFY_NON_OMINOUS_VAULTS)) {
                    return;
                }

                ci.cancel();
                List<ItemStack> lootItems = new ArrayList<>();
                lootItems.add(sharedData.getDisplayItem().copy());

                if (world.getGameRules().getBoolean(VaultTiming.SHOULD_ALSO_DROP_DEFAULT_LOOT)) {
                    lootItems.addAll(invokeGenerateLoot(world, config, pos, player, stack));
                }

                stack.decrement(config.keyItem().getCount());
                invokeUnlock(world, state, pos, config, serverData, sharedData, lootItems);
            } else {
                invokePlayFailedUnlockSound(world, serverData, pos, SoundEvents.BLOCK_VAULT_REJECT_REWARDED_PLAYER);
            }

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
