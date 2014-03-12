package main;

import graphics.Sprite;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public abstract class Button {
	
	private int x, y;
	private int width, height;
	private BufferedImage img;
	private Sprite sprite;
	private Main m;
	private String des = "";
	private Object owner;
	private int keybind = KeyEvent.VK_DEAD_MACRON;
	
	public Button(Main m, int x, int y, int width, int height, BufferedImage img, Object owner){
		this.m = m;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.img = img;
		this.owner = owner;
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
	
	public void destroy(){
		m.getGame().destroyButton(this);
		if(getSprite()!=null)
			m.getGame().destroySprite(getSprite());
		setSprite(null);
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

	public boolean isHere(int x, int y){
		//int offsetX = m.getWidth()-m.getGame().getPanelWidth();
		//int offsetY = m.getGame().getPanelWidth()+m.getGame().getMarginMinimap()*3;
		if( x>=getX() && x<=getX()+getWidth() &&
			y>=getY() && y<=getY()+getHeight() ){
			return true;
		}
		return false;
	}
	
	public void key(int key){
		if( key == getKeybind() ){
			run();
		}
	}
	
	public void click(int x, int y){
		if(isHere(x,y)){
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

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

	public Object getOwner() {
		return owner;
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}

	public int getKeybind() {
		return keybind;
	}

	public void setKeybind(int keybind) {
		this.keybind = keybind;
	}
}
