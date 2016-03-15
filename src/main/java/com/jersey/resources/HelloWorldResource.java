package com.jersey.resources;

import com.jersey.representations.HelloWorld;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HelloWorldResource {

    @GET
    public List<HelloWorld> getAll(){
        List<HelloWorld> products = new ArrayList<HelloWorld>();
        products.add(new HelloWorld(1, "Hello", "World"));
        products.add(new HelloWorld(2, "Hi", "World"));
        return products;
    }

    @GET
    @Path("{id}")
    public HelloWorld getOne(@PathParam("id")int id){
        if(id == 888){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }else {
            return new HelloWorld(id, "Hello", "World");
        }
    }
}