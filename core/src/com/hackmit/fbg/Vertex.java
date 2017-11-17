package com.hackmit.fbg;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class Vertex {

	public String name;
	public Texture photo;
	
	public Vector3 position;
	
	public Model model;
	public boolean isReady;
	
	public Vertex(String name, Texture photo) {
		this.name = name;
		this.photo = photo;
		
		this.model = null;
		this.isReady = false;
	}
	
	public void create(Vector3 location) {
		ModelBuilder modelBuilder = new ModelBuilder();
		Material quadMaterial = new Material(ColorAttribute.createDiffuse(Color.GREEN));
		model = modelBuilder.createRect(0, 0, 0, // (0, 0) 
        								0, 0, 1, // (0, 1)
        								0, 1, 1, // (1, 1)
        								0, 1, 0, // (1, 0)
        								1, 0, 0, // Normal
        		quadMaterial, Usage.Position | Usage.Normal);
		
		model.nodes.get(0).translation.set(location);
		
		isReady = true;
	}
}
