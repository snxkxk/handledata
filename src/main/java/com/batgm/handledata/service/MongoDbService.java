package com.batgm.handledata.service;

/**
 * @author yqq
 * @createdate 2021/4/20
 */
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;


@Service
public class MongoDbService {

    private static final Logger logger = LoggerFactory.getLogger(MongoDbService.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存对象
     * @param obj
     * @return
     */
    public String saveObj(Object obj) {
        mongoTemplate.save(obj);
        return "success";
    }

    /**
     * 查询所有
     * @return
     */
    public List<Object> findAll() {
        return mongoTemplate.findAll(Object.class);
    }

    /***
     * 根据id查询
     * @param id
     * @return
     */
    public Object getBookById(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.findOne(query, Object.class);
    }

    /**
     * 根据名称查询
     *
     * @param name
     * @return
     */
    public Object getBookByName(String name) {
        Query query = new Query(Criteria.where("name").is(name));
        return mongoTemplate.findOne(query, Object.class);
    }

    /**
     * 更新对象
     *
     * @param book
     * @return
     */
    public String updateBook(Object book,String id) {
        //Query query = new Query(Criteria.where("_id").is(id));
        //Update update = new Update().set("publish", book.getPublish()).set("info", book.getInfo());
        // updateFirst 更新查询返回结果集的第一条
        //mongoTemplate.updateFirst(query, update, Object.class);
        // updateMulti 更新查询返回结果集的全部
        // mongoTemplate.updateMulti(query,update,Book.class);
        // upsert 更新对象不存在则去添加
        // mongoTemplate.upsert(query,update,Book.class);
        return "success";
    }

    /***
     * 删除对象
     * @param obj
     * @return
     */
    public String deleteBook(Object obj) {
        mongoTemplate.remove(obj);
        return "success";
    }

    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    public String deleteBookById(String id) {
        // findOne
        Object book = getBookById(id);
        // delete
        deleteBook(book);
        return "success";
    }


}

