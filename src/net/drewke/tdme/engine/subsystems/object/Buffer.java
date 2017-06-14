package net.drewke.tdme.engine.subsystems.object;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.drewke.tdme.utils.Console;

/**
 * Buffer
 * @author Andreas Drewke
 * @version $Id$
 */
public class Buffer {

	private static ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024 * 1024);

	/**
	 * Get byte buffer
	 * @param bytes
	 * @return byte buffer
	 */
	protected static ByteBuffer getByteBuffer(int bytes) {
		if (byteBuffer == null || bytes > byteBuffer.capacity()) {
			if (byteBuffer != null) {
				Console.println("Buffer::getByteBuffer(): enlarge buffer from " + byteBuffer.capacity() + " to " + bytes + " bytes");
			}
			byteBuffer = ByteBuffer.allocateDirect(bytes);
		} else {
			byteBuffer.clear();
		}
		return byteBuffer.order(ByteOrder.nativeOrder());
	}

}
