<%@ page import="com.easy.web.pojo.Student" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Title</title>
    <link rel="stylesheet" href="/static/bootstrap-3.4.1-dist/css/bootstrap.css">
</head>
<body><% //JSP页面中可以嵌套Java代码 //JSP脚本：在这里可以写任意的Java代码 //request、response:JSP页面的内置对象 List<Student> list = (List<Student>) request.getAttribute("list"); %>
<table class="table table-striped table-bordered table-hover table-condensed">
    <tr>
        <td>ID</td>
        <td>名字</td>
        <td>年龄</td>
        <td>性别</td>
        <td>编辑</td>
        <td>删除</td>
    </tr>
    <% for (Student student : list) { %>
    <tr>
        <td><%=student.getId()%>
        </td>
        <td><%=student.getName()%>
        </td>
        <td><%=student.getAge()%>
        </td>
        <td><%=student.getGender()%>
        </td>
        <td>编辑</td>
        <td>删除</td>
    </tr>
    <% } %></table>
</body>
</html>
