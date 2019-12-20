package com.example.demo.googlemodel;

import java.util.List;

import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiModel;

@ApiModel("JSON Google Vision Response")
@Component
public class Json {
	
	List<Requests> requests;
	
	public List<Requests> getRequests() {
		return requests;
	}

	public void setRequests(List<Requests> requests) {
		this.requests = requests;
	}

}
