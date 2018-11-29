package org.seckill.service.impl;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SeckillServiceImpl implements SeckillService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    //md5盐值字符串，用户混淆md5
    private final String slat = "asdfawe123124123!@#$adf";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点，缓存优化,超时的基础上维护一致性
        //1:访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill==null){
            //2.访问数据库
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            }
            //3：放入redis
            redisDao.putSeckilll(seckill);
        }


        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //系统当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        //转换特定字符串的过程，不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的有点：
     * 1：开发团队达成一致约定，明确标注事务方法的编程风格
     * 2：保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求
     * 3：不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制
     */
    public SeckillExecution excuteSeckill(long seckillId, long userPhone, String md5) throws RepeatKillException, SeckillCloseException, SeckillException {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }
        //可以使用数据库储存过程优化，将事务放进mysql，从而减少row lock的持有时间
        try {
            //记录够买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if (insertCount <= 0) {
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            }

            //执行秒杀逻辑：减库存 + 记录够买行为 热点商品竞争
            int updateCount = seckillDao.reduceNumber(seckillId, new Date());
            if (updateCount <= 0) {
                //秒杀结束
                throw new SeckillCloseException("seckill is close");
            }
            //秒杀成功
            SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);

            return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
        } catch (SeckillCloseException e) {
            throw e;
        } catch (RepeatKillException e) {
            List<String> list = new ArrayList<String>();
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //所有编译期异常转换为运行时异常
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }
    }
}
