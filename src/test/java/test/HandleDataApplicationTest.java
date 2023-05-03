package test;

import com.batgm.handledata.HandleDataApplication;
import com.batgm.handledata.annotation.HandlingTime;
import com.batgm.handledata.entity.StudyActionRecord;
import com.batgm.handledata.service.StudyActionRecordService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * @author yqq
 * @createdate 2020/9/18
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HandleDataApplication.class)
//@Transactional
//@Rollback(true) // 事务自动回滚，默认是true。可以不写
public class HandleDataApplicationTest {

    @Autowired
    private StudyActionRecordService studyActionRecordService;



    @Test
    public void testListAll(){
        int pageNum=0,pageSize=10;
        PageHelper.startPage(pageNum, pageSize);
        List<StudyActionRecord> list = studyActionRecordService.listAll();
        PageInfo<StudyActionRecord> page = new PageInfo<>(list);
        System.out.println("##########testListAll###########");
        System.out.println(page.toString());
        list.forEach(member -> {
            System.out.println(member.toString());
            /*member.setId(null);
            member.setUserId(member.getUserId()+1);
            studyActionRecordService.save(member);*/
        });
        System.out.println("##########testListAll###########");
    }


    @Test
    public void testsave(){
        StudyActionRecord record = studyActionRecordService.findById(new BigInteger("66666"));
        for(int i=0;i<10000;i++){
            record.setUserId(record.getUserId()+1);
            studyActionRecordService.save(record);
        }
    }

    @Test
    public void testfindById(){

        System.out.println(studyActionRecordService.findById(new BigInteger("66666")));

    }

    @Test
    @Transactional
    @Rollback(false)
    public void testUpdate(){
        StudyActionRecord record = new StudyActionRecord();
        record.setId(new BigInteger("298074576"));
        record.setUpdateTime(new Date());
        record.setLoginPalce("xi'an-555");
        studyActionRecordService.updateByPrimaryKeySelective(record);
        //studyActionRecordService.updateByPrimaryKey(record);
    }

}
