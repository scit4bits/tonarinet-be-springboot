package org.scit4bits.tonarinetserver.service;

import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.repository.ChatMessageRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

/**
 * AI 관련 서비스를 처리하는 클래스
 */
@Service
@Slf4j
public class AIService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatClient chatClient;
    private final MessageWindowChatMemory chatMemory;

    // AI의 행동을 정의하는 기본 시스템 프롬프트
    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant for Tonarinet application.
            In Korean it's "토나리넷". In Japanese it's "トナリネット".
            Tonarinet is supporting service for the students study aboard and workers in foreign countries.
            You should be friendly, professional, and provide accurate information.
            Keep your responses concise and helpful.
            Keep your responses not far away from the context of service of supporting foreign people. (like helping with visa, job, accommodation, language barrier, cultural differences, etc.)
            If you don't know something, admit it rather than guessing.
            if possible, respond in the language of the user's request.
            """;

    private final OpenAiChatModel chatModel;

    /**
     * AIService 생성자
     * @param chatClientBuilder ChatClient 빌더
     * @param chatMessageRepository 채팅 메시지 레포지토리
     * @param chatModel OpenAI 채팅 모델
     */
    public AIService(ChatClient.Builder chatClientBuilder, ChatMessageRepository chatMessageRepository,
                     OpenAiChatModel chatModel) {
        this.chatMemory = MessageWindowChatMemory.builder().maxMessages(20).build();
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.chatMessageRepository = chatMessageRepository;
        this.chatModel = chatModel;
    }

    /**
     * 사용자 입력에 대한 AI 응답을 생성합니다.
     * @param userInput 사용자 입력 문자열
     * @return 생성된 AI 응답 문자열
     */
    public String generateResponse(String userInput) {
        try {
            log.debug("Generating AI response for input: {}", userInput);
            // 시스템 프롬프트와 사용자 입력을 포함하는 프롬프트 생성
            Prompt prompt = new Prompt(
                    new SystemMessage(SYSTEM_PROMPT),
                    new UserMessage(userInput));
            // AI 모델 호출
            ChatResponse response = chatModel.call(prompt);
            String result = response.getResult().getOutput().getText();
            log.debug("AI response generated successfully");
            return result;
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to generate AI response", e);
        }
    }

    /**
     * 대화 기록을 포함하여 AI 응답을 생성합니다.
     * @param userInput 사용자 입력 문자열
     * @param roomId 채팅방 ID
     * @return 생성된 AI 응답 문자열
     */
    public String generateResponseWithMemory(String userInput, Integer roomId) {
        try {
            // 대화 기록을 사용하여 AI 응답 생성
            String aiResponse = chatClient.prompt()
                    .user(userInput)
                    .system(SYSTEM_PROMPT)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, roomId.toString()))
                    .call()
                    .content();

            return aiResponse;
        } catch (Exception e) {
            log.error("Error calling OpenAI API with memory", e);
            throw new RuntimeException("Failed to generate AI response with memory", e);
        }
    }

    /**
     * HTML 형식으로 AI 응답을 생성합니다.
     * @param userInput 사용자 입력 문자열
     * @return HTML 형식의 AI 응답 문자열
     */
    public String generateHTMLResponse(String userInput) {
        String prompt = """
                Please provide your answer in HTML format. Use appropriate HTML tags such as <p>, <ul>, <li>, <strong>, <em>, and <br> to structure your response. Avoid using complex HTML structures like <div> or <span>. Ensure that the HTML is well-formed and easy to read.
                """;

        try {
            // HTML 형식으로 응답을 요청하는 프롬프트를 추가하여 AI 호출
            String aiResponse = chatClient.prompt()
                    .user(userInput)
                    .system(SYSTEM_PROMPT + "\n" + prompt)
                    .call()
                    .content();

            return aiResponse;
        } catch (Exception e) {
            log.error("Error calling OpenAI API with memory", e);
            throw new RuntimeException("Failed to generate AI response with memory", e);
        }
    }
}
