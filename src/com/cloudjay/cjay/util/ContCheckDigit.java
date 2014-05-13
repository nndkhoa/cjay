package com.cloudjay.cjay.util;

/** * * @author alex */
public class ContCheckDigit {
	/** Creates a new instance of ContCheckDigit */
	public ContCheckDigit() {
	}

	private static int[] map = { 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
			34, 35, 36, 37, 38 };
	private static int[] weights = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512 };

	/** * @param args the command line arguments */
	// public static void main(String[] args) { System.out.println( "CRC: " + getCRC("AAAB000001")); }
	public static int getCRC(String id) {
		int[] digits = new int[10];
		try {
			for (int i = 0; i < 4; i++) {

				digits[i] = ContCheckDigit.getNumber(id.charAt(i));
				// System.out.println(digits[i]);

			}
			for (int i = 4; i < 10; i++) {
				digits[i] = Integer.parseInt(id.substring(i, i + 1));
				// System.out.println(digits[i]);
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
		// System.out.println(Character.getNumericValue(c));
		return map[Character.getNumericValue(c) - 10]; // 10 is the offset from the returning value of A to 0
	}
}
