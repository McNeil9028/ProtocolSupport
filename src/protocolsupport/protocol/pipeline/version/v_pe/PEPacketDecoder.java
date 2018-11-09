package protocolsupport.protocol.pipeline.version.v_pe;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import protocolsupport.api.utils.NetworkState;
import protocolsupport.protocol.ConnectionImpl;
import protocolsupport.protocol.packet.middle.ServerBoundMiddlePacket;
import protocolsupport.protocol.packet.middleimpl.ServerBoundPacketData;
import protocolsupport.protocol.packet.middleimpl.serverbound.handshake.v_pe.ClientLogin;
import protocolsupport.protocol.packet.middleimpl.serverbound.handshake.v_pe.Ping;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_8_9r1_9r2_10_11_12r1_12r2.CustomPayload;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_pe.*;
import protocolsupport.protocol.pipeline.version.util.decoder.AbstractPacketDecoder;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.typeremapper.packet.PEDimensionSwitchMovementConfirmationPacketQueue;
import protocolsupport.protocol.typeremapper.pe.PEPacketIDs;
import protocolsupport.utils.recyclable.RecyclableCollection;
import protocolsupport.utils.recyclable.RecyclableEmptyList;
import protocolsupport.zplatform.impl.pe.PEProxyServerInfoHandler;

public class PEPacketDecoder extends AbstractPacketDecoder {

	{
		for (int i = 0; i < 255; i++) {
			registry.register(NetworkState.PLAY, i, Noop::new);
		}
		registry.register(NetworkState.HANDSHAKING, PEProxyServerInfoHandler.PACKET_ID, Ping::new);
		registry.register(NetworkState.HANDSHAKING, PEPacketIDs.LOGIN, ClientLogin::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.CLIENT_SETTINGS, ClientSettings::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.PLAYER_MOVE, PositionLook::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.PLAYER_ACTION, PlayerAction::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.PLAYER_STEER, SteerVehicle::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.ENTITY_TELEPORT, MoveVehicle::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.CHAT, Chat::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.ANIMATION, Animation::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.INTERACT, Interact::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.COMMAND_REQUEST, CommandRequest::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.TILE_DATA_UPDATE, BlockTileUpdate::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.MOB_EQUIPMENT, HeldSlot::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.ENTITY_EVENT, EntityStatus::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.MAP_INFO_REQUEST, MapInfoRequest::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.RIDER_JUMP, RiderJump::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.GOD_PACKET, GodPacket::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.EDIT_BOOK, BookEdit::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.ADVENTURE_SETTINGS, PlayerAbilities::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.CONTAINER_CLOSE, InventoryClose::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.CUSTOM_EVENT, CustomPayload::new);
		registry.register(NetworkState.PLAY, PEPacketIDs.SET_LOCAL_PLAYER_INITIALISED, LocalPlayerInitialised::new);
	}

	protected final PEDimensionSwitchMovementConfirmationPacketQueue dimswitchq;
	public PEPacketDecoder(ConnectionImpl connection, PEDimensionSwitchMovementConfirmationPacketQueue dimswitchq) {
		super(connection);
		this.dimswitchq = dimswitchq;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> list) throws Exception {
		if (!input.isReadable()) {
			return;
		}
		try {
			decodeAndTransform(ctx, input, list);
			if (input.isReadable()) {
				throw new DecoderException("Did not read all data from packet, bytes left: " + input.readableBytes());
			}
		} catch (Exception e) {
			throwFailedTransformException(e, input);
		}
	}

	@Override
	protected RecyclableCollection<ServerBoundPacketData> processPackets(Channel channel, RecyclableCollection<ServerBoundPacketData> data) {
		return dimswitchq.processServerBoundPackets(data);
	}

	@Override
	public int readPacketId(ByteBuf from) {
		return sReadPacketId(from);
	}

	public static int sReadPacketId(ByteBuf from) {
		int id = VarNumberSerializer.readVarInt(from);
		return id;
	}

	public class Noop extends ServerBoundMiddlePacket {

		public Noop(ConnectionImpl connection) {
			super(connection);
		}

		@Override
		public void readFromClientData(ByteBuf clientdata) {
			clientdata.skipBytes(clientdata.readableBytes());
		}

		@Override
		public RecyclableCollection<ServerBoundPacketData> toNative() {
			return RecyclableEmptyList.get();
		}

	}

}
