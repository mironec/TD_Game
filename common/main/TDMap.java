package main;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;

public class TDMap {
	private String map = "Map.map";
	
	private int width;
	private int height;
	private byte[] data;
	private BufferedImage image;
	
	public static final int METHOD_LOAD_FILE=0;
	
	private Main m;
	
	public TDMap (Main m) {
		this.m = m;
	}
	
	public boolean loadMap(){
		return loadMap(map,0);
	}
	
	public boolean loadMap(String mapname, int method){
		map = mapname;
		if(method==METHOD_LOAD_FILE){
			try {
				InputStream is = Main.class.getResourceAsStream(Game.RES_DIR + "maps/" + mapname);
				
				byte[] ret = new byte[TDMap.class.getResource(Game.RES_DIR + "maps/" + mapname).openConnection().getContentLength()];
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				
				byte data[] = new byte[1024];
				int count;
				
				while ((count = is.read(data, 0, 1024)) != -1) {
					out.write(data, 0, count);
				}
				
				ret = out.toByteArray();
				
			    is.close();
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
			InputStream is = Main.class.getResourceAsStream(Game.RES_DIR + "mapImages/" + map.substring(0,map.lastIndexOf('.')) + ".png");
			if(is!=null){
				BufferedImage img = ImageIO.read(is);
				image = new BufferedImage(getWidth()*m.getGame().getTileWidth(),getHeight()*m.getGame().getTileWidth(),BufferedImage.TYPE_INT_ARGB);
				image.getGraphics().drawImage(img, 0, 0, 
						getWidth()*m.getGame().getTileWidth(), getHeight()*m.getGame().getTileWidth(),
						0, 0, img.getWidth(), img.getHeight(), m);
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

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}
}
