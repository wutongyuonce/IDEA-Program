<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<form action="${pageContext.request.contextPath}/student?method=add" method="post">
    ID：<input type="text" name="id"/><br/>
    用户名：<input type="text" name="name"/><br/>
    年龄：<input type="text" name="age"/><br/>
    性别：<input type="text" name="gender"/><br/>
    <input type="submit" value="添加"/>
</form>
</body>
</html>
