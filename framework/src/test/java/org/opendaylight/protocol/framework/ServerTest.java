/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Test;

public class ServerTest {
	public static final int PORT = 18080;

	SimpleDispatcher clientDispatcher, dispatcher;

	final SimpleSessionListener pce = new SimpleSessionListener();

	SimpleSession session = null;

	ChannelFuture server = null;

	public final InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.5", PORT);

	@Test
	public void testConnectionEstablished() throws Exception {
		final Promise<Boolean> p = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);

		this.dispatcher = new SimpleDispatcher(new SessionNegotiatorFactory<SimpleMessage, SimpleSession, SimpleSessionListener>() {

			@Override
			public SessionNegotiator<SimpleSession> getSessionNegotiator(final SessionListenerFactory<SimpleSessionListener> factory,
					final Channel channel, final Promise<SimpleSession> promise) {
				p.setSuccess(true);
				return new SimpleSessionNegotiator(promise, channel);
			}
		}, new ProtocolHandlerFactory<>(new MessageFactory()), new DefaultPromise<SimpleSession>(GlobalEventExecutor.INSTANCE));

		this.server = this.dispatcher.createServer(this.serverAddress, new SessionListenerFactory<SimpleSessionListener>() {
			@Override
			public SimpleSessionListener getSessionListener() {
				return new SimpleSessionListener();
			}
		});

		this.server.get();

		this.clientDispatcher = new SimpleDispatcher(new SessionNegotiatorFactory<SimpleMessage, SimpleSession, SimpleSessionListener>() {
			@Override
			public SessionNegotiator<SimpleSession> getSessionNegotiator(final SessionListenerFactory<SimpleSessionListener> factory,
					final Channel channel, final Promise<SimpleSession> promise) {
				return new SimpleSessionNegotiator(promise, channel);
			}
		}, new ProtocolHandlerFactory<>(new MessageFactory()), new DefaultPromise<SimpleSession>(GlobalEventExecutor.INSTANCE));

		this.session = (SimpleSession) this.clientDispatcher.createClient(this.serverAddress,
				new NeverReconnectStrategy(GlobalEventExecutor.INSTANCE, 5000), new SessionListenerFactory<SimpleSessionListener>() {
			@Override
			public SimpleSessionListener getSessionListener() {
				return new SimpleSessionListener();
			}
		}).get(6, TimeUnit.SECONDS);

		assertEquals(true, p.get(3, TimeUnit.SECONDS));
	}

	@Test
	public void testConnectionFailed() throws IOException, InterruptedException, ExecutionException, TimeoutException {
		final Promise<Boolean> p = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);

		this.dispatcher = new SimpleDispatcher(new SessionNegotiatorFactory<SimpleMessage, SimpleSession, SimpleSessionListener>() {

			@Override
			public SessionNegotiator<SimpleSession> getSessionNegotiator(final SessionListenerFactory<SimpleSessionListener> factory,
					final Channel channel, final Promise<SimpleSession> promise) {
				p.setSuccess(true);
				return new SimpleSessionNegotiator(promise, channel);
			}
		}, new ProtocolHandlerFactory<>(new MessageFactory()), new DefaultPromise<SimpleSession>(GlobalEventExecutor.INSTANCE));

		this.server = this.dispatcher.createServer(this.serverAddress, new SessionListenerFactory<SimpleSessionListener>() {
			@Override
			public SimpleSessionListener getSessionListener() {
				return new SimpleSessionListener();
			}
		});

		this.server.get();

		this.clientDispatcher = new SimpleDispatcher(new SessionNegotiatorFactory<SimpleMessage, SimpleSession, SimpleSessionListener>() {
			@Override
			public SessionNegotiator<SimpleSession> getSessionNegotiator(final SessionListenerFactory<SimpleSessionListener> factory,
					final Channel channel, final Promise<SimpleSession> promise) {
				return new SimpleSessionNegotiator(promise, channel);
			}
		}, new ProtocolHandlerFactory<>(new MessageFactory()), new DefaultPromise<SimpleSession>(GlobalEventExecutor.INSTANCE));

		this.session = (SimpleSession) this.clientDispatcher.createClient(this.serverAddress,
				new NeverReconnectStrategy(GlobalEventExecutor.INSTANCE, 5000), new SessionListenerFactory<SimpleSessionListener>() {
			@Override
			public SimpleSessionListener getSessionListener() {
				return new SimpleSessionListener();
			}
		}).get(6, TimeUnit.SECONDS);

		final Future<?> session = this.clientDispatcher.createClient(this.serverAddress,
				new NeverReconnectStrategy(GlobalEventExecutor.INSTANCE, 5000), new SessionListenerFactory<SimpleSessionListener>() {
			@Override
			public SimpleSessionListener getSessionListener() {
				return new SimpleSessionListener();
			}
		});
		assertFalse(session.isSuccess());
	}

	@After
	public void tearDown() throws IOException {
		this.server.channel().close();
		this.dispatcher.close();
		this.clientDispatcher.close();
		try {
			Thread.sleep(100);
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
