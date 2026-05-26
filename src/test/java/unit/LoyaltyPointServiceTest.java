package unit;

import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.example.sc.enums.Tier;
import org.example.sc.gateway.FxGateway;
import org.example.sc.gateway.PromoGateWay;
import org.example.sc.models.ApiException;
import org.example.sc.models.QuoteRequest;
import org.example.sc.services.LoyaltyPointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith (VertxExtension.class)
@ExtendWith (MockitoExtension.class)
public class LoyaltyPointServiceTest {

	@Mock
	private FxGateway fxClient;
	@Mock
	private PromoGateWay promoClient;

	private LoyaltyPointService service;

	@BeforeEach
	void setUp() {

		service = new LoyaltyPointService (fxClient, promoClient);
	}

	@Test
	void shouldCalculateBasePoints( VertxTestContext testContext) {
		// Arrange
		QuoteRequest req = createValidRequest();
		double fxRate = 1.2;

		PromoGateWay.PromoResult mockPromo = new PromoGateWay.PromoResult(500, false,false);

		when(fxClient.getFxRate("USD")).thenReturn(Future.succeededFuture(fxRate));
		when(promoClient.getPromo("SUMMER25")).thenReturn(Future.succeededFuture(mockPromo));
		service.calCulatePoints(req).onComplete(testContext.succeeding(response -> {
			testContext.verify(() -> {
				assertThat(response.getBasePoints()).isEqualTo(1200);
				testContext.completeNow ();
			});

		}));
	}

	@Test
	void shouldApplyTierMultiplier( VertxTestContext testContext) {
		QuoteRequest req = createValidRequest();
		double fxRate = 1.2;

		PromoGateWay.PromoResult mockPromo = new PromoGateWay.PromoResult(500, false,false);

		when(fxClient.getFxRate("USD")).thenReturn(Future.succeededFuture(fxRate));
		when(promoClient.getPromo("SUMMER25")).thenReturn(Future.succeededFuture(mockPromo));
		service.calCulatePoints(req).onComplete(testContext.succeeding(response -> {
			testContext.verify(() -> {
				assertThat(response.getBasePoints()).isEqualTo(1200);
                assertThat (response.getTierBonus ()).isEqualTo ((int)Math.floor (response.getBasePoints() * Tier.valueOf ( req.getCustomerTier ()).bonus ()));
				testContext.completeNow ();
			});
		}));
	}


	@Test
	void shouldApplyPromoCodeDiscount(VertxTestContext testContext) {
		QuoteRequest req = createValidRequest ( );
		double fxRate = 1.2;

		PromoGateWay.PromoResult mockPromo = new PromoGateWay.PromoResult (500, false, false);

		when (fxClient.getFxRate ("USD")).thenReturn (Future.succeededFuture (fxRate));
		when (promoClient.getPromo ("SUMMER25")).thenReturn (Future.succeededFuture (mockPromo));
		service.calCulatePoints (req).onComplete (testContext.succeeding (response -> {
			testContext.verify (() -> {
				assertThat (response.getBasePoints ( )).isEqualTo (1200);
				assertThat (response.getPromoBonus ( )).isEqualTo (500);
				testContext.completeNow ( );
			});
		}));
	}
		@Test
		void shouldCalculateTotalPoints(VertxTestContext testContext) {
			QuoteRequest req = createValidRequest ( );
			double fxRate = 1.2;

			PromoGateWay.PromoResult mockPromo = new PromoGateWay.PromoResult (500, false, false);

			when (fxClient.getFxRate ("USD")).thenReturn (Future.succeededFuture (fxRate));
			when (promoClient.getPromo ("SUMMER25")).thenReturn (Future.succeededFuture (mockPromo));
			service.calCulatePoints (req).onComplete (testContext.succeeding (response -> {
				testContext.verify (() -> {
					assertThat (response.getBasePoints ( )).isEqualTo (1200);
					assertThat (response.getTotalPoints ( )).isEqualTo (2060);
					testContext.completeNow ( );
				});
			}));
		}
			@Test
			void testValidationInvalidAmount(VertxTestContext testContext) {
				QuoteRequest req = createValidRequest();
				req.setFareAmount(0);

				service.calCulatePoints(req).onComplete(testContext.failing(throwable -> {
					testContext.verify(() -> {
						assertThat(throwable).isInstanceOf(ApiException.class);
						assertThat(((ApiException) throwable).getErrorCode ()).isEqualTo("INVALID_AMOUNT");
						testContext.completeNow();
					});
				}));
			}

			@Test
			void testPromoExpiryWarning(VertxTestContext testContext) {
				QuoteRequest req = createValidRequest();
				PromoGateWay.PromoResult expiringPromo = new PromoGateWay.PromoResult(100, true,true);

				when(fxClient.getFxRate(anyString())).thenReturn(Future.succeededFuture(1.0));
				when(promoClient.getPromo(anyString())).thenReturn(Future.succeededFuture(expiringPromo));

				service.calCulatePoints(req).onComplete(testContext.succeeding(response -> {
					testContext.verify(() ->
						assertThat(response.getWarnings()).asInstanceOf (LIST).contains("PROMO_EXPIRES_SOON"));
						testContext.completeNow();
				}));
			}

	@Test
	void shouldFailWhenRequestIsNull(VertxTestContext testContext) {
		service.calCulatePoints(null).onComplete(testContext.failing(throwable -> {
			testContext.verify(() -> {
				assertThat(throwable).isInstanceOf(ApiException.class);
				assertThat(((ApiException) throwable).getErrorCode()).isEqualTo("INVALID Request");
				testContext.completeNow();
			});
		}));
	}

	@Test
	void shouldFailWhenCurrencyIsInvalid(VertxTestContext testContext) {
		QuoteRequest req = createValidRequest();
		req.setCurrency("ABC");

		service.calCulatePoints(req).onComplete(testContext.failing(throwable -> {
			testContext.verify(() -> {
				assertThat(throwable).isInstanceOf(ApiException.class);
				assertThat(((ApiException) throwable).getErrorCode()).isEqualTo("INVALID_CURRENCY");
				testContext.completeNow();
			});
		}));
	}

	@Test
	void shouldFailWhenTierIsInvalid(VertxTestContext testContext) {
		QuoteRequest req = createValidRequest();
		req.setCustomerTier("VIPPLUS");

		service.calCulatePoints(req).onComplete(testContext.failing(throwable -> {
			testContext.verify(() -> {
				assertThat(throwable).isInstanceOf(ApiException.class);
				assertThat(((ApiException) throwable).getErrorCode()).isEqualTo("INVALID_TIER");
				testContext.completeNow();
			});
		}));
	}

	@Test
	void shouldFailWhenCabinIsInvalid(VertxTestContext testContext) {
		QuoteRequest req = createValidRequest();
		req.setCabinClass("PRIVATE");

		service.calCulatePoints(req).onComplete(testContext.failing(throwable -> {
			testContext.verify(() -> {
				assertThat(throwable).isInstanceOf(ApiException.class);
				assertThat(((ApiException) throwable).getErrorCode()).isEqualTo("INVALID_CABIN");
				testContext.completeNow();
			});
		}));
	}

	@Test
	void shouldFailWhenPromoCodeIsInvalid(VertxTestContext testContext) {
		QuoteRequest req = createValidRequest();
		req.setPromoCode("BADCODE");

		service.calCulatePoints(req).onComplete(testContext.failing(throwable -> {
			testContext.verify(() -> {
				assertThat(throwable).isInstanceOf(ApiException.class);
				assertThat(((ApiException) throwable).getErrorCode()).isEqualTo("INVALID_PROMOCODE");
				testContext.completeNow();
			});
		}));
	}

	@Test
	void shouldAllowNullPromoCode(VertxTestContext testContext) {
		QuoteRequest req = createValidRequest();
		req.setPromoCode(null);

		PromoGateWay.PromoResult promo = new PromoGateWay.PromoResult(0, false, false);

		when(fxClient.getFxRate("USD")).thenReturn(Future.succeededFuture(1.0));
		when(promoClient.getPromo(null)).thenReturn(Future.succeededFuture(promo));

		service.calCulatePoints(req).onComplete(testContext.succeeding(response -> {
			testContext.verify(() -> {
				assertThat(response).isNotNull();
				testContext.completeNow();
			});
		}));
	}

	@Test
	void shouldCapTotalPointsAtMaxLimit(VertxTestContext testContext) {
		QuoteRequest req = createValidRequest();
		req.setFareAmount(100000.0);

		PromoGateWay.PromoResult promo = new PromoGateWay.PromoResult(5000, false, false);

		when(fxClient.getFxRate("USD")).thenReturn(Future.succeededFuture(10.0));
		when(promoClient.getPromo("SUMMER25")).thenReturn(Future.succeededFuture(promo));

		service.calCulatePoints(req).onComplete(testContext.succeeding(response -> {
			testContext.verify(() -> {
				assertThat(response.getTotalPoints()).isEqualTo(50000);
				testContext.completeNow();
			});
		}));
	}



	private QuoteRequest createValidRequest() {
		return QuoteRequest.builder()
				.fareAmount(1000.0)
				.currency("USD")
				.customerTier("GOLD")
				.cabinClass("ECONOMY")
				.promoCode("SUMMER25")
				.build();
	}
}
