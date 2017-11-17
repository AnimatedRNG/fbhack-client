package com.hackmit.fbg;

public class ConstellationRequest {
	public enum RequestType {
		GET_FRIENDS,
		GET_FRIEND_INFO
	}
	
	public RequestType type;
	public String data;
	
	public ConstellationRequest(RequestType type, String data) {
		this.type = type;
		this.data = data;
	}
}
