## CrAc

## Endpoints (This is a work in progress! The documentation may be not up-to-date from time to time!)

### Important Informations (please read carefully before using any endpoints)

-----------------------------------------------------------------

### THIS SYSTEM IS USING THE REST-CONCEPT! THE USED FRONTEND IS NEVER PERMANENTLY CONNECTED TO THE BACKEND!

### As a result, the credentials (name and password) OR a valid token must always be delivered in the header of the request for authentication!

The credentials have to be encoded, according to the Basic-Authentication standards.
If this is not the case, the server will just return a 401 "unauthorized"-message!

#### If the logged in user does not posses the rights for executing the method at a given endpoint, there will be a 403-message as return value. 

Eg.: A user without admin-rights tries to delete another user.

### Things to consider when using PUT or POST-Methods with JSon-Data

The Backend is using a library called Jackson, which maps the JSon to objects and vice versa.
This means, the fields in the JSon-data MUST match the fields of the class used in CrAc. 
Information for that is provided down below in the details for each endpoint.

### IMPORTANT: Not all fields have to be filled in! If only the name of the user should be changed, that is the only information that has to be sent via JSon. The Jackson-library takes all information sent and creates an object where it fills in all the fields it has provided data for, as long as this data does match the class-description.

In summary: Not all fields have to be sent via JSon, but the data sent MUST match the possible fields.

-----------------------------------------------------------------

### Login-related Endpoints

-----------------------------------------------------------------

**Get a valid token for the system and confirm your user**

##### *Request:*

GET /user/login

->the name and password have to be added in the header as the basic-authentication

##### *Response:*

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
	
### Now, that the user is confirmed, there is another option for authenticating to endpoints, the token.
The value of the token return by this endpoint can just be added to the header of the request in the custom-field "Token".
If it's valid, the system will act like the user is sending his actual name and password.
### The user can still authenticate via basic-authentication!

-----------------------------------------------------------------

**Delete your token**

##### *Request:*

GET /user/logout

##### *Response:*

Either a success or a failure-message, depending on the VALID (through basic authentication) user already having a token or not.
	
This endpoint will delete the token that was created by calling the login-endpoint!
#### If no basic-authentication is provided, the user now has no access to the system!

-----------------------------------------------------------------

### Synchronization-related Endpoints

-----------------------------------------------------------------

Before more funcionalities of the backend are explained in depth, the concept of it's competence-persistence needs to be explained.  
Since they are not directly persisted in the DB of the backend but loaded from another, a synchronization needs to take place.  
Additionally, the (now copied) competences need to be added to the intern storage-cache for performance-reasons.  
These are the endpoints that allow those actions (also test-data can be created from here):  

**Copy test-data to the platform and fully synchronize all competences from KOMET**

##### *Request:*

GET /synchronization/full
#### This function requires ADMIN-rights!

This endpoint includes all synchronization-functionality available!

##### *Response:*

Either a success or a failure-message.

-----------------------------------------------------------------

**Fully synchronize all competences from KOMET**

##### *Request:*

GET /synchronization/competences
#### This function requires ADMIN-rights!

This endpoint copies all competence-related data from the KOMET-DB to the CrAc-DB and caches them to the intern storage. It only works if the databases are configured correctly!

##### *Response:*

Either a success or a failure-message.

-----------------------------------------------------------------

**Synchronize all competences from KOMET-DB to CrAc-DB**

##### *Request:*

GET /synchronization/database
#### This function requires ADMIN-rights!

This endpoint copies all competence-related data from the KOMET-DB to the CrAc-DB.

##### *Response:*

Either a success or a failure-message.

-----------------------------------------------------------------

**Synchronizes the competences of the DB into the Competence-Storage of the application and caches the relations**

##### *Request:*

GET /synchronization/intern
#### This function requires ADMIN-rights!

This endpoint copies and caches the competences data of the CrAc-DB into the intern storage.

##### *Response:*

Either a success or a failure-message.

-----------------------------------------------------------------

**Add filters to the configuration**

##### *Request:*

GET /synchronization/filter
#### This function requires ADMIN-rights!

This endpoint adds matching-matrix-filters to the filter-configuration.

##### *Response:*

Either a success or a failure-message.

-----------------------------------------------------------------

**Copy test-data to the platform**

##### *Request:*

GET /synchronization/data
#### This function requires ADMIN-rights!

This endpoint adds test-data to the platform.

##### *Response:*

Either a success or a failure-message.

-----------------------------------------------------------------

### User-Functions

-----------------------------------------------------------------

### Errors

-----------------------------------------------------------------

**Responses of Endpoints are presented in the following format:**

The following JSON is just an example;  
"type" contains the type of the data (eg Task, User etc).  
"action" contains the action that is performed on the data.  
"success" states if the action was a success or not.  
If not, "errors" contains the different errors that happened.  
"object" contains details about the object the action is performed on (after the action).  
"meta" contains additional information.  

	{
	  "type": "Data-Type",
	  "rest_action": "CREATE",
	  "success": true,
	  "errors": [],
	  "object": {},
	  "meta": {}
	}
	
These are the possible errors that can appear:  

ACTION_NOT_VALID, ID_NOT_VALID, ID_NOT_FOUND, TASK_NOT_EXTENDABLE, PERMISSIONS_NOT_SUFFICIENT, ORGANISATIONAL_EXTENDS_SHIFT,  
WORKABLE_EXTENDS_ORGANISATIONAL, WORKABLE_EXTENDS_WORKABLE, SHIFT_EXTENDS, USER_NOT_PARTICIPATING, TASK_NOT_STARTED, TASK_NOT_READY,  
TASK_NOT_JOINABLE, TASK_ALREADY_IN_PROCESS, CHILDREN_NOT_READY, UNDEFINED_ERROR, START_NOT_ALLOWED, NOT_COMPLETED_BY_USERS,  
QUANTITY_TOO_SMALL, QUANTITY_TOO_HIGH, QUANTITY_INCORRECT, DATASETS_ALREADY_EXISTS, JSON_READ_ERROR, JSON_MAP_ERROR, JSON_WRITE_ERROR,  RESSOURCE_UNCHANGEABLE, TASK_IS_FULL, USERS_NOT_FRIENDS, WRONG_TYPE, ALREADY_FILLED, NOT_FOUND, TASK_HAS_OPEN_AMOUNT, CANNOT_BE_COPIED  

-----------------------------------------------------------------

**Get all users**

##### *Request:*

GET /user/all

##### *Response:*

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

##### *Request:*

GET /user/{id}

##### *Response:*

A user object with given id

	{
		"id": {id},
		"name": "searchedUser",
		...
	}

-----------------------------------------------------------------

**Create a new user**

##### *Request:*

POST /admin/user

#### This function requires ADMIN-rights!

	{
	    "name":"test",
	    "password": "test",
	    "firstName":"TestHans",
	    "lastName":"TestName",
	    "phone":"234",
	    "email":"asd@asd"
	}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Delete a user with given ID**

##### *Request:*

DELETE /admin/user/{id}
#### This function requires ADMIN-rights!

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Update the data of a user with given ID**

##### *Request:*

PUT /admin/user/{id}
#### This function requires ADMIN-rights!

Updates a user by given ID

	{
	    "name":"test",
	    "password": "test",
	    "firstName":"TestHans",
	    "lastName":"TestName",
	    "phone":"234",
	    "email":"asd@asd"
	}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Get the data of the currently logged in user**

##### *Request:*

GET /user

##### *Response:*

The object of the currently logged in user

	{
		"id": 3,
		"name": "currentUser",
		...
	}

-----------------------------------------------------------------

**Update the data of the currently logged in user**

##### *Request:*

PUT /user

Updates the currently logged in user

	{
	    "name":"currentUser",
	    "password": "test",
	    "firstName":"TestHans",
	    "lastName":"TestName",
	    "phone":"234",
	    "email":"asd@asd"
	}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Return a sorted list of elements with the best fitting users for the given task**

##### *Request:*

GET user/find/{task_id}

##### *Response:*

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

### Competence-Endpoints on logged in user

-----------------------------------------------------------------

**Add a competence with given ID to the currently logged in user, likeValue and proficiencyValue are mandatory**

##### *Request:*

	{
		"proficiencyValue": 50,
		"likeValue": 50
	}

POST competence/{competence_id}/add

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Adjust the values of a user-competence connection**

##### *Request:*

	{
		"proficiencyValue": 50,
		"likeValue": 50
	}

PUT competence/{competence_id}/adjust

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Show all competences, that are and not yet connected available to a user**

##### *Request:*

GET competence/available

##### *Response:*

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

##### *Request:*

DELETE competence/{competence_id}/remove

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

### Task-Endpoints on logged in user

-----------------------------------------------------------------

**Returns a sorted list of elements with the best fitting tasks for the logged in user**

##### *Request:*

GET task/find

##### *Response:*

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

##### *Request:*

GET task/find/{number_of_tasks}

##### *Response:*

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

##### *Request:*

GET user/{user_id}/friend

##### *Response:*

Json-data, a success

-----------------------------------------------------------------

**Shows the friends of the logged in user**

##### *Request:*

GET user/friends

##### *Response:*

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

##### *Request:*

GET user/{user_id}/unfriend

##### *Response:*

Json-data, a success

-----------------------------------------------------------------

**Shows the relationships of the logged in user**

##### *Request:*

GET user/relationships

##### *Response:*

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
#### This function requires ADMIN-rights!

##### *Request:*

PUT user/role/{role_id}/add

##### *Response:*

Json-data, a success
	
-----------------------------------------------------------------

**Removes a role from the logged in user**
#### This function requires ADMIN-rights!

##### *Request:*

DELETE user/role/{role_id}/remove

##### *Response:*

Json-data, a success

-----------------------------------------------------------------

**Adds a role to target User**
#### This function requires ADMIN-rights!

##### *Request:*

PUT admin/user/{user_id}/role/{role_id}/add

##### *Response:*

Json-data, a success
	
-----------------------------------------------------------------

**Removes a role from target user**
#### This function requires ADMIN-rights!

##### *Request:*

DELETE admin/user/{user_id}/role/{role_id}/add

##### *Response:*

Json-data, a success


-----------------------------------------------------------------

### Role-Endpoints

-----------------------------------------------------------------

**Returns all possible roles**

##### *Request:*

GET /role

##### *Response:*

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
	
##### *Request:*

DELETE /admin/role/{role_id}
#### This function requires ADMIN-rights!

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Creates a new role**
	
##### *Request:*

POST /admin/role
#### This function requires ADMIN-rights!

	{
	    "name": "DUMMYROLE"
	}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Updates the role with given id**
	
##### *Request:*

PUT /admin/role/{role_id}
#### This function requires ADMIN-rights!

	{
	    "name": "DUMMYROLE"
	}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

### Task-Endpoints

#### EXTEND TASKS FOR SUBTASKS

-----------------------------------------------------------------

**Returns all tasks affected by the chosen filters and the elasticsearch-query**

##### *Request:*

POST /task

A given query reduces the pool of the found tasks to those that match the query by name or description.  
A multitude of filters can be added, all with arbitrary [UNIQUE] parameters.  

Here is an example for a possible posted JSON-Document:  

	{
    "query": "here is my query",
    "filters": [
    	{
    	    "name": "FilterName1",
    	    "params": [
    	    	{
    	    		"name": "paramtername1",
    	    		"value": paramter1
    	    	},
    	    	{
    	    		"name": "paramtername2",
    	    		"value": paramter2
    	    	}
    	    	]
	
    	},
    	{
    	    "name": "FilterName2",
    	    "params": [
    	    	{
    	    		"name": "paramtername",
    	    		"value": paramter
    	    	}
    	    	]
	
    	}
    	]
	}

Existing filters:  

	{
    	    "name": "DateFilter",
    	    "params": [
    	    	{
    	    		"name": "startDateMin",
    	    		"value": 1499205600000
    	    	},
    	    	{
    	    		"name": "startDateMax",
    	    		"value": 1499205600000
    	    	},
    	    	{
    	    		"name": "endDateMin",
    	    		"value": 1499205600000
    	    	},
    	    	{
    	    		"name": "endDateMax",
    	    		"value": 1499205600000
    	    	}
    	    	]
    	}
    	
    	{
    	    "name": "FriendFilter",
    	    "params": [
    	    	{
    	    		"name": "UserName",
    	    		"value": {
    	    			"firstName": "Max",
    	    			"lastName": "Mustermann"
    	    		}
    	    	},
    	    	{
    	    		"name": "Musteradmin",
    	    		"value": {
    	    			"firstName": "Martin",
    	    			"lastName": "Mustermann"
    	    		}
    	    	}
    	    	]
    	}

##### *Response:*

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

**Returns target task and its relationship to the logged in user (if one exists) and updates the task if it's ready to start**

##### *Request:*

GET /task/{task_id}

##### *Response:*

The information about the task and its relationships is in the meta-object
	
-----------------------------------------------------------------

**Updates the task with given id**
	
##### *Request:*

PUT /task/{task_id}
#### This function requires permission!

	{
	    "name": "testTask",
	    "description": "this is a test",
	    "location": "Vienna",
	    "startTime": "2000-01-01T00:30:00",
	    "endTime": "2000-01-01T01:00:00",
	    "minAmountOfVolunteers": 10,
	    "maxAmountOfVolunteers": 30
	    "taskType": "ORGANISATIONAL"
	}
	
The following Task-Types can be chosen: ORGANISATIONAL, WORKABLE, SHIFT

ORGANISATIONAL: Can't be participated, only for organization purposes  
WORKABLE: These are the tasks volunteers can be participate on  
SHIFT: Workable tasks can be divided into shifts for marking time-spans on a task  

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Creates a new task**
	
##### *Request:*

POST /admin/task

	{
	    "name": "testTask",
	    "description": "this is a test",
	    "location": "Vienna",
	    "startTime": "2000-01-01T00:30:00",
	    "endTime": "2000-01-01T01:00:00",
	    "minAmountOfVolunteers": 10,
	    "maxAmountOfVolunteers": 30
	    "taskType": "ORGANISATIONAL"
	}
	
The following Task-Types can be chosen: ORGANISATIONAL, WORKABLE, SHIFT

ORGANISATIONAL: Can't be participated, only for organization purposes  
WORKABLE: These are the tasks volunteers can be participate on  
SHIFT: Workable tasks can be divided into shifts for marking time-spans on a task  

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Deletes the task with given id**
	
##### *Request:*

DELETE /admin/task/{task_id}
#### This function requires ADMIN-rights!

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Returns all completed tasks of a user by participationType**
	
##### *Request:*

GET /task/completed/{part_type}

{part_type} can be: PARTICIPATING, FOLLOWING, LEADING

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Returns all completed projects**
	
##### *Request:*

GET /task/completed

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Returns all tasks of logged in user, divided in the TaskParticipationTypes, but only if they are not completed**

##### *Request:*

GET task/type

##### *Response:*

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

**Adds target task to the open-tasks of the logged-in user or changes it's state; Choose either 'participate' or 'follow'**

##### *Request:*

PUT task/{task_id}/add/{state_name}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Removes the task with given id from the open-tasks of the currently logged in user**

##### *Request:*

DELETE task/{task_id}/remove


##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Adds target competence to target task, it is mandatory to add the proficiency, importanceLvl and the mandatoryflag (as boolean)**
	
##### *Request:*

	{
		"proficiency": 50,
		"importance": 50,
		"mandatory": true
	}

POST /task/{task_id}/competence/{competence_id}/require

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Adjust the values of a task-competence connection**
	
##### *Request:*

	{
		"proficiency": 50,
		"importance": 50,
		"mandatory": true
	}

PUT /task/{task_id}/competence/{competence_id}/adjust

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Add multiple competences**
	
##### *Request:*

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

##### *Response:*

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
	
##### *Request:*

#### This endpoint works exactly as the one above ( .../require), except it overwrite all previously assigned competences!
Post a JSON-file with an array containing competence-ids and meta-data.

PUT /task/{task_id}/competence/overwrite

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Removes a competence by given id to a task by given id**
	
##### *Request:*

DELETE /task/{task_id}/competence/{competence_id}/remove

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Add/Adjust multiple materials assigned to a task OR overwrite all materials assigned to a task**
	
##### *Request:*

POST /task/{task_id}/material/multiple/{action}

As action, either add or overwrite can be put!

	{
		"name": "cake",
		"description": "sweet baked food",
		"quantity": 3
	}

##### *Response:*

In details, the key of the object is the ID of the material:

	{
	  "success": true,
	  "details": {
	    "1": {
	      "quantity": "DEFAULT_VALUE_ASSIGNED",
	      "material": "CREATED",
	      "description": "DEFAULT_VALUE_ASSIGNED"
	    },
	    "2": {
	      "material": "CREATED"
	    },
	    "3": {
	      "material": "CREATED",
	      "material_id": "ID_NOT_VALID"
	    }
	  }
	}

The following messages can occur:  
ACTION_NOT_VALID, PERMISSIONS_NOT_SUFFICIENT  
ALREADY_EXISTS_VALUES_ADJUSTED, ID_NOT_VALID, CREATED, NOT_CREATED, NOT_ASSIGNED, DEFAULT_VALUE_ASSIGNED, VALUE_NOT_VALID, 

-----------------------------------------------------------------
	
**Add a material to target task**
	
##### *Request:*

POST /task/{task_id}/material/add

	{
		"name": "cake",
		"description": "sweet baked food",
		"quantity": 3
	}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Update fields of target material on target task**
	
##### *Request:*

PUT /task/{task_id}/material/{material_id}/update

	{
		"name": "cake",
		"description": "sweet baked food",
		"quantity": 3
	}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Remove a material from target task**
	
##### *Request:*

DELETE /task/{task_id}/material/{material_id}/remove

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Subscribe to a material of a task with a quantity, or change the quantity if already subscribed**
	
##### *Request:*

PUT /task/{task_id}/material/{material_id}/subscribe/{quantity}

##### *Response:*

Json-data, either a success or a failure message

Possible failures:

QUANTITY_TOO_SMALL
QUANTITY_TOO_HIGH

-----------------------------------------------------------------
	
**Unsubscribe to a subscribed material**
	
##### *Request:*

DELETE /task/{task_id}/material/{material_id}/unsubscribe

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
	
**Sets the relation between the logged in user and target task to done, meaning the user completed the task**
	
##### *Request:*

#### Use "true" or "false" for {done_boolean}

PUT /task/{task_id}/done/{done_boolean}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------
**Returns an array with all tasks that contain given task_name in their name**

##### *Request:*

GET /task/searchDirect/{task_name}

##### *Response:*

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

##### *Request:*

GET /task/{supertask_id}/extend

	{
	    "name": "testTask",
	    "description": "this is a test",
	    "location": "Vienna",
	    "startTime": "2000-01-01T00:30:00",
	    "endTime": "2000-01-01T01:00:00",
	    "minAmountOfVolunteers": 10,
	    "maxAmountOfVolunteers": 30
	    "taskType": "ORGANISATIONAL"
	}
	
The following Task-Types can be chosen: ORGANISATIONAL, WORKABLE, SHIFT  

ORGANISATIONAL: Can't be participated, only for organization purposes  
WORKABLE: These are the tasks volunteers can be participate on  
SHIFT: Workable tasks can be divided into shifts for marking time-spans on a task  

These types exclude each other when trying to extend them illogically.  

ORGANISATIONAL can only extend to another ORGANISATIONAL or WORKABLE,  
WORKABLE can only extend multiple SHIFT-tasks,  
every other extension is denied by the backend.  

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Copy target task (work in progress)**

##### *Request:*

GET /task/{task_id}/copy

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Updates the value of an task with an open amount of volunteers, based on the amount of volunteers on their child-tasks**

##### *Request:*

GET /task/{task_id}/updateAmountOfVolunteers

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Sets the TaskRepetitionState from once to periodic if possible, mandatory to add a date as json**

##### *Request:*

GET /task/{task_id}/periodic/set

	{
		"year": 0,
		"month": 0,
		"day": 2,
	   "hour": 2,
	   "minute": 2
	   
	}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Resets the TaskRepetitionState from periodic to once**

##### *Request:*

GET /task/{task_id}/periodic/undo

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Issues an invite-notification to the target user or group**

##### *Request:*

PUT /task/{task_id}/invite/{inv_type}/{inv_id}

{inv_type} can either be "user" or "group". {inv_id} then has to either be the user-id or the group-id.
In case of a group, the task is added to the "invitedToTasks"-List of the given task.

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Restrict a task to target group**

##### *Request:*

PUT /task/{task_id}/restrict/group/{group_id}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Restrict a task to multiple groups and replace the groups already there**

##### *Request:*

PUT /task/{task_id}/restrict/group/multiple  

	[
		{
			"id": 1
		},
		{
			"id": 3
		},
		{
			"id": 8
		}
	]

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Change the state of target task; Choose either 'publish', 'start', or 'complete'**
*For each state different prerequisite have to be fullfilled:*
*NOT_PUBLISHED: Default state*
*PUBLISHED: Only allowed when the task-fields are all filled*
*STARTED: Only allowed when the parent task is started and if sequential, the previous task is completed*
*COMPLETED: A task can only be completed when its children are all completed or if it has none*

##### *Request:*

PUT /task/{task_id}/state/{state_name}

##### *Response:*

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

##### *Request:*

GET /task/updateStarted

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Nominate someone as the leader of a task as creator**

##### *Request:*

GET /task/{task_id}/nominateLeader/{user_id}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Returns the values for the enum taskParticipationType**

##### *Request:*

GET /task/taskParticipationTypes

##### *Response:*

	[
	  "PARTICIPATING",
	  "FOLLOWING",
	  "LEADING"
	]

-----------------------------------------------------------------

**Returns the values for the enum taskState**

##### *Request:*

GET /task/taskStates

##### *Response:*

	[
	  "NOT_PUBLISHED",
	  "PUBLISHED",
	  "STARTED",
	  "COMPLETED"
	]

-----------------------------------------------------------------

**Returns the values for the enum taskType**

##### *Request:*

GET /task/taskTypes

##### *Response:*

	[
	  "PARALLEL",
	  "SEQUENTIAL"
	]

-----------------------------------------------------------------

**Returns all tasks, that are supertasks**

##### *Request:*

GET /task/parents

##### *Response:*

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

##### *Request:*

POST /task/elastic/query

	{
		"text": "This is a fulltext-query!"
	}

##### *Response:*

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

### Competence-Endpoints

-----------------------------------------------------------------

**Returns the competences of the currently logged in user, wrapped in the relationship-object**

##### *Request:*

GET competence

##### *Response:*

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

**Returns an array containing all competences**

##### *Request:*

GET /competence/all

##### *Response:*

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

**Returns all competences that are related to target competence, ordered by it's relation-value**

##### *Request:*

GET /competence/{competence_id}/related

##### *Response:*

Standard response, the related-competences are part of the meta-object

-----------------------------------------------------------------

**Returns target competence-topic-area and its mapped competences**

##### *Request:*

GET /competence/area/{area_id}

##### *Response:*

Standard response, the related-competences are part of the meta-object

-----------------------------------------------------------------

**Returns all competence-topic-areas**

##### *Request:*

GET /competence/area

##### *Response:*

Standard response, the related-competences are part of the meta-object

-----------------------------------------------------------------

**Returns a competence object with given id**

##### *Request:*

GET /competence/{competence_id}

##### *Response:*

	{
		"id": {competence_id},
		"name": "searchedCompetence",
		...
	}

-----------------------------------------------------------------

**Creates a new competence**

##### *Request:*

POST /admin/competence

#### This function requires ADMIN-rights! Only use for testing, this should be not used live!

	{
	    "name":"testCompetence",
	    "description": "this is a competence"
	}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Deletes competence by given ID**

##### *Request:*

DELETE /admin/competence/{competence_id}
#### This function requires ADMIN-rights! Only use for testing, this should be not used live!

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Updates a competence by given ID**

##### *Request:*

PUT /admin/competence/{competence_id}
#### This function requires ADMIN-rights! Only use for testing, this should be not used live!

	{
	    "name":"testCompetence",
	    "description": "this is a competence"
	}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Connects two competences via a type and additional values**

##### *Request:*

POST /competence/{competence1_id}/connect/{competence2_id}/type/{type_id}
#### Only use for testing, this should be not used live!

	{
	    "uniDirection": true
	}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

### Filter-Configuration-Endpoints
These endpoints handle the filter-configuration of the User-Task-Allocation (the matrix-part)

-----------------------------------------------------------------
**Adds a filter to the filter-configuration, based on it's name**

##### *Request:*

GET /configuration/filter/add/{filter_name}

The possible names for the filters are: LikeLevelFilter, ImportancyLevelFilter, ProficiencyLevelFilter, UserRelationFilter.

##### *Response:*

Json-data, either a success or a failure message when an invalid filter-name has been used.

-----------------------------------------------------------------
**Adds multiple filters to the filter-configuration, based on the list of filters in the posted JSon-file**

##### *Request:*

POST /configuration/filter/add

The possible names for the filters are: LikeLevelFilter, ImportancyLevelFilter, ProficiencyLevelFilter, UserRelationFilter.

	{
		"parameters": ["ImportancyLevelFilter", "LikeLevelFilter", "ProficiencyLevelFilter", "LikeLevelFilter"]
	}

##### *Response:*

Json-data, either a success or a failure message when only invalid filter-names have been used.
If that is the case, the configuration resets itself to the standard configuration.
This configuration consists of: LikeLevelFilter, ImportancyLevelFilter, ProficiencyLevelFilter.

-----------------------------------------------------------------
**Returns a list of all active filters**

##### *Request:*

GET /configuration/filter/print

##### *Response:*

	{
	  "success": "true",
	  "msg": "ProficiencyLevelFilter is available! LikeLevelFilter is available! UserRelationFilter is available! ImportancyLevelFilter is available! "
	}

-----------------------------------------------------------------
**Clears (empties) the list of active filters**

##### *Request:*

GET /configuration/filter/clear

##### *Response:*

Json-data, a success

-----------------------------------------------------------------
**Restores the standard state of the filter-configuration**

##### *Request:*

This endpoint restores the standard state of the configuration, consisting of: LikeLevelFilter, ImportancyLevelFilter, ProficiencyLevelFilter.

GET /configuration/filter/restore

##### *Response:*

Json-data, a success.

-----------------------------------------------------------------

### Notifications-Endpoints
These endpoints handle already issued notifications for the most parts

-----------------------------------------------------------------

**Returns all notifications, which target the logged in user**

##### *Request:*

GET /notification

##### *Response:*

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

##### *Request:*

GET /notification/admin

#### This function requires ADMIN-rights!

##### *Response:*

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

##### *Request:*

GET /notification/{notification_id}/accept

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Triggers the deny-method of the notification and deletes it**

##### *Request:*

GET /notification/{notification_id}/deny

##### *Response:*

Json-data, either a success or a failure message
	
-----------------------------------------------------------------
	
### Evaluation-Endpoints
These endpoints handle evaluations

-----------------------------------------------------------------
	
**Creates an evaluation (notification + entity) for the logged in user for target task**

##### *Request:*

POST /evaluation/task/{task_id}/self

##### *Response:*

Json-data, either a success or a failure message
	
-----------------------------------------------------------------

**Creates an evaluation (notification + entity) for every user, participating in target task**

##### *Request:*

POST /evaluation/task/{task_id}/all

##### *Response:*

Json-data, either a success or a failure message
	
-----------------------------------------------------------------

**Returns all evaluations of the logged in user**

##### *Request:*

GET /evaluation

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Resolves the evaluation. Updates the empty evaluation with sent data and deletes the notification**

##### *Request:*

PUT /evaluation/{evaluation_id}

for current evaluation send these attributes with arbitrary values:

	{
	    "likeValOthers": 0.5,
	    "likeValTask": 0.5
	}

##### *Response:*

Json-data, either a success or a failure message
	
-----------------------------------------------------------------

### Group-Endpoints
These endpoints handle groups

-----------------------------------------------------------------
	
**Get all groups**

##### *Request:*

GET /group

##### *Response:*

Json-data, either a success or a failure message
	
-----------------------------------------------------------------

**Get the group with given ID**

##### *Request:*

GET /group/{group_id}

##### *Response:*

Json-data, either a success or a failure message
	
-----------------------------------------------------------------

**Create a new group, creator is the logged-in user**

##### *Request:*

POST /group

	{
		"name": "groupExample",
		"description": "An example of a group",
		"maxEnrols": 5
	}

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Delete the group with given ID**

##### *Request:*

DELETE /group/{group_id}


##### *Response:*

Json-data, either a success or a failure message
	
-----------------------------------------------------------------

**Update the group with given ID**

##### *Request:*

PUT /group/{group_id}

	{
		"name": "groupExample",
		"description": "An example of a group",
		"maxEnrols": 5
	}
	
##### *Response:*

Json-data, either a success or a failure message
	
-----------------------------------------------------------------

**Add multiple users to a group**

##### *Request:*

PUT /group/{group_id}/add/multiple

Multiple users can be added to a given group by posting them in one JSON-document like this:

	[
		{
			"id": 1,
			"name": user1
		},
		{
			"id": 3,
			"name": user3
		},
		{
			"id": 8,
			"name": user8
		}
	]

##### *Response:*

Json-data, either a success or a failure message

-----------------------------------------------------------------

**Add target user to a group**

##### *Request:*

PUT /group/{group_id}/add/user/{user_id}

##### *Response:*

Json-data, either a success or a failure message
	
-----------------------------------------------------------------

**Removes target user from a group**

##### *Request:*

DELETE /group/{group_id}/remove/user/{user_id}

##### *Response:*

Json-data, either a success or a failure message
	
-----------------------------------------------------------------

## CHANGES

In this section, changes and their dates are noted.

-----------------------------------------------------------------

### 25.10.2016

Role-System implemented -> see section Role-Endpoints
The endpoint for the role-enums has been removed

Token-System implemented -> see section User-Endpoints for login (creation) and logout (destruction) with tokens
The "check"-endpoint is not part of the readMe anymore, since it's depricated and will be removed soon,
if used, change it to the "login"-endpoint

-----------------------------------------------------------------

### 11.11.2016

Endpoint-change -> Assigned competences of user now at the /competence-endpoint, all competences at the endpoint /competence/all

-----------------------------------------------------------------

### 16.11.2016

Endpoint-change -> A single task + task-user-relationship is available at the /user/task/{task_id}-endpoint

-----------------------------------------------------------------

### 27.11.2016

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

-----------------------------------------------------------------

### 14.12.2016

Change to how the amount of volunteers on tasks is handled  
The attribute can be set to any positive number, meaning the concrete amount of volunteers, or to "0", meaning an infinite number  
of volunteers can join the task. This "0" can also be updated to a concrete amount, based on the amount that's placed on the child-tasks:  

GET /task/{task_id}/updateAmountOfVolunteers -> NEW  

-----------------------------------------------------------------

### 16.12.2016

Change to the endpoint, that gets users for a specific task.  

GET /task/{task_id}/findMatchingUsers -> from /task/findMatchingUsers/{task_id}  

## MAJOR UPDATE TO THE MATCHING-SYSTEM!!!!  
-> Competences are synchronized from the DB into the running system for performance-reasons now, and thus MUST be synchronized by hand if there is a   change on KOMET! This can be done by calling the following endpoint with ADMIN-permissions:  

GET /admin/sync -> NEW  

-----------------------------------------------------------------

#### 23.1.2017  

A new filter-configuration has been added to the system.  
These filters influence the matching between users and tasks and can be added and removed.  
There are 4 filter-types at the moment:  
LikeLevelFilter (changed matching-values based on the user's affection towards a competence), ImportancyLevelFilter (changed matching-values based on the importance of the competence), ProficiencyLevelFilter (changes matching-values based on the users proficiency and the tasks needed proficiency in a competence) and UserRelationFilter (changes the matching-values based on the relation of the searching user to already participating users).  

Look up the filter-section of the readMe details to the endpoints.  

-----------------------------------------------------------------

#### 24.1.2017

A new endpoint returns a specified amount of best matches.  

GET /user/findMatchingTasks/{number_of_tasks} -> NEW  

-----------------------------------------------------------------

#### 12.2.2017

Materials can now be assigned to tasks.  
Endpoints for that are in the task-section.  

GET /task/{task_id}/material/add -> NEW  
GET /task/{task_id}/material/update/{material_id} -> NEW  
GET /task/{task_id}/material/remove/{material_id} -> NEW  

-----------------------------------------------------------------

#### 18.2.2017  

Materials can be subscribed to (with a quantity) by the logged in user.  
Adding a material returns the material-id.  

GET /task/{task_id}/material/{material_id}/subscribe/{quantity} -> NEW  
GET /task/{task_id}/material/{material_id}/unsubscribe -> NEW  

Look at the task-section for more info  
 
-----------------------------------------------------------------

#### 20.3.2017

Endpoints to set tasks ready-to-publish are removed, this is done automatically now!  
TaskTypes have been added! More information in the endpoint for adding tasks  
Error-Responses are updated, more information in the error-section!

-----------------------------------------------------------------

#### 29.3.2017

The Task-Workflow has been removed from the ReadMe! It can be found on SVN/contact me for information  

New Endpoints (especially important for competence-recommendation) :  

GET /area  
GET /area/{area_id}  
GET /{competence_id}/related  

More infos in the competence-section!  

Endpoints that are changed :  

OLD -> NEW  

Competence-related:  

GET /user/competence/{competence_id}/add -> POST /competence/{competence_id}/add  **(PARAMETERS NOW VIA JSON)**  
GET /user/competence/{competence_id}/adjust -> PUT /competence/{competence_id}/adjust  **(PARAMETERS NOW VIA JSON)**  
GET /user/competence/{competence_id}/remove -> DELETE /competence/{competence_id}/remove  
GET /user/competence -> GET /competence **(BOTH ENDPOINTS MERGED)**  

Task-related:  

GET /user/task/{task_id} -> GET /task/{task_id} **(BOTH ENDPOINTS MERGED)**  
GET /user/task -> GET /task/type  
GET /user/task/{task_id}/{state_name} -> PUT /task/{task_id}/add/{state_name}  
GET /user/task/{task_id}/remove -> DELETE /task/{task_id}/remove  
GET /user/findMatchingTasks -> GET /task/find  
GET /user/findMatchingTasks/{number_of_tasks} -> GET /task/find/{number_of_tasks}  
GET /task/{task_id}/findMatchingUsers -> GET /user/find/{task_id}   
POST /task/queryES -> POST /task/elastic/query
GET /task/{task_id}/competence/{competence_id}/require/{proficiency}/{importance}/{mandatory} -> POST /task/{task_id}/competence/{competence_id}/require  **(PARAMETERS NOW VIA JSON)**  
GET /task/{task_id}/material/{material_id}/remove -> DELETE /task/{task_id}/material/{material_id}/remove  
GET /task/{task_id}/material/{material_id}/subscribe/{quantity} -> PUT /task/{task_id}/material/{material_id}/subscribe/{quantity}  
GET /task/{task_id}/material/{material_id}/unsubscribe -> DELETE /task/{task_id}/material/{material_id}/unsubscribe  
GET /task/{task_id}/competence/{competence_id}/remove -> DELETE /task/{task_id}/competence/{competence_id}/remove  
GET /task/{task_id}/done/{done_boolean} -> PUT /task/{task_id}/done/{done_boolean}  
GET /task/{task_id}/state/{state_name} -> PUT /task/{task_id}/state/{state_name}  

-----------------------------------------------------------------

#### 29.6.2017

Changes for a better integration of the evaluation-topic have been made:  

New section for "Evaluation-Endpoints"!  

Endpoints for calling completed tasks (for more information, see section "Task-Endpoints":  

GET /task/completed/{part_type}  
GET /task/completed  

-----------------------------------------------------------------

#### 25.7.2017

Endpoints for groups have been added:  

New section "Group-Endpoints"!  
Endpoint for inviting users to a task is now PUT /task/{task_id}/invite/{inv_type}/{inv_id}  
Endpoint for restricting a task to different groups PUT /task/{task_id}/restrict/group/{group_id}

Endpoints for roles have changed --> see the "User-Endpoints"!  

-----------------------------------------------------------------

#### 3.8.2017

Major change for the "get all tasks"-Endpoints --> see the "Task-Endpoints" (first entry)!  

-----------------------------------------------------------------

#### 8.8.2017

New endpoints for adding and removing users to and from groups --> see "Group-Endpoints"  
