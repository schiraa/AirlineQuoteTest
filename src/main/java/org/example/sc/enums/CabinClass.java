package org.example.sc.enums;

public enum CabinClass {
	ECONOMY,
	BUSINESS,
	FIRST_CLASS;

	public static CabinClass from(String value) {
		return CabinClass.valueOf(value.toUpperCase());

	}

	}
