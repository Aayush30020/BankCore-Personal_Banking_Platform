package com.bankcore.controller;

import com.bankcore.service.AiBankingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiBankingService aiBankingService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @Valid @RequestBody AiChatRequest request
    ) {

        String response =
                aiBankingService.chat(request.message());

        return ResponseEntity.ok(
                new AiChatResponse(response)
        );
    }

    public record AiChatRequest(

            @NotBlank(message = "Message is required")
            @Size(
                    max = 2000,
                    message = "Message cannot exceed 2000 characters"
            )
            String message
    ) {}

    public record AiChatResponse(
            String response
    ) {}
}