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
	
	@Override
	public int hashCode() {
		return type.hashCode() * data.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ConstellationRequest)) {
			return false;
		} else {
			ConstellationRequest con = (ConstellationRequest) obj;
			return con.type == this.type && con.data == this.data;
		}
	}
}
