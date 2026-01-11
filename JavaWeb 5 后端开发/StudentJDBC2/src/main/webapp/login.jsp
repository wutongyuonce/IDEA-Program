<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>登录</title>
</head>
<body>
<form action="${pageContext.request.contextPath}/user?method=login" method="post">
    <%-- 从 Cookie 中读取记住的用户名 --%>
    用户名：<input type="text" name="name" value="${cookie.rememberedName.value}"><br>
    密码：<input type="password" name="password"><br>
        <%-- 根据 Cookie 是否存在自动勾选复选框 --%>
        <input type="checkbox" name="remember" value="true"
               <c:if test="${not empty cookie.rememberedName}">checked</c:if>> 记住用户名<br>
        <input type="submit" value="登录">
</form>
</body>
</html>
