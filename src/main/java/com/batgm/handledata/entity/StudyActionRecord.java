package com.batgm.handledata.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.*;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Table(name = "study_action_record")
@Data
@Document(collection="study_action_record")
public class StudyActionRecord extends BaseBean implements Serializable {
/*    @Id
    public BigInteger id;
    *//**
     * 注册日期
     *//*
    @Column(name = "create_time")
    public Date createTime;

    *//**
     * 更新时间
     *//*
    @Column(name = "update_time")
    public Date updateTime;*/

    /**
     * 项目Id
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 学习圈id
     */
    @Column(name = "study_circle_id")
    private Long studyCircleId;

    /**
     * 用户id
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 主表ID（研修活动6环节表id，课程6类型具体表ID）
     */
    @Column(name = "subject_table_id")
    private Long subjectTableId;

    /**
     * 父级表ID（研修活动,课程目录）
     */
    @Column(name = "father_table_id")
    private Long fatherTableId;

    /**
     * 学习类型（研修活动-环节，课程-6环节，）
     */
    @Column(name = "study_type")
    private String studyType;

    /**
     * 视频学习当前时间
     */
    @Column(name = "video_time")
    private Integer videoTime;

    /**
     * 学习的时间
     */
    @Column(name = "study_time")
    private Integer studyTime;

    /**
     * 是否视频学习
     */
    @Column(name = "study_time")
    private String action;

    /**
     * 是否视频学习
     */
    @Column(name = "login_palce")
    private String loginPalce;

    /**
     * 是否视频学习
     */
    @Column(name = "device_type")
    private String deviceType;

    /**
     * 是否视频学习
     */
    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * 学习计划ID
     */
    @Column(name = "study_plan_id")
    private Long studyPlanId;

    /**
     * 课程代码
     */
    @Column(name = "course_code")
    private String courseCode;

    /**
     * （作业id或评论id）
     */
    @Column(name = "detail_table_id")
    private Long detailTableId;

    private static final long serialVersionUID = 1L;

    public StudyActionRecord(java.math.BigInteger id, Long projectId, Long studyCircleId, Long userId, Long subjectTableId, Long fatherTableId, String studyType, Integer videoTime, Integer studyTime, String action, String loginPalce, String deviceType, String ipAddress, java.util.Date createTime, Long studyPlanId, String courseCode, Long detailTableId, java.util.Date updateTime) {
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

    public StudyActionRecord() {
        super();
    }
}