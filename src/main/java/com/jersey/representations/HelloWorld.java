package com.jersey.representations;

public class HelloWorld {
    private int id;
    private String hello;
    private String world;

    public HelloWorld(int id, String hello, String world) {
        this.id = id;
        this.hello = hello;
        this.world = world;
    }

    
    
	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}



	public String getHello() {
		return hello;
	}

	public void setHello(String hello) {
		this.hello = hello;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}



}
