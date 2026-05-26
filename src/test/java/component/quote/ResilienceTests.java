package component.quote;

import component.BaseTest;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.example.sc.constants.Constants;
import org.junit.jupiter.api.*;
import java.util.concurrent.TimeUnit;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.example.sc.TestData.TestDataGenerator.validRequest;


public class ResilienceTests extends BaseTest {

	@Test
	public void shouldReturn500WhenFxServerFails(Vertx vertx, VertxTestContext tc) {
		WebClient client = WebClient.create (vertx);
		fxStubs.setFxServer500Status (fxServer);
		client.post (port, Constants.HOST, Constants.POINT_URI)
				.sendJson (validRequest ( ), ar -> {
					if ( ar.succeeded ( ) && ar.result ( ).statusCode ( ) == 500 ) {
						tc.verify (() -> {
							assertThat ( ar.result ( ).bodyAsJsonObject ( ).getInteger (Constants.STATUS_CODE)).isEqualTo (500);
							assertThat (ar.result ( ).bodyAsJsonObject ( ).getString (Constants.MESSAGE)).contains ("FX Issue");

						});
						tc.completeNow ( );
					} else {
						tc.failNow (ar.result ( ).bodyAsString ( ));
					}
				});
	}

	@Test
	void shouldSucceedWhenFxFailsOnceThenRecovers(Vertx vertx, VertxTestContext tc) {
		fxStubs.setFxServerFailOnceThenRecover (fxServer);
		promoStubs.setPromoServer200status (promoServer);
		WebClient client = WebClient.create (vertx);
		client.post (port, Constants.HOST, Constants.POINT_URI)
				.sendJson (validRequest ( ), ar -> {
					if ( ar.succeeded ( ) ) {
						var response = ar.result ( );
						tc.verify (() -> {
							assertThat ( response.statusCode ( )).isEqualTo (200);
							assertThat (response.bodyAsJsonObject ( ).getDouble ("effectiveFxRate")).isEqualTo (1.2);
						});

						tc.completeNow ( );

					} else {
						tc.failNow (ar.cause ( ));
					}
				});
	}

	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void shouldFailWhenFxTimesOut(Vertx vertx, VertxTestContext tc) {
		fxStubs.setFxServerTimeOut (fxServer);
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

	@Test
	void shouldRetryExactlyThreeTimes(Vertx vertx, VertxTestContext tc) {
		fxStubs.setFxServer500Status (fxServer);
		WebClient client = WebClient.create (vertx);
		client.post (port, Constants.HOST, Constants.POINT_URI)
				.sendJson (validRequest ( ), ar -> {
					if ( ar.succeeded ( ) ) {
						tc.verify (() ->
							fxServer.verify (3,
									getRequestedFor (urlPathEqualTo (Constants.FX_URI)))
						);
						tc.completeNow ( );
					} else {
						tc.failNow (ar.cause ( ));
					}
				});
	}

	@Test
	void shouldReturn404WhenFxServerNotFound(Vertx vertx, VertxTestContext tc) {
		fxStubs.setFxServerResourceNotFound (fxServer);
		WebClient client = WebClient.create (vertx);
		client.post (port, Constants.HOST, Constants.POINT_URI)
				.sendJson (validRequest ( ), ar -> {
					if ( ar.succeeded ( ) ) {
						tc.verify (() ->
							assertThat ( ar.result ( ).statusCode ( )).isEqualTo (404)
						);
						tc.completeNow ( );
					} else {
						tc.failNow (ar.cause ( ));
					}
				});

	}



	}


