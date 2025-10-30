

- AI_DASHSCOPE_API_KEY  配置在环境变量中
- temperature 参数 值范围一般在 0～1，部分模型会大于 1
  - 值越低输出越确定（0，代表每次相同输入产生相同输出）
  - 值越高随机性越强（产生更开放或不常见的回答，适用于创意写作等场景）
```js
AdvisorChain 链调用一系列的增强器Advisor，每个增强器输入是 ChatClientRequest，
输出 ChatClientResponse（其中必定会用到的是 ChatModelCallAdvisor 或 ChatModelStreamAdvisor）
```

- 文档  https://java2ai.com/docs/1.0.0.2/spring-ai-sourcecode-explained/chapter-1-chat-first-experience/
- chat初体验