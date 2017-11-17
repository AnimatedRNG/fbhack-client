package com.hackmit.fbg;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

public class Constellation {
	public static final float TOO_CLOSE = 1.5f;
	
	public List<Vertex> vertices;
	public List<Edge> edges;
	
	public Vector3 roomOrigin = new Vector3(0, 0, 0);
	public Vector3 roomSize = new Vector3(5, 5, 5);
	
	public Constellation() {
		this.vertices = new ArrayList<Vertex>();
		this.edges = new ArrayList<Edge>();
		
		vertices.add(new Vertex("test", new Texture("badlogic.jpg")));
		vertices.add(new Vertex("test2", new Texture("badlogic.jpg")));
		edges.add(new Edge(vertices.get(0), vertices.get(1)));
	}
	
	public void update() {
		for (Vertex vertex : this.vertices) {
			if (!vertex.isReady) {
				vertex.create(findEmptyLocation());
				System.out.println("Vertex: " + vertex.toString());
			}
		}
		
		for (Edge edge : this.edges) {
			if (!edge.isReady) {
				edge.create();
			}
		}
	}
	
	public void render(ModelBatch batch) {
		for (Vertex vertex : this.vertices) {
			if (vertex.photo != null) {
				vertex.photo.bind();
			}
			batch.render(new ModelInstance(vertex.model));
		}
		
		for (Edge edge : this.edges) {
			batch.render(new ModelInstance(edge.model));
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
