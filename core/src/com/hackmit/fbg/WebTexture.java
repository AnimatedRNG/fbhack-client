package com.hackmit.fbg;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.utils.StreamUtils;

// https://stackoverflow.com/questions/21374752/libgdx-getting-image-from-facebook-at-runtime
public class WebTexture {
    private final String url;
    private Texture texture;
    private volatile byte[] textureBytes;
    
    private volatile byte[] tmp;

    public WebTexture(String url, Texture tempTexture) {
    	tmp = new byte[1000000];
    	
        this.url = url;
        texture = tempTexture;
        downloadTextureAsync();
    }

    private void downloadTextureAsync() {
    	textureBytes = downloadTextureBytes();
    }

    private int download(byte[] out, String url) {
    	InputStream in = null;
    	try {
    		HttpURLConnection conn = null;
    		conn = (HttpURLConnection)new URL(url).openConnection();
    		conn.setDoInput(true);
    		conn.setDoOutput(false);
    		conn.setUseCaches(true);
    		conn.connect();
    		in = conn.getInputStream();
    		int readBytes = 0;
    		while (true) {
    			int length = in.read(out, readBytes, out.length - readBytes);
    			if (length == -1) break;
    			readBytes += length;
    		}
    		return readBytes;
    	} catch (Exception ex) {
    		return 0;
    	} finally {
    		StreamUtils.closeQuietly(in);
    	}
    }

    private byte[] downloadTextureBytes() {
    	if (download(tmp, url) > 0) {
    		return tmp;
    	} else {
    		System.err.println("Um, downloadTextureBytes() failed");
    		return null;
    	}
    }

    public Texture getTexture() {
        if (textureBytes != null)
            processTextureBytes();
        return texture;
    }

    private void processTextureBytes() {
        try {
            Pixmap pixmap = new Pixmap(textureBytes, 0, textureBytes.length);
            Texture gdxTexture = new Texture(pixmap);
            gdxTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            texture = gdxTexture;
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            textureBytes = null;
        }
    }
}