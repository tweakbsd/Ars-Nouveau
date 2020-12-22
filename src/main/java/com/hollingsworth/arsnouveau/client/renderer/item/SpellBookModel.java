package com.hollingsworth.arsnouveau.client.renderer.item;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.common.items.SpellBook;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SpellBookModel extends AnimatedGeoModel<SpellBook> {

    ResourceLocation T1 =  new ResourceLocation(ArsNouveau.MODID , "geo/spellbook_tier1.geo.json");
    ResourceLocation T2 =  new ResourceLocation(ArsNouveau.MODID , "geo/spellbook_tier2.geo.json");
    ResourceLocation T3 =  new ResourceLocation(ArsNouveau.MODID , "geo/spellbook_tier3.geo.json");

    @Override
    public ResourceLocation getModelLocation(SpellBook book) {
        return T3;
    }

    @Override
    public ResourceLocation getTextureLocation(SpellBook book) {
        return new ResourceLocation(ArsNouveau.MODID, "textures/items/spellbook.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SpellBook book) {
        return new ResourceLocation(ArsNouveau.MODID , "animations/spellbook_animations.json");
    }
}
