package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.execption.RepeatKillException;
import org.seckill.execption.SeckillCloseException;
import org.seckill.execption.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String slat = "helhnwglmn/;.ksf;pkq[ij]1klwjhjfwl";

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        //缓存优化

        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill == null){
            seckill = seckillDao.queryById(seckillId);
            if(seckill == null)
                return new Exposer(false, seckillId);
            else
                redisDao.putSeckill(seckill);
        }



        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date now = new Date();
        if(now.getTime() < startTime.getTime() ||
            now.getTime() > endTime.getTime())
            return new Exposer(false, seckillId, now.getTime(),startTime.getTime(),endTime.getTime());

        String md5 = getMD5(seckillId);

        return new Exposer(true, md5, seckillId);
    }

    /**
     * MD5 转化特定字符串的过程
     * 不可逆
     * 加入盐值混淆，不希望被用户猜到结果
     * @param seckillId
     * @return MD5
     */
    public static String getMD5(long seckillId){
        String str = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(str.getBytes());
        return md5;
    }

    @Override
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, SeckillCloseException, RepeatKillException {
        if(md5 == null || !md5.equals(getMD5(seckillId)))
            throw new SeckillException("Seckill Data Rewrite");

        try {
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            Date now = new Date();
            if(insertCount <= 0){
                throw new RepeatKillException("Repeat Kill");
            }else{
                int updateCount = seckillDao.reduceNumber(seckillId, now);
                if(updateCount <= 0){
                    throw new SeckillCloseException("Seckill Closed");
                }else{
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS,successKilled);
                }
            }
        }catch (RepeatKillException e1){
            throw e1;

        }catch (SeckillCloseException e2){
            throw e2;

        }catch (Exception e){
            logger.error(e.getMessage(), e);
            // 所有编译起异常,转化为运行期异常
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }
    }

    /**
     * 存储过程
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws SeckillCloseException
     * @throws RepeatKillException
     */
    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) throws SeckillException, SeckillCloseException, RepeatKillException {
        if(md5 == null || !md5.equals(getMD5(seckillId)))
            return new SeckillExecution(seckillId, SeckillStateEnum.REPEAT_KILL);
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        //存储过程执行完之后result被赋值

        try {
            seckillDao.killByProcedure(map);
            int result = MapUtils.getInteger(map, "result", -2);
            if(result == 1){
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                return new SeckillExecution(seckillId,SeckillStateEnum.SUCCESS,successKilled);
            }else {
                return new SeckillExecution(seckillId, SeckillStateEnum.stateOf(result));
            }
        }catch  (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
        }

    }
}
