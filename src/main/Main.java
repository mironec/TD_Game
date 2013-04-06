package main;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

public class Main extends Applet implements Runnable, KeyListener, MouseListener, MouseMotionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 15L;
	
	public static final int RENDER_MODE_MENU = 0;
	public static final int RENDER_MODE_GAME = 1;
	
	public int width, height = 1;
	BufferedImage backbuffer;
	Graphics backbufferG;
	Thread mainThread, fpsThread;
	int fps, frames = 0;
	boolean atWork = false;
	int renderMode = RENDER_MODE_MENU;
	int cas=0;
	boolean keyDown[];
	boolean mouseDown[];
	Point mousePoint = new Point(-1,-1);
	Point mouseStart;
	Point mouseEnd;
	double keySensitivity = 0.25D;
	int elapsedTime = 0;
	int elapsedTime2 = 0;
	int interiorDelta = 0;
	
	private Game game;

	private Event lastEvent;
	
	int delta;
	long lastDelta;
	
	long loops = 0;
	
	public void init () {
		width = this.getWidth();
		height = this.getHeight();
		
		backbuffer = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		backbufferG = backbuffer.getGraphics();
		mainThread = new Thread(this);
		fpsThread = new Thread(this);
		keyDown = new boolean [256];
		mouseDown = new boolean [256];
		
		this.addKeyListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		lastDelta = System.nanoTime()/1000000;
		delta = 0;
		
		game = new Game(this);
		game.init();
		
		mainThread.start();
		fpsThread.start();
		
		boolean dobre = game.loadMap("mapa.txt",Map.METHOD_LOAD_FILE);
		game.readMap();
		if(!dobre){
			System.out.println("PROBLEM!!!");
			System.exit(1);
		}
		renderMode=RENDER_MODE_GAME;
		
	}
	
	public void update( Graphics g ) {
      g.drawImage( backbuffer, 0, 0, this );
      atWork=false;
   }

   public void paint( Graphics g ) {
      update( g );
   }

	public void paintbb(int delta){
		if(renderMode==RENDER_MODE_GAME){
			frames++;
			
			game.render(delta);
			
			backbufferG.setColor(Color.white);
			backbufferG.drawString("FPS: " + fps, 10, 10);
		}
		if(renderMode==RENDER_MODE_MENU){
			frames++;
			backbufferG.setColor(Color.white);
			backbufferG.fillRect(0, 0, width, height);
			
			backbufferG.setColor(Color.black);
			backbufferG.drawString("FPS: " + fps, 10, 10);
		}
		
		repaint();
	}

	@SuppressWarnings("static-access")
	public void run() {
		while(true){
			if(Thread.currentThread()==mainThread){
				
				//if(!atWork){
					//atWork=true;
					int delta = (int) (System.nanoTime()/1000000 - lastDelta);
					lastDelta = System.nanoTime()/1000000;
					
					if(delta>0){
						logic(delta);
						handleInput(delta);
						events(delta);
						paintbb(delta);
					}
					
				//}
			}
			if(Thread.currentThread()==fpsThread){
				fps=frames;
				frames=0;
				cas++;
				try {fpsThread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
			}
		}
	}
	
	
	public void logic(int delta){
		if(renderMode==RENDER_MODE_GAME){
			game.logic(delta);
		}
	}
	
	public void events(int delta){
		for(Event e=getLastEvent();e!=null;e=e.getPrevious()){
			e.logic(delta);
		}
	}
	
	public void handleInput (int delta) {
		if(renderMode==RENDER_MODE_GAME){
			game.handleInput(delta);
		}
	}

	public void keyPressed(KeyEvent e) {
		keyDown[e.getKeyCode()]=true;
	}

	public void keyReleased(KeyEvent e) {
		keyDown[e.getKeyCode()]=false;
	}

	public void keyTyped(KeyEvent e) {}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {
		mouseStart = new Point(e.getPoint());
		mouseDown[e.getButton()] = true;
	}

	public void mouseReleased(MouseEvent e) {
		mouseEnd = new Point(e.getPoint());
		mouseDown[e.getButton()] = false;
	}

	public void mouseDragged(MouseEvent e) {
		mousePoint = e.getPoint();
	}

	public void mouseMoved(MouseEvent e) {
		mousePoint = e.getPoint();
	}
	
	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Event getLastEvent() {
		return lastEvent;
	}

	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
	}
	
	public void setNewEvent(Event e){
		if(getLastEvent()==null){
			setLastEvent( e );
		}
		else{
			getLastEvent().setNext(e);
			e.setPrevious(getLastEvent());
			setLastEvent(e);
		}
	}
	
	public void destroyEvent(Event e){
		if(getLastEvent().equals(e)){
			setLastEvent(e.getPrevious());
		}
		if(e.getPrevious()!=null){e.getPrevious().setNext(e.getNext());}
		if(e.getNext()!=null){e.getNext().setPrevious(e.getPrevious());}
	}
}
