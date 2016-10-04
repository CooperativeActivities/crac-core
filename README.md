##CrAc

##Endpoints (This is a work in progress! The documentation may be not up-to-date from time to time!)

###Login-related

####THE CREDENTIALS (NAME AND PASSWORD) FOR ATHENTICATION MUST ALWAYS BE DELIVERED IN THE HEADER OF THE REQUEST AS BASIC AUTHENTICATION! 
If this is not the case, the server will just return an "unauthorized"-message!
####If the logged in user does not posses the rights for executing the method at a given endpoint, there will be a 403-message as return value. 
Eg.: A user without admin-rights tries to delete another user.

-----------------------------------------------------------------

**Get login response**

#####*Request:*

GET /user/check

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

-----------------------------------------------------------------

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

POST /admin/user

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

DELETE /admin/user/{id}
####This function requires ADMIN-rights!

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Update the data of a user with given ID**

#####*Request:*

PUT /admin/user/{id}
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

GET /user

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

PUT /user

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

-----------------------------------------------------------------

**Add a competence with given ID to the currently logged in user**

#####*Request:*

GET user/competence/{competence_id}/add

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Remove a competence with given ID from the currently logged in user**

#####*Request:*

GET user/competence/{competence_id}/remove

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

###Task-Endpoints on logged in user

-----------------------------------------------------------------

**Removes the task with given id from the open-tasks of the currently logged in user**

#####*Request:*

GET user/task/{task_id}/remove


#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Returns all tasks of logged in user, divided in the TaskParticipationTypes**

#####*Request:*

GET user/task

#####*Response:*

	{
	  "following": [],
	  "participating": [
	    {
	      "id": 1,
	      "name": "Water the flowers",
	      "description": "All about watering the different flowers in the garden.",
			...
	    }
	  ],
	  "leading": []
	}

-----------------------------------------------------------------

**Returns a sorted list of elements with the best fitting tasks for the logged in user**

#####*Request:*

GET user/findMatchingTasks

#####*Response:*

	[
	  {
	    "task": {
	      "id": 3,
	      "name": "Program a watering tool",
	      ...
	    },
	    "assessment": 0.945
	  },
	  {
	    "task": {
	      "id": 2,
	      "name": "Water the roses",
	      ...
	    },
	    "assessment": 0.54
	  },
	  {
	    "task": {
	      "id": 4,
	      "name": "Water the lillies",
	      ...
	    },
	    "assessment": 0.54
	  }
	]

-----------------------------------------------------------------
**Issues a friend-request-notification to target user**

#####*Request:*

GET user/{user_id}/friend

#####*Response:*

Json-data, a success

-----------------------------------------------------------------

**Shows the friends of the logged in user**

#####*Request:*

GET user/friends

#####*Response:*

	[
		{
			"id": 1,
			"name": "friend1",
			...
		},
		{
			"id": 2,
			"name": "friend2",
			...
		}
	]

-----------------------------------------------------------------

**Unfriends target user**

#####*Request:*

GET user/{user_id}/unfriend

#####*Response:*

Json-data, a success

-----------------------------------------------------------------

**Shows the relationships of the logged in user**

#####*Request:*

GET user/relationships

#####*Response:*

	[
	  {
	    "relatedUser": {
	      "id": 2,
	      "name": "user1",
	      ...
	    },
	    "likeValue": 20,
	    "friends": true
	  },
	  {
	    "relatedUser": {
	      "id": 4,
	      "name": "user2",
	      ...
	    },
	    "likeValue": -10,
	    "friends": false
	  }
	]

-----------------------------------------------------------------

**Returns the values for the enum taskParticipationType**

#####*Request:*

GET user/roles

#####*Response:*

	[
	  "USER",
	  "ADMIN"
	]

-----------------------------------------------------------------

###Task-Endpoints

####EXTEND TASKS FOR SUBTASKS

-----------------------------------------------------------------

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

**Returns a task object with given id**

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

PUT /admin/task/{task_id}
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

**Creates a new task**
	
#####*Request:*

POST /admin/task/{task_id}

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

**Deletes the task with given id**
	
#####*Request:*

DELETE /admin/task/{task_id}
####This function requires ADMIN-rights!

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Adds target competence to target task, it is mandatory to add the proficiency and importanceLvl**
	
#####*Request:*

GET /task/{task_id}/competence/{competence_id}/require/{proficiency}/{importance}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Removes a competence by given id to a task by given id**
	
#####*Request:*

GET /task/{task_id}/competence/{competence_id}/remove

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Returns an array with all tasks that contain given task_name in their name**

#####*Request:*

GET /task/searchDirect/{task_name}"

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

**Creates a task, that is set as the child of the chosen existing task**

#####*Request:*

GET /task/{supertask_id}/extend"

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

**Copy target task (work in progress)**

#####*Request:*

GET /task/{task_id}/copy"

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Sets the TaskRepetitionState from once to periodic if possible, mandatory to add a date as json**

#####*Request:*

GET /task/{task_id}/periodic/set"

	{
		"year": 0,
		"month": 0,
		"day": 2,
	   "hour": 2,
	   "minute": 2
	   
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Resets the TaskRepetitionState from periodic to once**

#####*Request:*

GET /task/{task_id}/periodic/undo"

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Issues an invite-notification to the target-user**

#####*Request:*

GET /task//{task_id}/invite/{user_id}"

#####*Response:*

Json-data, either a success or a failure message

	
-----------------------------------------------------------------

**Adds target task to the open-tasks of the logged-in user or changes it's state; Choose either 'participate', 'follow', or 'lead'**

#####*Request:*

GET /task/{task_id}/{state_name}"

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Change the state of target task; Choose either 'publish', 'start', or 'complete'**
*For each state different prerequisite have to be fullfilled:*
*NOT_PUBLISHED: Default state*
*PUBLISHED: Only allowed when the task-fields are all filled*
*STARTED: Only allowed when the parent task is started and if sequential, the previous task is completed*
*COMPLETED: A task can only be completed when its children are all completed or if it has none*

#####*Request:*

GET /task/{task_id}/state/{state_name}"

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Nominate someone as the leader of a task as creator**

#####*Request:*

GET /task/{task_id}/nominateLeader/{user_id}"

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Returns the values for the enum taskParticipationType**

#####*Request:*

GET /task/taskParticipationTypes"

#####*Response:*

	[
	  "PARTICIPATING",
	  "FOLLOWING",
	  "LEADING"
	]

-----------------------------------------------------------------

**Returns the values for the enum taskState**

#####*Request:*

GET /task/taskStates"

#####*Response:*

	[
	  "NOT_PUBLISHED",
	  "PUBLISHED",
	  "STARTED",
	  "COMPLETED"
	]

-----------------------------------------------------------------

**Returns the values for the enum taskType**

#####*Request:*

GET /task/taskTypes"

#####*Response:*

	[
	  "PARALLEL",
	  "SEQUENTIAL"
	]

-----------------------------------------------------------------

**Returns all tasks, that are supertasks**

#####*Request:*

GET /task/parents"

#####*Response:*

	[
		{
			"id": 1,
			"name": "supertask_1",
			...
		},
		{
			"id": 2,
			"name": "supertask_2",
			...
		}
	]

-----------------------------------------------------------------

**Fulltext-queries all tasks with Elasticsearch and returns the found ones. If bound to competence-system, compares if tasks are doable**

#####*Request:*

POST /task/queryES"

	{
		"text": "This is a fulltext-query!"
	}

#####*Response:*

	[
		{ "task": {
			"id": 1,
			"name": "task_1",
			...
			}, 
		"assessment" : 0.5 },
		{ "task": {
			"id": 2,
			"name": "task_2",
			...
			}, 
		"assessment" : 0.7 },
	]
	
-----------------------------------------------------------------

**Return a sorted list of elements with the best fitting users for the given task**

#####*Request:*

GET /task/findMatchingUsers/{task_id}"

#####*Response:*

	[
		{ "task": {
			"id": 1,
			"name": "task_1",
			...
			}, 
		"assessment" : 0.5,
		"doable" : true },
		{ "task": {
			"id": 2,
			"name": "task_2",
			...
			}, 
		"assessment" : 0.7 ,
		"doable" : true },
		...
	]


-----------------------------------------------------------------

###Competence-Endpoints

-----------------------------------------------------------------

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

POST /admin/competence

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

DELETE /admin/competence/{competence_id}
####This function requires ADMIN-rights!

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Updates a competence by given ID**

#####*Request:*

PUT /admin/competence/{competence_id}
####This function requires ADMIN-rights!

	{
	    "name":"testCompetence",
	    "description": "this is a competence"
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Connects two competences via a type and additional values**

#####*Request:*

POST /competence/{competence1_id}/connect/{competence2_id}/type/{type_id}

	{
	    "uniDirection": true
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

###Notifications-Endpoints
These endpoint handle already issued notifications for the most parts

-----------------------------------------------------------------

**Returns all notifications, which target the logged in user**

#####*Request:*

GET /notification

#####*Response:*

	[
	  {
	    "notificationId": "22517116313100323167",
	    "targetId": 3,
	    ...
	  },
	  {
	    "notificationId": "528924t6523027823167",
	    "targetId": 3,
	    ...
	  }
	]
	
-----------------------------------------------------------------

**Returns all notifications in the system**

#####*Request:*

GET /notification/admin

####This function requires ADMIN-rights!

#####*Response:*

	[
	  {
	    "notificationId": "22517116313100323167",
	    "targetId": 3,
	    ...
	  },
	  {
	    "notificationId": "528924t6523027823167",
	    "targetId": 1,
	    ...
	  },
	  {
	    "notificationId": "528924o2347235287821",
	    "targetId": 2,
	    ...
	  }
	]
	
-----------------------------------------------------------------

**Triggers the accept-method of the notification and deletes it**

#####*Request:*

GET /notification/{notification_id}/accept

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Triggers the deny-method of the notification and deletes it**

#####*Request:*

GET /notification/{notification_id}/deny

#####*Response:*

Json-data, either a success or a failure message
	