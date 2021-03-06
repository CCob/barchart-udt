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
import java.nio.IntBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSocketUDT {

	static final Logger log = LoggerFactory.getLogger(TestSocketUDT.class);

	@Before
	public void init() throws Exception {

		log.info("started {}", System.getProperty("os.arch"));

	}

	@After
	public void done() throws Exception {
	}

	@Test
	public void testSelectEx0() {

		log.info("testSelectEx0");

		try {

			final InetSocketAddress localAddress1 = localSocketAddress();

			final InetSocketAddress localAddress2 = localSocketAddress();

			final SocketUDT socketServer = new SocketUDT(TypeUDT.DATAGRAM);
			socketServer.setOption(OptionUDT.UDT_RCVSYN, false);
			socketServer.setOption(OptionUDT.UDT_SNDSYN, false);
			socketServer.bind(localAddress1);
			socketServer.listen(1);
			// socketServer.accept();

			final SocketUDT socketClient = new SocketUDT(TypeUDT.DATAGRAM);
			socketClient.setOption(OptionUDT.UDT_RCVSYN, false);
			socketClient.setOption(OptionUDT.UDT_SNDSYN, false);
			socketClient.bind(localAddress2);
			socketClient.listen(1);
			// socketClient.accept();

			final long timeout = 1 * 1000 * 1000;

			final SocketUDT[] selectArray = new SocketUDT[] { socketServer,
					socketClient };

			socketServer.clearError();

			final long timeStart = System.currentTimeMillis();

			// SocketUDT.selectExtended(selectArray, timeout);

			final long timeFinish = System.currentTimeMillis();

			final long timeDiff = timeFinish - timeStart;
			log.info("timeDiff={}", timeDiff);

			// log.info("isSelectedRead={}", socketServer.isSelectedRead());
			// log.info("isSelectedWrite={}", socketServer.isSelectedWrite());
			// log.info("isSelectedException={}", socketServer
			// .isSelectedException());

			log.info("getError={}", socketServer.getError());
			log.info("getErrorCode={}", socketServer.getErrorCode());
			log.info("getgetErrorMessage={}", socketServer.getErrorMessage());

			socketServer.close();
			socketClient.close();

		} catch (final Exception e) {
			fail("SocketException; " + e.getMessage());
		}

	}

	@Test(expected = ExceptionUDT.class)
	public void testInvalidClose0() throws ExceptionUDT {

		SocketUDT socket = null;

		try {

			socket = new SocketUDT(TypeUDT.DATAGRAM);

		} catch (final ExceptionUDT e) {

			fail("SocketException; " + e.getMessage());

		}

		final int realID = socket.socketID;

		final int fakeID = realID + 123;

		log.info("real: {} ; fake : {} ; ", realID, fakeID);

		// throws exception
		socket.testInvalidClose0(fakeID);

	}

	@Test
	public void testIsOpen() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);
		assertTrue(socket.isOpen());

		socket.setOption(OptionUDT.Is_Receive_Synchronous, false);
		socket.setOption(OptionUDT.Is_Send_Synchronous, false);
		assertTrue(socket.isOpen());

		final InetSocketAddress localSocketAddress = localSocketAddress();

		socket.bind(localSocketAddress);
		assertTrue(socket.isOpen());

		socket.listen(1);
		assertTrue(socket.isOpen());

		final SocketUDT connector = socket.accept();
		assertNull(connector);
		assertTrue(socket.isOpen());

		socket.close();
		assertFalse(socket.isOpen());

		socket.close();
		assertFalse(socket.isOpen());

		// log.info("sleep 1");
		// Thread.sleep(10 * 1000);

		socket.close();
		assertTrue(socket.isClosed());

		// log.info("sleep 2");
		// Thread.sleep(10 * 1000);

		socket.close();
		assertTrue(socket.isClosed());

		log.info("isOpen pass");

	}

	/** no exceptions is pass */
	@Test
	public void testEpollCreate0() throws Exception {

		final int epollID = SocketUDT.epollCreate0();
		SocketUDT.epollRelease0(epollID);

	}

	@Test(expected = ExceptionUDT.class)
	public void testEpollRelease0() throws Exception {

		SocketUDT.epollRelease0(-1);

	}

	/** no exceptions is pass */
	@Test
	public void testEpollAdd0_Remove() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		//

		final int epollID = SocketUDT.epollCreate0();

		assertTrue(epollID > 0);

		SocketUDT.epollAdd0(epollID, socket.socketID, EpollUDT.Opt.ALL.code);

		SocketUDT.epollRemove0(epollID, socket.socketID);

		SocketUDT.epollRelease0(epollID);

	}

	/**
	 * NO exception
	 * <p>
	 * "If a socket is already in the epoll set, it will be ignored if being
	 * added again. Adding invalid or closed sockets will cause error. However,
	 * they will simply be ignored without any error returned when being
	 * removed."
	 * 
	 * */
	@Test
	public void testEpollAdd0_AgainSocketException() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		//

		final int epollID = SocketUDT.epollCreate0();

		assertTrue(epollID > 0);

		SocketUDT.epollAdd0(epollID, socket.socketID, EpollUDT.Opt.ALL.code);
		SocketUDT.epollAdd0(epollID, socket.socketID, EpollUDT.Opt.ALL.code);
		SocketUDT.epollAdd0(epollID, socket.socketID, EpollUDT.Opt.ALL.code);

		SocketUDT.epollRelease0(epollID);

	}

	/**
	 * YES exception; see http://udt.sourceforge.net/udt4/index.htm
	 * <p>
	 * "If a socket is already in the epoll set, it will be ignored if being
	 * added again. Adding invalid or closed sockets will cause error. However,
	 * they will simply be ignored without any error returned when being
	 * removed."
	 */
	@Test(expected = ExceptionUDT.class)
	public void testEpollAdd0_InvalidSocketException() throws Exception {

		final int epollID = SocketUDT.epollCreate0();

		SocketUDT.epollAdd0(epollID, -1, EpollUDT.Opt.ALL.code);

		SocketUDT.epollRelease0(epollID);

	}

	/**
	 * NO exception; see http://udt.sourceforge.net/udt4/index.htm
	 * <p>
	 * "If a socket is already in the epoll set, it will be ignored if being
	 * added again. Adding invalid or closed sockets will cause error. However,
	 * they will simply be ignored without any error returned when being
	 * removed."
	 */
	@Test
	public void testEpollRemove0_IvalidSocketException() throws Exception {

		final int epollID = SocketUDT.epollCreate0();

		SocketUDT.epollRemove0(epollID, -1);
		SocketUDT.epollRemove0(epollID, -1);
		SocketUDT.epollRemove0(epollID, -1);

		SocketUDT.epollRelease0(epollID);

	}

	/**
	 * NOT TRUE
	 * 
	 * "Finally, for epoll_wait, negative timeout value will make the function
	 * to waituntil an event happens. If the timeout value is 0, then the
	 * function returns immediately with any sockets associated an IO event. If
	 * timeout occurs before any event happens, the function returns 0."
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEpollWait0_ZeroTimeout() throws Exception {

		try {

			final int epollID = SocketUDT.epollCreate0();

			final IntBuffer readBuffer = SocketUDT.newDirectIntBufer(10);
			final IntBuffer writeBuffer = SocketUDT.newDirectIntBufer(10);
			final IntBuffer sizeBuffer = SocketUDT.newDirectIntBufer(10);
			final long millisTimeout = 0;

			SocketUDT.epollWait0(epollID, readBuffer, writeBuffer, sizeBuffer,
					millisTimeout);

			SocketUDT.epollRelease0(epollID);

		} catch (final ExceptionUDT e) {

			if (e.getError() == ErrorUDT.ETIMEOUT) {
				return;
			} else {
				throw e;
			}

		}

	}

	/** FIXME */
	@Ignore
	@Test
	public void testEpollWait0_Accept() throws Exception {

		final int epollID = SocketUDT.epollCreate0();

		final SocketUDT accept = new SocketUDT(TypeUDT.DATAGRAM);
		accept.configureBlocking(false);
		accept.bind0(localSocketAddress());
		accept.listen0(1);

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		client.configureBlocking(false);
		client.bind0(localSocketAddress());

		final IntBuffer readBuffer = SocketUDT.newDirectIntBufer(10);
		final IntBuffer writeBuffer = SocketUDT.newDirectIntBufer(10);
		final IntBuffer sizeBuffer = SocketUDT.newDirectIntBufer(10);

		SocketUDT.epollAdd0(epollID, accept.socketID, EpollUDT.Opt.ALL.code);
		SocketUDT.epollAdd0(epollID, client.socketID, EpollUDT.Opt.ALL.code);

		assertEquals(StatusUDT.LISTENING.getCode(), accept.getStatus0());
		assertEquals(StatusUDT.OPENED.getCode(), client.getStatus0());

		client.connect0(accept.getLocalSocketAddress());

		final int readyCount1 = SocketUDT.epollWait0( //
				epollID, readBuffer, writeBuffer, sizeBuffer, 50);

		log.info("readyCount1 : {}", readyCount1);

		assertEquals(2, readyCount1);
		assertEquals(1, sizeBuffer.get(SocketUDT.UDT_READ_INDEX));
		assertEquals(1, sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX));
		assertEquals(accept.socketID, readBuffer.get(0));
		assertEquals(accept.socketID, writeBuffer.get(0));

		final SocketUDT server = accept.accept0();

		Thread.sleep(50);

		assertEquals(StatusUDT.CONNECTED.getCode(), server.getStatus0());
		assertEquals(StatusUDT.CONNECTED.getCode(), client.getStatus0());

		final int readyCount2 = SocketUDT.epollWait0( //
				epollID, readBuffer, writeBuffer, sizeBuffer, 50);

		log.info("readyCount2 : {}", readyCount2);

		assertEquals(2, readyCount2);
		assertEquals(0, sizeBuffer.get(SocketUDT.UDT_READ_INDEX));
		assertEquals(2, sizeBuffer.get(SocketUDT.UDT_WRITE_INDEX));
		assertEquals(client.socketID, writeBuffer.get(0));
		assertEquals(accept.socketID, writeBuffer.get(1));

		SocketUDT.epollRelease0(epollID);

	}

	@Test(expected = ExceptionUDT.class)
	public void testAcceptNoListen() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		socket.accept();

	}

	@Test
	public void testAcceptListenNone() throws Exception {

		final SocketUDT socket = new SocketUDT(TypeUDT.DATAGRAM);

		socket.configureBlocking(false);

		socket.bind(localSocketAddress());

		socket.listen(1);

		assertNull(socket.accept());

	}

	@Test
	public void testAcceptListenOne() throws Exception {

		final SocketUDT accept = new SocketUDT(TypeUDT.DATAGRAM);
		accept.configureBlocking(false);
		accept.bind(localSocketAddress());

		assertEquals(StatusUDT.OPENED, accept.getStatus());

		accept.listen(1);

		assertEquals(StatusUDT.LISTENING, accept.getStatus());

		assertNull(accept.accept());

		final SocketUDT client = new SocketUDT(TypeUDT.DATAGRAM);
		client.configureBlocking(false);
		client.bind(localSocketAddress());

		client.connect(accept.getLocalSocketAddress());

		Thread.sleep(100);

		assertNotNull(accept.accept());

		Thread.sleep(100);

		assertNull(accept.accept());

	}

}
