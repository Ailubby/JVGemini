package com.my.test;



import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;

/**
 * @author zhe.sun
 * @Description: TODO
 * @date 2019/2/17 13:11
 */
public class LogTest {

    /**
     * @param args
     */
    public static void main(String[] args)  throws IOException, JoranException {

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        File externalConfigFile = new File("E:\\workspaces\\JVGemini\\src\\main\\resources\\logback.xml");
        if(!externalConfigFile.exists()){
            throw new IOException("Logback External Config File Parameter does not reference a file that exists");
        }else{
            if(!externalConfigFile.isFile()){
                throw new IOException("Logback External Config File Parameter exists, but does not reference a file");
            }else{
                if(!externalConfigFile.canRead()){
                    throw new IOException("Logback External Config File exists and is a file, but cannot be read.");
                }else{
                    JoranConfigurator configurator = new JoranConfigurator();
                    configurator.setContext(lc);
                    lc.reset();
                    configurator.doConfigure("E:\\workspaces\\JVGemini\\src\\main\\resources\\logback.xml");
                    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
                }
            }
        }

        Logger logger = LoggerFactory.getLogger(LogTest.class);
        logger.info("log success!");
    }
}
