/*
 * Copyright (C), 2002-2017, guanyang
 * FileName: UploadServlet.java
 * Author:   guanyang
 * Date:     2017年4月26日 下午5:43:40
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package org.gy.solr.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 功能描述：
 * 
 * @version 2.0.0
 * @author guanyang
 */
public class UploadServlet extends HttpServlet {

    private static final long   serialVersionUID  = 3264138868318471932L;

    private static final String UPLOAD_PATH_PARAM = "uploadPath";
    private static final Logger LOGGER            = LoggerFactory.getLogger(UploadServlet.class);

    private static final String DEFAULT_ENCODE    = "UTF-8";

    private static final String DEFAULT_PATH      = PropertiesUtil.getValue("solr.home");

    private static final String DEFAULT_TMP       = PropertiesUtil.getValue("solr.path") + File.separator + "uploadTemp";

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) {
        // 判断提交上来的数据是否是上传表单的数据
        if (!ServletFileUpload.isMultipartContent(req)) {
            // 按照传统方式获取数据
            forward(req, resp, "没有上传文件");
            return;
        }
        // 上传时生成的临时文件保存目录
        File file = checkFilePath(DEFAULT_TMP);

        StringBuilder builder = new StringBuilder();
        try {
            // 创建一个文件上传解析器
            ServletFileUpload fileUpload = wrapServletFileUpload(file);
            // 使用ServletFileUpload解析器解析上传数据，解析结果返回的是一个List<FileItem>集合，每一个FileItem对应一个Form表单的输入项
            List<FileItem> list = fileUpload.parseRequest(req);
            // 检查文件存储路径，如果沒有指定路径，则取默认路径
            String path = getFilePath(list);
            checkFilePath(path);

            for (FileItem item : list) {
                String str = wrapFileUpload(item, path);
                if (str != null) {
                    builder.append(str);
                }
            }
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            LOGGER.error("单个文件超出最大限制：" + e.getMessage(), e);
            builder.append("单个文件超出最大限制：" + e.getMessage());
        } catch (FileUploadBase.SizeLimitExceededException e) {
            LOGGER.error("上传文件的总的大小超出限制的最大值：" + e.getMessage(), e);
            builder.append("上传文件的总的大小超出限制的最大值：" + e.getMessage());
        } catch (FileUploadException e) {
            LOGGER.error("文件上传异常：" + e.getMessage(), e);
            builder.append("文件上传异常：" + e.getMessage());
        }
        String message = builder.toString();
        if ("".equals(message)) {
            message = "上传文件数据错误";
        }
        forward(req, resp, message);

    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) {
        doGet(req, resp);
    }

    /**
     * 功能描述:页面跳转输出
     * 
     * @param req
     * @param resp
     * @param message
     */
    private void forward(HttpServletRequest req,
                         HttpServletResponse resp,
                         String message) {
        req.setAttribute("message", message);
        try {
            req.getRequestDispatcher("/message.jsp").forward(req, resp);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private ServletFileUpload wrapServletFileUpload(File repository) {
        // 使用Apache文件上传组件处理文件上传步骤：
        // 1、创建一个DiskFileItemFactory工厂
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        // 设置工厂的缓冲区的大小，当上传的文件大小超过缓冲区的大小时，就会生成一个临时文件存放到指定的临时目录当中。
        diskFileItemFactory.setSizeThreshold(1024 * 100);
        // 设置上传时生成的临时文件的保存目录
        diskFileItemFactory.setRepository(repository);
        // 2、创建一个文件上传解析器
        ServletFileUpload fileUpload = new ServletFileUpload(diskFileItemFactory);
        // 解决上传文件名的中文乱码
        fileUpload.setHeaderEncoding(DEFAULT_ENCODE);
        // 监听文件上传进度
        fileUpload.setProgressListener(new ProgressListener() {
            @Override
            public void update(long readedBytes,
                               long totalBytes,
                               int currentItem) {
                LOGGER.info("当前已处理：{}，文件大小为：{}，文件索引：{}", readedBytes, totalBytes, currentItem);
            }
        });
        // 设置上传单个文件的大小的最大值，目前是设置为1024*1024字节，也就是5MB
        fileUpload.setFileSizeMax(1024 * 1024 * 5L);
        // 设置上传文件总量的最大值，最大值=同时上传的多个文件的大小的最大值的和，目前设置为50MB
        fileUpload.setSizeMax(1024 * 1024 * 50L);

        return fileUpload;
    }

    /**
     * 功能描述:
     * 
     * @param item
     */
    private String wrapFileUpload(FileItem item,
                                  String path) {
        if (item.isFormField()) {
            // 如果fileitem中封装的是普通输入项的数据，则不处理
            return null;
        }
        // 如果fileitem中封装的是上传文件，得到上传的文件名称，
        String fileName = item.getName();
        if (fileName == null || "".equals(fileName.trim())) {
            return item.getFieldName() + "：文件名称为空<br/>";
        }
        // 处理获取到的上传文件的文件名的路径部分，只保留文件名部分
        fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        InputStream fis = null;
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            // 获取item中的上传文件的输入流
            fis = item.getInputStream();
            // 创建一个文件输出流
            fos = new FileOutputStream(path + File.separator + fileName);

            bis = new BufferedInputStream(fis);

            bos = new BufferedOutputStream(fos);

            byte[] b = new byte[1024];

            int len = 0;

            while ((len = bis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            bos.flush();

            // 删除处理文件上传时生成的临时文件
            item.delete();

            return "文件：" + fileName + "，上传成功<br/>";

        } catch (Exception e) {
            LOGGER.error("上传文件异常：" + e.getMessage(), e);
            return "文件：" + fileName + "，上传异常：" + e.getMessage() + "<br/>";
        } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(fis);
        }
    }

    private String getFilePath(List<FileItem> list) {
        for (FileItem item : list) {
            String path = validateUploadPathParam(item);
            if (path != null && !"".equals(path.trim())) {
                return path;
            }
        }
        return DEFAULT_PATH;
    }

    private String validateUploadPathParam(FileItem item) {
        if (item.isFormField() && UPLOAD_PATH_PARAM.equals(item.getFieldName())) {
            try {
                String path = item.getString(DEFAULT_ENCODE);
                if (path != null && !"".equals(path.trim())) {
                    return path;
                }
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return null;
    }

    private File checkFilePath(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

}
