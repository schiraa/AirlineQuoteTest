package org.example.sc.gateway;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.example.sc.constants.Constants;
import org.example.sc.models.ApiException;

public class FxGateway {


	private final int portNo;
		private final WebClient client;

		public FxGateway(Vertx vertx,int _portNo) {
			this.client = WebClient.create(vertx);
			this.portNo = _portNo;
		}

		public Future<Double> getFxRate(String currency) {
			Promise<Double> promise = Promise.promise ();
			callFxApi (currency, 0, promise);
			return promise.future();
		}

		private void callFxApi(String currency, int count, Promise<Double> promise) {
			client.get(portNo, Constants.HOST, Constants.FX_URI+"?from="+currency +"&to=EUR")
					.timeout(2000)
					.send(ar -> {
						if (ar.succeeded() && ar.result ().statusCode () == 200) {

							JsonObject body = ar.result().bodyAsJsonObject();


							promise.complete(body.getDouble("rate"));
						} else if (count < 2) {
							callFxApi (currency, count + 1, promise);
						} else {
							     if( ar.succeeded ( )) {
									 var resp = ar.result ();
									 promise.fail(new ApiException (resp.statusCode (),resp.statusMessage (),resp.bodyAsString ()));

								 } else {
									 promise.fail(new ApiException (500,ar.cause ().getMessage (),ar.cause ().getMessage ()));

								 }
						}
					});
		}
	}

