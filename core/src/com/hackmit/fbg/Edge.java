package com.hackmit.fbg;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class Edge {

	public Vertex v1;
	public Vertex v2;
	
	public Model model;
	public boolean isReady;
	
	public Edge(Vertex v1, Vertex v2) {
		this.v1 = v1; 
		this.v2 = v2;
		
		this.model = null;
		this.isReady = false;
	}
	
	public void create() {
		Vector3 offset = new Vector3(0.0f, 0.5f, 0.5f);
		Vector3 start = new Vector3(v1.position).add(offset);
        Vector3 end = new Vector3(v2.position).add(offset);
        
        Material mat = new Material(ColorAttribute.createDiffuse(Color.GREEN));
		
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder builder = modelBuilder.part("line", 1, 3, mat);
		builder.line(start, end);
		model = modelBuilder.end();
		
		isReady = true;
	}
}
