<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<link href="/DocumentUploadAdmin/resources/css/bootstrap.css" rel="stylesheet">
<link href="/DocumentUploadAdmin/resources/js/bootstrap.js" rel="stylesheet">
<link href="/DocumentUploadAdmin/resources/css/Features.css" rel="stylesheet">
<style>
   
</style>

<script></script>
    <title>Admin Web Page</title>
      
       <form method="POST" action="/DocumentUploadAdmin/sendEmail" modelAttribute="adminForm">
        <div class="container">
        <br><br>
         <p><strong>Document Upload</strong></p>   
            <div class="form-group">
                 <label>To</label>
                 <input id ="emailTo" class="form-control" name= "emailTo">
             </div>
             <div class="form-group">
                 <label>CC</label>
                 <input id ="emailCc" class="form-control" name= "emailCc">
             </div>
             <div class="form-group">
                  <textarea class="form-control" id="emailBody" name= "emailBody" rows="3" placeholder="Compose Email Body"></textarea>
             </div> 
            <div class="form-group">
                 <label>Loan Id</label>
                 <input id ="loanId" class="form-control" name= "loanId">
             </div>
             <div class="form-group">
                <label>Decide later</label>
                <input id="taskId" class="form-control" name= "taskId">
             </div>             
             <div class="form-group">
              <label>Select Department</label>
                <select id="department" class="form-control" name="department">
                        <option value="" label="Select Department">Department</option>
                        <option value="business">Business</option>
                        <option value="mortgage">Mortgage</option>
                </select>
              </div>
              <button class="btn btn-primary" type="submit">Next</button>
              <br>
        </div>
    </form>
 

