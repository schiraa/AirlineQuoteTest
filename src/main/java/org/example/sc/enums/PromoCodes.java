package org.example.sc.enums;

public enum PromoCodes {
	SUMMER25,
	WINTER25,
	CHRISTMAS40,
	NEWYEAR50;

	public static PromoCodes from(String value) {
		return PromoCodes.valueOf (value.toUpperCase ());
	}
}
