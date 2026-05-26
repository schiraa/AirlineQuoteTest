package component;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.example.sc.constants.Constants;
import org.example.sc.gateway.FxGateway;
import org.example.sc.gateway.PromoGateWay;
import org.example.sc.handlers.QuoteHandler;
import org.example.sc.services.LoyaltyPointService;
import org.example.sc.stubs.FxStubs;
import org.example.sc.stubs.PromoStubs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ExtendWith (VertxExtension.class)
public class BaseTest {
	private static final Logger log =
			LoggerFactory.getLogger(BaseTest.class);
	protected  int port;
	protected   WireMockServer fxServer;
	protected   WireMockServer promoServer;
	protected HttpServer appServer;
	protected FxStubs fxStubs;
	protected PromoStubs promoStubs;
	@BeforeEach
	public   void setUp(Vertx vertx, VertxTestContext tc) {
		fxServer = new WireMockServer (
				WireMockConfiguration.options().dynamicPort ());
		promoServer = new WireMockServer (
				WireMockConfiguration.options().dynamicPort ());
		fxServer.start();
		promoServer.start();
		fxServer.resetAll();
		promoServer.resetAll();
		fxStubs = new FxStubs ();
		promoStubs = new PromoStubs ();
		Router router = Router.router (vertx);
		router.route().handler (BodyHandler.create ());
		router.route ().handler (ctx->{
			log.info ("request {} :",ctx.request ());
			ctx.next ();
		});
		router.post (Constants.POINT_URI)
				.handler (new QuoteHandler (new LoyaltyPointService(new FxGateway(vertx, fxServer.port ( )),
						new PromoGateWay (vertx, promoServer.port ( )))));
		vertx.createHttpServer ( )
				.requestHandler (router)
				.listen (0)
				.onSuccess (res -> {
					this.appServer = res;
					log.info ("Server has been Started at" + res.actualPort () );
					port = res.actualPort ();
					tc.completeNow ();
				})
				.onFailure (err -> {
					log.error (err.getMessage () );
					tc.failNow ("Server is not started");
				});

	}


	@AfterEach
	public void tearDown() {
		if (appServer != null) {
			appServer.close();
		}
		if (fxServer != null && fxServer.isRunning()) {
			fxServer.stop();
		}
		if (promoServer != null && promoServer.isRunning()) {
			promoServer.stop();
		}
	}


}
