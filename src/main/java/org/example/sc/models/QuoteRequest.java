package org.example.sc.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteRequest {
	private double fareAmount;
	private String currency;
	private String cabinClass;
	private String customerTier;
	private String promoCode;
}
