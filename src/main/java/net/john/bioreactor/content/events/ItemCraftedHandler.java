package net.john.bioreactor.content.events;

import net.john.bioreactor.content.item.BioreactorItems;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "bioreactor", bus = Mod.EventBusSubscriber.Bus.FORGE )
public class ItemCraftedHandler {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.getCrafting();                     // ItemStack résultant du craft :contentReference[oaicite:0]{index=0}
        // on teste glass_bottle ET bioreactor:syringe_empty
        if (
                crafted.getItem() == Items.GLASS_BOTTLE ||
                crafted.getItem() == BioreactorItems.SYRINGE_EMPTY.get())
        {
            crafted.getOrCreateTag().putBoolean("oxic", true);
        }
    }
}
