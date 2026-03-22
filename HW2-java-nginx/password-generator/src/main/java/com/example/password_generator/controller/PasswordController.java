package com.example.password_generator.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PasswordController {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIALS = "#.,!@&^%*";

    private final Random random = new Random();

    @GetMapping("/")
    public String generatePassword() throws InterruptedException {
        int length = 8 + random.nextInt(9);

        List<Character> passwordChars = new ArrayList<>();

        passwordChars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        passwordChars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        passwordChars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        passwordChars.add(SPECIALS.charAt(random.nextInt(SPECIALS.length())));

        String allChars = LOWER + UPPER + DIGITS + SPECIALS;

        for (int i = 4; i < length; i++) {
            passwordChars.add(allChars.charAt(random.nextInt(allChars.length())));
        }

        Collections.shuffle(passwordChars, random);

        StringBuilder sb = new StringBuilder();
        for (char c : passwordChars) {
            sb.append(c);
        }

        Thread.sleep(50);

        return sb.toString();
    }
}