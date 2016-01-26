package com.cc.eventcalendar.calendarview.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtil {
	public static String EMPTYSTRING = "";

	public static String NULLSTRING = "null";

	public static Pattern EMAIL_REGULAR = Pattern
			.compile("^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$");

	public static Pattern PASSWORD_REGULAR = Pattern
			.compile("^.*(?=.{6,})(?=.*[a-zA-Z])(?=.*\\d).*$");

	/**
	 * Check whether the specified string is a empty string.
	 * 
	 * @param aStr
	 *            The string to be checked.
	 * @return true if the string is a empty string or null, otherwise false.
	 */
	public static boolean isEmpty(String aStr) {
		return aStr == null || aStr.equals(EMPTYSTRING);
	}

    /**
     * Check whether the specified string is a empty string or only contains blank space.
     *
     * @param aStr
     *            The string to be checked.
     * @return true if the string is a empty string or null, otherwise false.
     */
    public static boolean isEmptyWithoutBlank(String aStr) {
        return aStr == null || aStr.trim().equals(EMPTYSTRING);
    }

	/**
	 * Check the specified string whether is a email.
	 * 
	 * @param email
	 *            the specified string.
	 * @return true if the specified string is a email otherwise false.
	 */
	public static boolean isEmail(String email) {
		if (isEmpty(email)) {
			return false;
		}

		Matcher m = EMAIL_REGULAR.matcher(email);

		return m.matches();
	}

	/**
	 * Convert a {@link String} object to a {@linkplain InputStream}
	 * 
	 * @param aStr
	 *            the string resource.
	 * @return a {@linkplain InputStream} object that convert from the string.
	 */
	public static InputStream str2InputStream(String aStr) {
		ByteArrayInputStream is = null;
		if (isEmpty(aStr)) {
			return null;
		}
		try {
			is = new ByteArrayInputStream(aStr.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return is;
	}

	/**
	 * Make sure the string not null point if the specified string is null
	 * return {@linkplain #EMPTYSTRING}
	 * 
	 * @param aStr
	 *            the string.
	 * @return the string self if it is not null, otherwise return
	 *         {@linkplain #EMPTYSTRING}.
	 */
	public static String strNotNull(String aStr) {
		if (aStr == null) {
			return EMPTYSTRING;
		}

		return aStr;
	}

	public static boolean numberString2Boolean(String numStr) {
		if (isEmpty(numStr)) {
			return false;
		}

		return !numStr.equals("0");

	}

	public static boolean isValidPassword(String password) {
		Matcher m = PASSWORD_REGULAR.matcher(password);
		return m.matches();
	}

	public static String convertUrl(String urlStr) {
		if (StrUtil.isEmpty(urlStr)) {
			return urlStr;
		}
		try {
			URL url = new URL(urlStr);
			URI uri = new URI(url.getProtocol(), url.getUserInfo(),
					url.getHost(), url.getPort(), url.getPath(),
					url.getQuery(), url.getRef());
			return uri.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return urlStr;
		}
	}
}
