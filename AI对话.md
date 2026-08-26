# AI对话核心概念

| 概念           | 说明         | 作用                     |
|----------------|--------------|--------------------------|
| `Chatmodel`    | chat模型     | 统一不同模型的调用方式   |
| `ChatClient`   | chat客户端   | 便于客户调用api客户端    |
| `Prompt`       | 给ai的提示词 | 封装客户输入以及系统指令 |
| `ChatResponse` | chat响应     | 提供标准化的响应格式     |
| `Flux`         | 流式         | 通过流的方式调用         |

## 完整流程
    （1）构建 prompt
    （2）通过 chatClient 调用 ChatModel
    （3）返回 chatResponse

