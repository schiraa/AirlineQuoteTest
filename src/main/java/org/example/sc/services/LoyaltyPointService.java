package org.example.sc.services;
import io.vertx.core.*;
import org.example.sc.enums.CabinClass;
import org.example.sc.enums.Currency;
import org.example.sc.enums.PromoCodes;
import org.example.sc.enums.Tier;
import org.example.sc.gateway.FxGateway;
import org.example.sc.gateway.PromoGateWay;
import org.example.sc.models.ApiException;
import org.example.sc.models.QuoteRequest;
import org.example.sc.models.QuoteResponse;

import java.util.ArrayList;
import java.util.List;

public class LoyaltyPointService {

	private static final int MAX_POINTS = 50000;
	private final FxGateway fxClient;
	private final PromoGateWay promoClient;

		public LoyaltyPointService(FxGateway _fxClient, PromoGateWay _promoClient) {
			this.fxClient = _fxClient;
			this.promoClient = _promoClient;
		}

		public Future<QuoteResponse> calCulatePoints(QuoteRequest req) {
			return validate(req)
					.compose (f ->fxClient.getFxRate (req.getCurrency ()))
					.compose (rate->promoClient.getPromo (req.getPromoCode ( ))
					.map (promo->compute (req,rate,promo)));
		}

		private QuoteResponse compute(
				QuoteRequest req,
				double fxRate,
				PromoGateWay.PromoResult promo) {

			int base = (int) Math.floor(req.getFareAmount () * fxRate);
			Tier tier = Tier.from(req.getCustomerTier ());
			int tierBonus = (int) Math.floor(base * tier.bonus());
			int promoBonus = promo.bonus();
			int total = Math.min(base + tierBonus + promoBonus, MAX_POINTS);
			List<String> warnings = new ArrayList<>();
			if (promo.expiresSoon()) {
				warnings.add("PROMO_EXPIRES_SOON");
			}
			return QuoteResponse.builder()
					.basePoints (base)
					.tierBonus (tierBonus)
					.promoBonus (promoBonus)
					.totalPoints (total)
					.effectiveFxRate (fxRate)
					.warnings (warnings)
					.build();
		}

		private Future<Void> validate(QuoteRequest req) {
			if(req == null) {
				return Future.failedFuture (new ApiException ( 400,"INVALID Request", "Request shouldnt be null"));
			}

			if (req.getFareAmount () <= 0) {
				return Future.failedFuture (new ApiException (400,"INVALID_AMOUNT","Fare Amount must be grater than zero"));
			}

			try {
				if(req.getCurrency () == null ) {
					throw new IllegalArgumentException ( "Currency should not be null" );
				}
				Currency.from (req.getCurrency ( ));
			}catch ( IllegalArgumentException iae ) {
				return Future.failedFuture (new ApiException (400,"INVALID_CURRENCY","Currency is not supported"));
			}

              try {
				  if(req.getCustomerTier () == null ) {
					  throw new IllegalArgumentException ( "Tier should not be null" );
				  }
				  Tier.from (req.getCustomerTier ( ));
			  } catch ( IllegalArgumentException iae ) {
				 return  Future.failedFuture (new ApiException (400,"INVALID_TIER","Customer Tier is not Valid"));
			  }

			  try{
				  if(req.getCabinClass () == null ) {
					  throw new IllegalArgumentException ( "Cabin class should not be null" );
				  }
				  CabinClass.from (req.getCabinClass ( ));
			  } catch ( IllegalArgumentException iae ) {
				 return Future.failedFuture (new ApiException (400,"INVALID_CABIN","Cabin class is not allowed"));
			  }

			  if(req.getPromoCode () != null) {
				  try {
					  PromoCodes.from (req.getPromoCode ());
				  } catch ( IllegalArgumentException e ) {
					 return Future.failedFuture (new ApiException (400,"INVALID_PROMOCODE","Promocode is not Valid"));
				  }
			  }



			return Future.succeededFuture (  );
		}
	}

