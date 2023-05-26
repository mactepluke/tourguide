package com.tripmaster.tourguide.helper;

public class InternalTestHelper {
	// Set this default up to 100,000 for testing
	private static int internalUserNumber = 100;

	private InternalTestHelper()	{
	}
	
	public static void setInternalUserNumber(int internalUserNumber) {
		InternalTestHelper.internalUserNumber = internalUserNumber;
	}
	
	public static int getInternalUserNumber() {
		return internalUserNumber;
	}
}
