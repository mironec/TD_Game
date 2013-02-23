package main;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

public class Map {
	private String map = "Map.map";
	
	private int width;
	private int height;
	private byte[] data;
	private Image image;
	
	public static final int METHOD_LOAD_FILE=0;
	
	private Main m;
	
	public Map (Main m) {
		this.m = m;
	}
	
	public boolean loadMap(){
		return loadMap(map,0);
	}
	
	public boolean loadMap(String mapname, int method){
		map = mapname;
		if(method==METHOD_LOAD_FILE){
			try {
				URL url = new URL(m.getCodeBase().toString() + "maps/" + mapname);
				
				URLConnection conn = url.openConnection();
				byte[] ret = new byte[conn.getContentLength()];
				BufferedInputStream in = new BufferedInputStream(url.openStream());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				
				byte data[] = new byte[1024];
				int count;
				
				while ((count = in.read(data, 0, 1024)) != -1) {
					out.write(data, 0, count);
				}
				
				ret = out.toByteArray();
				
			    in.close();
			    out.close();
			    
			    readMap(ret);
			    loadImage();
				
			    return true;
			}
			catch (MalformedURLException e) {e.printStackTrace();} 
			catch (IOException e) {e.printStackTrace();}
		}
		
		return false;
	}
	
	public boolean readMap(byte map[]){
		boolean done = true;
		
	    try{
	    	data=map;
		    setWidth(data[0]*128+data[1]);
			setHeight(data[2]*128+data[3]);
	    } catch (Exception e){done = false;}
		
	    return done;
	}
	
	public void loadImage(){
		try {
			URL url = new URL(m.getCodeBase().toString() + "mapImages/" + map.substring(0,map.lastIndexOf('.')) + ".png");
			if(url.openConnection().getContentLength()>0){
				image = ImageIO.read(url).getScaledInstance(getWidth()*m.getGame().getTileWidth(), getHeight()*m.getGame().getTileWidth(), Image.SCALE_DEFAULT);
			}
			else{image=null;}
		} catch (MalformedURLException e) {e.printStackTrace();}
		  catch (IOException e) {e.printStackTrace();}
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	public byte[] getData(){
		return data;
	}
	
	public void setData(byte[] data){
		this.data = data;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}
}
