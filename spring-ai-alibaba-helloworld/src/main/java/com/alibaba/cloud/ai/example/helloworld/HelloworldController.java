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

package com.alibaba.cloud.ai.example.helloworld;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */
@RestController
@RequestMapping("/helloworld")
public class HelloworldController {

    private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";

    private final ChatClient dashScopeChatClient;

    // 也可以使用如下的方式注入 ChatClient
    public HelloworldController(ChatClient.Builder chatClientBuilder) {

        this.dashScopeChatClient = chatClientBuilder
                //使用预设的系统提示语；
                .defaultSystem(DEFAULT_PROMPT)
                //具备基于窗口的消息记忆功能，支持多轮对话；
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder().build()
                        ).build())
                //能够记录对话日志；logging:
                //  level:
                //    org:
                //      springframework:
                //        ai:
                //          chat:
                //            client:
                //              advisor: DEBUG  开启日志可以查看到这个Advisors是记录日志的
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数在生成回复时应用特定的
                .defaultOptions(
                        //  在生成回复时应用特定的 top-p 采样策略以平衡创造力和准确性
                        DashScopeChatOptions.builder().withTopP(0.7)
                                .build()
                )
                .build();
    }

    /**
     * ChatClient 简单调用
     */
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query) {

        return dashScopeChatClient.prompt(query).call().content();
    }

    /**
     * ChatClient 流式调用
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query, HttpServletResponse response) {

        response.setCharacterEncoding("UTF-8");
        return dashScopeChatClient.prompt(query)
//               每次调用时手动设置options
                .options(DashScopeChatOptions.builder().withTopP(0.8).build())
                .stream().content();
    }

    /**
     * ChatClient 使用自定义的 Advisor 实现功能增强.
     * eg:
     * http://127.0.0.1:18080/helloworld/advisor/chat/123?query=你好，我叫jack，之后的会话中都带上我的名字
     * 你好，jack！很高兴认识你。在接下来的对话中，我会记得带上你的名字。有什么想聊的吗？
     * http://127.0.0.1:18080/helloworld/advisor/chat/123?query=我叫什么名字？
     * 你叫jack呀。有什么事情想要分享或者讨论吗，jack？
     * <p>
     * refer: https://docs.spring.io/spring-ai/reference/api/chat-memory.html#_memory_in_chat_client
     */
    @GetMapping("/advisor/chat/{conversationId}")
    public Flux<String> advisorChat(
            HttpServletResponse response,
            @PathVariable String conversationId,
            @RequestParam String query
    ) {

        response.setCharacterEncoding("UTF-8");

        return this.dashScopeChatClient.prompt(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)
                ).stream().content();
    }

}
