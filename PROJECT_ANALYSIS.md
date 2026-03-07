# websocket-demo 项目分析

## 1. 项目定位与技术栈
- 这是一个基于 **Spring Boot + WebSocket + MyBatis + MySQL** 的实时多人投票系统，前端为静态 HTML + 原生 JS。 
- WebSocket 端点是 `/ws`，通过 `roomId` query 参数区分房间。 

## 2. 核心架构

### 2.1 WebSocket 实时投票链路
1. 浏览器连接 `/ws?roomId=xxx`。
2. `VoteWebSocketHandler` 在连接建立时把会话加入内存列表，并增加房间在线人数。
3. 客户端发送 `vote/start/stop` 指令。
4. `VoteService` 将业务路由到对应 `VoteRoom`（按 roomId 维护）。
5. 服务端将 `ResultMessage` 广播给同房间所有在线会话。

该链路完全基于内存状态（`ConcurrentHashMap<String, VoteRoom>`），重启后状态会丢失。

### 2.2 登录注册链路
- `AuthController` 提供 `/login` 和 `/register` 两个表单接口。
- `UserService` 通过 MyBatis `UserMapper` 查询/写入用户。
- 登录成功后把 `user` 放入 `HttpSession`。

## 3. 主要优点
- 结构清晰：控制层（controller）与投票业务（service/model）拆分明确。
- 房间隔离逻辑直观：以 `roomId` 为键组织投票状态。
- 协议简单：`command + option` 模式便于前端快速接入。

## 4. 关键问题与风险

### 4.1 编译/运行阻断问题（高优先级）
1. **`UserMapper` 的类型与包名不一致**：接口引用 `com.example.websocketdemo.entity.User`，但项目实际 User 在 `model` 包。
2. **`UserService` 调用方法名不一致**：调用 `userMapper.insert(user)`，接口/映射是 `insertUser`。
3. **MyBatis 别名包配置不一致**：`type-aliases-package` 指向 `entity` 包，实际模型在 `model` 包。

> 这些问题会导致应用在编译或启动阶段失败，需先修复。

### 4.2 安全问题（高优先级）
- 密码明文存储/比对（无哈希）。
- 数据库账号密码写死在配置文件。
- WebSocket `setAllowedOrigins("*")` 过宽。
- `start/stop` 依赖 session 的 `role`，但当前 WebSocket 会话里未看到把登录角色注入 attributes 的可靠逻辑。

### 4.3 并发与稳定性问题（中优先级）
- `VoteRoom` 内部使用 `HashMap/HashSet/int`，在并发 WebSocket 访问下不是线程安全。
- `VoteWebSocketHandler` 的 `sessions` 用 `ArrayList`，并发连接/断开/广播存在竞态风险。
- 大量空 `catch` 会吞掉关键异常，排障困难。

### 4.4 协议与边界处理问题（中优先级）
- `roomId` 解析采用 `query.split("=")[1]`，无法处理多个参数或 URL 编码。
- `option` 无合法性校验（非 1/2/3 可能触发 NPE）。
- 前端固定 `ws://localhost:8080`，部署到非本机或 HTTPS 下会有兼容问题。

### 4.5 可测试性问题（中优先级）
- 当前仅有空的 `contextLoads` 测试。
- WebSocket 投票流程、重复投票、防并发等均无自动化测试覆盖。

## 5. 建议的演进路线

### 阶段 A（先保证能跑）
1. 统一 `User` 包路径与 Mapper 方法名。
2. 修正 MyBatis `type-aliases-package`。
3. 增加最小集成测试，至少覆盖登录/注册基本路径。

### 阶段 B（提升稳定性）
1. 将 `VoteRoom` 改为线程安全实现（原子计数 + 并发集合或显式锁）。
2. 会话容器改为并发安全结构（如 `CopyOnWriteArrayList` 或分房间 session map）。
3. 补充参数校验与统一异常处理。

### 阶段 C（生产化）
1. 使用密码哈希（BCrypt）。
2. 收敛 CORS/Origin 策略并接入鉴权（JWT 或 Spring Security）。
3. 如需多实例部署，引入 Redis Pub/Sub 或消息队列做跨节点广播与状态共享。

## 6. 结论
该项目作为课程/毕业设计原型很合适，功能路径完整、上手成本低；但要达到稳定可上线水平，需要优先解决 **编译一致性 + 并发安全 + 基础安全** 三类问题。完成阶段 A/B 后，系统质量会有明显提升。
