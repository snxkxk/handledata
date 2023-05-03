package com.batgm.handledata.service;

import com.batgm.handledata.entity.EsMysqlRecord;
import com.batgm.handledata.entity.StudyActionRecord;

import java.math.BigInteger;
import java.util.List;

/**
 * @author yqq
 * @createdate 2020/3/27
 */
public interface EsMysqlRecordService {

    List<EsMysqlRecord> listAll();

    int save(EsMysqlRecord member);

    EsMysqlRecord findById(BigInteger id);

    int deleteById(BigInteger id);

    int update(EsMysqlRecord bean);

    EsMysqlRecord getOneEsMysqlRecord(EsMysqlRecord bean);
}
