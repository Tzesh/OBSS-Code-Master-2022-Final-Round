package com.example.springboot.utils;

import org.jsoup.Connection;

import java.lang.reflect.Field;

public class RedirectChecker {
    public static int getRedirectsNumber(Connection.Response response)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = response.getClass().getDeclaredField("numRedirects");
        field.setAccessible(true);

        return (int) field.get(response);
    }
}
