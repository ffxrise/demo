package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 配置junit和spring整合，junit启动时加载springioc容器、
 * spring-test,JUNIT
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件
@ContextConfiguration("classpath:spring/spring-dao.xml")
public class SeckillDaoTest {

    //注入dao实现类依赖
    @Resource
    private SeckillDao seckillDao;

    @Test
    public void queryById() {
        long id = 1000;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    /**
     * org.mybatis.spring.MyBatisSystemException:
     * nested exception is org.apache.ibatis.binding.BindingException:
     * Parameter 'offset' not found. Available parameters are [0, 1, param1, param2]
     */
    //List<Seckill> queryAll(int offset, int limit);
    //java没有保存形参的记录queryAll(int offset, int limit) =》queryAll(arg0, arg1)
    @Test
    public void queryAll() {
        List<Seckill> seckills = seckillDao.queryAll(0, 100);
        for (Seckill seckill : seckills){
            System.out.println(seckill);
        }
    }

    @Test
    public void reduceNumber() {
        Date killTime = new Date();
        int i = seckillDao.reduceNumber(1000L, killTime);
        System.out.println(i);
    }


}