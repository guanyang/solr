package org.gy.solr.listener;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FileUtils;
import org.gy.solr.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class SystemListener implements ServletContextListener {

    private static final Logger logger         = LoggerFactory.getLogger(SystemListener.class);

    private static final String SOLR_HOME_NAME = "solr_home";

    private static final String SOLR_HOME_PATH = PropertiesUtil.getValue("solr.home");

    @Override
    public void contextInitialized(ServletContextEvent event) {
        /******** jul to slf4j *********/
        SLF4JBridgeHandler.install();
        // 复制配置文件
        copyFile(event);

    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        /******** jul to slf4j *********/
        SLF4JBridgeHandler.uninstall();
    }

    private void copyFile(ServletContextEvent event) {
        logger.info("SolrHomeConfig init start");
        String classPath = event.getServletContext().getRealPath("/");
        StringBuilder builder = new StringBuilder(classPath);
        builder.append(File.separator).append("WEB-INF").append(File.separator).append("classes").append(File.separator).append(SOLR_HOME_NAME);
        logger.info("classPath=" + builder.toString());

        File homeDir = new File(SOLR_HOME_PATH);
        if (!homeDir.exists()) {
            // 创建目录
            homeDir.mkdirs();
            
            try {
                FileUtils.copyDirectory(new File(builder.toString()), homeDir);
            } catch (IOException e) {
                logger.error("copyToPath error:" + e.getMessage(), e);
            }
        }

    }

}
