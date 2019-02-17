package com.my.jvgemini.thread;

import com.my.jvgemini.ConverVideoUtils;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * @author zhe.sun
 * @Description: 格式转换线程
 * @date 2019/2/9 13:26
 */
public class TaskConverVideo implements Callable<Boolean> {

    private File file;

    private String targetExtension;

    private boolean isDelSourseFile;

    private String filePath;

    public TaskConverVideo(File file, String targetExtension, boolean isDelSourseFile, String filePath) {
        this.file = file;
        this.targetExtension = targetExtension;
        this.isDelSourseFile = isDelSourseFile;
        this.filePath = filePath;
    }

    public Boolean call() throws Exception {
        ConverVideoUtils cv = new ConverVideoUtils(filePath);
        return cv.beginConver(targetExtension, isDelSourseFile, file);
    }
}
