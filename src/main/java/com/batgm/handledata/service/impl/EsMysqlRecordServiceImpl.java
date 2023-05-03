package com.batgm.handledata.service.impl;

import com.batgm.handledata.dao.EsMysqlRecordMapper;
import com.batgm.handledata.entity.EsMysqlRecord;
import com.batgm.handledata.service.EsMysqlRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

/**
 * @author yqq
 * @createdate 2020/3/27
 */
@Service("esMysqlRecordService")
public class EsMysqlRecordServiceImpl implements EsMysqlRecordService {

    @Autowired
    private EsMysqlRecordMapper esMysqlRecordMapper;


    @Override
    public List<EsMysqlRecord> listAll() {
        return esMysqlRecordMapper.selectAll();
    }

    @Override
    public int save(EsMysqlRecord member) {
        return esMysqlRecordMapper.insert(member);
    }

    @Override
    public EsMysqlRecord findById(BigInteger id) {
        return esMysqlRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    public int deleteById(BigInteger id) {
        return esMysqlRecordMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int update(EsMysqlRecord member) {
        return esMysqlRecordMapper.updateByPrimaryKey(member);
    }

    @Override
    public EsMysqlRecord getOneEsMysqlRecord(EsMysqlRecord bean) {
        return esMysqlRecordMapper.selectOne(bean);
    }
}
