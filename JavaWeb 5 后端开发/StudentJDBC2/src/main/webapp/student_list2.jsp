<%@ page import="com.easy.web.pojo.Student" %>
<%@ page import="java.util.List" %>
<%@ page import="com.easy.web.PageInfo" %>
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
    PageInfo pageInfo = (PageInfo) request.getAttribute("pageInfo");
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
        if (pageInfo != null && pageInfo.getList() != null) {
            for (Student student : (List<Student>) pageInfo.getList()) {
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
    <%
        }
    %>
</table>

<!-- 新增的分页条数选择器 -->
<div class="form-inline" style="margin: 20px 0">
    <div class="form-group">
        <label>每页显示：</label>
        <select class="form-control" id="pageSizeSelect"
                onchange="changePageSize(this.value)"
                style="margin-left: 10px; width: 100px">
            <option value="4" <%= pageInfo!=null && pageInfo.getPageSize()==4 ? "selected" : "" %>>4条</option>
            <option value="5" <%= pageInfo!=null && pageInfo.getPageSize()==5 ? "selected" : "" %>>5条</option>
            <option value="6" <%= pageInfo!=null && pageInfo.getPageSize()==6 ? "selected" : "" %>>6条</option>
        </select>
    </div>
</div>

<!-- 新增的页面选择跳转器 -->
<nav aria-label="Page navigation">
    <ul class="pagination">
        <%--点击跳转上一页--%>
        <%
            if(pageInfo != null && pageInfo.getPageNo() != null && pageInfo.getPageSize() != null){
                if (pageInfo.getPageNo() == 1) {
        %>
        <li class="disabled">
            <a href="#" aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
            </a>
        <li>
        <%
                }else{
        %>
        <li>
            <a href="${pageContext.request.contextPath}/student?method=selectByPage&pageNo=<%=pageInfo.getPageNo()-1%>&pageSize=<%=pageInfo.getPageSize()%>"
               aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
            </a>
        </li>
        <%
                }
            }
        %>

        <%--循环得到跳转每一页的选项栏--%>
        <%
            if(pageInfo != null && pageInfo.getPageNo() != null && pageInfo.getPageSize() != null &&
                    pageInfo.getTotalPage() != null){
                for (int i = 1; i <= pageInfo.getTotalPage(); i++) {
                    if (i == pageInfo.getPageNo()) {
        %>
        <li class="active"><a href="#"><%=i%>
        </a></li>
        <%
                    } else {
        %>
        <li><a href="${pageContext.request.contextPath}/student?method=selectByPage&pageNo=<%=i%>&pageSize=<%=pageInfo.getPageSize()%>">
            <%=i%>
        </a></li>
        <%
                    }
                }
            }
        %>

        <%--点击跳转下一页--%>
        <%
            if(pageInfo != null && pageInfo.getPageNo() != null && pageInfo.getPageSize() != null){
                if (pageInfo.getPageNo() == pageInfo.getTotalPage()) {
        %>
        <li class="disabled">
            <a href="#" aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
            </a>
        <li>
                <%
                }else{
        %>
        <li>
            <a href="${pageContext.request.contextPath}/student?method=selectByPage&pageNo=<%=pageInfo.getPageNo()+1%>&pageSize=<%=pageInfo.getPageSize()%>"
               aria-label="Previous">
                <span aria-hidden="true">&raquo;</span>
            </a>
        </li>
        <%
                }
            }
        %>
    </ul>
</nav>

<script>
    // 新增的切换每页条数函数
    function changePageSize(pageSize) {
        // 跳转到第一页，带上新的pageSize参数
        const baseUrl = '${pageContext.request.contextPath}/student?method=selectByPage';
        location.href = baseUrl + '&pageNo=1&pageSize=' + pageSize;
    }

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
