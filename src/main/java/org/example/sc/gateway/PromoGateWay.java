package org.example.sc.gateway;
import io.vertx.core.*;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.example.sc.constants.Constants;
import org.example.sc.models.ApiException;

public class PromoGateWay {

	private final WebClient client;
	private  int portNo;
	public PromoGateWay(Vertx vertx, int _portNo) {
			this.client = WebClient.create(vertx);
			this.portNo = _portNo;
	}

		public Future<PromoResult> getPromo(String code) {
			if (code == null || code.isBlank()) {
				return Future.succeededFuture(PromoResult.empty());
			}

			Promise<PromoResult> result = Promise.promise ();

			client.get(portNo, Constants.HOST, Constants.PROMO_URI)
					.timeout(1000)
					.send(ar -> {
						if (ar.succeeded() ) {
							if( ar.result ().statusCode () == 200) {
								try {
									JsonObject b = ar.result ( ).bodyAsJsonObject ( );
									result.complete (new PromoResult (
											b.getInteger ("bonus"),
											b.getBoolean ("isExpired"),
											b.getBoolean ("expiresSoon")
									));
								} catch ( DecodeException e ) {
									result.fail (new ApiException (500, e.getMessage ( ), e.getMessage ( )));

								}
							} else if ( ar.result ().statusCode () == 404 ) {
								result.fail (new ApiException (500, ar.result ().statusMessage (), ar.result ().bodyAsString ()));

							} else if ( ar.result ().statusCode () == 500 ) {
								result.fail (new ApiException (500, ar.result ().statusMessage (), ar.result ().bodyAsString ()));

							}

						} else {
							result.complete(PromoResult.empty());
						}
					});

			return result.future();
		}

		public record PromoResult(int bonus, boolean isExpired,  boolean expiresSoon) {
			static PromoResult empty() {
				return new PromoResult(0,false, false);
			}
		}
	}


