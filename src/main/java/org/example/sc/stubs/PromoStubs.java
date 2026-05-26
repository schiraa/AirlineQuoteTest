package org.example.sc.stubs;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.example.sc.constants.Constants;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

public class PromoStubs {

	public  void setPromoServer500Status(WireMockServer promoServer) {
		promoServer.stubFor(get(urlPathEqualTo (Constants.PROMO_URI))
				.willReturn(aResponse()
						.withStatus (500)
						.withStatusMessage ("PROMO ERROR")
						.withBody("Promo Issue: Error in checking Promo Codes")));
	}

	public  void setPromoServerFailOnceThenRecover(WireMockServer promoServer) {
		promoServer.stubFor(get(urlPathEqualTo(Constants.PROMO_URI))
				.inScenario("retry")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(500))
				.willSetStateTo("second"));

		promoServer.stubFor(get(urlPathEqualTo(Constants.PROMO_URI))
				.inScenario("retry")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBodyFile("fx/promo/expired.json")));
	}

	public  void setPromoServerTimeOut(WireMockServer promoServer) {
		promoServer.stubFor(get(urlPathEqualTo(Constants.PROMO_URI))
				.willReturn(aResponse()
						.withFixedDelay(5000)));
	}

	public  void setPromoServer200status(WireMockServer promoServer) {
		promoServer.stubFor(get(urlPathEqualTo(Constants.PROMO_URI))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBodyFile("fx/promo/expired.json")));
	}

	public  void setPromoServerResourceNotFound(WireMockServer promoServer) {
		promoServer.stubFor(get(urlPathEqualTo(Constants.FX_URI))
				.willReturn(aResponse ()
						.withStatus(404)));
	}
}
