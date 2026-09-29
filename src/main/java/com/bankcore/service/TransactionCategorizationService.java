package com.bankcore.service;

import com.bankcore.entity.TransactionCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionCategorizationService {

    private final ChatClient.Builder chatClientBuilder;

    public TransactionCategory categorize(String description) {

        if (description == null || description.isBlank()) {
            return TransactionCategory.OTHER;
        }

        String response = chatClientBuilder
                .build()
                .prompt()
                .system("""
                        You are a banking transaction categorization system.

                        Categorize the transaction description into exactly
                        ONE of these categories:

                        FOOD
                        SHOPPING
                        BILLS
                        TRANSPORT
                        ENTERTAINMENT
                        HEALTH
                        EDUCATION
                        TRANSFER
                        OTHER

                        Rules:

                        - FOOD:
                          Restaurants, food delivery, groceries, cafes,
                          meals, snacks, food orders.

                        - SHOPPING:
                          Online shopping, clothing, electronics,
                          retail purchases, Amazon, Flipkart, etc.

                        - BILLS:
                          Electricity, water, gas, internet, phone bills,
                          subscriptions and utility payments.

                        - TRANSPORT:
                          Uber, Ola, metro, bus, fuel, parking, taxis,
                          transportation-related payments.

                        - ENTERTAINMENT:
                          Movies, games, concerts, streaming entertainment,
                          events and similar activities.

                        - HEALTH:
                          Hospitals, doctors, pharmacies, medicines,
                          medical tests and healthcare.

                        - EDUCATION:
                          Courses, tuition, books for education,
                          educational institutions and training.

                        - TRANSFER:
                          Money transfers between bank accounts or
                          person-to-person transfers when the description
                          clearly indicates a transfer.

                        - OTHER:
                          Anything that does not clearly fit another category.

                        IMPORTANT:
                        Return ONLY the category name.
                        Do not return explanations.
                        Do not return markdown.
                        Do not return punctuation.
                        """)
                .user(description)
                .call()
                .content();

        return parseCategory(response);
    }

    private TransactionCategory parseCategory(String response) {

        if (response == null || response.isBlank()) {
            return TransactionCategory.OTHER;
        }

        String normalized =
                response.trim()
                        .toUpperCase()
                        .replaceAll("[^A-Z_]", "");

        try {
            return TransactionCategory.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return TransactionCategory.OTHER;
        }
    }
}