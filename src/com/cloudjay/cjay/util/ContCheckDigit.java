package com.cloudjay.cjay.util;

public class ContCheckDigit {

	public ContCheckDigit() {
	}

	private static int[] map = { 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
			34, 35, 36, 37, 38 };
	private static int[] weights = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512 };

	public static int getCRC(String id) {

		int[] digits = new int[10];
		try {
			for (int i = 0; i < 4; i++) {
				digits[i] = ContCheckDigit.getNumber(id.charAt(i));
			}

			for (int i = 4; i < 10; i++) {
				digits[i] = Integer.parseInt(id.substring(i, i + 1));
			}

		} catch (Exception e) {
			return -1;
		}

		for (int i = 0; i < 10; i++) {
			digits[i] = digits[i] * weights[i];
		}

		int total = 0;
		for (int i = 0; i < 10; i++) {
			total = total + digits[i];
		}

		return total % 11;
	}

	private static int getNumber(char c) {

		return map[Character.getNumericValue(c) - 10]; // 10 is the offset from the returning value of A to 0

	}
}
