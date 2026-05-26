package org.example.sc.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.example.sc.constants.Constants;
import org.example.sc.handlers.QuoteHandler;
import org.example.sc.services.LoyaltyPointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {
	private static final Logger log =
			LoggerFactory.getLogger(HttpServerVerticle.class);
	private LoyaltyPointService loyaltyPointService;
   public HttpServerVerticle(LoyaltyPointService _loyaltyPointService) {
	   this.loyaltyPointService = _loyaltyPointService;
   }
	@Override
	public void start(Promise<Void> promise) {

		QuoteHandler quoteHandler = new QuoteHandler(loyaltyPointService);
		Router router = Router.router (vertx);
        router.route ().handler (BodyHandler.create ());
		router.post(Constants.POINT_URI).handler(quoteHandler);
		vertx.createHttpServer()
				.requestHandler(router)
				.listen(0)
				.onSuccess (server -> {
					log.info ("Server has been started at port No" + server.actualPort () );
					vertx.eventBus().publish("server.discovery", server.actualPort());
					promise.complete ();
				}).onFailure (s->{
					log.info ("Server has been started" );
					promise.fail (s.getMessage ());
				});
	}
}
