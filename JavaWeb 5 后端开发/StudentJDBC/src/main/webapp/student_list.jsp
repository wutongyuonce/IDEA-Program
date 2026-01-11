<%@ page import="com.easy.web.pojo.Student" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/bootstrap-3.4.1-dist/css/bootstrap.css">
</head>
<body>
<%
    //JSP页面中可以嵌套Java代码
    //JSP脚本：在这里可以写任意的Java代码
    //request、response:JSP页面的内置对象
    List<Student> list = (List<Student>) request.getAttribute("list");
%>
<a class="btn btn-primary" href="student_add.jsp">添加</a>
<table class="table table-striped table-bordered table-hover table-condensed">
    <tr>
        <td>ID</td>
        <td>名字</td>
        <td>年龄</td>
        <td>性别</td>
        <td>编辑</td>
        <td>删除</td>
    </tr>
    <%
        if (list != null) {
            for (Student student : list) {
    %>
    <tr>
        <td><%=student.getId()%>
        </td>
        <td><%=student.getName()%>
        </td>
        <td><%=student.getAge()%>
        </td>
        <td><%=student.getGender()%>
        </td>
        <td><a href="student?method=toUpdate&id=<%=student.getId()%>">编辑</a></td>
        <%--/deleteStudent?id=12 --%>
        <%--<td><a href="/deleteStudent?id=<%=student.getId()%>">删除</a></td>--%>
        <%--<td><a href="/student?method=deleteById&id=<%=student.getId()%>">删除</a></td>--%>
        <td><a href="javascript:deleteById(<%=student.getId()%>)">删除</a></td>
    </tr>
    <%
        }
    } else {
    %>
    <tr>
        <td colspan="6">没有学生数据</td>
    </tr>
    <% } %>
</table>

<script>
    function deleteById(id) {
        var isDelete = confirm('您确认要删除？');
        if (isDelete) {
            const baseUrl = '${pageContext.request.contextPath}/student?method=deleteById';
            location.href = baseUrl + '&id=' + id;
        }
    }
</script>
</body>
</html>