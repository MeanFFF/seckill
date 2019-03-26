package org.seckill.dao.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
// 告诉spring,junit配置文件
@ContextConfiguration({"classpath:Spring/Spring-dao.xml"})
public class RedisDaoTest {

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private RedisDao redisDao;

    private long id = 1001;

    @Test
    public void getSeckill() {
        Seckill seckill = redisDao.getSeckill(id);
        if(seckill == null){
            seckill = seckillDao.queryById(id);
            String result = redisDao.putSeckill(seckill);
            System.out.println(result);
            seckill = redisDao.getSeckill(id);
            System.out.println(seckill);
        }
    }
}