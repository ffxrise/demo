package org.seckill.dao.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * 配置junit和spring整合，junit启动时加载springioc容器、
 * spring-test,JUNIT
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件
@ContextConfiguration("classpath:spring/spring-dao.xml")
public class RedisDaoTest {
    @Autowired
    private RedisDao redisDao;

    private long id = 1002;
    @Autowired
    private SeckillDao seckillDao;
    @Test
    public void testSeckill() {
        //get and put
        Seckill seckill =redisDao.getSeckill(id);
        System.out.println(seckill);
        if (seckill==null){
            seckill = seckillDao.queryById(id);
            if (seckill!=null){
                String result = redisDao.putSeckilll(seckill);
                System.out.println(result);
                Seckill seckill1 = redisDao.getSeckill(id);
                System.out.println(seckill);
            }
        }
    }


}