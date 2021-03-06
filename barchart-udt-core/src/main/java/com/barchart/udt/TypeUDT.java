/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

/**
 * UDT socket mode type.
 * 
 * NOTE: {@link #TypeUDT} means stream vs datagram;
 * {@link com.barchart.udt.nio.KindUDT} means server vs client.
 * <p>
 * maps to socket.h constants<br>
 * SOCK_STREAM = 1<br>
 * SOCK_DGRAM = 2<br>
 */
public enum TypeUDT {

	/** The STREAM type. Defines stream - oriented UDT mode. */
	STREAM(1), //

	/** The DATAGRAM. Defines datagram/message - oriented UDT mode. */
	DATAGRAM(2), //

	;

	/** The code */
	public final int code;

	/**
	 * Instantiates a new type udt.
	 * 
	 * @param code
	 *            the code
	 */
	private TypeUDT(final int code) {
		this.code = code;
	}

}
