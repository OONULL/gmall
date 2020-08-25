package com.sq.gmall.seckill.controller;

import com.sq.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @title: SeckillController
 * @Description
 * @Author sq
 * @Date: 2020/8/22 21:12
 * @Version 1.0
 */
@Controller
public class SeckillController {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RedissonClient redissonClient;

    @RequestMapping("kill")
    @ResponseBody
    public String kill(){
        /*Jedis jedis = redisUtil.getJedis();
        //开启监控
        jedis.watch("106");
        int stock = Integer.parseInt(jedis.get("106"));
        if(stock>0){
            Transaction multi = jedis.multi();
            multi.incrBy("106",-1);
            List<Object> exec = multi.exec();
            if(exec!=null&&exec.size()>0){
                System.out.println(stock+":"+(100000-stock));
                //消息队列发送订单消息
            }else {
                System.out.println(stock+"抢购失败");
            }
        }
        jedis.close();*/

        RSemaphore semaphore = redissonClient.getSemaphore("106");
        boolean b = semaphore.tryAcquire();
        if(b){
            System.out.println("抢购成功");
            //消息队列发送订单消息
        }else {
            System.out.println("抢购失败");
        }
        return "1";
    }
}
