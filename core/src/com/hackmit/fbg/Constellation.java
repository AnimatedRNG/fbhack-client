package com.hackmit.fbg;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class Constellation {
	public static final float TOO_CLOSE = 0.08f;
	
	public Map<String, Vertex> vertices;
	public List<Edge> edges;
	public Queue<IDPair> pendingEdges;
	public Queue<Vertex> pendingVertices;
	
	public Vector3 roomOrigin = new Vector3(0, 3, 0);
	public Vector3 roomSize = new Vector3(20, 2.5f, 20);
	
	public Queue<ConstellationRequest> networkRequests;
	public Set<ConstellationRequest> oldRequests;
	public Texture tmp;
	
	public boolean updateMe = false;

	public Constellation(Vector3 roomOrigin, Vector3 roomSize) {
		this();
		this.roomOrigin = roomOrigin;
		this.roomSize = roomSize;
	}

	public Constellation() {
		this.vertices = new HashMap<String, Vertex>();
		this.edges = new ArrayList<Edge>();
		this.pendingEdges = new ConcurrentLinkedQueue<IDPair>();
		this.pendingVertices = new ConcurrentLinkedQueue<Vertex>();
		
		this.networkRequests = new ConcurrentLinkedQueue<ConstellationRequest>();
		this.oldRequests = new HashSet<ConstellationRequest>();
		
		vertices.put("matthew.pfeiffer2", new Vertex("Matthew Pfeiffer", new Texture("badlogic.jpg"), "matthew.pfeiffer2"));
		
		tmp = new Texture("badlogic.jpg");
		
		this.networkRequests.add(new ConstellationRequest(
				ConstellationRequest.RequestType.GET_FRIENDS,
				encodeURL("matthew.pfeiffer2")));
		this.networkRequests.add(new ConstellationRequest(
				ConstellationRequest.RequestType.GET_FRIEND_INFO,
				encodeURL("matthew.pfeiffer2")));
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				networkUpdate();
			}
		}).start();
	}
	
	public void networkUpdate() {
		while (true) {
			final ConstellationRequest head = this.networkRequests.poll();
			if (head != null)
				System.out.println("Current head: " + head.data);
			if (head != null && 
					(!vertices.containsKey(head.data) ||
					head.type.equals(ConstellationRequest.RequestType.GET_FRIENDS) ||
					vertices.size() <= 2)) {
				String url = "http://35.185.60.3";
				
				if (head.type.equals(ConstellationRequest.RequestType.GET_FRIENDS)) {
					url += "/friends/";
				} else {
					url += "/info/";
				}
				
				url += head.data;
				
				System.out.println("url: " + url);
				HttpRequest httpGet = new HttpRequest(HttpMethods.GET);
				httpGet.setUrl(url);
				
				if (!oldRequests.contains(head)) {
					Gdx.net.sendHttpRequest(httpGet, new ConstellationHttpResponseHandler(head));
					oldRequests.add(head);
				}
			}
		}
	}
	
	public String encodeURL(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8").replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			return url;
		}
	}

	public void update() {
		update(roomOrigin);
	}

	public void update(Vector3 newOrigin) {
		Vector3 oldOrigin = this.roomOrigin;
		this.roomOrigin = newOrigin;

		System.out.println("New origin: " + newOrigin);
		System.out.println("Old origin: " + oldOrigin);

		while (!this.pendingVertices.isEmpty()) {
			Vertex newVertex = this.pendingVertices.poll();
			System.out.println("Added new vertex " + newVertex.ID);
			this.vertices.put(newVertex.ID, newVertex);
		}
		
		for (int i = 0; i < 5 && !pendingEdges.isEmpty(); i++) {
			IDPair pendingEdge = this.pendingEdges.poll(); 
			System.out.println("Polled pending edge " + pendingEdge);
			if (this.vertices.containsKey(pendingEdge.first) &&
					this.vertices.containsKey(pendingEdge.second)) {
				System.out.println("Added new edge " + pendingEdge.toString());
				this.edges.add(new Edge(vertices.get(pendingEdge.first),
						vertices.get(pendingEdge.second)));
			} else {
				this.pendingEdges.add(pendingEdge);
			}
		}
		
		for (Vertex vertex : this.vertices.values()) {
			if (!vertex.isReady) {
				vertex.create(findEmptyLocation());
				System.out.println("Vertex: " + vertex.toString());
			} else if (!newOrigin.equals(oldOrigin)) {
				Vector3 offset = new Vector3(oldOrigin).sub(newOrigin);
				vertex.position.add(offset);
				vertex.model.nodes.get(0).translation.add(offset);
			}
		}
		
		System.out.println("Size of edges " + edges.size());
		for (Edge edge : this.edges) {
			if (!edge.isReady) {
				edge.create();
			} else if (!newOrigin.equals(oldOrigin)) {
				edge.create();
			}
		}
	}
	
	public void render(ModelBatch batch) {
		for (Vertex vertex : this.vertices.values()) {
			if (vertex.photo != null) {
				vertex.photo.bind();
			}
			ModelInstance instance = new ModelInstance(vertex.model);
			batch.render(instance);
		}
		
		for (Edge edge : this.edges) {
			batch.render(new ModelInstance(edge.model));
		}
	}
	
	class ConstellationHttpResponseHandler implements HttpResponseListener {
		public ConstellationHttpResponseHandler(ConstellationRequest req) {
			this.head = req;
		}
		
		@Override
		public void handleHttpResponse(HttpResponse httpResponse) {
			String resultAsString = httpResponse.getResultAsString();
			System.out.println("Response: " + resultAsString);
			List<String> response = new ArrayList<String>(Arrays.asList(
						resultAsString.split("\\s*,\\s*")));
			if (head.type.equals(ConstellationRequest.RequestType.GET_FRIENDS)) {
				// If we are querying for friends
				String myID = response.get(0);
				System.out.println("myId: " + myID);
				//response.remove(0);

                for (int i=2;i<response.size();i+=2){
                    String friendID=response.get(i)
                    double weight=Double.parseDouble(response.get(i+1))
					networkRequests.add(new ConstellationRequest(
						ConstellationRequest.RequestType.GET_FRIEND_INFO, 
						friendID));

					pendingEdges.add(new IDPair(myID, friendID, weight));

					System.out.println("friendID: " + friendID);					
				}
			} else {
				// If we want details about a certain friend
				assert(response.size() == 2);
				String name = response.get(0);
				String photoURL = response.get(1);
				if (photoURL.equals("none")) {
					pendingVertices.add(new Vertex(name, new Texture("badlogic.jpg"), head.data));
				} else {
					pendingVertices.add(new Vertex(name, new WebTexture(photoURL, tmp), head.data));
				}
				
				networkRequests.add(new ConstellationRequest(
						ConstellationRequest.RequestType.GET_FRIENDS, 
						head.data));
				
				System.out.println("Photo url: " + photoURL);
				
				updateMe = true;
			}
		}

		@Override
		public void failed(Throwable t) {
			t.printStackTrace();
		}

		@Override
		public void cancelled() {
			System.out.println("Request cancelled!");
		}

		private ConstellationRequest head;
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
			for (Vertex vertex : vertices.values()) {
				if (vertex.position != null && 
						vertex.position.dst(testVertex) < TOO_CLOSE) {
					terminate = false;
				}
			}
		}
		return testVertex;
	}
	
	public void pick(Ray ray) {
		Vertex result = null;
        float distance = Float.MAX_VALUE;
        Set<Entry<String, Vertex>> entrySet = vertices.entrySet();
        for (Entry<String, Vertex> entry : entrySet) {
            final Vertex instance = entry.getValue();
            
            Vector3 intersection = new Vector3();
            if (Intersector.intersectRayPlane(ray, 
            		new Plane(new Vector3(1, 0, 0), instance.position), 
            		intersection)) {
            	float dist1 = ray.origin.dst(intersection);
            	float dist2 = intersection.dst(instance.position);
            	
            	if (dist2 < 1 && dist1 < distance) {
            		result = instance;
            		distance = dist1;
            	}
            }
        }
        
        if (result != null) {
        	System.out.println("Adding a network request for person " + result.ID);
        	networkRequests.add(new ConstellationRequest(
        		ConstellationRequest.RequestType.GET_FRIENDS,
        		result.ID));
        }
	}
	
	class IDPair {
		public final String first;
		public final String second;
		public final weight
		public IDPair(String first, String second,double weight) {
			this.first = first;
			this.second = second;
			this.weight = weight;
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof IDPair)) {
				return false;
			}
			IDPair castOther = (IDPair) other;
			return (this.first.equals(castOther.first) && this.second.equals(castOther.second)) ||
					(this.first.equals(castOther.second) && this.second.equals(castOther.first));
		}
		public double getWeight(){
		    return weight;
		}
		
		@Override
		public int hashCode() {
			return first.hashCode() * second.hashCode(); 
		}
		
		@Override
		public String toString() {
			return first + ", " + second;
		}
	}
}
