package org.example.sc.models;

public class QuoteTestData extends QuoteRequest{
	public int totalPoints;

	public QuoteTestData(double fareAmount, String currency, String cabinClass,String customerTier,String promoCode,int totalPoints) {
		super(fareAmount,currency,cabinClass,customerTier,promoCode);
		this.totalPoints = totalPoints;
	}

	public int getTotalPoints() {
		return this.totalPoints;
	}

}
