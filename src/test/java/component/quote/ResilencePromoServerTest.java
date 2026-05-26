package component.quote;

import component.BaseTest;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.example.sc.constants.Constants;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.example.sc.TestData.TestDataGenerator.validRequest;


public class ResilencePromoServerTest extends BaseTest {
	@Test
	public void shouldReturn500WhenPromoServerFails(Vertx vertx, VertxTestContext tc) {
		WebClient client = WebClient.create (vertx);
		fxStubs.setFxServer200status (fxServer);
		promoStubs.setPromoServer500Status (promoServer);
		client.post (port, Constants.HOST, Constants.POINT_URI)
				.sendJson (validRequest ( ), ar -> {
					if ( ar.succeeded ( ) && ar.result ( ).statusCode ( ) == 500 ) {
						tc.verify (() -> {
							assertThat ( ar.result ( ).bodyAsJsonObject ( ).getInteger (Constants.STATUS_CODE)).isEqualTo (500);
							assertThat (ar.result ( ).bodyAsJsonObject ( ).getString (Constants.MESSAGE)).contains ("Promo Issue");

						});
						tc.completeNow ( );
					} else {
						tc.failNow (ar.result ( ).bodyAsString ( ));
					}
				});
	}

	@Test
	void shouldFailWhenPromosServerTimesOut(Vertx vertx, VertxTestContext tc) {
		fxStubs.setFxServer200status (fxServer);
		promoStubs.setPromoServerTimeOut (promoServer);
		WebClient client = WebClient.create (vertx);
		client.post (port, Constants.HOST, Constants.POINT_URI)
				.sendJson (validRequest ( ), ar -> {
					if ( ar.succeeded ( ) ) {
						tc.verify (() ->
							assertThat ( ar.result ( ).statusCode ( )).isEqualTo (200)
						);
						tc.completeNow ( );
					} else {
						tc.failNow (ar.cause ( ));
					}
				});
	}


	@Test
	void shouldReturn200WhenPromoServerNotFound(Vertx vertx, VertxTestContext tc) {
		fxStubs.setFxServer200status (fxServer);
		promoStubs.setPromoServerResourceNotFound (promoServer);
		WebClient client = WebClient.create (vertx);
		client.post (port, Constants.HOST, Constants.POINT_URI)
				.sendJson (validRequest ( ), ar -> {
					if ( ar.succeeded ( ) ) {
						tc.verify (() ->
							assertThat ( ar.result ( ).statusCode ( )).isEqualTo (500)
						);
						tc.completeNow ( );
					} else {
						tc.failNow (ar.cause ( ));
					}
				});

	}




}
