# LangChain4j Agent Demo

基于 Java 21、Maven 和 LangChain4j 1.21.0 的多模块示例，演示模型调用、AI Service、自动工具调用和手动工具调用。四个 Demo 都有独立的 `main` 方法，问题写在代码中，无需控制台输入。

## 环境准备

- JDK 21
- Maven 3.9+（也可使用 IntelliJ IDEA 自带 Maven）
- 有效的 OpenAI 兼容 API Key；工具示例需要模型支持工具调用
- 可选：已构建的 `agent-security-javaagent.jar`，用于运行时安全拦截

## 配置模型

推荐通过运行进程的环境变量提供密钥。在 IDEA 的 Run Configuration → Environment variables 中设置 `OPENAI_API_KEY`，或在终端输入（以下适用于 zsh，输入不会显示）：

```sh
read -rs 'OPENAI_API_KEY?请输入 API Key: '
export OPENAI_API_KEY
```

公共配置位于 `agent-core/src/main/resources/openai.properties`，包含当前项目的 API 地址、模型、温度、超时和重试次数。换用其他服务时，可在运行配置中设置 `OPENAI_BASE_URL` 和 `OPENAI_MODEL`。

也可使用本地配置文件：

```sh
cp agent-core/src/main/resources/openai-local.properties.example \
   agent-core/src/main/resources/openai-local.properties
```

然后在本地文件中填写 `openai.api-key`，按需覆盖 `openai.base-url` 和 `openai.model`。已有本地配置时无需再次复制。

API Key、API 地址和模型的优先级为：**环境变量 > openai-local.properties > openai.properties**。其余参数由本地配置覆盖公共配置。

## 在 IntelliJ IDEA 中运行

1. 打开仓库根目录的 `pom.xml`，作为 Maven 项目导入。
2. 将 Project SDK 和运行配置的 JRE 设为 JDK 21，等待依赖加载完成。
3. 配置密钥后，打开 `agent-app` 中任意 Demo，点击 `main` 方法旁的运行按钮。
4. 如需手动创建 Application 配置，将 classpath 模块设为 `agent-app`，Main class 选择下表中的类（包名为 `com.example.agent.app`）。

| Main class | 演示内容 |
| --- | --- |
| `AiServiceDemo` | 接口代理和系统提示，适合首次验证模型连接 |
| `AiServiceToolsDemo` | 自动工具调用、会话记忆和多轮订单查询 |
| `ProgrammaticChatDemo` | 显式构造请求，输出回答和 Token 用量 |
| `ProgrammaticToolsDemo` | 手动分派工具、回填结果并循环调用模型 |

运行工具示例时会查询 `ORD-1001` 和 `ORD-1002` 并比较退款资格。示例工具只查询，不执行退款。修改各 Demo 中的问题字符串即可测试其他输入。

## 在命令行中运行

在仓库根目录执行以下命令，先构建并安装所有模块，再收集运行依赖：

```sh
mvn clean install
mvn -f agent-app/pom.xml dependency:copy-dependencies -DincludeScope=runtime
java -cp 'agent-app/target/classes:agent-app/target/dependency/*' \
  com.example.agent.app.AiServiceDemo
```

将最后一行替换为其他 Demo 的完整类名即可。以上 classpath 分隔符适用于 macOS/Linux；Windows 使用 `;`。运行前需在同一终端设置密钥，或准备好本地配置文件。

只做构建检查可执行 `mvn clean verify`；构建通过不代表模型服务连接成功。

## 挂载安全 Java Agent

将构建好的 Java Agent JAR 放到仓库根目录的 `tools/agent-security-javaagent.jar`（按需创建 `tools/` 目录）。JAR 已被 `.gitignore` 忽略，只保留在本机。

在 IDEA 的 Run Configuration 中将 **Working directory** 设为本仓库根目录，再通过 Modify options → Add VM options 添加以下参数（这是 JVM 参数）：

```text
-javaagent:tools/agent-security-javaagent.jar=agent-app/src/main/resources/security-policy.properties
```

以上路径均相对于运行进程的工作目录，不包含本机用户名或仓库外的目录结构。先在 `agent-security` 项目中构建 Java Agent，确认 JAR 和策略文件存在，再运行 Demo。

命令行中从本仓库根目录执行，将相同参数放在主类之前：

```sh
java \
  '-javaagent:tools/agent-security-javaagent.jar=agent-app/src/main/resources/security-policy.properties' \
  -cp 'agent-app/target/classes:agent-app/target/dependency/*' \
  com.example.agent.app.AiServiceToolsDemo
```

当前 `security-policy.properties` 配置了 `deny.tools=lookupOrder`，并包含文本拒绝规则和长度限制。因此挂载 Agent 后，订单工具示例可能被拦截，具体结果取决于 Agent 的实现。验证正常模型调用可先运行 `AiServiceDemo`；验证工具拦截可运行 `AiServiceToolsDemo` 或 `ProgrammaticToolsDemo`。调整策略后重新启动运行进程。

## 本地密钥与 Git

- 真实密钥放在环境变量或 `openai-local.properties` 中；公共配置及 `.example` 模板只保留空值或占位符。
- `.gitignore` 已忽略本地配置、`.env` 及其变体、证书私钥、IDE 文件、日志和 `target/` 构建产物。不要使用 `git add -f` 强制提交这些文件。
- 本地 properties 会被 Maven 复制到编译输出。需要分发产物时，先移走本地密钥文件，再执行干净构建，运行时改用环境变量。
- 模型工厂开启了请求日志；分享控制台输出前检查其中的业务内容和敏感信息。

提交前可检查：

```sh
git status --short --ignored
git diff --cached
git check-ignore agent-core/src/main/resources/openai-local.properties
```

忽略规则只保护匹配的未跟踪文件，不能撤回已提交的密钥。如果密钥曾被提交或分享，应撤销并更换密钥。

## 模块结构

| 模块 | 职责 |
| --- | --- |
| `agent-domain` | 订单模型和内存仓库 |
| `agent-tools` | 订单查询与退款资格工具 |
| `agent-core` | 模型配置、Agent 接口和装配 |
| `agent-app` | 四个独立运行入口及安全策略 |

依赖方向：`agent-app → agent-core → agent-tools → agent-domain`。
