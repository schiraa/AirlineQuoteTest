package component.quote;

import component.BaseTest;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.example.sc.Main;
import org.example.sc.TestData.TestDataGenerator;
import org.example.sc.constants.Constants;
import org.example.sc.models.QuoteTestData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

public class QuoteEdgeCasesTest extends BaseTest {

	private int port;

	@BeforeEach
	void SetUp(VertxTestContext testContext) {

		Main.startServer()
				.onSuccess(actualPort -> {
					this.port = actualPort;
					testContext.completeNow();
				})
				.onFailure(testContext::failNow);
	}

	static Stream<QuoteTestData> quoteRequestProvider() {
		return TestDataGenerator.getEdgeCasesTestData ();
	}

	@ParameterizedTest
	@MethodSource("quoteRequestProvider")
	void shouldCalculatePointsForNoneWithPromo(QuoteTestData quoteTestData, Vertx vertx, VertxTestContext tc) {
		WebClient client = WebClient.create (vertx);
		client.post (port, Constants.HOST, Constants.POINT_URI)
				.sendJson (new JsonObject ( )
								.put (Constants.FARE_AMOUNT, quoteTestData.getFareAmount ( ))
								.put (Constants.CURRENCY, quoteTestData.getCurrency ( ))
								.put (Constants.CABIN_CLASS, quoteTestData.getCabinClass ( ))
								.put (Constants.CUSTOMER_TIER, quoteTestData.getCustomerTier ( ))
								.put (Constants.PROMO_CODE, quoteTestData.getPromoCode ( ))
						, ar -> {

							if ( ar.succeeded ( ) ) {

								var resp = ar.result ( );
								System.out.println (ar.result ( ).bodyAsString ( ));

								var body = resp.bodyAsJsonObject ( );
								tc.verify (() -> {
									assertThat (resp.statusCode ( )).isEqualTo (200);
									assertThat (body.getInteger (Constants.BASE_POINTS)).isGreaterThan (0);
									assertThat (body.getInteger (Constants.BASE_POINTS)).isEqualTo (
											(int) Math.floor (quoteTestData. getFareAmount () * body.getDouble (  Constants.EFFECTIVE_FX_RATE)));
									assertThat (body.getInteger (Constants.TOTAL_POINTS)).isLessThanOrEqualTo (50_000);
									assertThat (body.getInteger (Constants.TOTAL_POINTS)).isEqualTo (quoteTestData.getTotalPoints ());

								});
								tc.completeNow ( );

							} else {
								tc.failNow (ar.cause ( ));
							}
						});
	}

	@AfterAll
	public static void tearDown(Vertx vertx) {
		if ( vertx != null ) {
			vertx.close ( );
		}
	}
}
