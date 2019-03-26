package org.seckill.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.rmi.server.ExportException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:Spring/Spring-*.xml"})
public class SeckillServiceImplTest {

    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService service;

    @Test
    public void getSeckillList() {
        List<Seckill> list = service.getSeckillList();
        logger.info("list={}", list);
        /**
         * [Seckill{seckillId=1000, name='1000元秒杀ipx', startTime=Fri Mar 15 08:00:00 CST 2019, endTime=Sat Mar 16 08:00:00 CST 2019, createTime=Fri Mar 15 20:02:54 CST 2019},
         * Seckill{seckillId=1001, name='500元秒杀ipad mini 4', startTime=Fri Mar 15 08:00:00 CST 2019, endTime=Sat Mar 16 08:00:00 CST 2019, createTime=Fri Mar 15 20:02:54 CST 2019},
         * Seckill{seckillId=1002, name='1000元秒杀redmi note 7', startTime=Fri Mar 15 08:00:00 CST 2019, endTime=Sat Mar 16 08:00:00 CST 2019, createTime=Fri Mar 15 20:02:54 CST 2019},
         * Seckill{seckillId=1003, name='200元秒杀三星S10+', startTime=Fri Mar 15 08:00:00 CST 2019, endTime=Sat Mar 16 08:00:00 CST 2019, createTime=Fri Mar 15 20:02:54 CST 2019}]
         */
    }

    @Test
    public void getById() {
        long seckillId = 1000L;
        Seckill seckill = service.getById(seckillId);
        logger.info("seckill={}", seckill);
        /**
         * seckill=Seckill{
         * seckillId=1000,
         * name='1000元秒杀ipx',
         * startTime=Fri Mar 15 08:00:00 CST 2019,
         * endTime=Sat Mar 16 08:00:00 CST 2019,
         * createTime=Fri Mar 15 20:02:54 CST 2019}
         */
    }

    @Test
    public void exportSeckillUrl() {
        Exposer exposer = service.exportSeckillUrl(1000);
        logger.info("exposer={}", exposer);
        /**
         * exposer=Exposer{exposed=false,
         * md5='null',
         * seckillId=1000,
         * now=1552884057233,
         * startTime=1552608000000,
         * endTime=1552694400000}
         *
         * exposer=Exposer{exposed=true,
         * md5='f9eb3e2719479c4f9c3d32b653fd659f',
         * seckillId=1000,
         * now=0,
         * startTime=0,
         * endTime=0}
         */
    }

    @Test
    public void executeSeckill() {
        long seckillId = 1000L;
        long userPhone = 15659170387L;
        SeckillExecution seckillExecution = service.executeSeckill(seckillId, userPhone, "f9eb3e2719479c4f9c3d32b653fd659f");
        logger.info("seckillExecution={}", seckillExecution);
    }
}