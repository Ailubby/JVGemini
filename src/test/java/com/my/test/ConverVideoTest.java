package com.my.test;

import com.my.jvgemini.Contants;
import com.my.jvgemini.ConverVideoUtils;

/**
 * @author zhe.sun
 * @Description: TODO
 * @date 2019/2/6 9:38
 */
public class ConverVideoTest {
    public void run() {
        try {
            // 转换并截图
            String filePath = Contants.videofolder;
            ConverVideoUtils cv = new ConverVideoUtils(filePath);
            String targetExtension = ".mp4";
            boolean isDelSourseFile = false;
            Long start = System.currentTimeMillis();
            boolean beginConver = cv.threadBatchConver(targetExtension,isDelSourseFile);
            Long end = System.currentTimeMillis();
            System.out.println(beginConver);
            if(beginConver) {
                //批量转换成功
                String dealTime = (end - start) + "";
                System.out.println("——————————————————————————————————————————————批量处理时间:" + dealTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        ConverVideoTest c = new ConverVideoTest();
        c.run();
    }
}
