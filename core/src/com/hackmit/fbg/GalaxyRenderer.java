package com.hackmit.fbg;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class GalaxyRenderer extends ApplicationAdapter {
	private PerspectiveCamera cam;
	private CameraInputController camController;
	private ModelBatch modelBatch;
	
	private Model test;
	private Model line;
	
	Texture img;	
	
	@Override
	public void create () {
		modelBatch = new ModelBatch();
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        
		camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
        
        ModelBuilder modelBuilder = new ModelBuilder();
        Material quadMaterial = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        quadMaterial.set(IntAttribute.createCullFace(GL20.GL_NONE));
        Model model = modelBuilder.createBox(5f, 5f, 5f, 
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            Usage.Position | Usage.Normal);
        model = modelBuilder.createRect(0, 0, 0, // (0, 0) 
        								0, 1, 0, // (0, 1)
        								1, 1, 0, // (1, 1)
        								1, 0, 0, // (1, 0)
        								0, 0, 1, // Normal
        		quadMaterial, Usage.Position | Usage.Normal);
        line = createLine(
        		new Vector3(0, 0, 0), 
        		new Vector3(3, 1, 1),
        		quadMaterial);
        test = model;
        
		img = new Texture("badlogic.jpg");
	}
	
	private Model createLine(
			Vector3 start, 
			Vector3 end, 
			Material mat) {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder builder = modelBuilder.part("line", 1, 3, mat);
		builder.line(start, end);
		Model model = modelBuilder.end();
		return model;
	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        camController.update();
        
        test.nodes.get(0).rotation.set(new Quaternion().setFromCross(
        		new Vector3(0, 0, 1),
        		new Vector3(cam.position).nor()));
        
        modelBatch.begin(cam);
        modelBatch.render(new ModelInstance(test));
        modelBatch.render(new ModelInstance(line));
        modelBatch.end();
	}
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		img.dispose();
	}
}
