package com.batgm.handledata.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.*;
import lombok.Data;

@Table(name = "es_mysql_record")
@Data
public class EsMysqlRecord extends BaseBean implements Serializable {
    /**
     * 主键
     */
    @Id
    private BigInteger id;

    /**
     * 表名
     */
    @Id
    @Column(name = "table_name")
    private String tableName;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 上次数据同步更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 最新一次更新量
     */
    private Integer num;

    /**
     * 同步次数
     */
    private Integer ct;

    /**
     * 同步总条数
     */
    private Integer total;

    /**
     * 说明
     */
    private String content;

    private static final long serialVersionUID = 1L;

    public EsMysqlRecord(BigInteger id, String tableName, Date createTime, Date updateTime, Integer num, Integer ct, Integer total, String content) {
        this.id = id;
        this.tableName = tableName;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.num = num;
        this.ct = ct;
        this.total = total;
        this.content = content;
    }

    public EsMysqlRecord() {
        super();
    }
}