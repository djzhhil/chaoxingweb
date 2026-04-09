# 学习模块实现说明

## 概述

学习模块实现了超星学习通平台的自动学习功能，支持视频、音频、文档、阅读等多种任务类型的自动化学习。

## 核心功能

### 1. 视频学习 (studyVideo)

#### 功能特点
- ✅ 自动获取视频信息（时长、dtoken等）
- ✅ 模拟真实播放过程
- ✅ 定期上报学习进度
- ✅ 智能处理403错误并自动重试
- ✅ 支持断点续学（从上次播放位置继续）
- ✅ 自动生成加密签名(enc)
- ✅ 速率限制控制，避免请求过快

#### 实现流程
1. **获取视频状态**: 调用 `getMediaStatus` 接口获取视频的 dtoken、duration 等信息
2. **检查完成状态**: 如果已播放时间 >= 总时长，直接返回成功
3. **模拟播放**: 
   - 初始化播放参数（playTime, lastLogTime, waitTime等）
   - 循环模拟播放过程
   - 每隔30-90秒上报一次进度
   - 实时计算播放进度
4. **进度上报**: 
   - 生成enc签名
   - 构造请求参数（clazzId, playingTime, duration, rt等）
   - 调用 `reportVideoProgress` 接口
   - 解析响应判断是否通过
5. **异常处理**:
   - 403错误：最多重试2次，尝试刷新会话状态
   - 其他错误：记录日志并返回失败

#### 关键参数
- `THRESHOLD`: 每次循环的时间间隔（0.5秒）
- `MAX_FORBIDDEN_RETRY`: 最大403重试次数（2次）
- `waitTime`: 进度上报间隔（30-90秒随机）

### 2. 文档学习 (studyDocument)

#### 功能特点
- ✅ 自动提取知识点ID
- ✅ 调用文档学习接口
- ✅ 标记文档为已学习状态

#### 实现流程
1. **提取knowledgeId**: 从 otherInfo 中解析 nodeId
2. **调用学习接口**: 发送GET请求到文档学习接口
3. **返回结果**: 根据响应状态返回成功或失败

### 3. 阅读任务学习 (studyRead)

#### 功能特点
- ✅ 自动获取知识点ID（支持从JobDTO或其他信息中提取）
- ✅ 调用阅读任务学习接口
- ✅ 解析响应JSON获取任务完成消息
- ✅ 完善的错误处理和日志记录

#### 实现流程
1. **获取knowledgeId**: 
   - 优先使用 JobDTO 中的 knowledgeId
   - 如果为空，尝试从 otherInfo 中提取 nodeId
   - 如果仍为空，使用默认值 "0"
2. **调用学习接口**: 
   - 发送GET请求到 `https://mooc1.chaoxing.com/ananas/job/readv2`
   - 传递参数：jobid, knowledgeid, courseid, clazzid, jtoken
3. **解析响应**: 
   - 检查HTTP状态码是否为200
   - 解析JSON响应中的msg字段
   - 记录任务完成消息
4. **返回结果**: 根据响应状态返回成功或失败

#### API接口
```java
boolean completeReadStudy(String jobId, String knowledgeId, String courseId,
                         String clazzId, String jtoken)
```

### 4. 空页面学习 (studyEmptyPage)

#### 功能特点
- ✅ 简单访问即可完成任务
- ✅ 适用于无实际内容的章节

### 4. 辅助功能

#### generateEnc - 生成加密签名
```java
String enc = generateEnc(clazzId, jobId, objectId, playingTime, duration, uid);
```
- 使用MD5算法生成签名
- 签名字符串格式: `[clazzId][uid][jobId][objectId][playingTime*1000][d_yHJ!$pdA~5][duration*1000][0_duration]`

#### reportVideoProgress - 上报视频进度
- 构造完整的请求参数
- 处理rt参数（从otherInfo中提取或使用默认值）
- 支持videoFaceCaptureEnc、attDuration等可选参数
- 返回isPassed字段判断是否通过

#### refreshVideoStatus - 刷新视频状态
- 重新获取视频元数据
- 用于403错误恢复
- 更新dtoken、duration、playTime等信息

#### extractCpiFromOtherInfo - 提取CPI
- 从otherInfo字符串中解析cpi参数
- 使用正则表达式匹配

#### extractKnowledgeIdFromOtherInfo - 提取知识点ID
- 从otherInfo字符串中解析nodeId
- 用于文档学习任务

## API接口

### ChaoxingApiClient 新增方法

#### 1. getMediaStatus
```java
String getMediaStatus(String objectId, String fid, boolean isVideo)
```
获取视频/音频的状态信息，返回JSON字符串包含dtoken、duration等字段。

#### 2. reportVideoProgress
```java
String reportVideoProgress(String cpi, String dtoken, Map<String, String> params)
```
上报视频学习进度，返回JSON字符串包含isPassed字段。

#### 3. completeDocumentStudy
```java
boolean completeDocumentStudy(String jobId, String knowledgeId, String courseId, 
                              String clazzId, String jtoken)
```
完成文档学习任务，返回是否成功。

#### 4. completeReadStudy
```java
boolean completeReadStudy(String jobId, String knowledgeId, String courseId,
                         String clazzId, String jtoken)
```
完成阅读任务学习，返回是否成功，并解析响应JSON获取任务完成消息。

## 使用示例

### 学习单个视频任务
```java
JobDTO job = new JobDTO();
job.setJobId("job_123");
job.setJobName("第一章视频");
job.setJobType(JobType.VIDEO);
job.setCourseId("course_001");
job.setClazzId("clazz_001");
job.setKnowledgeId("knowledge_001");
job.setObjectId("object_123");
job.setDtoken("dtoken_xyz");
job.setOtherinfo("cpi=12345&nodeId_67890-rt_1");
job.setPlayingTime(0);
job.setDuration(300);

StudyResultDTO result = jobService.studyVideo(job);
System.out.println("学习结果: " + result.getResult());
System.out.println("消息: " + result.getMessage());
```

### 学习章节所有任务
```java
List<StudyResultDTO> results = jobService.studyChapterJobs(
    "course_001", 
    "clazz_001", 
    "knowledge_001", 
    "12345"
);

for (StudyResultDTO result : results) {
    System.out.printf("任务%s: %s - %s%n", 
        result.getJobId(), 
        result.getResult(), 
        result.getMessage()
    );
}
```

## 注意事项

1. **会话管理**: 确保在调用学习方法前已经登录并设置了有效的Cookie、FID、UID
2. **速率限制**: 系统自动控制请求频率，无需手动干预
3. **错误处理**: 403错误会自动重试，超过最大重试次数后跳过该任务
4. **断点续学**: 支持从上次播放位置继续学习
5. **日志记录**: 详细的日志输出便于调试和问题排查

## 测试

运行测试类 `ChaoxingJobServiceTest` 验证功能:
```bash
mvn test -Dtest=ChaoxingJobServiceTest
```

## 待优化项

1. ⏳ 添加音频学习专用方法（目前视频方法可复用）
2. ⏳ 实现作业任务的自动答题功能
3. ⏳ 添加学习进度可视化（进度条）
4. ⏳ 支持多线程并发学习
5. ⏳ 添加学习统计和报告功能

## 技术栈

- Java 17+
- Spring Boot
- Jackson (JSON处理)
- RestTemplate (HTTP客户端)
- JUnit 5 (单元测试)

## 参考

Python原始实现提供了核心逻辑参考，Java版本在保持功能一致的基础上进行了以下改进：
- 更清晰的代码结构
- 更好的错误处理
- 完善的日志记录
- 类型安全的实现
