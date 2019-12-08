package ee.taltech.iti0202.idcode;

public class IDCode {

	public static final int ZERO = 0;
	private static final int YEAR_LOCATION = 3;
	private static final int MONTH_LOCATION = 5;
	private static final int DAY_LOCATION = 7;
	private static final int ID_CODE_LENGTH = 11;
	private static final int MONTHS_IN_A_YEAR = 12;
	private static final int LEAP_FEB = 29;
	private static final int NO_LEAP_FEB = 28;
	private static final int DAYS_IN_JAN = 31;
	private static final int DAYS_IN_APRIL = 30;
	private static final int[] MULTIPLIERS1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1};
	private static final int[] MULTIPLIERS2 = {3, 4, 5, 6, 7, 8, 9, 1, 2, 3};
	private static final int LEAP_MAX = 400;
	private static final int YEAR_MAX = 99;
	private static final int QUEUE_MAX = 999;
	private static final int EPOCH_YEAR = 1700;

	public static boolean isIDCodeCorrect(String idCode) {

		return idCode.length() == ID_CODE_LENGTH && idCode.matches("[0-9]+")
				&& isGenderNumberCorrect(idCode) && isYearNumberCorrect(idCode)
				&& isMonthNumberCorrect(idCode) && isDayNumberCorrect(idCode)
				&& isQueueNumberCorrect(idCode) && isControlNumberCorrect(idCode);

	}

	private static boolean isGenderNumberCorrect(String idCode) {
		return idCode.substring(0, 1).matches("^[1-6]$");
	}

	private static boolean isYearNumberCorrect(String idCode) {
		int year = Integer.parseInt(idCode.substring(YEAR_LOCATION - 2, YEAR_LOCATION));
		return year >= 0 && year <= YEAR_MAX;
	}

	private static boolean isMonthNumberCorrect(String idCode) {
		int month = Integer.parseInt(idCode.substring(YEAR_LOCATION, MONTH_LOCATION));
		return month > 0 && month <= MONTHS_IN_A_YEAR;
	}

	private static boolean isDayNumberCorrect(String idCode) {
		int year = getFullYear(idCode);
		boolean leap = isLeapYear(year);
		String month = idCode.substring(YEAR_LOCATION, MONTH_LOCATION);
		int day = Integer.parseInt(idCode.substring(MONTH_LOCATION, DAY_LOCATION));
		if (Integer.parseInt(month) == 2) {
			if (leap) {
				return day > 0 && day <= LEAP_FEB;
			} else {
				return day > ZERO && day <= NO_LEAP_FEB;
			}
		} else if (month.equals("04") || month.equals("06") || month.equals("09") || month.equals("11")) {
			return day > 0 && day <= DAYS_IN_APRIL;
		} else {
			return day > 0 && day <= DAYS_IN_JAN;
		}

	}

	private static boolean isQueueNumberCorrect(String idCode) {
		int queue = Integer.parseInt(idCode.substring(DAY_LOCATION, 10));
		return queue > 0 && queue <= QUEUE_MAX;
	}

	private static boolean isControlNumberCorrect(String idCode) {
		int controlNumber = 0;
		for (int i = 0; i < 10; i++) {
			controlNumber += MULTIPLIERS1[i] * Character.getNumericValue(idCode.charAt(i));
		}
		controlNumber %= ID_CODE_LENGTH;
		if (controlNumber == 10) {
			controlNumber = 0;
			for (int i = 0; i < 10; i++) {
				controlNumber += MULTIPLIERS2[i] * Character.getNumericValue(idCode.charAt(i));
			}
			controlNumber %= ID_CODE_LENGTH;
			if (controlNumber == 10) {
				controlNumber = 0;
			}
		}

		return controlNumber == Integer.parseInt(idCode.substring(10));
	}

	private static boolean isLeapYear(int fullYear) {
		if (fullYear % LEAP_MAX == 0) {
			return true;
		} else if (fullYear % 100 == 0) {
			return false;
		}
		return fullYear % 4 == 0;
	}

	public static String getInformationFromIDCode(String idCode) {
		if (!isIDCodeCorrect(idCode)) {
			return "Given invalid ID code!";
		}
		return "This is a " + getGender(idCode).toString().toLowerCase() + " born on "
				+ idCode.substring(5, DAY_LOCATION) + "." + idCode.substring(3, 5) + "." + getFullYear(idCode);
	}

	public static Gender getGender(String idCode) {
		String genderNumber = idCode.substring(0, 1);
		if (genderNumber.matches("^[1|3|5]$")) {
			return Gender.MALE;
		}
		return Gender.FEMALE;
	}

	public static int getFullYear(String idCode) {
		return Integer.parseInt(idCode.substring(1, 3)) + EPOCH_YEAR
				+ ((Integer.parseInt(idCode.substring(0, 1)) + 1) / 2) * 100;
	}

	public static void main(String[] args) {

	}

	private enum Gender {
		MALE, FEMALE
	}
}
