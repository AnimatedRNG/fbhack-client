package com.hackmit.fbg;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector3;

public class Constellation {
	public static final float TOO_CLOSE = 1.0f;
	
	public List<Vertex> vertices;
	public List<Edge> edges;
	
	public Vector3 roomOrigin = new Vector3(0, 0, 0);
	public Vector3 roomSize = new Vector3(1, 2, 1);
	
	public Constellation() {
		this.vertices = new ArrayList<Vertex>();
		this.edges = new ArrayList<Edge>();
	}
	
	public void update() {
		for (Vertex vertex : this.vertices) {
			if (!vertex.isReady) {
				vertex.create(findEmptyLocation());
			}
		}
	}
	
	private float halfRand() {
		return (float) (Math.random() - 0.5f);
	}
	
	private Vector3 findEmptyLocation() {
		boolean terminate = false;
		Vector3 testVertex = null;
		while (!terminate) {
			terminate = true;
			testVertex = new Vector3(
					roomOrigin.x + halfRand() * roomSize.x,
					roomOrigin.y + halfRand() * roomSize.y,
					roomOrigin.z + halfRand() * roomSize.z);
			for (Vertex vertex : vertices) {
				if (vertex.position != null && 
						vertex.position.dst(testVertex) < TOO_CLOSE) {
					terminate = false;
				}
			}
		}
		return testVertex;
	}
}
