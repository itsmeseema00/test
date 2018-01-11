<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<link href="/ProjectView/resources/bootstrap.min.css" rel="stylesheet">
<link href="/ProjectView/resources/jquery.dataTables.min.css" rel="stylesheet">
<link href="/ProjectView/resources/jquery-ui.css" rel="stylesheet">
<link href="/ProjectView/resources/Features.css" rel="stylesheet">
<script src="/ProjectView/resources/jquery-2.1.1.min.js"></script>
<script src="/ProjectView/resources/jquery-ui.js"></script>
<script src="/ProjectView/resources/jquery.dataTables.min.js"></script>
<script src="/ProjectView/resources/bootstrap.min.js"></script>
<script type="text/javascript" src="/ProjectView/resources/projectview.js${asset_id}"></script>
<title>ProjectView</title>
</head>
<body>
    <h1 class="projectviewheader">Project View</h1>
    <form method="POST" action="<%=request.getContextPath()%>/webteam">
        <table>
            <tr>
                <td><select id="status" class="projectviewSelect" name="status">
                        <option value="" label="Select Status">Status</option>
                        <option value="IN PROGRESS">IN PROGRESS</option>
                        <option value="TO DO">To DO</option>
                </select></td>
                <td><select id="requestDept" class="projectviewSelect" name="requestDept">
                        <option value="" label="Select Request Dept">Requesting Department</option>
                        <c:forEach var="item" items="${projectViewForm.requestDeptSet}">
                            <option value="${item}">${item}</option>
                        </c:forEach>
                </select></td>
                <td><select id="requestStatus" class="projectviewSelect" name="requestStatus">
                        <option value="" label="Select Status">Request Status</option>
                        <c:forEach var="item" items="${projectViewForm.requestStatusSet}">
                            <option value="${item}">${item}</option>
                        </c:forEach>
                </select></td>
                <td>
                    <button class="btn round" input type="submit" style="margin-bottom: -39px;">Filter</button>
                </td>
                
            </tr>
        </table>
    </form>
    
    <br />
    <table style="border: 1px solid grey" id="dashboard" class="table table-striped table-bordered" cellspacing="0" cellpadding="0" width="92%"
        bordercolor="00FF00"
    >
        <thead>
            <tr>
                <th>Key</th>
                <th>Summary</th>
                <th>Requesting Department</th>
                <th>Request Status</th>
                <th>Order</th>
                <th>Status</th>
                <th>BA</th>
                <th>Lead</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${projectViewForm.tickets}" var="ticket" varStatus="status">
                <tr>
                    <td>${ticket.ticketId}
                    <td>${ticket.summary}</td>
                    <td>${ticket.requestDept}</td>
                    <td>${ticket.requestStatus}</td>
                    <td>${ticket.order}</td>
                    <td>${ticket.status}</td>
                    <td>${ticket.ba}</td>
                    <td>${ticket.lead}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</body>
</html>