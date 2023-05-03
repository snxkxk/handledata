package com.batgm.handledata.service.impl;

import com.batgm.handledata.annotation.HandlingTime;
import com.batgm.handledata.dao.StudyActionRecordMapper;
import com.batgm.handledata.entity.StudyActionRecord;
import com.batgm.handledata.service.StudyActionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

/**
 * @author yqq
 * @createdate 2020/3/27
 */
@Service("studyActionRecordService")
@CacheConfig(cacheNames = "handledata_studyActionRecord")
@Transactional
public class StudyActionRecordServiceImpl implements StudyActionRecordService {

    @Autowired
    private StudyActionRecordMapper studyActionRecordMapper;


    @Override
    public List<StudyActionRecord> listAll() {
        return studyActionRecordMapper.selectAll();
    }

    @Override
    public int save(StudyActionRecord member) {
        return studyActionRecordMapper.insert(member);
    }

    @Override
    @Cacheable( key = "'studyActionRecord:'+#id", unless = "#result == null")
    @HandlingTime(value = "studyActionRecordService.findById")
    public StudyActionRecord findById(BigInteger id) {
        return studyActionRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    @CacheEvict( key = "'studyActionRecord:'+#id")
    public int deleteById(BigInteger id) {
        return studyActionRecordMapper.deleteByPrimaryKey(id);
    }

    @Override
    @CachePut( key = "'studyActionRecord:'+#studyActionRecord.id", unless = "#result == null")
    public StudyActionRecord updateByPrimaryKeySelective(StudyActionRecord studyActionRecord) {
         studyActionRecordMapper.updateByPrimaryKeySelective(studyActionRecord);
        return studyActionRecordMapper.selectByPrimaryKey(studyActionRecord.getId());
    }

    @Override
    @CachePut( key = "'studyActionRecord:'+#studyActionRecord.id", unless = "#result == null")
    public StudyActionRecord updateByPrimaryKey(StudyActionRecord studyActionRecord) {
        studyActionRecordMapper.updateByPrimaryKey(studyActionRecord);
        return studyActionRecordMapper.selectByPrimaryKey(studyActionRecord.getId());
    }
}
