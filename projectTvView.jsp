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
<script type="text/javascript" src="/ProjectView/resources/projecttvview.js${asset_id}"></script>
<title>Project View</title>
</head>
<body style="position: relative; margin-top: 0em">
    <h1 class=projecttvviewheader>Project View</h1>
    <form method="POST" action="<%=request.getContextPath()%>">
        <table class="no border">
            <tr>
                <td colspan="3" style="padding-bottom: 1em;"><select id="prioritySet" class="projecttvviewSelect" name="prioritySet">
                        <option value="" label="Requested Priority">Requested Priority</option>
                        <c:forEach var="item" items="${projectViewForm.prioritySet}">
                            <option value="${item}">${item}</option>
                        </c:forEach>
                </select> <select id="requestDept" class="projecttvviewSelect" name="requestDept">
                        <option value="" label="Requested Department">Requested Department</option>
                        <c:forEach var="item" items="${projectViewForm.requestDeptSet}">
                            <option value="${item}">${item}</option>
                        </c:forEach>
                </select>
                    <button class="btn round" type="submit">Filter</button> <span
                    style="float: right; background-color: rgb(0, 52, 116); color: white; padding: 0px 15px;"
                > </span></td>
            </tr>
            <tr>
                <td style="vertical-align: top; padding-top: 0.1em;"><c:set var="TO PROGRESS" value="${ticket.status}" /> <input type="hidden"
                    value="status">
                    <h4>
                        <b>In Progress</b>
                    </h4>
                    <table style="border: 1px solid grey" id="dashboard2" align="left" class="table table-striped table-bordered" cellspacing="0"
                        cellpadding="0" width="100%" bordercolor="00FF00">
                        <thead>
                            <tr>
                                <th>PR</th>
                                <th>Title</th>
                                <th>Department</th>
                                <th>Stand-up Time</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${projectViewForm.tickets}" var="ticket" varStatus="status">
                                <c:if test="${ticket.status eq 'In Progress'}">
                                    <tr>
                                        <td>${ticket.ticketId}</td>
                                        <td>${ticket.summary}</td>
                                        <td><c:if test="${ticket.requestDept ne ''}"> ${ticket.requestDept}</c:if></td>
                                        <td><c:if test="${ticket.standUpTime ne 'null'}"> ${ticket.standUpTime}</c:if></td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                        </tbody>
                    </table></td>
                <td style="padding: 0.3em;"></td>
                <td style="vertical-align: top; padding-top: 0.1em;"><c:set var="TO DO" value="${ticket.status}" /> 
                <input type="hidden" value="status">
                    <h4>
                        <b>Pending</b>
                    </h4>
                    <table style="border: 1px solid grey" id="dashboard" align="right" class="table table-striped table-bordered" cellspacing="0"
                        cellpadding="0"  bordercolor="00FF00">
                        <thead>
                            <tr>
                                <th>PR</th>
                                <th>Title</th>
                                <th>Department</th>
                                <th>Requested Priority</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${projectViewForm.tickets}" var="ticket" varStatus="status">
                                <c:if test="${ticket.status eq 'To Do'}">
                                    <tr>
                                        <td>${ticket.ticketId}</td>
                                        <td>${ticket.summary}</td>
                                        <td>${ticket.requestDept}</td>
                                        <td>${ticket.priority}</td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                        </tbody>
                    </table></td>
            </tr>
        </table>
    </form>
</html>