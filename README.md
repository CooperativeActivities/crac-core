##CrAc

##Endpoints

###Login-related

####THE CREDENTIALS (NAME AND PASSWORD) FOR ATHENTICATION MUST ALWAYS BE DELIVERED IN THE HEADER OF THE REQUEST AS BASIC AUTHENTICATION! 
If this is not the case, the server will just return an "unauthorized"-message!
####If the logged in user does not posses the rights for executing the method at a given endpoint, there will be a 403-message as return value. 
Eg.: A user without admin-rights tries to delete another user.

**Request**

GET /user/login

->the name and password have to be added in the header as the basic-authentication

**Response**

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

###User-Functions

**Request**

GET /user

**Response**

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

**Request**

GET /user/{id}

**Response**

A user object with given id

	{
		"id": {id},
		"name": "searchedUser",
		...
	}

**Request**

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

**Response**

Json-data, either a success or a failure message

**Request**

DELETE /user/{id}
####This function requires ADMIN-rights!

**Response**

Json-data, either a success or a failure message

**Request**

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

**Response**

Json-data, either a success or a failure message

**Request**

GET /user/me

**Response**

The object of the currently logged in user

	{
		"id": 3,
		"name": "currentUser",
		...
	}

**Request**

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

**Response**

Json-data, either a success or a failure message

**Request**

GET user/addCompetence/{competence_id}

Adds the competence with given id to the currently logged in user

**Response**

Json-data, either a success or a failure message

**Request**

GET user/removeCompetence/{competence_id}

Removes the competence with given id from the currently logged in user

**Response**

Json-data, either a success or a failure message

**Request**

GET user/addTask/{task_id}

Adds the task with given id to the open-tasks of the currently logged in user

**Response**

Json-data, either a success or a failure message

**Request**

GET user/removeTask/{task_id}

Removes the task with given id from the open-tasks of the currently logged in user

**Response**

Json-data, either a success or a failure message

**Request**

GET user/followTask/{task_id}

Adds the task with given id to the followed-tasks of the currently logged in user

**Response**

Json-data, either a success or a failure message

**Request**

GET user/unfollowTask/{task_id}

Removes the task with given id from the follow-tasks of the currently logged in user

**Response**

Json-data, either a success or a failure message

**Request**

GET user/leadTask/{task_id}

Adds the task with given id to the leading-tasks of the currently logged in user

**Response**

Json-data, either a success or a failure message

**Request**

GET user/abandonTask/{task_id}

Removes the task with given id from the leading-tasks of the currently logged in user

**Response**

Json-data, either a success or a failure message