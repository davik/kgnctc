package com.quickml.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Currency {

public static String convertToIndianCurrency(double amount) {
        
        int whole_number = (int)(amount);
        int temp_whole_number = whole_number;
        int decimal = (int) ((amount-whole_number) * 100);
        int digits_length = String.valueOf(temp_whole_number).length();
        int i = 0;
        ArrayList<String> str = new ArrayList<>();
        HashMap<Integer, String> words = new HashMap<>();
        words.put(0, "");
        words.put(1, "One");
        words.put(2, "Two");
        words.put(3, "Three");
        words.put(4, "Four");
        words.put(5, "Five");
        words.put(6, "Six");
        words.put(7, "Seven");
        words.put(8, "Eight");
        words.put(9, "Nine");
        words.put(10, "Ten");
        words.put(11, "Eleven");
        words.put(12, "Twelve");
        words.put(13, "Thirteen");
        words.put(14, "Fourteen");
        words.put(15, "Fifteen");
        words.put(16, "Sixteen");
        words.put(17, "Seventeen");
        words.put(18, "Eighteen");
        words.put(19, "Nineteen");
        words.put(20, "Twenty");
        words.put(30, "Thirty");
        words.put(40, "Forty");
        words.put(50, "Fifty");
        words.put(60, "Sixty");
        words.put(70, "Seventy");
        words.put(80, "Eighty");
        words.put(90, "Ninety");
        String digits[] = {"", "Hundred", "Thousand", "Lakh", "Crore"};
        while (i < digits_length) {
            int divider = (i == 2) ? 10 : 100;
            whole_number = temp_whole_number % divider;
            temp_whole_number = temp_whole_number / divider;
            i += divider == 10 ? 1 : 2;
            if (whole_number > 0) {
                int counter = str.size();
                String plural = (counter > 0 && whole_number > 9) ? "s" : "";
                String tmp = (whole_number < 21) ? words.get(Integer.valueOf((int) whole_number)) + " " + digits[counter] + plural : words.get(Integer.valueOf((int) Math.floor(whole_number / 10) * 10)) + " " + words.get(Integer.valueOf((int) (whole_number % 10))) + " " + digits[counter] + plural;                
                str.add(tmp);
            } else {
                str.add("");
            }
        }
 
        Collections.reverse(str);
        String Rupees = String.join(" ", str).trim();
 
        String paise = (decimal) > 0 ? " and " + words.get(Integer.valueOf((int) (decimal - decimal % 10))) + " " + words.get(Integer.valueOf((int) (decimal % 10))) + " Paisa" : "";
        return "Rupees " + Rupees + paise + " Only";
    }
}