package io.baschel.ulserver.net;

import io.baschel.ulserver.Main;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by macobas on 13/06/16.
 */
public class NetVerticle extends AbstractVerticle {

    private NetServer tcpServer;
    private Set<ClientConnection> clientConnections = new HashSet<>();
    public static final String EVENTBUS_ADDRESS = NetVerticle.class.getName();
    public static final Logger L = LoggerFactory.getLogger(NetVerticle.class);

    @Override
    public void start() {
        int port = Main.config.serverConfig.port;
        NetServerOptions opts = new NetServerOptions().setTcpNoDelay(false).setTcpKeepAlive(true);
        tcpServer = vertx.createNetServer(opts);

        tcpServer.connectHandler(this::handleConnection);

        tcpServer.listen(port, result -> {
            if (!result.succeeded()) {
                L.error("Failed to deploy", result.cause());
                vertx.close();
            } else
                L.info("Server started and bound on {}", port);
        });
    }

    private void handleConnection(NetSocket socket) {
        ClientConnection cxn = new ClientConnection(socket.writeHandlerID());
        L.info("NEW CONNECTION from {}", socket.remoteAddress().toString());
        clientConnections.add(cxn);
        socket.handler(cxn::handleMessage);
        socket.closeHandler(cxn::onClose);
    }
}
