##CrAc

##Endpoints (This is a work in progress! The documentation may be not up-to-date from time to time!)

###Important Informations (please read carefully before using any endpoints)

-----------------------------------------------------------------

###THIS SYSTEM IS USING THE REST-CONCEPT! THE USED FRONTEND IS NEVER PERMANENTLY CONNECTED TO THE BACKEND!

###As a result, the credentials (name and password) OR a valid token must always be delivered in the header of the request for authentication!

The credentials have to be encoded, according to the Basic-Authentication standards.
If this is not the case, the server will just return a 401 "unauthorized"-message!

####If the logged in user does not posses the rights for executing the method at a given endpoint, there will be a 403-message as return value. 

Eg.: A user without admin-rights tries to delete another user.

###Things to consider when using PUT or POST-Methods with JSon-Data

The Backend is using a library called Jackson, which maps the JSon to objects and vice versa.
This means, the fields in the JSon-data MUST match the fields of the class used in CrAc. 
Information for that is provided down below in the details for each endpoint.

###IMPORTANT: Not all fields have to be filled in! If only the name of the user should be changed, that is the only information that has to be sent via JSon. The Jackson-library takes all information sent and creates an object where it fills in all the fields it has provided data for, as long as this data does match the class-description.

In summary: Not all fields have to be sent via JSon, but the data sent MUST match the possible fields.

-----------------------------------------------------------------

###Login-related Endpoints

-----------------------------------------------------------------

**Get a valid token for the system and confirm your user**

#####*Request:*

GET /user/login

->the name and password have to be added in the header as the basic-authentication

#####*Response:*

If the name and password transferred in the header are correct:

	{
	  "success": "true",
	  "action": "create_token",
	  "id": "1",
	  "user": "user1",
	  "token": "r84r42cu9vs78jvj389cuj4cac",
	  "roles": [
	    {
	      "id": 1,
	      "name": "ADMIN",
	      "mappedPermissionTypes": [],
	      "mappedUser": [
	        1
	      ]
	    }
	  ]
	}

else the standard unauthorized-message will appear:

	{
		"status": 401, 
		"error": "Unauthorized"
	}
	
If the user already has a valid token, the system will output it and return a message that reminds the user, that he is already logged in.
	
###Now, that the user is confirmed, there is another option for authenticating to endpoints, the token.
The value of the token return by this endpoint can just be added to the header of the request in the custom-field "Token".
If it's valid, the system will act like the user is sending his actual name and password.
###The user can still authenticate via basic-authentication!

-----------------------------------------------------------------

**Delete your token**

#####*Request:*

GET /user/logout

#####*Response:*

Either a success or a failure-message, depending on the VALID (through basic authentication) user already having a token or not.
	
This endpoint will delete the token that was created by calling the login-endpoint!
####If no basic-authentication is provided, the user now has no access to the system!

-----------------------------------------------------------------

###User-Functions

-----------------------------------------------------------------

**Get all users**

#####*Request:*

GET /user/all

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

**Add a competence with given ID to the currently logged in user, likeValue and proficiencyValue are mandatory**

#####*Request:*

GET user/competence/{competence_id}/add/{likeValue}/{proficiencyValue}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Adjust the values of a user-competence connection**

#####*Request:*

GET user/competence/{competence_id}/adjust/{likeValue}/{proficiencyValue}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Show all competences, that are and not yet connected available to a user**

#####*Request:*

GET user/competence/available

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

**Returns target task and its relationship to the logged in user**

#####*Request:*

GET user/task/{task_id}


#####*Response:*

	[
	  {
	    "id": 1,
	    "name": "Water the flowers",
	    "description": "All about watering the different flowers in the garden.",
	    ...
	  },
	  {
	    "id": 1,
	    "user": 1,
	    "task": 1,
	    "participationType": "PARTICIPATING"
	  }
	]

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

**Returns the competences of the currently logged in user, wrapped in the relationship-object**

#####*Request:*

GET user/competence

#####*Response:*

	[
	  {
	    "id": 16,
	    "user": 3,
	    "competence": {
	      "id": 6,
	      "name": "javascript-programming",
	      ...
	      ]
	    },
	    "likeValue": 1,
	    "proficiencyValue": 50
	  },
	  {
	    "id": 17,
	    "user": 3,
	    "competence": {
	      "id": 7,
	      "name": "php-programming",
	      ...
	      ]
	    },
	    "likeValue": 1,
	    "proficiencyValue": 50
	  },
	  ...
	]


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

**Return a sorted list of a defined number of elements with the best fitting tasks for the logged in user**

#####*Request:*

GET user/findMatchingTasks/{number_of_tasks}

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

**Adds a role to the logged in User**
####This function requires ADMIN-rights!

#####*Request:*

GET user/role/{role_id}/add

#####*Response:*

Json-data, a success
	
-----------------------------------------------------------------

**Removes a role from the logged in user**
####This function requires ADMIN-rights!

#####*Request:*

GET user/role/{role_id}/remove

#####*Response:*

Json-data, a success

-----------------------------------------------------------------

###Role-Endpoints

-----------------------------------------------------------------

**Returns all possible roles**

#####*Request:*

GET /role

#####*Response:*

	[
	  {
	    "id": 1,
	    "name": "USER",
	    "mappedPermissionTypes": [],
	    "mappedUser": [
	      1
	    ]
	  },
	  {
	    "id": 2,
	    "name": "ADMIN",
	    ...
	  },
	  ...
	]

-----------------------------------------------------------------

**Deletes the role with given id**
	
#####*Request:*

DELETE /admin/role/{role_id}
####This function requires ADMIN-rights!

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Creates a new role**
	
#####*Request:*

POST /admin/role
####This function requires ADMIN-rights!

	{
	    "name": "DUMMYROLE"
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Updates the role with given id**
	
#####*Request:*

PUT /admin/role/{role_id}
####This function requires ADMIN-rights!

	{
	    "name": "DUMMYROLE"
	}

#####*Response:*

Json-data, either a success or a failure message

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

**Returns a task object with given id and updates the task if it's ready to start**

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
	    "urgency": 3,
	    "amountOfVolunteers": 30
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Creates a new task**
	
#####*Request:*

POST /admin/task

	{
	    "name": "testTask",
	    "description": "this is a test",
	    "location": "Vienna",
	    "startTime": "2000-01-01T00:30:00",
	    "endTime": "2000-01-01T01:00:00",
	    "urgency": 3,
	    "minAmountOfVolunteers": 10,
	    "maxAmountOfVolunteers": 30
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
	
**Adds target competence to target task, it is mandatory to add the proficiency, importanceLvl and the mandatoryflag (as boolean)**
	
#####*Request:*

GET /task/{task_id}/competence/{competence_id}/require/{proficiency}/{importance}/{mandatory}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Add multiple competences**
	
#####*Request:*

Post a JSON-file with an array containing competence-ids and meta-data.  

POST /task/{task_id}/competence/require  

Each object can hold 4 different attributes:  

competenceId (1 or higher)  
neededProficiencyLevel (between 0 and 100)  
importanceLevel (between 0 and 100)  
mandatory (0 [for false] or 1 [for true])  

	[
		{
			"competenceId": 1,
			"neededProficiencyLevel": 100,
			"importanceLevel": 10,
			"mandatory": 1
		},
		{
			"competenceId": 2,
			"neededProficiencyLevel": 100,
			"mandatory": 0
		},
		{
			"competenceId": 100,
			"neededProficiencyLevel": 100
		},
		{
			"neededProficiencyLevel": 100
		}
	]

#####*Response:*

	{
	  "success": true,
	  "details": {
	    "1": {
	      "competence_status": "COMPETENCE_ASSIGNED"
      		"mandatory": "VALUE_NOT_VALID"
	    },
	    "2": {
	      "competence_status": "ALREADY_ASSIGNED_VALUES_ADJUSTED",
	      "importanceLevel": "NOT_ASSIGNED"
	    },
	    "100": {
	      "competence_status": "COMPETENCE_NOT_FOUND"
	    }
	  }
	}

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Overwrites all assigned competences with given competences**
	
#####*Request:*

####This endpoint works exactly as the one above ( .../require), except it overwrite all previously assigned competences!
Post a JSON-file with an array containing competence-ids and meta-data.

PUT /task/{task_id}/competence/overwrite

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Removes a competence by given id to a task by given id**
	
#####*Request:*

GET /task/{task_id}/competence/{competence_id}/remove

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Add a material to target task**
	
#####*Request:*

POST /task/{task_id}/material/add

	{
		"name": "cake",
		"description": "sweet baked food",
		"quantity": 3
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Update fields of target material on target task**
	
#####*Request:*

PUT /task/{task_id}/material/update/{material_id}

	{
		"name": "cake",
		"description": "sweet baked food",
		"quantity": 3
	}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Remove a material from target task**
	
#####*Request:*

GET /task/{task_id}/material/remove/{material_id}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Subscribe to a material of a task with a quantity, or change the quantity if already subscribed**
	
#####*Request:*

GET /task/{task_id}/material/{material_id}/subscribe/{quantity}

#####*Response:*

Json-data, either a success or a failure message

Possible failures:

QUANTITY_TOO_SMALL
QUANTITY_TOO_HIGH

-----------------------------------------------------------------
	
**Unsubscribe to a subscribed material**
	
#####*Request:*

GET /task/{task_id}/material/{material_id}/unsubscribe

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Sets a single task ready to be published, only works if it's children are ready**

There are a few prerequisites to this. 
First of all, the following fields have to be set: amountOfVolunteers, description, startTime, endTime, location.
If the task is a supertask and has children, all children need to be ready-to-publish first.
If the task is a leaf (so users can participate), at least one competence has to be added.
	
#####*Request:*

GET /task/{task_id}/publish/ready/single

#####*Response:*

Json-data, either a success or a failure message

Possible failures:

	{
	  "success": false,
	  "error": "bad_request",
	  "cause": "CHILDREN_NOT_READY"
	}
	
	{
	  "success": false,
	  "error": "bad_request",
	  "cause": "TASK_NOT_READY"
	}
	
	{
	  "success": false,
	  "error": "bad_request",
	  "cause": "PERMISSIONS_NOT_SUFFICIENT"
	}

-----------------------------------------------------------------
	
**Sets target task and all children ready to be published**

This endpoint follows the same rules described above for every single task it tries to set ready-to-publish
	
#####*Request:*

GET /task/{task_id}/publish/ready/tree

#####*Response:*

Json-data, either a success or a failure message

The cause-object contains an array that states the ID of the task that failed and the cause why it failed.

	{
	  "success": false,
	  "error": "bad_request",
	  "cause": {
	    "4": "TASK_NOT_READY",
	    "5": "TASK_NOT_READY"
	  }
	}

-----------------------------------------------------------------
	
**Sets the relation between the logged in user and target task to done, meaning the user completed the task**
	
#####*Request:*

####Use "true" or "false" for {done_boolean}

GET /task/{task_id}/done/{done_boolean}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
**Returns an array with all tasks that contain given task_name in their name**

#####*Request:*

GET /task/searchDirect/{task_name}

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

GET /task/{supertask_id}/extend

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

GET /task/{task_id}/copy

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Updates the value of an task with an open amount of volunteers, based on the amount of volunteers on their child-tasks**

#####*Request:*

GET /task/{task_id}/updateAmountOfVolunteers

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Sets the TaskRepetitionState from once to periodic if possible, mandatory to add a date as json**

#####*Request:*

GET /task/{task_id}/periodic/set

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

GET /task/{task_id}/periodic/undo

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Issues an invite-notification to the target-user**

#####*Request:*

GET /task//{task_id}/invite/{user_id}

#####*Response:*

Json-data, either a success or a failure message

	
-----------------------------------------------------------------

**Adds target task to the open-tasks of the logged-in user or changes it's state; Choose either 'participate', 'follow', or 'lead'**

#####*Request:*

GET /user/task/{task_id}/{state_name}

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

GET /task/{task_id}/state/{state_name}

#####*Response:*

Json-data, either a success or a failure message

Possible failures:

	{
	  "success": false,
	  "error": "bad_request",
	  "cause": "CHILDREN_NOT_READY"
	}
	
	{
	  "success": false,
	  "error": "bad_request",
	  "cause": "START_NOT_ALLOWED"
	}
	
	{
	  "success": false,
	  "error": "bad_request",
	  "cause": "TASK_NOT_READY"
	}
	
	{
	  "success": false,
	  "error": "bad_request",
	  "cause": "NOT_COMPLETED_BY_USERS"
	}
	
	{
	  "success": false,
	  "error": "bad_request",
	  "cause": "UNDEFINED_ERROR"
	}

	{
	  "success": false,
	  "error": "bad_request",
	  "cause": "PERMISSIONS_NOT_SUFFICIENT"
	}



-----------------------------------------------------------------

**Starts all tasks, that fullfill the prerequisites and are ready to starts**

#####*Request:*

GET /task/updateStarted

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Nominate someone as the leader of a task as creator**

#####*Request:*

GET /task/{task_id}/nominateLeader/{user_id}

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Returns the values for the enum taskParticipationType**

#####*Request:*

GET /task/taskParticipationTypes

#####*Response:*

	[
	  "PARTICIPATING",
	  "FOLLOWING",
	  "LEADING"
	]

-----------------------------------------------------------------

**Returns the values for the enum taskState**

#####*Request:*

GET /task/taskStates

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

GET /task/taskTypes

#####*Response:*

	[
	  "PARALLEL",
	  "SEQUENTIAL"
	]

-----------------------------------------------------------------

**Returns all tasks, that are supertasks**

#####*Request:*

GET /task/parents

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

POST /task/queryES

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

GET /task/{task_id}/findMatchingUsers

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

**Synchronizes the competences of the DB into the CompetenceStorage of the application and caches the relations**
##VERY IMPORTANT:
-> When booting the system, this is done automatically. Later on, if there is a change on the DB, this HAS to be called,
since internally, the system only works with the cached data!

#####*Request:*

GET /admin/sync

#####*Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Returns an array containing all competence-relationships of the logged in user**

#####*Request:*

GET /competence

#####*Response:*

	[
	  {
	    "id": 10,
	    "user": 1,
	    "competence": {
	      "id": 3,
	      "name": "walking",
			...
	    },
	    "likeValue": 50,
	    "proficiencyValue": 50,
	    "selfAssigned": false
	  },
	  {
	    "id": 14,
	    "user": 1,
	    "competence": {
	      "id": 4,
			...
	    },
	    "likeValue": 10,
	    "proficiencyValue": 50,
	    "selfAssigned": false
	  },
	  ...
	]

-----------------------------------------------------------------

**Returns an array containing all competences**

#####*Request:*

GET /competence/all

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

###Filter-Configuration-Endpoints
These endpoints handle the filter-configuration of the User-Task-Allocation (the matrix-part)

-----------------------------------------------------------------
**Adds a filter to the filter-configuration, based on it's name**

#####*Request:*

GET /configuration/filter/add/{filter_name}

The possible names for the filters are: LikeLevelFilter, ImportancyLevelFilter, ProficiencyLevelFilter, UserRelationFilter.

#####*Response:*

Json-data, either a success or a failure message when an invalid filter-name has been used.

-----------------------------------------------------------------
**Adds multiple filters to the filter-configuration, based on the list of filters in the posted JSon-file**

#####*Request:*

POST /configuration/filter/add

The possible names for the filters are: LikeLevelFilter, ImportancyLevelFilter, ProficiencyLevelFilter, UserRelationFilter.

	{
		"parameters": ["ImportancyLevelFilter", "LikeLevelFilter", "ProficiencyLevelFilter", "LikeLevelFilter"]
	}

#####*Response:*

Json-data, either a success or a failure message when only invalid filter-names have been used.
If that is the case, the configuration resets itself to the standard configuration.
This configuration consists of: LikeLevelFilter, ImportancyLevelFilter, ProficiencyLevelFilter.

-----------------------------------------------------------------
**Returns a list of all active filters**

#####*Request:*

GET /configuration/filter/print

#####*Response:*

	{
	  "success": "true",
	  "msg": "ProficiencyLevelFilter is available! LikeLevelFilter is available! UserRelationFilter is available! ImportancyLevelFilter is available! "
	}

-----------------------------------------------------------------
**Clears (empties) the list of active filters**

#####*Request:*

GET /configuration/filter/clear

#####*Response:*

Json-data, a success

-----------------------------------------------------------------
**Restores the standard state of the filter-configuration**

#####*Request:*

This endpoint restores the standard state of the configuration, consisting of: LikeLevelFilter, ImportancyLevelFilter, ProficiencyLevelFilter.

GET /configuration/filter/restore

#####*Response:*

Json-data, a success.

-----------------------------------------------------------------

###Notifications-Endpoints
These endpoints handle already issued notifications for the most parts

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
	
-----------------------------------------------------------------
	
##CHANGES

In this section, changes and their dates are noted.

-----------------------------------------------------------------

###25.10.2016

Role-System implemented -> see section Role-Endpoints
The endpoint for the role-enums has been removed

Token-System implemented -> see section User-Endpoints for login (creation) and logout (destruction) with tokens
The "check"-endpoint is not part of the readMe anymore, since it's depricated and will be removed soon,
if used, change it to the "login"-endpoint

-----------------------------------------------------------------

###11.11.2016

Endpoint-change -> Assigned competences of user now at the /competence-endpoint, all competences at the endpoint /competence/all

-----------------------------------------------------------------

###16.11.2016

Endpoint-change -> A single task + task-user-relationship is available at the /user/task/{task_id}-endpoint

-----------------------------------------------------------------

###27.11.2016

With the big changes to tasks, the following endpoints have been implemented:
GET /task/{task_id}/publish/ready/single -> NEW
GET /task/{task_id}/publish/ready/tree -> NEW
GET /task/{task_id}/done/{done_boolean} -> NEW
GET /task/updateStarted -> NEW

The following endpoints where changed:
GET /task/{task_id}/state/{state_name} -> FUNCTIONALITY CHANGED
PUT /admin/task -> from /task, now only usable with admin or editor permissions
GET /task/{task_id} -> FUNCTIONALITY CHANGED, now updates the task if it's ready to start
GET /user/task/{task_id}/{state_name} -> from /task/{task_id}/{state_name}

###This is the summary of the changes to the Task-Workflow

1. not published
Admin/Editor kann Task (Task auf oberster Ebene entspricht meistens einem Projekt) erstellen
Creator wird im Task permanent dokumentiert und und ist automatisch auch Leader des erstellten Tasks (Creator == Leader)
Leader hat Änderungsrechte am Task
Leader kann Felder befüllen, ändern
Leader kann weitere CrAc-User als Leader für jeweiligen Task bestimmen
Leader kann weitere Sub-Tasks erstellen
Leader kann Leader für Sub-Tasks erstellen
Leader hat generell Berechtigungen über alle Tasks/Sub-Tasks unter “seinem” Task
Nur weil jemand globale Rolle “Editor” hat, darf er deswegen den Task nicht verändern – Editor erlaubt nur einen neuen Task zu erstellen, von welchem er dann automatisch Creator ist – ergo die genannten Privilegien erhält (Änderungsrecht, etc.)
Alle Felder eines Tasks (außer dem “creator”-Feld) sind in diesem Status noch veränderbar
Die Struktur des gesamten Task-Baumes ist gänzlich veränderbar (Sub-Tasks, Siblings, Leaderbestimmung, etc.)
Not published Task ist für niemanden sichtbar außer den Admin (Schreibrechte), Editor (Leserechte), und den einzelnen Leader (generell Leserechte; bei eigenem Task/Sub-Tasks Schreibrechte) welche im gesamten Task-Baum definiert sind
Jeder Leader eines Tasks kann für seinen Bereich bekanntgeben, dass er “ready-to-publish” ist
Vorgehensweise von unten nach oben im Baum
Notifikation an Super-Leader (quasi Projektverantwortlicher), wenn alle Sub-Tasks “ready-to-publish” sind, was bedeutet, dass alle Verantwortlichen das ok geben für die Veröffentlichung (es ist davon auszugehen, dass daher die Struktur für den gesamten Task überlegt und umgesetzt wurde)
Wird ein “ready-to-publish” an einer Stelle “vergessen”, so kann der darüberliegende Leader das “ready-to-publish” erzwingen.

2. published
Wenn ein Task von einem Leader (ggf. auch Admin) veröffentlicht wird, bekommen alle User lesend Zugriff auf den Task-Baum mit den einzelnen Tasks und dabei auch folgende Funktionalitäten
Task durchscrollen, anzeigen, Sub-Tasks öffnen
Teilnehmen / Absagen
Folgen / Entfolgen
Kommentieren
Handelt es sich um einzelne Tasks anstatt eines Task-Baumes, so gilt selbiges für die einzelnen Tasks
Leader haben weiterhin Schreibrechte auf den Task, allerdings erste Einschränkungen nach der Veröffentlichung:
Leader kann keine Sub-Tasks mehr von einem Blatt erstellen (da am Blatt nun ev. bereits User zugesagt haben)
Leader kann bestimmte Felder nicht mehr verändern: Aktuell = creator, createdate, createtime
Leader kann Status nicht mehr in den Status “notpublished” rückführen
Leader hat aber weiterhin Rechte auf gesamten Sub-Baum “seines” Tasks
Leader kann neue Task hinzufügen, jedoch wie bereits erwähnt nicht mehr unter einem Blatt, sondern nur mehr als Siblings zu anderen Tasks
Anzeige in Taskliste(n)
Aktivierung für Matching-Kalkulation


3. started
Im Gegensat zu den Stati unpublished/published welche für den gesamten Task-Baum (quasi das Projekt) gelten, kann jeder einzelne Task in den Status “started” hinübergeführt werden
Die überführung in den Status “started” kann entweder manuell durch den Leader oder automatisch aufgrund des definierten Zeitraumes / Zeitpunktes erfolgen
Sub-Task kann aber erst gestartet werden nachdem Super-Task gestartet wurde
Ein offizieller Start eines Tasks bringt folgende Auswirkungen mit sich:
User darf nicht mehr über das System absagen (Leader / Admin haben jedoch noch die Möglichkeit einen User händisch von der Teilnahmeliste zu entfernen)
Teilnehmen ist weiterhin möglich, solange die max. Teilnehmeranzahl nicht erreicht ist (ggf. plus Tolleranz?  Diskussion soft/hard limit Teilnehmeranzahl)
Follow/Unfollow ist weiterhin möglich
Leader darf nun auch die Felder nicht mehr ändern im Task, da der Task bereits gestartet ist
Kommentieren ist weiterhin möglich und dient u.a. dem Informationsaustausch
Wenn die Teilnehmeranzahl erfüllt ist und der Task “in time” ist:
Task nicht mehr in der Liste der “offenen Tasks” anzeigen (ev. sollte es eine eigene Liste “laufende Tasks” geben)
Matching-Kalkulation abschwächen damit der Task im Matching nach hinten gereiht wird oder ggf. nicht mehr vorgeschlagen wird
Wenn die Teilnehmeranzahl nicht erfüllt ist und der Task “in time” ist:
Task nicht mehr in der Liste der “offenen Tasks” anzeigen
Matching-Kalkulation noch nicht abschwächen – der Task sollte weiterhin matchen, ev. finden sich dadurch weitere Helfer
Wenn die Teilnehmeranzahl nicht erfüllt ist und der Task “critical” ist:
Task weiterhin in der Liste der “offenen Tasks” anzeigen
Task ev. auf Startseite anzeigen und hervorheben
Matching-Kalkulation anpassen damit Task eher Benutzern vorgeschlagen wird

4. completed
Jeder einzelne Task kann auf completed gesetzt werden, dabei ist jedoch zu unterscheiden, dass bei einem einzelnen Task mehrere User teilnehmen können, z.B. 3 Kuchen werden von 3 User gebacken – folglich kann jeder User seinen Task (seinen gebackenen Kuchen) für sich abschließen. Erst nachdem alle den Task abgeschlossen haben, bekommt der Leader eine Notifikation dass der Task abgeschlossen werden kann.
Ähnlich wie beim “ready-to-publish” werden die Tasks von unten nach oben abgeschlossen – es kann kein Super-Task abgeschlossen werden, wenn der Sub-Task noch nicht abgeschlossen ist.
Ausnahme: der Leader hat wieder die Möglichkeit ein complete zu erzwingen, z.B. falls der Benutzer darauf vergessen hat
Nach dem der Task abgeschlossen ist können keine Änderungen mehr gemacht werden
Diskussion: Sollte der Task wieder eröffnet werden können, z.B. wenn die Lösung nicht ausreichend ist?
Nach dem ein Task gänzlich (von allen Usern & folglich Leader) abgeschlossen wurde, wird die Evaluierungsfunktion freigeschalten
Task wird nicht mehr in Taskliste angezeigt (jedoch kann Task in eigener Liste “Completed Tasks” abgerufen werden um ggf. vergangene Tasks zu betrachten, beurteilen, etc.)
Tasks wird im Matching nicht mehr berücksichtigt



Anmerkungen
Follow deutet das prinzipielle Interesse eines Users an einem Task oder einem Projekt an
Leader und User wissen somit wer noch Interesse hätte mitzuarbeiten
Ein Pool an ev. vorhandenen Helfern ist vorhanden
Der Interessent (Follower) bekommt je nach Einstellungen Notifikationen zum jeweiligen Task
Zeitraum eines Sub-Tasks muss innerhalb des Super-Tasks sein, d.h. wenn das Projekt Adventbazar von 01.10. bis zum 25.11. andauert, dann dürfen die Sub-Tasks nicht außerhalb dieses Zeitraumes sein. Wird hingegen nur ein Zeitpunkt angegeben, z.B. Adventbazar am 25.11. dann müssen die Sub-Task innerhalb des aktuellen Erstelldatums und dem 25.11. sein.

-----------------------------------------------------------------

###14.12.2016

Change to how the amount of volunteers on tasks is handled
The attribute can be set to any positive number, meaning the concrete amount of volunteers, or to "0", meaning an infinite number
of volunteers can join the task. This "0" can also be updated to a concrete amount, based on the amount that's placed on the child-tasks:

GET /task/{task_id}/updateAmountOfVolunteers -> NEW

-----------------------------------------------------------------

###16.12.2016

Change to the endpoint, that gets users for a specific task.

GET /task/{task_id}/findMatchingUsers -> from /task/findMatchingUsers/{task_id}

##MAJOR UPDATE TO THE MATCHING-SYSTEM!!!!
-> Competences are synchronized from the DB into the running system for performance-reasons now, and thus MUST be synchronized by hand if there is a change on KOMET! This can be done by calling the following endpoint with ADMIN-permissions:

GET /admin/sync -> NEW

-----------------------------------------------------------------

####23.1.2017

A new filter-configuration has been added to the system.
These filters influence the matching between users and tasks and can be added and removed.
There are 4 filter-types at the moment:
LikeLevelFilter (changed matching-values based on the user's affection towards a competence), ImportancyLevelFilter (changed matching-values based on the importance of the competence), ProficiencyLevelFilter (changes matching-values based on the users proficiency and the tasks needed proficiency in a competence) and UserRelationFilter (changes the matching-values based on the relation of the searching user to already participating users).

Look up the filter-section of the readMe details to the endpoints.

-----------------------------------------------------------------

####24.1.2017

A new endpoint returns a specified amount of best matches.

GET /user/findMatchingTasks/{number_of_tasks} -> NEW

-----------------------------------------------------------------

####12.2.2017

Materials can now be assigned to tasks.
Endpoints for that are in the task-section.

GET /task/{task_id}/material/add -> NEW
GET /task/{task_id}/material/update/{material_id} -> NEW
GET /task/{task_id}/material/remove/{material_id} -> NEW

-----------------------------------------------------------------

####18.2.2017

Materials can be subscribed to (with a quantity) by the logged in user.
Adding a material returns the material-id.

GET /task/{task_id}/material/{material_id}/subscribe/{quantity} -> NEW
GET /task/{task_id}/material/{material_id}/unsubscribe -> NEW

Look at the task-section for more info

-----------------------------------------------------------------
