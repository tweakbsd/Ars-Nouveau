package com.hollingsworth.arsnouveau.client.renderer.item;

import com.hollingsworth.arsnouveau.common.items.SpellBook;

public class SpellBookRenderer extends FixedGeoItemRenderer<SpellBook> {
    public SpellBookRenderer() {
        super(new SpellBookModel());
    }

    @Override
    public Integer getUniqueID(Object animatable) {
        return currentItemStack.hasTag() && currentItemStack.getTag().getBoolean(SpellBook.OPEN_TAG) ? 1 : 0;
    }
}