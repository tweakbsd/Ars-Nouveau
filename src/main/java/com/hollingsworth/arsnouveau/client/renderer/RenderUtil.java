package com.hollingsworth.arsnouveau.client.renderer;

import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class RenderUtil {

    public static int getIDFromStack(ItemStack stack)
    {
        return 1;
    }

    public static AnimationController getControllerForStack(AnimationFactory factory, ItemStack stack, String controllerName)
    {
        return factory.getOrCreateAnimationData(getIDFromStack(stack)).getAnimationControllers().get(controllerName);
    }
}
