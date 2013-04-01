package main;

import java.awt.image.BufferedImage;

public abstract class Button {
	
	private int x, y;
	private int width, height;
	private BufferedImage img;
	private Sprite sprite;
	private Main m;
	private Button previous;
	private Button next;
	
	public Button(Main m, int x, int y, int width, int height, BufferedImage img){
		this.m = m;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.img = img;
	}

	public abstract void run();
	
	public void drawLogic(){
		if(getSprite() == null) {
			setSprite(new Sprite(m,x,y,img,this,false));
			m.getGame().setNewSprite(getSprite());
		}
		else{
			if( !getSprite().getImage().equals(getImg()) ){
				m.getGame().destroySprite(getSprite());
				setSprite(new Sprite(m,x,y,img,this,false));
				m.getGame().setNewSprite(getSprite());
			}
		}
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
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

	
	public void click(int x, int y){
		int offsetX = m.getWidth()-m.getGame().getPanelWidth();
		int offsetY = m.getGame().getPanelWidth()+m.getGame().getMarginMinimap()*3;
		if( x>=getX()+offsetX && x<=getX()+getWidth()+offsetX &&
			y>=getY()+offsetY && y<=getY()+getHeight()+offsetY ){
			run();
		}
	}

	public BufferedImage getImg() {
		return img;
	}

	public void setImg(BufferedImage img) {
		this.img = img;
	}

	public Sprite getSprite() {
		return sprite;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	public Button getPrevious() {
		return previous;
	}

	public void setPrevious(Button previous) {
		this.previous = previous;
	}

	public Button getNext() {
		return next;
	}

	public void setNext(Button next) {
		this.next = next;
	}
}
