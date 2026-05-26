package component.quote;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.example.sc.Main;
import org.example.sc.TestData.TestDataGenerator;
import org.example.sc.constants.Constants;
import org.example.sc.models.ApiException;
import org.example.sc.models.QuoteTestData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith (VertxExtension.class)
public class PointsCalculationHappyPathTest {
	private static final Logger log =
			LoggerFactory.getLogger(PointsCalculationHappyPathTest.class);

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

	 static Stream<QuoteTestData> quoteRequestProvider () {
		 return TestDataGenerator.getHappyPathTestData ();
	 }

	@ParameterizedTest
	@MethodSource("quoteRequestProvider")
	void
	shouldCalculatePointsWithPromo( QuoteTestData request ,Vertx vertx,VertxTestContext tc) {
		 WebClient client = WebClient.create (vertx);
		client.post (port, Constants.HOST, Constants.POINT_URI)
				.sendJson (new JsonObject ( )
								.put ("fareAmount", request.getFareAmount ())
								.put ("currency", request.getCurrency ())
								.put ("cabinClass", request.getCabinClass ())
								.put ("customerTier", request.getCustomerTier ())
								.put ("promoCode", request.getPromoCode ())
						, ar -> {

								if ( ar.succeeded ( ) ) {

									var resp = ar.result ( );
									System.out.println (ar.result ( ).bodyAsString ( ));

									var body = resp.bodyAsJsonObject ( );
                                     tc.verify (()->{
										 assertThat (resp.statusCode ( )).isEqualTo (200);
										 assertThat (body.getInteger (Constants.BASE_POINTS)).isGreaterThan (0);
										 assertThat (body.getInteger (Constants.TOTAL_POINTS)).isLessThanOrEqualTo (50_000);
										 assertThat (body.getInteger (Constants.TOTAL_POINTS)).isEqualTo (request.getTotalPoints ());

									 });
								  tc.completeNow ();

								}

								else {
									tc.failNow (new ApiException (503, "Connection Refused","Connection Refused"));
								}
							});
	 }

	@AfterAll
	public static void tearDown(Vertx vertx){
		if(vertx != null){
			vertx.close ();
		}
	}

}