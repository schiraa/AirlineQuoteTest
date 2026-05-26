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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class PointsCalculationUnHappyPathTest {
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

	static Stream<JsonObject> getQuoteTestata() {
		return TestDataGenerator.getUnHappyPathTestData ();
	}

	//Test if data sent is invalid
	@ParameterizedTest
	@MethodSource("getQuoteTestata")
	public void shouldReturn400WhenSentInvalidData(JsonObject json, Vertx vertx, VertxTestContext tc) {
		WebClient client = WebClient.create (vertx);
		client.post (port, Constants.HOST, Constants.POINT_URI)
				.sendJson (json
						, ar -> {

							if ( ar.succeeded ( ) ) {
								var resp = ar.result ( );
								tc.verify (() -> {
									assertThat (resp.statusCode ( )).isEqualTo (400);
									assertThat (resp.bodyAsJsonObject ( ).getString (Constants.ERROR_CODE)).isNotEmpty ();
								});
								tc.completeNow ( );

							} else {
								tc.failNow (new ApiException (503, ar.cause ( ).getMessage ( ), ar.cause ( ).getMessage ( )));
							}
						});
	}
}
