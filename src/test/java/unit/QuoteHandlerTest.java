

package unit;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import org.example.sc.handlers.QuoteHandler;
import org.example.sc.models.ApiException;
import org.example.sc.models.QuoteRequest;
import org.example.sc.models.QuoteResponse;
import org.example.sc.services.LoyaltyPointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteHandlerTest {

	@Mock
	private LoyaltyPointService loyaltyPointService;

	@Mock
	private RoutingContext routingContext;

	@Mock
	private RequestBody requestBody;

	@Mock
	private HttpServerResponse httpServerResponse;

	private QuoteHandler quoteHandler;

	@BeforeEach
	void setUp() {
		quoteHandler = new QuoteHandler(loyaltyPointService);
	}

	@Test
	void shouldReturnJsonResponseWhenCalculatePointsSucceeds() {
		QuoteRequest quoteRequest = new QuoteRequest();
		QuoteResponse quoteResponse  = QuoteResponse.builder()
				.basePoints(100)
				.tierBonus(20)
				.promoBonus(10)
				.totalPoints(130)
				.effectiveFxRate(1.25)
				.warnings(null)
				.build();
		when(routingContext.body()).thenReturn(requestBody);
		when(requestBody.asPojo(QuoteRequest.class)).thenReturn(quoteRequest);
		when(routingContext.response()).thenReturn(httpServerResponse);
		when(httpServerResponse.putHeader(anyString(), anyString())).thenReturn(httpServerResponse);
		when(loyaltyPointService.calCulatePoints(quoteRequest))
				.thenReturn( Future.succeededFuture(quoteResponse));

		quoteHandler.handle(routingContext);

		verify(routingContext).body();
		verify(requestBody).asPojo(QuoteRequest.class);
		verify(loyaltyPointService).calCulatePoints(quoteRequest);
		verify(routingContext).response();
		verify(httpServerResponse).putHeader("Content-Type", "application/json");
		verify(httpServerResponse).end(anyString());
	}

	@Test
	void shouldReturnErrorResponseWhenApiExceptionOccurs() {
		QuoteRequest quoteRequest = new QuoteRequest();
		ApiException apiException = mock(ApiException.class);

		when(apiException.getStatusCode()).thenReturn(400);
		when(apiException.getErrorCode()).thenReturn("QUOTE_001");
		when(apiException.getMessage()).thenReturn("Invalid quote request");

		when(routingContext.body()).thenReturn(requestBody);
		when(requestBody.asPojo(QuoteRequest.class)).thenReturn(quoteRequest);
		when(routingContext.response()).thenReturn(httpServerResponse);
		when(httpServerResponse.setStatusCode(400)).thenReturn(httpServerResponse);
		when(loyaltyPointService.calCulatePoints(quoteRequest))
				.thenReturn(Future.failedFuture(apiException));

		quoteHandler.handle(routingContext);

		ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);

		verify(routingContext).body();
		verify(requestBody).asPojo(QuoteRequest.class);
		verify(loyaltyPointService).calCulatePoints(quoteRequest);
		verify(routingContext).response();
		verify(httpServerResponse).setStatusCode(400);
		verify(httpServerResponse).end(responseCaptor.capture());

		String responseJson = responseCaptor.getValue();
		assertThat(responseJson).contains("400");
		assertThat(responseJson).contains("QUOTE_001");
		assertThat(responseJson).contains("Invalid quote request");
	}
}

