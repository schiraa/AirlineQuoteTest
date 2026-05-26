package org.example.sc.TestData;

import io.vertx.core.json.JsonObject;
import org.example.sc.constants.Constants;
import org.example.sc.models.QuoteTestData;
import java.util.stream.Stream;

public class TestDataGenerator {

	public static JsonObject validRequest() {
		return new JsonObject (  )
				.put(Constants.FARE_AMOUNT, 8000.00)
				.put(Constants.CURRENCY, "USD")
				.put(Constants.CABIN_CLASS, "ECONOMY")
				.put(Constants.CUSTOMER_TIER, "GOLD")
				.put(Constants.PROMO_CODE, "SUMMER25");
	}

	public static Stream<QuoteTestData> getEdgeCasesTestData() {
		return Stream.of(
				new QuoteTestData (10415.50, "USD", "ECONOMY", "GOLD", "SUMMER25",49999),
				new QuoteTestData (80000.00, "USD", "BUSINESS", "NONE", "SUMMER25",50000),
				new QuoteTestData (5000.00, "USD", "FIRST_CLASS", "SILVER", "SUMMER25",21410),
				new QuoteTestData (9030.80, "USD", "ECONOMY", "PLATINUM", "SUMMER25", 50000)
		);
	}

	public static JsonObject InvalidRequest(String field, String condition) {
		return new JsonObject (  )
				.put(Constants.FARE_AMOUNT, field.equalsIgnoreCase (Constants.FARE_AMOUNT)?getInvalidFareAmount (condition) : 8000.00 )
				.put(Constants.CURRENCY,field.equalsIgnoreCase (Constants.CURRENCY)?getInvalidStringValues (condition) : "USD")
				.put(Constants.CABIN_CLASS, field.equalsIgnoreCase (Constants.CABIN_CLASS)?getInvalidStringValues (condition): "ECONOMY")
				.put(Constants.CUSTOMER_TIER, field.equalsIgnoreCase (Constants.CUSTOMER_TIER)?getInvalidStringValues (condition):"GOLD")
				.put(Constants.PROMO_CODE,field.equalsIgnoreCase (Constants.PROMO_CODE)?getInvalidStringValues (condition):"SUMMER25");
	}

	public static Stream<JsonObject> getUnHappyPathTestData() {
		return Stream.of (TestDataGenerator.InvalidRequest (Constants.FARE_AMOUNT, "zero"),
				TestDataGenerator.InvalidRequest (Constants.FARE_AMOUNT, "Negative"),
				TestDataGenerator.InvalidRequest (Constants.CURRENCY, "Invalid"),
				TestDataGenerator.InvalidRequest (Constants.CURRENCY, "blank"),
				TestDataGenerator.InvalidRequest (Constants.CABIN_CLASS, "Invalid"),
				TestDataGenerator.InvalidRequest (Constants.CABIN_CLASS, "blank"),
				TestDataGenerator.InvalidRequest (Constants.CUSTOMER_TIER, "Invalid"),
				TestDataGenerator.InvalidRequest (Constants.CUSTOMER_TIER, "blank"),
				TestDataGenerator.InvalidRequest (Constants.PROMO_CODE, "Invalid"),
				TestDataGenerator.InvalidRequest (Constants.PROMO_CODE, "blank"),
				TestDataGenerator.getJSonWithMissingFields (Constants.FARE_AMOUNT),
				TestDataGenerator.getJSonWithMissingFields (Constants.CURRENCY),
				TestDataGenerator.getJSonWithMissingFields (Constants.CABIN_CLASS),
				TestDataGenerator.getJSonWithMissingFields (Constants.CUSTOMER_TIER)
		);
	}

	public static Stream<QuoteTestData> getHappyPathTestData() {
		return Stream.of (
				new QuoteTestData (8000,"USD","ECONOMY","GOLD", "SUMMER25",38476),
				new QuoteTestData (8000,"JPY","BUSINESS","NONE", "SUMMER25",10308),
				new QuoteTestData (8000,"USD","FIRST_CLASS","SILVER", "SUMMER25",34072),
				new QuoteTestData (8000,"USD","ECONOMY","PLATINUM", "SUMMER25",44348)

		);
	}

	public static JsonObject getJSonWithMissingFields(String field) {
		var json = validRequest ();
		 json.remove (field);
		 return json;
	}

	public static double getInvalidFareAmount( String condition) {
	       double val = 0.00;
				 if ( condition.equalsIgnoreCase ("Negative") ) {
                   val = -100.00;
				 } else if(condition.equalsIgnoreCase ("zero")) {
					 val = 0.00;
				 }
				 return val;
	}

	public static String getInvalidStringValues( String condition) {
		String val = "";

		if(condition.equalsIgnoreCase ("Invalid")) {
			val = "JJJ";
		} else if(condition.equalsIgnoreCase ("blank")) {
			val = "";
		}
		return val;
	}


}
