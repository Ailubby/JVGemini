package com.my.jvgemini.util;

import com.my.jvgemini.aspect.ExcuteTime;
import com.my.jvgemini.common.Contants;
import com.my.jvgemini.thread.TaskConverVideo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhe.sun
 * @Description: 视频格式转换工具
 * @date 2019/2/6 9:25
 */
@Slf4j
@Component
public class ConverVideoUtils {
    private Date dt;
    private long begintime;
    private String sourceVideoPath;//源视频路径
    private String filerealname; // 文件名 不包括扩展名
    private String filename; // 包括扩展名
    private String videofolder = Contants.videofolder; // 别的格式视频的目录
    private String targetfolder = Contants.targetfolder; // flv视频的目录
    private String ffmpegpath = Contants.ffmpegpath; // ffmpeg.exe的目录
    private String mencoderpath = Contants.mencoderpath; // mencoder的目录
    private String imageRealPath = Contants.imageRealPath; // 截图的存放目录

    public ConverVideoUtils() {
        sourceVideoPath = Contants.videofolder;
    }

    public ConverVideoUtils(String path) {
        sourceVideoPath = path;
    }

    public String getPATH() {
        return sourceVideoPath;
    }

    public void setPATH(String path) {
        sourceVideoPath = path;
    }

    @ExcuteTime
    public void testAspect(){
        log.info("11111");
    }

    /**
     * @param targetExtension
     * @param isDelSourseFile
     */
    public boolean batchConver(String targetExtension, boolean isDelSourseFile) {
        boolean result = true;
        File[] files = new File(sourceVideoPath).listFiles();
        for(File file : files) {
            if(result) {
                result = beginConver(targetExtension, isDelSourseFile, file);
            }else{
                break;
            }
        }
        return result;
    }

    /**
     * TODO 优化多线程
     * @param targetExtension
     * @param isDelSourseFile
     */
    @ExcuteTime
    public boolean threadBatchConver(String targetExtension, boolean isDelSourseFile) throws Exception{
        boolean result = true;
        File[] files = new File(sourceVideoPath).listFiles();
        ExecutorService exec = Executors.newFixedThreadPool(files.length);
        CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(exec);
        for(File file : files) {
            TaskConverVideo converVideotask = new TaskConverVideo(file,targetExtension,isDelSourseFile,sourceVideoPath);
            completionService.submit(converVideotask);
        }
        for(int i=0;i<files.length;i++){
            boolean temp = completionService.take().get();
            if (!temp) {
                result = false;
            }
            log.info(result + "\t");
        }
        exec.shutdown();
        return result;
    }

    /**
     * 转换视频格式
     * @param targetExtension 目标视频扩展名 .xxx
     * @param isDelSourseFile 转换完成后是否删除源文件
     * @return
     */
    public boolean beginConver(String targetExtension, boolean isDelSourseFile, File file) {
        filename = file.getName();
        filerealname = filename.substring(0, filename.lastIndexOf(".")).toLowerCase();
        log.info("----接收到文件(" + sourceVideoPath + filename +  ")需要转换-------------------------- ");
        if (!checkfile(sourceVideoPath + filename)) {
            log.info(sourceVideoPath + filename + "文件不存在" + " ");
            return false;
        }
        dt = new Date();
        begintime = dt.getTime();
        log.info("----开始转文件(" + sourceVideoPath + filename + ")-------------------------- ");
        if (process(targetExtension,isDelSourseFile,sourceVideoPath + filename)) {
            Date dt2 = new Date();
            log.info("转换成功 ");
            long endtime = dt2.getTime();
            long timecha = (endtime - begintime);
            String totaltime = sumTime(timecha);
            log.info("转换视频格式共用了:" + totaltime + " ");
            if (processImg(sourceVideoPath + filename)) {
                log.info("截图成功了！ ");
            } else {
                log.info("截图失败了！ ");
            }
            if (isDelSourseFile) {
                deleteFile(sourceVideoPath + filename);
            }
            //sourceVideoPath = null;
            return true;
        } else {
            //sourceVideoPath = null;
            return false;
        }
    }

    /**
     * 对视频进行截图
     * @param sourceVideoPath 需要被截图的视频路径（包含文件名和扩展名）
     * @return
     */
    public boolean processImg(String sourceVideoPath) {
        if (!checkfile(sourceVideoPath)) {
            log.info(sourceVideoPath + " is not file");
            return false;
        }
        File fi = new File(sourceVideoPath);
        filename = fi.getName();
        filerealname = filename.substring(0, filename.lastIndexOf(".")).toLowerCase();
        List<String> command = new java.util.ArrayList<String>();
        //第一帧： 00:00:01
        //time ffmpeg -ss 00:00:01 -i test1.flv -f image2 -y test1.jpg
        command.add(ffmpegpath);
//		command.add("-i");
//		command.add(videoRealPath + filerealname + ".flv");
//		command.add("-y");
//		command.add("-f");
//		command.add("image2");
//		command.add("-ss");
//		command.add("38");
//		command.add("-t");
//		command.add("0.001");
//		command.add("-s");
//		command.add("320x240");
        command.add("-ss");
        command.add("00:00:01");
        command.add("-i");
        command.add(sourceVideoPath);
        command.add("-f");
        command.add("image2");
        command.add("-y");
        command.add(imageRealPath + filerealname + ".jpg");
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(command);
            builder.start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 实际转换视频格式的方法
     * @param targetExtension 目标视频扩展名
     * @param isDelSourseFile 转换完成后是否删除源文件
     * @param sourceVideoPath 源文件url
     * @return
     */
    private boolean process(String targetExtension, boolean isDelSourseFile, String sourceVideoPath) {
        int type = checkContentType(sourceVideoPath);
        boolean status = false;
        if (type == 0) {
            //如果type为0用ffmpeg直接转换
            status = processVideoFormat(sourceVideoPath,targetExtension, isDelSourseFile);
        } else if (type == 1) {
            //如果type为1，将其他文件先转换为avi，然后在用ffmpeg转换为指定格式
            String avifilepath = processAVI(type);
            if (avifilepath == null){
                // avi文件没有得到
                return false;
            }else {
                log.info("开始转换:");
                status = processVideoFormat(avifilepath,targetExtension, isDelSourseFile);
            }
        }
        return status;
    }

    /**
     * 检查文件类型
     * @return
     */
    private int checkContentType(String sourceVideoPath) {
        String type = sourceVideoPath.substring(sourceVideoPath.lastIndexOf(".") + 1, sourceVideoPath.length()).toLowerCase();
        // ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
        if (type.equals("avi")) {
            return 0;
        } else if (type.equals("mpg")) {
            return 0;
        } else if (type.equals("wmv")) {
            return 0;
        } else if (type.equals("3gp")) {
            return 0;
        } else if (type.equals("mov")) {
            return 0;
        } else if (type.equals("mp4")) {
            return 0;
        } else if (type.equals("asf")) {
            return 0;
        } else if (type.equals("asx")) {
            return 0;
        } else if (type.equals("flv")) {
            return 0;
        }
        // 对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等),
        // 可以先用别的工具（mencoder）转换为avi(ffmpeg能解析的)格式.
        else if (type.equals("wmv9")) {
            return 1;
        } else if (type.equals("rm")) {
            return 1;
        } else if (type.equals("rmvb")) {
            return 1;
        }
        return 9;
    }

    /**
     * 检查文件是否存在
     * @param path
     * @return
     */
    private boolean checkfile(String path) {
        File file = new File(path);
        if (!file.isFile()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     *  对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等), 可以先用别的工具（mencoder）转换为avi(ffmpeg能解析的)格式.
     * @param type
     * @return
     */
    private String processAVI(int type) {
        List<String> command = new java.util.ArrayList<String>();
        command.add(mencoderpath);
        command.add(sourceVideoPath);
        command.add("-oac");
        command.add("mp3lame");
        command.add("-lameopts");
        command.add("preset=64");
        command.add("-ovc");
        command.add("xvid");
        command.add("-xvidencopts");
        command.add("bitrate=600");
        command.add("-of");
        command.add("avi");
        command.add("-o");
        command.add(videofolder + filerealname + ".avi");
        // 命令类型：mencoder 1.rmvb -oac mp3lame -lameopts preset=64 -ovc xvid
        // -xvidencopts bitrate=600 -of avi -o rmvb.avi
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(command);
            Process p = builder.start();
            doWaitFor(p);
            return videofolder + filerealname + ".avi";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 转换为指定格式
     * ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
     * @param oldfilepath
     * @param targetExtension 目标格式扩展名 .xxx
     * @param isDelSourceFile 转换完成后是否删除源文件
     * @return
     */
    private boolean processVideoFormat(String oldfilepath, String targetExtension, boolean isDelSourceFile) {
        if (!checkfile(oldfilepath)) {
            log.info(oldfilepath + " is not file");
            return false;
        }
        //ffmpeg -i FILE_NAME.flv -ar 22050 NEW_FILE_NAME.mp4
        List<String> command = new java.util.ArrayList<String>();
        command.add(ffmpegpath);
        command.add("-i");
        command.add(oldfilepath);
        command.add("-ar");
        command.add("22050");
        command.add(targetfolder + filerealname + targetExtension);
        try {
            ProcessBuilder builder = new ProcessBuilder();
            String cmd = command.toString();
            builder.command(command);
            Process p = builder.start();
            doWaitFor(p);
            p.destroy();
            //转换完成后删除源文件
//			if (isDelSourceFile) {
//				deleteFile(sourceVideoPath);
//			}
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
     * @param oldfilepath
     * @return
     */
    private boolean processFLV(String oldfilepath) {
        if (!checkfile(sourceVideoPath)) {
            log.info(oldfilepath + " is not file");
            return false;
        }
        List<String> command = new java.util.ArrayList<String>();
        command.add(ffmpegpath);
        command.add("-i");
        command.add(oldfilepath);
        command.add("-ab");
        command.add("64");
        command.add("-acodec");
        command.add("mp3");
        command.add("-ac");
        command.add("2");
        command.add("-ar");
        command.add("22050");
        command.add("-b");
        command.add("230");
        command.add("-r");
        command.add("24");
        command.add("-y");
        command.add(targetfolder + filerealname + ".flv");
        try {
            ProcessBuilder builder = new ProcessBuilder();
            String cmd = command.toString();
            builder.command(command);
            Process p = builder.start();
            doWaitFor(p);
            p.destroy();
            deleteFile(oldfilepath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int doWaitFor(Process p) {
        InputStream in = null;
        InputStream err = null;
        int exitValue = -1; // returned to caller when p is finished
        try {
            log.info("coming");
            in = p.getInputStream();
            err = p.getErrorStream();
            boolean finished = false; // Set to true when p is finished

            while (!finished) {
                try {
                    while (in.available() > 0) {
                        Character c = new Character((char) in.read());
                        System.out.print(c);
                    }
                    while (err.available() > 0) {
                        Character c = new Character((char) err.read());
                        System.out.print(c);
                    }

                    exitValue = p.exitValue();
                    finished = true;

                } catch (IllegalThreadStateException e) {
                    Thread.currentThread().sleep(500);
                }
            }
        } catch (Exception e) {
            log.warn("doWaitFor();: unexpected exception - " + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

            } catch (IOException e) {
                log.info(e.getMessage());
            }
            if (err != null) {
                try {
                    err.close();
                } catch (IOException e) {
                    log.info(e.getMessage());
                }
            }
        }
        return exitValue;
    }

    public void deleteFile(String filePath) {
        File file=new File(filePath);
        if(file.exists()&&file.isFile())
            file.delete();
    }

    public String sumTime(Long totalTime) {
        return totalTime + "";
    }
}

