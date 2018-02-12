package io.baschel.ulserver;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.baschel.ulserver.config.MasterConfig;
import io.baschel.ulserver.db.DbVerticle;
import io.baschel.ulserver.game.GameVerticle;
import io.baschel.ulserver.msgs.db.PlayerRecordRequest;
import io.baschel.ulserver.net.NetVerticle;
import io.baschel.ulserver.util.Json;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static Vertx vertx;
    public static MasterConfig config;

    public static void main(String[] args) throws IOException {
        vertx = Vertx.vertx();
        Properties props = System.getProperties();
        String configPath = props.getProperty("ulserver.config");
        Config c = ConfigFactory.parseFile(new File(configPath));
        config = Json.objectFromJsonObject(new JsonObject(c.root().render(ConfigRenderOptions.concise())), MasterConfig.class);
        vertx.deployVerticle(new NetVerticle());
        vertx.deployVerticle(new GameVerticle());
        vertx.deployVerticle(new DbVerticle());
        // debugging only for now
        createHttpServer();
    }

    public static void createHttpServer()
    {
        HttpServer serv = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.get("/playerRecord/:playerName").handler(ctx -> {
            PlayerRecordRequest.of(ctx.request().getParam("playerName")).withArts(true).withItems(true).send(reply -> {
                if(reply.failed()) {
                    ctx.response().setStatusCode(500).setStatusMessage(reply.toString()).end(reply.cause().toString());
                } else {
                    ctx.response().setStatusCode(200).end(((JsonObject)reply.result().body()).encodePrettily());
                }
            });
        });
        serv.requestHandler(router::accept).listen(8080);
    }
}
