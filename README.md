##CrAc

##Endpoints

###Login-related

####THE CREDENTIALS (NAME AND PASSWORD) FOR ATHENTICATION MUST ALWAYS BE DELIVERED IN THE HEADER OF THE REQUEST AS BASIC AUTHENTICATION! 
If this is not the case, the server will just return an "unauthorized"-message!
####If the logged in user does not posses the rights for executing the method at a given endpoint, there will be a 403-message as return value. 
Eg.: A user without admin-rights tries to delete another user.

**Get login response**

#####*Request:*

GET /user/login

->the name and password have to be added in the header as the basic-authentication

#####*Response:*

If the name and password transferred in the header are correct:

	{
		"user": "userName",
		"login": "true
	}

else the standard unauthorized-message will appear:

	{
		"status": 401, 
		"error": "Unauthorized"
	}
	
-----------------------------------------------------------------

###User-Functions

**Get all users**

#####*Request:*

GET /user

#####*Response:*

An array containing all users

	[
		{
			"id": 1,
			"name": "testUser",
			...
		},
		{
			"id": 2,
			"name": "AnotherUser",
			...
		}
	]
	
-----------------------------------------------------------------
	
**Get one user by ID**

#####*Request:*

GET /user/{id}

#####*Response:*

A user object with given id

	{
		"id": {id},
		"name": "searchedUser",
		...
	}

-----------------------------------------------------------------

**Create a new user**

#####*Request:*

POST /user

####This function requires ADMIN-rights!

	{
	    "name":"test",
	    "password": "test",
	    "role":"USER",
	    "firstName":"TestHans",
	    "lastName":"TestName",
	    "phone":"234",
	    "email":"asd@asd"
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Delete a user with given ID**

#####*Request:*

DELETE /user/{id}
####This function requires ADMIN-rights!

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Update the data of a user with given ID**

#####*Request:*

PUT /user/{id}
####This function requires ADMIN-rights!

Updates a user by given ID

	{
	    "name":"test",
	    "password": "test",
	    "role":"USER",
	    "firstName":"TestHans",
	    "lastName":"TestName",
	    "phone":"234",
	    "email":"asd@asd"
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Get the data of the currently logged in user**

#####*Request:*

GET /user/me

#####*Response:*

The object of the currently logged in user

	{
		"id": 3,
		"name": "currentUser",
		...
	}

-----------------------------------------------------------------

**Update the data of the currently logged in user**

#####*Request:*

PUT /user/updateMe

Updates the currently logged in user

	{
	    "name":"currentUser",
	    "password": "test",
	    "role":"ADMIN",
	    "firstName":"TestHans",
	    "lastName":"TestName",
	    "phone":"234",
	    "email":"asd@asd"
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

###Competence-Endpoints on logged in user

**Add a competence with given ID to the currently logged in user**

#####*Request:*

GET user/addCompetence/{competence_id}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Remove a competence with given ID from the currently logged in user**

#####*Request:*

GET user/removeCompetence/{competence_id}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

###Task-Endpoints on logged in user

**Add a task with given ID to the open-tasks (kind of the to-do list) of the logged in user**

#####*Request:*

GET user/addTask/{task_id}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Removes the task with given id from the open-tasks of the currently logged in user**

#####*Request:*

GET user/removeTask/{task_id}


#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Adds the task with given id to the followed-tasks of the currently logged in user**

#####*Request:*

GET user/followTask/{task_id}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Removes the task with given id from the follow-tasks of the currently logged in user**

#####*Request:*

GET user/unfollowTask/{task_id}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Adds the task with given id to the leading-tasks of the currently logged in user**

#####*Request:*

GET user/leadTask/{task_id}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Removes the task with given id from the leading-tasks of the currently logged in user**

#####*Request:*

GET user/abandonTask/{task_id}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

###Project and Task-Endpoints

####PROJECT IS THE SUPER-TYPE TO A TASK
User-Interaction happens on the task-level, but every task has to be assigned to a project.
So while REST-functions on tasks are possible, it is highly recommended to use the other comfort functions for this, because of inconsistency reasons.
Inconsistency producing REST-calls will also not be mentioned in this readme, they can be however looked up in the javadoc-documentation of the project if needed.

####First the endpoints of the projects themselves

**Gets all projects**

#####*Request:*

GET /project

#####*Response:*

An array containing all projects

	[
		{
			"id": 1,
			"name": "testProject",
			"childTasks": [
				{
					"id": 1,
					"name": "testTask"
				},
				...
			],
			...
		},
		{
			"id": 2,
			"name": "AnotherProject",
			...
		}
	]

-----------------------------------------------------------------

**Gets one project with given ID**

#####*Request:*

GET /project/{project_id}

#####*Response:*

	{
		"id": {id},
		"name": "searchedProject",
		childTasks": [
				{
					"id": 1,
					"name": "testTask"
				},
				...
			],
			...
		...
	}
	
-----------------------------------------------------------------

**Creates a new project**

#####*Request:*

POST /project

	{
	    "name": "testProject",
	    "description": "this is a test",
	    "location": "Vienna",
	    "startTime": "2000-01-01T00:30:00",
	    "endTime": "2000-01-01T01:00:00"
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Deletes a project with given id**

#####*Request:*

DELETE /project/{project_id}
####This function requires ADMIN-rights!


#####*Response:*

Json-data, either a success or a failure message
	
-----------------------------------------------------------------

**Updates the project with given id**	

#####*Request:*

PUT /project/{project_id}
####This function requires ADMIN-rights!


	{
	    "name": "testProject",
	    "description": "this is a test",
	    "location": "Vienna",
	    "startTime": "2000-01-01T00:30:00",
	    "endTime": "2000-01-01T01:00:00"
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Creates a task and adds it to the given project.**

#####*Request:*

POST /{project_id}/addTask

	{
	    "name": "testProject",
	    "description": "this is a test",
	    "location": "Vienna",
	    "startTime": "2000-01-01T00:30:00",
	    "endTime": "2000-01-01T01:00:00",
	    "urgency": 4,
	    "amountOfVolunteers": 25
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Deletes a task by given id and removes it from the chosen project**

#####*Request:*

DELETE /project/{project_id}/removeTask/{task_id}
####This function requires ADMIN-rights!

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

####The endpoints of tasks

**Returns an array containing all tasks**

#####*Request:*

GET /task

#####*Response:*

	[
		{
			"id": 1,
			"name": "testTask",
			...
		},
		{
			"id": 2,
			"name": "AnotherTask",
			...
		}
	]

-----------------------------------------------------------------

**Returns a project object with given id**

#####*Request:*

GET /task/{task_id}

#####*Response:*

	{
		"id": {task_id},
		"name": "searchedTask",
		...
	}
	
-----------------------------------------------------------------

**Updates the task with given id**
	
#####*Request:*

PUT /task/{task_id}
####This function requires ADMIN-rights!

	{
	    "name": "testTask",
	    "description": "this is a test",
	    "location": "Vienna",
	    "startTime": "2000-01-01T00:30:00",
	    "endTime": "2000-01-01T01:00:00"
	    "urgency": 3,
	    "amountOfVolunteers": 30
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Adds a competence by given id to a task by given id**
	
#####*Request:*

GET /task/{task_id}/addCompetence/{competence_id}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Adds or updated the feedback of a task**

#####*Request:*

POST /task/{task_id}/addFeedback

	{
		"feedback": "Some feedback"
	}
	
#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Adds an attachment to a task by given ID**

#####*Request:*

POST /task/{task_id}/addAttachment

####THIS IS NOT A JSON-REQUEST!
The file has to be sent via a multipart-form.
It then is copied to the server and added to the given task

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Removes given attachment from given task and deletes the attached file from the server**

#####*Request:*

DELETE /task/{task_id}/removeAttachment/{attachment_id}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Returns an array with all tasks that contain given task_name in their name**

#####*Request:*

GET /task/getByName/{task_name}"

#####*Response:*

	[
		{
			"id": 1,
			"name": "task_name",
			...
		},
		{
			"id": 2,
			"name": "x_task_name_y",
			...
		}
	]

-----------------------------------------------------------------

###Comment-handling

**Creates and adds a comment to given task**

#####*Request:*

POST /task/{task_id}/addComment

	{
		"name": "testComment",
		"content": "this is test content"
	}
	
#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Removes and deletes a given comment from a given task**

#####*Request:*

DELETE /task/{task_id}/removeComment/{comment_id}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Returns all comments of given task as array**

#####*Request:*

GET task/{task_id}/getComments

#####*Response:*

	[
	  {
	    "id": 1,
	    "name": "testComment",
	    "content": "this is a comment",
	    "task": 1
	  },
	  {
	  	"id": 2,
	  	...
	  },
	  ...
	]

-----------------------------------------------------------------

###Competence-Endpoints

**Returns an array containing all competences**

#####*Request:*

GET /competence

#####*Response:*

	[
		{
			"id": 1,
			"name": "testCompetence",
			...
		},
		{
			"id": 2,
			"name": "AnotherCompetence",
			...
		}
	]

-----------------------------------------------------------------

**Returns a user object with given id**

#####*Request:*

GET /competence/{competence_id}

#####*Response:*

	{
		"id": {competence_id},
		"name": "searchedCompetence",
		...
	}

-----------------------------------------------------------------

**Creates a new competence**

#####*Request:*

POST /competence

####This function requires ADMIN-rights!

	{
	    "name":"testCompetence",
	    "description": "this is a competence"
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Deletes competence by given ID**

#####*Request:*

DELETE /competence/{competence_id}
####This function requires ADMIN-rights!

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Updates a competence by given ID**

#####*Request:*

PUT /competence/{competence_id}
####This function requires ADMIN-rights!

	{
	    "name":"testCompetence",
	    "description": "this is a competence"
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

###Simple Taskfeed

**Returns all Tasks, which neededCompetences match with the addedCompetences of the logged in user**

#####*Request:*

GET /newsfeed

#####*Response:*

	[
		{
			"id": 1,
			"name": "testTask",
			...
		},
		{
			"id": 2,
			"name": "AnotherTask",
			...
		}
	]
