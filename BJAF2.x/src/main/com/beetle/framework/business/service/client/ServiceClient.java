package com.beetle.framework.business.service.client;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.beetle.framework.AppProperties;
import com.beetle.framework.business.service.common.AsyncMethodCallback;
import com.beetle.framework.business.service.common.RpcConst;
import com.beetle.framework.business.service.common.RpcRequest;
import com.beetle.framework.business.service.common.RpcResponse;
import com.beetle.framework.business.service.common.codec.CodecFactory;
import com.beetle.framework.log.AppLogger;
import com.beetle.framework.util.ClassUtil;
import com.beetle.framework.util.queue.BlockQueue;
import com.beetle.framework.util.thread.task.NamedThreadFactory;

public final class ServiceClient {
	private final String host;
	private final int port;
	private final int connAmout;
	private final ClientBootstrap bootstrap;
	private final BlockQueue channelQueue;
	private final static Map<String, ServiceClient> clients = new HashMap<String, ServiceClient>();
	private static final AppLogger logger = AppLogger
			.getInstance(ServiceClient.class);
	// https://issues.jboss.org/browse/NETTY-424
	private static final ChannelFactory channelFactory = new NioClientSocketChannelFactory(
			Executors.newCachedThreadPool(new NamedThreadFactory(
					"ServiceClient-bossExecutor-", true)),
			Executors.newCachedThreadPool(new NamedThreadFactory(
					"ServiceClient-workerExecutor-", true)));

	private static class RpcClientPipelineFactory implements
			ChannelPipelineFactory {

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline p = Channels.pipeline();
			p.addLast("objectDecoder", CodecFactory.createDecoder());
			p.addLast("objectEncoder", CodecFactory.createEncoder());
			p.addLast("rpcHander", new RpcClientHandler());
			return p;
		}
	}

	public static ServiceClient getInstance(String host, int port) {
		String key = host + port;
		ServiceClient client = clients.get(key);
		if (client == null) {
			synchronized (clients) {
				if (!clients.containsKey(key)) {
					client = new ServiceClient(host, port);
					clients.put(key, client);
				} else {
					client = clients.get(key);
				}
			}
		}
		return client;
	}

	public static void releaseAllResources() {
		Iterator<ServiceClient> it = clients.values().iterator();
		while (it.hasNext()) {
			it.next().clear();
		}
		clients.clear();
	}

	private ServiceClient(String host, int port) {
		this.host = host;
		this.port = port;
		// Configure the client.
		bootstrap = new ClientBootstrap(channelFactory);
		bootstrap.setOption("tcpNoDelay", Boolean.parseBoolean(AppProperties
				.get("rpc_client_tcpNoDelay", "true")));
		bootstrap.setOption("keepAlive", Boolean.parseBoolean(AppProperties
				.get("rpc_client_keepAlive", "true")));
		bootstrap.setOption("connectTimeoutMillis", AppProperties.getAsInt(
				"rpc_client_connectTimeoutMillis", 1000 * 30));
		bootstrap.setOption("receiveBufferSize", AppProperties.getAsInt(
				"rpc_client_receiveBufferSize", 1024 * 1024));
		bootstrap.setPipelineFactory(new RpcClientPipelineFactory());
		connAmout = AppProperties.getAsInt("rpc_client_connectionAmount", 1);
		this.channelQueue = new BlockQueue();
	}

	private static final String rpcHanderName = "rpcHander";

	public Object invokeWithShortConnect(final RpcRequest req) throws Throwable {
		if (isAsyncReq(req)) {
			throw new RpcClientException(
					"Short connection mode does not support asynchronous callback");
		}
		final Channel channel = open();
		// RpcClientHandler rpcHander = channel.getPipeline().get(
		// RpcClientHandler.class);
		try {
			RpcClientHandler rpcHander = (RpcClientHandler) channel
					.getPipeline().get(rpcHanderName);
			RpcResponse res = rpcHander.invoke(req);
			// channel.close().awaitUninterruptibly();
			// logger.debug("invokeWithShortConnect res:{}", res);
			if (res != null) {
				if (res.getReturnFlag() >= 0) {
					return res.getResult();
				}
				dealErrException(req, res);
			}
			return null;
		} finally {
			channel.close();
		}
	}

	private static boolean isAsyncReq(final RpcRequest req) {
		Class<?> cc[] = req.getParameterTypes();
		for (Class<?> c : cc) {
			if (c.equals(AsyncMethodCallback.class)) {
				return true;
			}
		}
		return false;
	}

	private AtomicBoolean initFlag = new AtomicBoolean(false);

	private void initConns() {
		if (!initFlag.compareAndSet(false, true)) {
			return;
		}
		for (int i = 0; i < this.connAmout; i++) {
			// channelQueue.push(open());
			channelQueue.push(new MockChannel());
		}
	}

	public Object invokeWithLongConnect(final RpcRequest req) throws Throwable {
		initConns();
		logger.debug("channelQueue size:{}", channelQueue.size());
		Channel channel = (Channel) channelQueue.pop(5 * 1000);
		if (channel == null || !channel.isOpen()) {
			if (channel != null)
				channel.close();
			channel = open();
			// channelQueue.push(channel);
			if (logger.isDebugEnabled()) {
				logger.debug(
						"channel is close,open a new one[{}] in the queue",
						channel);
			}
		}
		final RpcClientHandler rpcHander = (RpcClientHandler) channel
				.getPipeline().get(rpcHanderName);
		synchronized (rpcHander) {
			try {
				// logger.debug("rpcHander:{}", rpcHander);
				if (isAsyncReq(req)) {
					req.setAsync(true);
				}
				logger.debug("invokeWithLongConnect req:{}", req);
				if (req.isAsync()) {
					rpcHander.asyncInvoke(req);
					return null;
				} else {
					RpcResponse res = rpcHander.invoke(req);
					logger.debug("invokeWithLongConnect res:{}", res);
					if (res != null) {
						if (res.getReturnFlag() >= 0) {
							return res.getResult();
						}
						dealErrException(req, res);
					}
					return null;
				}
			} finally {
				if (channel != null) {
					if (channelQueue.size() > this.connAmout) {
						channel.close();
					} else {
						channelQueue.push(channel);
					}
				}
			}
		}
	}

	private void dealErrException(final RpcRequest req, RpcResponse res)
			throws Exception, Throwable {
		if (req.getExceptionTypes() != null
				&& req.getExceptionTypes().length > 0) {
			if (res.getReturnFlag() == RpcConst.ERR_CODE_CLIENT_INVOKE_TIMEOUT_EXCEPTION) {
				throwNewException(req, res);
			} else {
				if (res.getException() != null)
					throw (Throwable) res.getException();
				throwNewException(req, res);
			}
		} else {
			if (res.getReturnFlag() == RpcConst.ERR_CODE_CLIENT_INVOKE_TIMEOUT_EXCEPTION)
				throw new RpcClientException(res.getReturnFlag(),
						res.getReturnMsg());
			throw new RpcClientException(
					RpcConst.ERR_CODE_REMOTE_CALL_EXCEPTION,
					"remote call err：{" + res.getReturnMsg() + "["
							+ res.getReturnFlag() + "]}");
		}
	}

	private void throwNewException(final RpcRequest req, RpcResponse res)
			throws Throwable {
		Class<?> t = req.getExceptionTypes()[0];
		@SuppressWarnings("rawtypes")
		Class[] constrParamTypes = new Class[] { String.class };
		Object[] constrParamValues = new Object[] { res.getReturnMsg() + "["
				+ res.getReturnFlag() + "]" };
		Throwable tb = (Throwable) ClassUtil.newInstance(t.getName(),
				constrParamTypes, constrParamValues);
		throw tb;
	}

	public boolean checkServerConnection() {
		boolean f = true;
		Channel c = null;
		try {
			c = open();
		} catch (RpcClientException e) {
			f = false;
			logger.error(e.getMessage() + "[" + e.getErrCode() + "]");
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return f;
	}

	private Channel open() {
		ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(
				host, port));
		Channel channel = connectFuture.awaitUninterruptibly().getChannel();
		if (channel.isConnected()) {
			logger.info("connect[" + host + "(" + port + ") OK]");
			return channel;
		} else {
			throw new RpcClientException(RpcConst.ERR_CODE_CONN_EXCEPTION,
					"connecting to the server[" + host + "," + port + "] error");
		}
	}

	public void clear() {
		// bootstrap.releaseExternalResources();
		try {
			while (true) {
				Channel c = (Channel) channelQueue.poll();
				if (c != null) {
					c.close();
					logger.info("release connect[" + host + "(" + port + ")]");
				} else {
					break;
				}
			}
		} finally {
			channelQueue.clear();
			this.bootstrap.releaseExternalResources();
		}
	}
}
