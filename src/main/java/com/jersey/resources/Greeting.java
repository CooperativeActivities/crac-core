package com.jersey.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class Greeting {

	@GET
    public String greeting() {
        return "Hello! Jersey up and running! <br/> Go to: <a href=\"hello-world\">me</a>";
    }

}