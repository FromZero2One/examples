/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.example.local;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@SpringBootApplication
public class ChatClientOrChatModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatClientOrChatModelApplication.class, args);
    }

    private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";

    /**
     * chatModel 的实现
     *
     * @return
     */
    @Bean("chatModel")
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder().apiKey(System.getenv("AI_DASHSCOPE_API_KEY")).build();
    }

    /**
     * chatClient 的实现
     *
     * @param model
     * @return
     */
    @Bean("chatClient")
    public ChatClient dashScopeChatClient(ChatModel model) {
        return ChatClient.builder(model)
                //使用预设的系统提示语；
                .defaultSystem(DEFAULT_PROMPT)
                //具备基于窗口的消息记忆功能，支持多轮对话；
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder().build()
                        ).build())
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数在生成回复时应用特定的
                .defaultOptions(
                        //  在生成回复时应用特定的 top-p 采样策略以平衡创造力和准确性
                        DashScopeChatOptions.builder().withTopP(0.7)
                                .build()
                )
                .build();
    }

}
