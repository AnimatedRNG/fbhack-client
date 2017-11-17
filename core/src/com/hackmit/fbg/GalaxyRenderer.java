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
	private Constellation constellation;
	
	Texture img;	
	
	@Override
	public void create () {
		modelBatch = new ModelBatch();
		
		this.constellation = new Constellation();
		this.constellation.update();
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        
		camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
        
		img = new Texture("badlogic.jpg");
	}

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LESS);
        
        camController.update();
        
        /*test.nodes.get(0).rotation.set(new Quaternion().setFromCross(
        		new Vector3(1, 0, 0),
        		new Vector3(cam.position).nor()));*/
        
        modelBatch.begin(cam);
        this.constellation.render(modelBatch);
        modelBatch.end();
	}
	
	@Override
	public void dispose () {
		modelBatch.dispose();
		img.dispose();
	}
}
