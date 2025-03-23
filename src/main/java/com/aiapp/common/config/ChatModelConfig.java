package com.aiapp.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatModelConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(
                        getSimpleLoggerAdvisor()        // Logging: logging.level.org.springframework.ai.chat.client.advisor=DEBUG
                )
                .build();
    }

    private SimpleLoggerAdvisor getSimpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor(
                request -> "Custom request: " + request.userText(),
                response -> "Custom response: " + response.getResult(),
                0
        );
    }
}
