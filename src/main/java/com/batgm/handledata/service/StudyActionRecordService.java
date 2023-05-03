package com.batgm.handledata.service;

import com.batgm.handledata.entity.StudyActionRecord;

import java.math.BigInteger;
import java.util.List;

/**
 * @author yqq
 * @createdate 2020/3/27
 */
public interface StudyActionRecordService {

    List<StudyActionRecord> listAll();

    int save(StudyActionRecord member);

    StudyActionRecord findById(BigInteger id);

    int deleteById(BigInteger id);

    StudyActionRecord updateByPrimaryKeySelective(StudyActionRecord studyActionRecord);

    StudyActionRecord updateByPrimaryKey(StudyActionRecord studyActionRecord);
}
