package main;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Animation {
	
	private Main m;
	private int x;
	private int y;
	private Object owner;
	private BufferedImage image;
	private Animation previous;
	private Animation next;
	private boolean repeat;
	private int time;			//in ms
	private int duration;		//in ms
	
	private int maxPhases;
	private int imageWidth;
	
	public Animation (Main m, int x, int y, BufferedImage image, Object owner, boolean repeat, int duration) {
		this.m = m;
		this.x = x;
		this.y = y;
		this.owner = owner;
		setImage(image);
		this.time = 0;
		this.duration = duration;
		this.repeat = repeat;
	}
	
	public static BufferedImage getImagePhase(BufferedImage img, int phase, Main m){
		int width = img.getWidth();
		BufferedImage returnImage = new BufferedImage(width,width,BufferedImage.TYPE_INT_ARGB);
		returnImage.getGraphics().drawImage(img,0,0,width,width,0,width*phase,width,width*(phase+1),m);
		returnImage.getGraphics().dispose();
		
		return returnImage;
	}
	
	public static int getImagePhases(BufferedImage img){
		return (int)Math.ceil((double)img.getHeight()/(double)img.getWidth());
	}
	
	public void draw (Graphics g, int offsetX, int offsetY, int delta) {
		if( !isRepeat() && (time + delta >= duration) ){m.getGame().destroyAnimation(this); return;}
		time = (time + delta) % duration;
		int phase = (int) Math.floor((double)time / (double)duration * (double)maxPhases);
		
		BufferedImage img = new BufferedImage( imageWidth, imageWidth, BufferedImage.TYPE_INT_ARGB );
		img.getGraphics().drawImage(getImage(), 0, 0, imageWidth, imageWidth, 0, imageWidth*phase, imageWidth, imageWidth*(phase+1), m);
		
		g.drawImage(img, getX()-offsetX, getY()-offsetY, m);
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
		if(image!=null){
			maxPhases = (int) Math.ceil( image.getHeight() / image.getWidth() );
			imageWidth = image.getWidth();
		}
		this.image = image;
	}

	public Animation getPrevious() {
		return previous;
	}

	public void setPrevious(Animation previous) {
		this.previous = previous;
	}

	public Animation getNext() {
		return next;
	}

	public void setNext(Animation next) {
		this.next = next;
	}

	public Object getOwner() {
		return owner;
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}

	public boolean isRepeat() {
		return repeat;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

}
