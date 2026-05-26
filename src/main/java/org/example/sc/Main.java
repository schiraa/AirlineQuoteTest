package org.example.sc;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.example.sc.gateway.FxGateway;
import org.example.sc.gateway.PromoGateWay;
import org.example.sc.servers.FxMockServer;
import org.example.sc.servers.PromoServer;
import org.example.sc.services.LoyaltyPointService;
import org.example.sc.stubs.FxStubs;
import org.example.sc.stubs.PromoStubs;
import org.example.sc.verticles.HttpServerVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
	private static final Logger log =
			LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
        startServer ();
	}

	public static Future<Integer> startServer(){
		Promise<Integer> promise = Promise.promise();
		Vertx vertx = Vertx.vertx();
		FxMockServer.startServer ();
		PromoServer.startServer ();
		FxStubs fxStubs = new FxStubs ();
		FxGateway fxGateway = new FxGateway(vertx,FxMockServer.fxServer.port () );
		PromoGateWay promoGateway = new PromoGateWay(vertx,PromoServer.promoServer.port ());
		LoyaltyPointService service =
				new LoyaltyPointService (fxGateway, promoGateway);
		PromoStubs promoStubs = new PromoStubs ();
		fxStubs.runAllCurrencies (FxMockServer.fxServer);
		promoStubs.setPromoServer200status (PromoServer.promoServer);
		vertx.eventBus().consumer("server.discovery", message -> {
			Integer port = (Integer) message.body();
			promise.tryComplete(port);
		});
		vertx.deployVerticle(new HttpServerVerticle (service))
				.onFailure (promise::fail);

        return promise.future ();

	}
}