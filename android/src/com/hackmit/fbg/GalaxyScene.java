package com.hackmit.fbg;

import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.github.claywilkinson.arcore.gdx.ARCoreScene;
import com.github.claywilkinson.arcore.gdx.PlaneAttachment;
import com.github.claywilkinson.arcore.gdx.SimpleShaderProvider;
import com.github.claywilkinson.helloargdx.PlaneMaterial;
import com.github.claywilkinson.helloargdx.PlaneModel;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.PlaneHitResult;
import com.google.ar.core.Pose;
import com.google.ar.core.exceptions.NotTrackingException;

import java.util.Collections;

/**
 * Created by srinivas on 11/17/17.
 */

public class GalaxyScene extends ARCoreScene {

    private Constellation constellation;
    private boolean renderNow;

    @Override
    public void create() {
        super.create();

        new Timer().scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                if (constellation == null) {
                    // Check whether we should create the constellation or not;
                    // do we have an anchor
                } else {
                    // Update the constellation to the right position
                    if (constellation.updateMe) {
                        constellation.update();
                    }

                    renderNow = true;
                    //constellation.render(modelBatch);
                }
            }
        }, 1f, 1f);
    }

    /** Create a new shader provider that is aware of the Plane material custom shader. */
    @Override
    protected ShaderProvider createShaderProvider() {
        return new SimpleShaderProvider() {
            @Override
            protected Shader createShader(Renderable renderable) {
                if (renderable.material.id.startsWith(PlaneMaterial.MATERIAL_ID_PREFIX)) {
                    return PlaneMaterial.getShader(renderable);
                } else {
                    return super.createShader(renderable);
                }
            }
        };
    }

    private void handleInput(Frame frame) {
        if (Gdx.input.justTouched()) {

            int x = Gdx.input.getX();
            int y = Gdx.input.getY();

            if (frame.getTrackingState() == Frame.TrackingState.TRACKING) {
                for (HitResult hit : frame.hitTest(x, y)) {
                    // Check if any plane was hit, and if it was hit inside the plane polygon.
                    if (hit instanceof PlaneHitResult && ((PlaneHitResult) hit).isHitInPolygon()) {
                        // Cap the number of objects created. This avoids overloading both the
                        // rendering system and ARCore.
                        /*if (instances.size() >= 16) {
                            String key = instances.keySet().iterator().next();
                            PlaneAttachment<ModelInstance> item = instances.remove(key);
                            getSession().removeAnchors(Collections.singletonList(item.getAnchor()));
                        }*/
                        // Adding an Anchor tells ARCore that it should track this position in
                        // space. This anchor will be used in PlaneAttachment to place the 3d model
                        // in the correct position relative both to the world and to the plane.
                        try {
                            PlaneAttachment<ModelInstance> planeAttachment =
                                    new PlaneAttachment<>(
                                            ((PlaneHitResult) hit).getPlane(),
                                            getSession().addAnchor(hit.getHitPose()),
                                            null);

                            //instances.put(planeAttachment.getAnchor().getId(), planeAttachment);

                            Pose p = planeAttachment.getPose();
                            // position and rotate
                            Vector3 dir =
                                    new Vector3(frame.getPose().tx(), frame.getPose().ty(), frame.getPose().tz());
                            Vector3 pos =
                                    new Vector3(p.tx(),p.ty(),p.tz());
                            pos.add(new Vector3(0, 1.7f, 0f));
                            //item.transform.translate(pos);
                            if (constellation == null) {
                                Log.w("HelloScene", "Created constellation!");
                                constellation = new Constellation(pos,
                                        new Vector3(5, 1.3f, 5f));
                            }
                            constellation.update(pos);
                        } catch (NotTrackingException e) {
                            Log.w("HelloScene", "not tracking: " + e);
                        }

                        // Hits are sorted by depth. Consider only closest hit on a plane.
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void render(Frame frame, ModelBatch modelBatch) {
        // Draw all the planes detected
        drawPlanes(modelBatch);

        handleInput(frame);
        if (renderNow) {
            this.constellation.render(modelBatch);
        }
    }

    /** Draws the planes detected. */
    private void drawPlanes(ModelBatch modelBatch) {
        Array<ModelInstance> planeInstances = new Array<>();
        int index = 0;
        for (Plane plane : getSession().getAllPlanes()) {

            // check for planes that are no longer valid
            if (plane.getSubsumedBy() != null
                    || plane.getTrackingState() == Plane.TrackingState.STOPPED_TRACKING) {
                continue;
            }
            // New plane
            ModelInstance instance = new ModelInstance(PlaneModel.createPlane(plane, index++));
            instance.transform.setToTranslation(
                    plane.getCenterPose().tx(), plane.getCenterPose().ty(), plane.getCenterPose().tz());
            planeInstances.add(instance);
        }
        modelBatch.render(planeInstances);
    }
}
