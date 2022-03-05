package com.example.springboot.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class URLChecker {
    public static boolean isURL(String URL) { // to prevent misspelling URLs and the probable errors that we could get due to wrong URL
        try {
            java.net.URL tempURL = new URL(URL);
            return true;
        } catch (MalformedURLException e) {
            System.out.println("Given URL is not valid. Please check and retype.");
            return false;
        }
    }
}
