package com.elytradev.movingworld.common.experiments.network.messages.server;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;
import com.elytradev.movingworld.common.experiments.MovingWorldExperimentsMod;
import com.elytradev.movingworld.common.experiments.network.MovingWorldExperimentsNetworking;
import com.elytradev.movingworld.common.experiments.network.marshallers.BlockStateMarshaller;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;

@ReceivedOn(Side.CLIENT)
public class MessageBlockChange extends Message {

    @MarshalledAs("varint")
    public int dimension;
    public BlockPos pos;
    @MarshalledAs(BlockStateMarshaller.MARSHALLER_NAME)
    public IBlockState blockState;

    public MessageBlockChange(NetworkContext ctx) {
        super(ctx);
    }

    public MessageBlockChange(World world, BlockPos pos) {
        super(MovingWorldExperimentsNetworking.networkContext);
        WorldServer wServer = (WorldServer) MovingWorldExperimentsMod.modProxy.getCommonDB().getWorldFromDim(world.provider.getDimension());
        this.pos = pos;
        this.dimension = wServer.provider.getDimension();

        this.blockState = wServer.getBlockState(pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void handle(EntityPlayer sender) {
        WorldClient worldClient = (WorldClient) MovingWorldExperimentsMod.modProxy.getClientDB().getWorldFromDim(dimension);
        worldClient.invalidateRegionAndSetBlock(pos, blockState);
        worldClient.setBlockState(pos, blockState, 2);
    }
}
