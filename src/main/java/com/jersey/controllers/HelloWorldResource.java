package com.jersey.controllers;

import com.jersey.representations.HelloWorld;
import com.models.HelloWorldDAO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Path("/hello-world")
public class HelloWorldResource {
	
    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<HelloWorld> getAll(){
        List<HelloWorld> products = new ArrayList<HelloWorld>();
        helloWorldDAO.count();
        products.add(new HelloWorld("Hi", "World"));
        return products;
    }
   

    @GET
    @Path("{id}")
    public HelloWorld getOne(@PathParam("id")int id){
        if(id == 888){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }else {
            return new HelloWorld("Hello", "World");
        }
    }
	@Autowired
	  private HelloWorldDAO helloWorldDAO;


    
}