package main;

import java.awt.Image;

public class NPCType {

	private Main m;
	private Image image;
	
	public NPCType (Main m){
		this.m = m;
		setImage(this.m.createImage(this.m.getGame().getTileWidth(),this.m.getGame().getTileWidth()));
	}
	
	public Image getImage(){
		return image;
	}
	
	public void setImage(Image image){
		this.image = image;
	}
}