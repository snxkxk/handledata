package com.batgm.handledata.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.math.BigInteger;
import java.util.Date;

@Data
public class BaseBean {
    @Id
    public BigInteger id;
    /**
     * 注册日期
     */
    @Column(name = "create_time")
    public Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    public Date updateTime;

}


