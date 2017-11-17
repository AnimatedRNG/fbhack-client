package com.hackmit.fbg;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
	
	public Mesh createFullScreenQuad() {
		Mesh mesh = new Mesh(true, 4, 6, 
                new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

        mesh.setVertices(new float[] { 0, -0.5f, -0.5f, 0, 0,
                                       0, 0.5f, -0.5f, 1, 0,
                                       0, 0.5f, 0.5f, 1, 1,
                                       0, -0.5f, 0.5f, 0, 1 });
                                       
        mesh.setIndices(new short[] { 0, 1, 2, 0, 2, 3 });
        
        return mesh;
	}
	
	public void create(Vector3 location) {
		ModelBuilder modelBuilder = new ModelBuilder();
		//Material quadMaterial = new Material(ColorAttribute.createDiffuse(Color.GREEN));
		//Material quadMaterial = new Material();
		Material quadMaterial = new Material(TextureAttribute.createDiffuse(photo));
		quadMaterial.set(IntAttribute.createCullFace(GL20.GL_NONE));
		
		long attributes = Usage.Position | Usage.TextureCoordinates;
		
		modelBuilder.begin();
		modelBuilder.manage(photo);
		MeshPartBuilder meshBuilder = 
				modelBuilder.part("quad", GL20.GL_TRIANGLES, attributes, quadMaterial);
		meshBuilder.addMesh(createFullScreenQuad());
		model = modelBuilder.end();
		
		/*model = modelBuilder.createRect(0, 0, 0, // (0, 0) 
        								0, 0, 1, // (0, 1)
        								0, 1, 1, // (1, 1)
        								0, 1, 0, // (1, 0)
        								1, 0, 0, // Normal
        		quadMaterial, attributes);*/
		
		model.nodes.get(0).translation.set(location);
		this.position = location;
		
		isReady = true;
	}
	
	@Override
	public String toString() {
		return this.name + " " + position.toString();
	}
}
