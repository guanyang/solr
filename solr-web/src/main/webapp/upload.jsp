<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>   
<!DOCTYPE HTML>
<html>
<head>
<title>文件上传</title>
</head>

<body>
	<form action="${pageContext.request.contextPath}/upload"
		enctype="multipart/form-data" method="post">
		上传路径：<input type="text" name="uploadPath">不填，则取系统配置路径<br/>
		上传文件：<input type="file" name="uploadFile"><br />
		如果文件名称同服务器一致，则直接覆盖服务器文件<br/>
		<input type="submit" value="提交">
	</form>
</body>
</html>