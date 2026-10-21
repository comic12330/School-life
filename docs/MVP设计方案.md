# 基于安卓的校园生活服务系统 —— 设计说明（课程作业级）

> 定位：**课程期末作业**。目标是能跑通、能演示、代码量适中、报告能截图，不做企业级工程化。
> 版本：v3（2026-10-19）

---

## 1. 需求与范围

**题目**：基于安卓的校园生活服务系统的设计与实现。提供基于安卓手机移动端的校园卡账户查询、课表查询等服务，主要包括：

1. **校园卡账户管理**：校园卡账号的查询和挂失；
2. **教务系统查询**：获取学生各类学业信息；
3. **天气预报查询**：连接国内天气预报服务器获取天气信息；
4. **我的课表管理**：个人课表动态管理，并提供闹钟提醒。

| 决策项 | 结论 |
| --- | --- |
| 语言 | Java（安卓端 + 后端同语言） |
| 安卓界面 | XML 布局 + Fragment + 底部导航（4 个 Tab） |
| 数据库 | **MySQL 8.0**（本机已有），库名 `school_life` |
| 后端 | **Spring Boot 2.7.18**（本机 JDK 只有 11，3.x 需 Java 17） |
| 数据来源 | 校园卡/教务/课表存 MySQL，后端出接口；天气 App 直连国内 API |
| 登录 | 做登录、不做注册；预置 2 个账号。**不做 token，登录后把学号存本地，后续接口带学号** |
| 游客 | 仅天气公开；校园卡/教务/课表需登录 |
| 校园卡 | 仅余额 + 挂失/解挂 + 流水（不做充值、不做图表） |
| 课表 | 支持单双周；不做调课规则 |
| 提醒 | AlarmManager 本地通知（数据从后端取） |

**运行前提**：演示个人功能前要先把后端跑起来；后端没启动时页面提示「无法连接服务器」，天气不受影响。

---

## 2. 环境（本机实测）

| 项 | 结果 |
| --- | --- |
| JDK | 11.0.18（`D:\My Java\jdk 11`）→ 后端用 **Spring Boot 2.7.18** |
| Maven | `D:\maven\apache-maven-3.6.1`，直接用本机 `mvn`（不搞 Wrapper） |
| MySQL | 服务 `MySQL80` 运行中，`root@localhost:3306`，8.0.42 |
| 安卓工程 | `D:\Android-workspace\Schoollife`，AGP 9.3.0，minSdk 24 |

---

## 3. 总体架构

```
Android App (Java)                 Spring Boot (Java 11)              MySQL 8.0
──────────────────                 ──────────────────────             ─────────
校园卡/教务/课表  ──REST/JSON──▶    Controller ──▶ Repository  ──▶    school_life 库
（登录后可用）                      （不做 Service 层，直接调 Repository）
天气            ──直连──▶ devapi.qweather.com（不经后端）
闹钟提醒        ──本地 AlarmManager + 通知
```

**为什么这样简化：**
- 后端只做「接口 → 查库」两件事，**不写 Service 层、不做统一响应包装、不做全局异常处理**（作业够用即可）。
- JPA 的 `JpaRepository` 自带增删改查，省掉手写 SQL。
- 天气是公开数据、不需要账号，App 直连最省事。
- 闹钟必须在手机上触发，只能在客户端做。

---

## 4. 数据库设计（MySQL）

- 库名 `school_life`；JDBC URL 带 `createDatabaseIfNotExist=true`，后端启动自动建库。
- 建表由 JPA `ddl-auto=update` 自动完成（报告可另附 `schema.sql` 截图）。
- 5 张表，列名用小写下划线。

**user（账号 + 学生档案）**

| 列名 | 类型 | 说明 |
| --- | --- | --- |
| id | INT PK AUTO_INCREMENT | |
| student_no | VARCHAR(20) UNIQUE | 学号，登录名 |
| password | VARCHAR(64) | 密码（作业用明文） |
| name | VARCHAR(32) | 姓名 |
| grade / college / major / clazz | VARCHAR(64) | 年级 / 学院 / 专业 / 班级 |

**campus_card**

| 列名 | 类型 | 说明 |
| --- | --- | --- |
| id | INT PK AUTO_INCREMENT | |
| student_no | VARCHAR(20) | 归属学生 |
| card_no | VARCHAR(20) | 卡号 |
| name | VARCHAR(32) | 持卡人 |
| balance | DECIMAL(10,2) | 余额 |
| lost | TINYINT(1) | 是否挂失 |
| updated_at | BIGINT | 更新时间戳(ms) |

**card_transaction**

| 列名 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK AUTO_INCREMENT | |
| student_no | VARCHAR(20) | 归属学生 |
| amount | DECIMAL(10,2) | 消费为负 / 充值为正 / 挂失解挂为 0 |
| type | VARCHAR(16) | 消费 / 充值 / 挂失 / 解挂 |
| place | VARCHAR(64) | 地点或说明 |
| time | BIGINT | 时间戳(ms) |

**grade**

| 列名 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK AUTO_INCREMENT | |
| student_no | VARCHAR(20) | 归属学生 |
| semester | VARCHAR(16) | 学期，如 2025-2026-1 |
| course_name | VARCHAR(64) | 课程名 |
| credit | DECIMAL(3,1) | 学分 |
| score | DECIMAL(5,1) | 百分制成绩 |

**course**

| 列名 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK AUTO_INCREMENT | |
| student_no | VARCHAR(20) | 归属学生 |
| name / teacher / room | VARCHAR(64/32/32) | 课程名 / 教师 / 教室 |
| day_of_week | INT | 1=周一 … 7=周日 |
| start_section / end_section | INT | 起 / 止节（1~12） |
| start_week / end_week | INT | 起 / 止教学周 |
| week_type | INT | 0=每周, 1=单周, 2=双周 |
| remind | TINYINT(1) | 是否提醒 |
| remind_minutes | INT | 提前分钟数 |

**种子数据**（后端启动时写入，先查 count 为 0 才插，重启不重复）：
- `2023051234 / 123456` 张同学：1 张校园卡（余额 68.50 + 5 条流水）、6 门成绩、7 门课（**含 1 门单周课 + 1 门双周课**）。
- `2023051235 / 123456` 李同学：余额 12.00、2 门成绩、2 门课（用来演示数据隔离）。

---

## 5. 后端接口（Spring Boot，端口 8080）

**后端结构（极简 4 层文件）：**

```
server/src/main/java/com/example/schoollife/
├── SchoolLifeServerApplication.java   # 启动类 + 一个 CommandLineRunner 写种子数据
├── entity/     User, CampusCard, CardTransaction, Grade, Course        # 5 个实体
├── repository/ UserRepository, CampusCardRepository,
│               CardTransactionRepository, GradeRepository, CourseRepository
└── controller/ AuthController, CardController, EduController, CourseController
server/src/main/resources/application.yml
```

**接口清单**（直接返回实体或 List，不做统一包装）

| 方法 | 路径 | 参数 | 返回 |
| --- | --- | --- | --- |
| POST | `/api/login` | JSON `{studentNo, password}` | 成功返回 `User` 对象；失败返回 `null` |
| GET | `/api/card/{studentNo}` | - | `{card:CampusCard, transactions:[...]}` |
| POST | `/api/card/{studentNo}/lost` | 查询参数 `lost=true/false` | 更新后的 `CampusCard` |
| GET | `/api/edu/{studentNo}` | - | `{profile:User, grades:[...]}` |
| GET | `/api/courses/{studentNo}` | - | `[Course]` |
| POST | `/api/courses` | JSON `Course` | 新增后的 `Course` |
| PUT | `/api/courses` | JSON `Course`（含 id） | 更新后的 `Course` |
| DELETE | `/api/courses/{id}` | - | `null` |

**测试用 curl：**

```bash
curl -X POST http://localhost:8080/api/login -H "Content-Type: application/json" -d "{\"studentNo\":\"2023051234\",\"password\":\"123456\"}"
curl http://localhost:8080/api/courses/2023051234
curl -X POST "http://localhost:8080/api/card/2023051234/lost?lost=true"
```

**application.yml：**

```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/school_life?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: 你的MySQL密码
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

**pom.xml 依赖**：`spring-boot-starter-web`、`spring-boot-starter-data-jpa`、`mysql-connector-j`（parent 为 `spring-boot-starter-parent 2.7.18`，`java.version=11`）。

---

## 6. 安卓端设计

### 6.1 页面（底部 4 个 Tab）

| Tab | 页面 | 内容 | 权限 |
| --- | --- | --- | --- |
| 校园卡 | `CardFragment` | 卡号 / 余额 / 状态 / 挂失解挂按钮 / 流水列表 | 需登录 |
| 教务 | `EduFragment` | 学生信息 / GPA+总学分 / 成绩列表 / 退出登录 | 需登录 |
| 天气 | `WeatherFragment` | 城市搜索 / 实时天气 / 未来 7 天 | 公开 |
| 课表 | `ScheduleFragment` | 周次切换 / 12×7 网格 / FAB 新增 / 编辑弹窗（含单双周） | 需登录 |

未登录时，前三个个人页面显示「请先登录」+ 按钮，点了打开 `LoginActivity`。

### 6.2 关键类（不做多余抽象）

```
com/example/school_life/
├── SchoolLifeApp.java              # Application：建通知渠道
├── LoginActivity.java              # 登录页（调 /api/login）
├── MainActivity.java               # 底部导航
├── Constants.java                  # ★ 常量集中放这里（BASE_URL、天气 Key 等直接写死）
├── net/  ApiClient.java  ApiService.java   # Retrofit：校园接口 + 天气接口
│         model/  ApiResult、天气响应模型、登录请求
├── alarm/ Notifications.java  AlarmScheduler.java  ClassAlarmReceiver.java
├── ui/  card/  edu/  weather/  schedule/   （Fragment + Adapter + CourseEditDialog）
└── util/  Prefs.java（存学号/城市）  SectionTimes.java  GpaCalculator.java
          WeekUtils.java  AlarmTimeCalculator.java  TermPrefs.java
```

**简化点（相对上一版）：**
- 不再用 `BuildConfig` + `local.properties` 注入配置，改成 `Constants.java` 里直接写常量（作业更直观，也能截图）。
- 不再单独写 `SessionManager`，合并成 `Prefs.java`（存学号、存城市）。
- 只保留 3 个小的纯逻辑单元测试（GPA / 单双周 / 提醒时刻），用于报告展示测试结果。

### 6.3 两个必须注意的坑

1. **BASE_URL**：模拟器里访问电脑上的后端要用 `http://10.0.2.2:8080/`（不是 localhost）；真机用电脑局域网 IP。
2. **明文 HTTP**：Android 9+ 默认禁止 `http://`，需在 `<application>` 加 `android:usesCleartextTraffic="true"`。

**权限**：`INTERNET`、`POST_NOTIFICATIONS`、`SCHEDULE_EXACT_ALARM`。

---

## 7. 几个关键规则（固定写死，避免歧义）

**GPA 绩点换算**

| 百分制 | ≥90 | 85–89 | 82–84 | 78–81 | 75–77 | 72–74 | 68–71 | 64–67 | 60–63 | <60 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 绩点 | 4.0 | 3.7 | 3.3 | 3.0 | 2.7 | 2.3 | 2.0 | 1.5 | 1.0 | 0 |

`GPA = Σ(学分×绩点) / Σ学分`（保留 2 位小数）。

**单双周**：`weekType` 0=每周/1=单周/2=双周。显示条件 = `startWeek ≤ 当前周 ≤ endWeek` 且（每周 或 单周且周号为奇 或 双周且周号为偶）。

**节次时间**：1节 08:00、2节 08:55、3节 10:00、4节 10:55、5节 14:00、6节 14:55、7节 16:00、8节 16:55、9节 19:00、10节 19:55、11节 20:50、12节 21:45。

**提醒时刻** = 当天该节开始时间 − 提前分钟数，取 `startWeek..endWeek` 内第一个未来的周；找不到就不预约。

---

## 8. 实施步骤（5 步）

| 步骤 | 内容 | 完成标志 |
| --- | --- | --- |
| **① 后端工程 + 数据库** | 建 `server/` 工程（pom、启动类、application.yml）；5 个实体 + 5 个 Repository；`ddl-auto=update` 自动建表 | 启动成功，MySQL 里出现 `school_life` 库和 5 张表 |
| **② 后端接口 + 种子数据** | 启动类里写 `CommandLineRunner` 灌种子数据；4 个 Controller 实现 8 个接口 | 8 个 curl 全部返回正确 JSON |
| **③ 安卓依赖 + 主框架 + 登录** | 加 Retrofit/Navigation 依赖；`Constants`；底部导航；`LoginActivity`；未登录提示 | 4 个 Tab 能切；登录成功进入、失败提示 |
| **④ 校园卡 + 教务** | 两个页面调接口渲染；GPA 计算（先写小测试） | 余额/挂失/流水正常；成绩与 GPA 正确；换账号数据不同 |
| **⑤ 天气 + 课表 + 提醒** | 天气页（直连 API）；课表网格 + 增删改 + 单双周；AlarmManager 通知 | 天气可切城市；课表按周次与单双周正确显隐；改动落库；提醒能弹出 |

---

## 9. 验收清单（10 项）

| # | 验收点 | 期望 |
| --- | --- | --- |
| 1 | 后端启动 | `mvn spring-boot:run` 成功，MySQL 自动生成库表与种子数据 |
| 2 | 后端未启动 | App 提示「无法连接服务器」不崩溃；天气仍可用 |
| 3 | 游客访问 | 天气可用；校园卡/教务/课表显示「请先登录」 |
| 4 | 登录 | `2023051234/123456` 成功；错误密码提示失败 |
| 5 | 校园卡查询 | 余额 68.50、状态正常、流水列表（消费红/充值绿） |
| 6 | 校园卡挂失 | 状态变已挂失、按钮变「解挂」、流水多一条；**重启 App 仍为已挂失**（证明落库） |
| 7 | 教务查询 | 学生信息完整；6 门成绩 + GPA + 总学分 |
| 8 | 天气查询 | 默认北京实时天气 + 7 天预报；搜索「上海」切换成功 |
| 9 | 课表管理 | 12×7 网格；点空位新增、点课程编辑/删除、**重启后仍在**；切到第 2 周单双周课程正确显隐 |
| 10 | 数据隔离 | 换成 `2023051235` 登录，看到的是李同学的数据 |

---

## 10. 后续可扩展（本次不做）

注册功能、密码加密、JWT 鉴权、校园卡充值、消费统计图表、调课规则、课表导入导出、天气定位选城、离线缓存、后端部署到云服务器。
