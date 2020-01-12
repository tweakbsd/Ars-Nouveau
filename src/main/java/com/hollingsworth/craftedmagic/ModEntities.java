package com.hollingsworth.craftedmagic;

import com.hollingsworth.craftedmagic.entity.EntityProjectileSpell;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(ExampleMod.MODID)
public class ModEntities {
    public static final EntityType<EntityProjectileSpell> PROJECTILE_SPELL_ENTITY = null;

//    public static void init() {
//        // Every entity in our mod has an ID (local to this mod)
//        int id = 1;
//         EntityRegistry.registerModEntity(new ResourceLocation(""), EntityProjectileSpell.class, "ProjectileSpell", id++, ExampleMod.instance, 64, 3, true);
////        EntityRegistry.registerModEntity(EntityWeirdZombie.class, "WeirdZombie", id++, ModTut.instance, 64, 3, true, 0x996600, 0x00ff00);
////
////        // We want our mob to spawn in Plains and ice plains biomes. If you don't add this then it will not spawn automatically
////        // but you can of course still make it spawn manually
////        EntityRegistry.addSpawn(EntityWeirdZombie.class, 100, 3, 5, EnumCreatureType.MONSTER, Biomes.PLAINS, Biomes.ICE_PLAINS);
////
////        // This is the loot table for our mob
////        LootTableList.register(EntityWeirdZombie.LOOT);
//    }
    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus= Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {
    public static final int lightballID = 29;

        /**
         * Register this mod's {@link Entity} types.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
            System.out.println("Registered entitites");
//            final EntityEntry[] entries = {
//                    createBuilder("mod_projectile_spell")
//                            .entity(EntityProjectileSpell.class)
//                            .tracker(64, 20, false)
//                            .build(),
//
//
//            };
            final EntityType<EntityProjectileSpell> spell_proj = build(
                    "spell_proj",
                    EntityType.Builder.<EntityProjectileSpell>create((EntityProjectileSpell::new), EntityClassification.MISC)
                            .size(0.5f, 0.5f));
            event.getRegistry().registerAll(
                    spell_proj
            );

//            EntityRegistry.registerModEntity(new ResourceLocation(ExampleMod.MODID, "dmlightball"),
//                    EntityProjectileSpell.class, ExampleMod.MODID + ".dmlightball", lightballID, ExampleMod.instance,
//                    80, 20, true);

            //event.getRegistry().registerAll(entries);

        }
    }
    /**
     * Build an {@link EntityType} from a {@link EntityType.Builder} using the specified name.
     *
     * @param name    The entity type name
     * @param builder The entity type builder to build
     * @return The built entity type
     */
    private static <T extends Entity> EntityType<T> build(final String name, final EntityType.Builder<T> builder) {
        final ResourceLocation registryName = new ResourceLocation(ExampleMod.MODID, name);

        final EntityType<T> entityType = builder
                .build(registryName.toString());

        entityType.setRegistryName(registryName);

        return entityType;
    }


    public static void initModels() {
        //RenderingRegistry.registerEntityRenderingHandler(EntityWeirdZombie.class, RenderWeirdZombie.FACTORY);
    }
}