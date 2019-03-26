package org.seckill.dao;

import java.util.Date;
import java.util.List;


import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * 配置spring和junit整合,junit启动时加载springIOC容器
 *
 * @date spring-test,junit
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
// 告诉spring,junit配置文件
@ContextConfiguration({"classpath:Spring/Spring-dao.xml"})
public class SeckillDaoTest {

    // 注入dao实现类的依赖
    @Autowired
    private SeckillDao seckillDao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void testReduceNumber() {
        int updateCount = seckillDao.reduceNumber(1000L, new Date());
        System.out.println(updateCount==1);
    }

    @Test
    public void testQueryById() {
        long id = 1000;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
        /**
         * 1000元秒杀iphone Seckill{seckillId=1000, name='1000元秒杀iphone',
         * number=100, startTime=Sun Nov 01 00:00:00 CST 2015, endTime=Mon Nov
         * 02 00:00:00 CST 2015, createTime=Fri Jun 03 17:30:36 CST 2016}
         */
    }

    @Test
    public void testQueryAll() {
        List<Seckill> list = seckillDao.queryAll(0, 10);
        for(Seckill s : list){
            System.out.println(s);
        }
    }

}