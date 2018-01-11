<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>

<head>
    <title>Project view Error Page</title>
</head>
<body>
    <strong>Error Occured</strong> <br/>
    <h4>Error Info ::: </h4> <br/> <c:out value="${errorMessage}" />
</body>
</html>