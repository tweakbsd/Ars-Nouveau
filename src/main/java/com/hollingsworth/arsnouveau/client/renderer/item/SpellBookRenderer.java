package com.hollingsworth.arsnouveau.client.renderer.item;

import com.hollingsworth.arsnouveau.common.items.SpellBook;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public class SpellBookRenderer extends FixedGeoItemRenderer<SpellBook> {
    public SpellBookRenderer() {
        super(new SpellBookModel());
    }

    @Override
    public void render(GeoModel model, Object animatable, float partialTicks,
                       RenderType type, MatrixStack matrixStackIn, IRenderTypeBuffer renderTypeBuffer, IVertexBuilder vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        SpellBookModel bookModel = (SpellBookModel)getGeoModelProvider();
        bookModel.isOpen = currentItemStack.hasTag() && currentItemStack.getTag().getInt(SpellBook.OPEN_TICKS) > 20;
//        if(currentItemStack.hasTag())
//            System.out.println(currentItemStack.getTag().getInt(SpellBook.OPEN_TICKS));
        super.render(model, animatable, partialTicks,type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);

    }


        @Override
    public Integer getUniqueID(Object animatable) {
        return currentItemStack.hasTag() && currentItemStack.getTag().getBoolean(SpellBook.OPEN_TAG) ? 1 : 0;
    }
}