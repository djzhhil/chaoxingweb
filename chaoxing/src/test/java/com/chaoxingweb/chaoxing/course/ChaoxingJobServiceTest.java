package com.chaoxingweb.chaoxing.course;

import com.chaoxingweb.chaoxing.dto.JobDTO;
import com.chaoxingweb.chaoxing.dto.StudyResultDTO;
import com.chaoxingweb.chaoxing.enums.JobType;
import com.chaoxingweb.chaoxing.enums.StudyResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 任务服务测试
 *
 * @author 小克 🐕💎
 * @since 2026-04-08
 */
@SpringBootTest
public class ChaoxingJobServiceTest {

    @Autowired
    private ChaoxingJobService jobService;

    /**
     * 测试获取任务列表
     */
    @Test
    public void testGetJobList() {
        // 这里需要填入真实的课程信息
        String courseId = "test_course_id";
        String clazzId = "test_clazz_id";
        String knowledgeId = "test_knowledge_id";
        String cpi = "test_cpi";

        Map<String, Object> result = jobService.getJobList(courseId, clazzId, knowledgeId, cpi);
        
        assertNotNull(result);
        assertTrue(result.containsKey("jobList"));
        assertTrue(result.containsKey("jobInfo"));
    }

    /**
     * 测试视频学习
     */
    @Test
    public void testStudyVideo() {
        JobDTO job = new JobDTO();
        job.setJobId("test_job_id");
        job.setJobName("测试视频");
        job.setJobType(JobType.VIDEO);
        job.setCourseId("test_course_id");
        job.setClazzId("test_clazz_id");
        job.setKnowledgeId("test_knowledge_id");
        job.setObjectId("test_object_id");
        job.setDtoken("test_dtoken");
        job.setOtherinfo("cpi=12345&nodeId_67890-rt_1");
        job.setPlayingTime(0);
        job.setDuration(300);

        StudyResultDTO result = jobService.studyVideo(job);
        
        assertNotNull(result);
        assertNotNull(result.getResult());
        assertNotNull(result.getMessage());
    }

    /**
     * 测试文档学习
     */
    @Test
    public void testStudyDocument() {
        JobDTO job = new JobDTO();
        job.setJobId("test_job_id");
        job.setJobName("测试文档");
        job.setJobType(JobType.DOCUMENT);
        job.setCourseId("test_course_id");
        job.setClazzId("test_clazz_id");
        job.setKnowledgeId("test_knowledge_id");
        job.setJtoken("test_jtoken");
        job.setOtherinfo("nodeId_67890-rt_1");

        StudyResultDTO result = jobService.studyDocument(job);
        
        assertNotNull(result);
        assertNotNull(result.getResult());
        assertNotNull(result.getMessage());
    }

    /**
     * 测试阅读任务学习
     */
    @Test
    public void testStudyRead() {
        JobDTO job = new JobDTO();
        job.setJobId("test_read_job_id");
        job.setJobName("测试阅读任务");
        job.setJobType(JobType.READ);
        job.setCourseId("test_course_id");
        job.setClazzId("test_clazz_id");
        job.setKnowledgeId("test_knowledge_id");
        job.setJtoken("test_jtoken");
        job.setOtherinfo("nodeId_67890-rt_1");

        StudyResultDTO result = jobService.studyRead(job);
        
        assertNotNull(result);
        assertNotNull(result.getResult());
        assertNotNull(result.getMessage());
        System.out.println("阅读任务测试结果: " + result.getResult() + " - " + result.getMessage());
    }

    /**
     * 测试学习任务
     */
    @Test
    public void testStudyJob() {
        JobDTO job = new JobDTO();
        job.setJobId("test_job_id");
        job.setJobName("测试任务");
        job.setJobType(JobType.VIDEO);
        job.setCourseId("test_course_id");
        job.setClazzId("test_clazz_id");
        job.setKnowledgeId("test_knowledge_id");
        job.setObjectId("test_object_id");
        job.setDtoken("test_dtoken");
        job.setOtherinfo("cpi=12345&nodeId_67890-rt_1");
        job.setPlayingTime(0);
        job.setDuration(300);

        StudyResultDTO result = jobService.studyJob(job);
        
        assertNotNull(result);
        assertNotNull(result.getResult());
    }

    /**
     * 测试学习章节所有任务
     */
    @Test
    public void testStudyChapterJobs() {
        String courseId = "test_course_id";
        String clazzId = "test_clazz_id";
        String knowledgeId = "test_knowledge_id";
        String cpi = "test_cpi";

        List<StudyResultDTO> results = jobService.studyChapterJobs(courseId, clazzId, knowledgeId, cpi);
        
        assertNotNull(results);
    }

    /**
     * 测试生成enc签名
     */
    @Test
    public void testGenerateEnc() {
        String clazzId = "12345";
        String jobId = "job_001";
        String objectId = "obj_001";
        int playingTime = 60;
        int duration = 300;
        String uid = "user_001";

        String enc = jobService.generateEnc(clazzId, jobId, objectId, playingTime, duration, uid);
        
        assertNotNull(enc);
        assertFalse(enc.isEmpty());
        assertEquals(32, enc.length()); // MD5哈希长度为32
    }
}
