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
<script src="https://code.jquery.com/jquery-1.10.2.js"></script>
<script src="/ProjectView/resources/jquery-ui.js"></script>
<script src="/ProjectView/resources/jquery.dataTables.min.js"></script>
<script src="/ProjectView/resources/jquery.dataTables.natural.js"></script>
<script src="/ProjectView/resources/bootstrap.min.js"></script>
<script type="text/javascript" src="/ProjectView/resources/irview.js${asset_id}"></script>
<title>IR View</title> 
</head>

<script>   
        $("div.wrapper").scrollLeft();
</script>

<body>
  <div class="wrapper">
    <h1 class="projectviewheader">IR View</h1>
    <form method="POST" action="<%=request.getContextPath()%>/ir">
         <table>
            <tr>
                <td><select id="prioritySet" class="projectviewSelect" name="prioritySet">
                        <option value="" label="Select Priority">Select Priority</option>
                        <c:forEach var="item" items="${projectViewForm.irPriorityList}">
                            <option value="${item}">${item}</option>
                        </c:forEach>
                </select></td>
                
                <td><select id="status" class="projectviewSelect" name="status">
                        <option value="" label="Select Status">Select Status</option>
                        <c:forEach var="item" items="${projectViewForm.irStatusSet}">
                            <option value="${item}">${item}</option>
                        </c:forEach>
                </select></td>
                
                <td><select id="department" class="projectviewSelect" name="department">
                        <option value="" label="Select Department">Select Department</option>
                        <c:forEach var="item" items="${projectViewForm.requestDeptSet}">
                            <option value="${item}">${item}</option>
                        </c:forEach>
                </select></td>
                
                <td>
                    <button class="btn round" type="submit" style="margin-bottom: -39px;">Filter</button>
                </td>
                <!-- <td>
                    <button class="btn round" type="submit" style="margin-bottom: -39px; margin-left: 10%" name="showOlderTicket" value= true>Show older closed ticket</button>
                </td> -->
            </tr>
        </table>
    </form>
    <br /><br />
    <table style="border: 1px solid grey" id="dashboard" class="table table-striped table-bordered" cellspacing="0" cellpadding="0" width="92%"
        bordercolor="00FF00">
        <thead>
            <tr>
                <th>IR-Number</th>
                <th>Heat Ticket #</th>
                <th>Title</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Requesting Department</th>                
                <th>Expected UAT Date</th>
                <th>Test Server Url</th>
             </tr>
        </thead>
        <tbody>
            <c:forEach items="${projectViewForm.tickets}" var="viewIr" varStatus="status">
                <tr>
                    <td>${viewIr.ticketId}</td>
                    <td>${viewIr.heat}</td>
                    <td>${viewIr.summary}</td>
                    <td>${viewIr.irPriority}</td>
                    <td>${viewIr.status}</td>
                    <td>${viewIr.requestDept}</td>
                    <td>${viewIr.expectedUatDate}</td>
                    <td>${viewIr.testUrl}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
  </div>    
</body>
</html>