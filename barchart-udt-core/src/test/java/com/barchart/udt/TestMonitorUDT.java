/**
 * Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.udt;

import static com.barchart.udt.util.TestHelp.*;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMonitorUDT {

	private static final Logger log = LoggerFactory
			.getLogger(TestMonitorUDT.class);

	@Before
	public void setUp() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMonitor() {

		try {

			SocketUDT serverSocket = new SocketUDT(TypeUDT.DATAGRAM);
			InetSocketAddress serverAddress = localSocketAddress();
			serverSocket.bind(serverAddress);
			serverSocket.listen(1);

			SocketUDT clientSocket = new SocketUDT(TypeUDT.DATAGRAM);
			InetSocketAddress clientAddress = localSocketAddress();
			clientSocket.bind(clientAddress);

			clientSocket.connect(serverAddress);

			SocketUDT acceptSocket = serverSocket.accept();

			log.info("client montitor={}", clientSocket.toStringMonitor());
			log.info("accept montitor={}", acceptSocket.toStringMonitor());

		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

}
