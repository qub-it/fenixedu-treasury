package org.fenixedu.treasury.util;

import com.google.common.base.Strings;

public class FiscalCodeValidation {

    public static boolean isValidFiscalNumber(final String countryCode, final String fiscalNumber) {
        return (!Strings.isNullOrEmpty(countryCode) && !Constants.isDefaultCountry(countryCode))
                || FiscalCodeValidation.isValidcontrib(countryCode, fiscalNumber);
    }

    public static boolean isValidcontrib(final String countryCode, final String contrib) {
        boolean functionReturnValue = false;
        functionReturnValue = false;
        int i = 0;
        long checkDigit = 0;

        if (contrib.length() == 9) {
            int numericValue = Character.getNumericValue(contrib.charAt(0));
            if (contrib.charAt(0) == '1' || contrib.charAt(0) == '2' || contrib.charAt(0) == '5' || contrib.charAt(0) == '6'
                    || contrib.charAt(0) == '9') {
                checkDigit = numericValue * 9;
                for (i = 2; i <= 8; i++) {
                    checkDigit = checkDigit + (Character.getNumericValue(contrib.charAt(i - 1)) * (10 - i));
                }
                checkDigit = 11 - (checkDigit % 11);
                if ((checkDigit >= 10))
                    checkDigit = 0;
                if ((checkDigit == Character.getNumericValue(contrib.charAt(8))))
                    functionReturnValue = true;
            }
        }
        return functionReturnValue;
    }
}
