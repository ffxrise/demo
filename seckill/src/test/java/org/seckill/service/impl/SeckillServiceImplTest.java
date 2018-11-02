package org.seckill.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exporter;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml","classpath:spring/spring-service.xml"})
public class SeckillServiceImplTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> seckillList = seckillService.getSeckillList();
        logger.info("list={}",seckillList);
    }

    @Test
    public void getById() {
        Seckill seckill = seckillService.getById(1000L);
        logger.info("seckill={}",seckill);
    }

    @Test
    public void exportSeckillUrl() {
        Exporter exporter = seckillService.exportSeckillUrl(1000L);
        logger.info("exporter={}",exporter);
        //exposed=true, md5='1e67a1e2619acde996610ea19a8b2aec', seckillId=1000, now=0, start=0, end=0
    }

    @Test
    public void excuteSeckill() {
        SeckillExecution seckillExecution = seckillService.excuteSeckill(1000L, 18937561223L, "1e67a1e2619acde996610ea19a8b2aec");
        logger.info("seckillExecution={}",seckillExecution);
    }
}