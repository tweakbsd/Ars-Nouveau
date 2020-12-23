package com.hollingsworth.arsnouveau.common.network;

import com.hollingsworth.arsnouveau.common.block.tile.IAnimationListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketAnimateBook {

    final int entityID;
    final int arg;
    final int itemSlot;

    public PacketAnimateBook(int entityID, int arg, int itemSlot){
        this.entityID = entityID;
        this.arg = arg;
        this.itemSlot = itemSlot;
    }


    public static PacketAnimateBook decode(PacketBuffer buf) {
        return new PacketAnimateBook(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void encode(PacketAnimateBook msg, PacketBuffer buf) {
        buf.writeInt(msg.entityID);
        buf.writeInt(msg.arg);
        buf.writeInt(msg.itemSlot);
    }

    public static class Handler {
        public static void handle(final PacketAnimateBook m, final Supplier<NetworkEvent.Context> ctx) {
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                ctx.get().setPacketHandled(true);
                return;
            }

            ctx.get().enqueueWork(new Runnable() {
                // Use anon - lambda causes classloading issues
                @Override
                public void run() {
                    Minecraft mc = Minecraft.getInstance();
                    ClientWorld world = mc.world;
                    if(world != null && world.getEntityByID(m.entityID) instanceof PlayerEntity){
                        ItemStack stack =((PlayerEntity) world.getEntityByID(m.entityID)).inventory.getStackInSlot(m.itemSlot);
                        System.out.println(stack);
                        if(stack.getItem() instanceof IAnimationListener){
                            ((IAnimationListener) stack.getItem()).startAnimation(m.arg);
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);

        }
    }
}
