package graphics;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import main.Main;

public class Sprite {
	private Main m;
	private int x;
	private int y;
	private Object owner;
	private BufferedImage image;
	private Sprite previous;
	private Sprite next;
	private boolean persistent;
	
	public Sprite (Main m, int x, int y, BufferedImage image, Object owner, boolean persistent) {
		this.m = m;
		this.x = x;
		this.y = y;
		this.image = image;
		this.owner = owner;
		this.setPersistent(persistent);
	}
	
	public void draw (Graphics g, int offsetX, int offsetY) {
		g.drawImage(getImage(), getX()-offsetX, getY()-offsetY, m);
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

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public Sprite getPrevious() {
		return previous;
	}

	public void setPrevious(Sprite previous) {
		this.previous = previous;
	}

	public Sprite getNext() {
		return next;
	}

	public void setNext(Sprite next) {
		this.next = next;
	}

	public Object getOwner() {
		return owner;
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}
}
