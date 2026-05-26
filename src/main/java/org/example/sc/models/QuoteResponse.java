package org.example.sc.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuoteResponse {

	private int basePoints;
	private int tierBonus;
	private int promoBonus;
	private int totalPoints;
	private double effectiveFxRate;
	private List<String> warnings;
}
