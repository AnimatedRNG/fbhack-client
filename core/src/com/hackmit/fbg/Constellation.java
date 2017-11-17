package com.hackmit.fbg;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

public class Constellation {
	public static final float TOO_CLOSE = 1.5f;
	
	public Map<String, Vertex> vertices;
	public List<Edge> edges;
	public Queue<IDPair> pendingEdges;
	public Queue<Vertex> pendingVertices;
	
	public Vector3 roomOrigin = new Vector3(0, 0, 0);
	public Vector3 roomSize = new Vector3(5, 5, 5);
	
	public Queue<ConstellationRequest> networkRequests;
	public Texture badlogic;
	public Texture tmp;
	
	public boolean updateMe = false;
	
	public Constellation() {
		this.vertices = new HashMap<String, Vertex>();
		this.edges = new ArrayList<Edge>();
		this.pendingEdges = new ConcurrentLinkedQueue<IDPair>();
		this.pendingVertices = new ConcurrentLinkedQueue<Vertex>();
		
		this.networkRequests = new ConcurrentLinkedQueue<ConstellationRequest>();
		
		vertices.put("1", new Vertex("test", new Texture("badlogic.jpg"), "1"));
		vertices.put("4", new Vertex("test2", new Texture("badlogic.jpg"), "4"));
		edges.add(new Edge(vertices.get("1"), vertices.get("4")));
		
		badlogic = new Texture("badlogic.jpg");
		tmp = new Texture("badlogic.jpg");
		
		this.networkRequests.add(new ConstellationRequest(
				ConstellationRequest.RequestType.GET_FRIENDS,
				encodeURL("Matthew Pfeiffer")));
		
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
			if (head != null) {
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
				
				Gdx.net.sendHttpRequest(httpGet, new ConstellationHttpResponseHandler(head));
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
			}
		}
		
		System.out.println("Size of edges " + edges.size());
		for (Edge edge : this.edges) {
			if (!edge.isReady) {
				edge.create();
			}
		}
	}
	
	public void render(ModelBatch batch) {
		for (Vertex vertex : this.vertices.values()) {
			if (vertex.photo != null) {
				vertex.photo.bind();
			}
			batch.render(new ModelInstance(vertex.model));
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
				response.remove(0);

				for (String friendID : response) {
					networkRequests.add(new ConstellationRequest(
							ConstellationRequest.RequestType.GET_FRIEND_INFO, 
							friendID));
					pendingEdges.add(new IDPair(myID, friendID));

					System.out.println("friendID: " + friendID);					
				}
			} else {
				// If we want details about a certain friend
				assert(response.size() == 2);
				String name = response.get(0);
				String photoURL = response.get(1);
				if (photoURL.equals("none")) {
					pendingVertices.add(new Vertex(name, badlogic, head.data));
				} else {
					pendingVertices.add(new Vertex(name, new WebTexture(photoURL, tmp), head.data));
				}
				
				
				
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
	
	class IDPair {
		public final String first;
		public final String second;
		
		public IDPair(String first, String second) {
			this.first = first;
			this.second = second;
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
