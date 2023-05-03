package com.batgm.handledata.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

@Data
@Document(collection="study_action_record")
public class StudyActionRecordBean extends BaseBean implements Serializable {
  /**
     * 项目Id
     */
    private Long projectId;

    /**
     * 学习圈id
     */
    private Long studyCircleId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 主表ID（研修活动6环节表id，课程6类型具体表ID）
     */
    private Long subjectTableId;

    /**
     * 父级表ID（研修活动,课程目录）
     */
    private Long fatherTableId;

    /**
     * 学习类型（研修活动-环节，课程-6环节，）
     */
    private String studyType;

    /**
     * 视频学习当前时间
     */
    private Integer videoTime;

    /**
     * 学习的时间
     */
    private Integer studyTime;

    /**
     * 是否视频学习
     */
    private String action;

    /**
     * 是否视频学习
     */
    private String loginPalce;

    /**
     * 是否视频学习
     */
    private String deviceType;

    /**
     * 是否视频学习
     */
    private String ipAddress;

    /**
     * 学习计划ID
     */
    private Long studyPlanId;

    /**
     * 课程代码
     */
    private String courseCode;

    /**
     * （作业id或评论id）
     */
    private Long detailTableId;

    private static final long serialVersionUID = 1L;

    public StudyActionRecordBean(BigInteger id, Long projectId, Long studyCircleId, Long userId, Long subjectTableId, Long fatherTableId, String studyType, Integer videoTime, Integer studyTime, String action, String loginPalce, String deviceType, String ipAddress, Date createTime, Long studyPlanId, String courseCode, Long detailTableId, Date updateTime) {
        this.id = id;
        this.projectId = projectId;
        this.studyCircleId = studyCircleId;
        this.userId = userId;
        this.subjectTableId = subjectTableId;
        this.fatherTableId = fatherTableId;
        this.studyType = studyType;
        this.videoTime = videoTime;
        this.studyTime = studyTime;
        this.action = action;
        this.loginPalce = loginPalce;
        this.deviceType = deviceType;
        this.ipAddress = ipAddress;
        this.createTime = createTime;
        this.studyPlanId = studyPlanId;
        this.courseCode = courseCode;
        this.detailTableId = detailTableId;
        this.updateTime = updateTime;
    }

    public StudyActionRecordBean() {
        super();
    }
}