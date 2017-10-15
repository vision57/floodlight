package net.floodlightcontroller.core.internal;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @since 2017/10/15 上午1:46
 */
public class QKDCipherHandler extends ChannelDuplexHandler {

	private byte[] key = { 42, 81 };

	private int encCursor;

	private int decCursor;

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof ByteBuf) {
			ByteBuf data = ((ByteBuf) msg);
			int dataCount = data.readableBytes();
			ByteBuf encrypted = ctx.alloc().buffer(dataCount);
			for (int i = 0; i < dataCount; ++i) {
				encrypted.writeByte((byte) (data.readByte() ^ this.key[(this.encCursor + i) % this.key.length]));
			}
			this.encCursor += dataCount;
			data.release();
			super.write(ctx, encrypted, promise);
		} else {
			super.write(ctx, msg, promise);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof ByteBuf) {
			ByteBuf data = ((ByteBuf) msg);
			int dataCount = data.readableBytes();
			ByteBuf decrypted = ctx.alloc().buffer(dataCount);
			for (int i = 0; i < dataCount; ++i) {
				decrypted.writeByte((byte) (data.readByte() ^ this.key[(this.decCursor + i) % this.key.length]));
			}
			this.decCursor += dataCount;
			data.release();
			super.channelRead(ctx, decrypted);
		} else {
			super.channelRead(ctx, msg);
		}
	}
}
