package com.hollingsworth.arsnouveau.common.potions;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.api.recipe.PotionIngredient;
import com.hollingsworth.arsnouveau.setup.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.ItemsRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.*;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ArsNouveau.MODID)
public class ModPotions {

    public static final ShieldEffect SHIELD_POTION = new ShieldEffect();
    public static final ShockedEffect SHOCKED_EFFECT = new ShockedEffect();
    public static final ManaRegenEffect MANA_REGEN_EFFECT = new ManaRegenEffect();
    public static final SummoningSicknessEffect SUMMONING_SICKNESS = new SummoningSicknessEffect();
    public static final HexEffect HEX_EFFECT = new HexEffect();
    public static final ScryingEffect SCRYING_EFFECT = new ScryingEffect();
    public static final GlideEffect GLIDE_EFFECT = new GlideEffect();
    public static final SnareEffect SNARE_EFFECT = new SnareEffect();

    @ObjectHolder("mana_regen_potion")
    public static Potion MANA_REGEN_POTION;
    @ObjectHolder("mana_regen_potion_long")
    public static Potion LONG_MANA_REGEN_POTION;

    @ObjectHolder("mana_regen_potion_strong")
    public static Potion STRONG_MANA_REGEN_POTION;
    public static void addRecipes() {
        ItemStack AWKWARD = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD);
        ItemStack manaPot = PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.MANA_REGEN_POTION);
        ItemStack manaPotLong = PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.LONG_MANA_REGEN_POTION);
        ItemStack manaPotStrong = PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.STRONG_MANA_REGEN_POTION);

        ItemStack water = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        BrewingRecipeRegistry.addRecipe(new BrewingRecipe(new PotionIngredient(AWKWARD), Ingredient.of(BlockRegistry.MANA_BERRY_BUSH),  manaPot));
        BrewingRecipeRegistry.addRecipe(new BrewingRecipe(Ingredient.of(manaPot), Ingredient.of(Items.GLOWSTONE_DUST),  manaPotStrong));
        BrewingRecipeRegistry.addRecipe(new BrewingRecipe(Ingredient.of(manaPot), Ingredient.of(Items.REDSTONE),  manaPotLong));

        BrewingRecipeRegistry.addRecipe(new BrewingRecipe(Ingredient.of(water), Ingredient.of(ItemsRegistry.WILDEN_WING), PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.LEAPING)));
        BrewingRecipeRegistry.addRecipe(new BrewingRecipe(Ingredient.of(water), Ingredient.of(ItemsRegistry.WILDEN_HORN), PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRENGTH)));
        BrewingRecipeRegistry.addRecipe(new BrewingRecipe(Ingredient.of(water), Ingredient.of(ItemsRegistry.WILDEN_SPIKE), PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.LONG_WATER_BREATHING)));
    }

    @Mod.EventBusSubscriber(modid = ArsNouveau.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerEffects(final RegistryEvent.Register<Effect> event) {
            final IForgeRegistry<Effect> registry = event.getRegistry();
            registry.registerAll(SCRYING_EFFECT,
                    SHIELD_POTION,
                    MANA_REGEN_EFFECT,
                    SUMMONING_SICKNESS,
                    SHOCKED_EFFECT,
                    HEX_EFFECT,
                    GLIDE_EFFECT,
                    SNARE_EFFECT);
        }

        @SubscribeEvent
        public static void registerPotions(final RegistryEvent.Register<Potion> event) {
            final IForgeRegistry<Potion> registry = event.getRegistry();

            registry.register(new Potion(new EffectInstance(MANA_REGEN_EFFECT, 3600)).setRegistryName("mana_regen_potion"));
            registry.register(new Potion(new EffectInstance(MANA_REGEN_EFFECT, 9600)).setRegistryName("mana_regen_potion_long"));
            registry.register(new Potion(new EffectInstance(MANA_REGEN_EFFECT, 3600, 1)).setRegistryName("mana_regen_potion_strong"));
        }
    }
}