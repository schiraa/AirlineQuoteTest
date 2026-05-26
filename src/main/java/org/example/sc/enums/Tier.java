package org.example.sc.enums;

public enum Tier {
	NONE(0.0),
	SILVER(0.15),
	GOLD(0.30),
	PLATINUM(0.50);

	private final double bonus;

	Tier(double bonus) {
		this.bonus = bonus;
	}

	public double bonus() {
		return bonus;
	}

	public static Tier from(String value) {
		return Tier.valueOf(value.toUpperCase());
	}
}
