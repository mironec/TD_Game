package main;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import tower.Tower;

/*
 * © Copyright Miron Zelina 2013
 */

public class Main extends Applet implements Runnable, KeyListener, MouseListener, MouseMotionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 22L;
	
	public static final int RENDER_MODE_MENU = 0;
	public static final int RENDER_MODE_GAME = 1;
	public static final int RENDER_MODE_SCORESCREEN = 2;
	
	public int width, height = 1;
	BufferedImage backbuffer;
	Graphics backbufferG;
	BufferedImage backbuffer2;
	Graphics backbuffer2G;
	Thread mainThread;
	int fps, frames = 0;
	boolean atWork = false;
	private boolean spedUp = false;
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
	TextField nameTF;
	
	public static Main instance;
	
	private Game game;

	private ArrayList<Event> events = new ArrayList<Event>();
	
    final double GAME_HERTZ = 30.0;
    final double TIME_BETWEEN_UPDATES = 1000000000 / GAME_HERTZ;
    double lastUpdateTime = System.nanoTime();
    
    final double TARGET_FPS = 100;
    final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;
    double lastRenderTime = System.nanoTime();

    int lastSecondTime = (int) (lastUpdateTime / 1000000000);
	
	private boolean gameRunning = true;
	private boolean paused = false;
	
	boolean logicPaused = false;
	boolean renderPaused = false;
	
	public void init () {
		instance = this;
		
		width = this.getWidth();
		height = this.getHeight();
		
		backbuffer = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		backbufferG = backbuffer.getGraphics();
		backbuffer2 = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		backbuffer2G = backbuffer2.getGraphics();
		mainThread = new Thread(this);
		keyDown = new boolean [1024];
		mouseDown = new boolean [256];
		
		this.addKeyListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		game = new Game(this);
		game.init();
		
		mainThread.start();
		
		renderMode = RENDER_MODE_GAME;
		lastRenderTime = lastUpdateTime = System.nanoTime();
	}
	
	public void update( Graphics g ) {
      g.drawImage( backbuffer2, 0, 0, this );
      atWork=false;
   }

   public void paint( Graphics g ) {
      update( g );
   }

	public void paintbb(int delta){
		if(!renderPaused){
			if(renderMode==RENDER_MODE_GAME){			
				game.render(delta);
				
				backbufferG.setColor(Color.white);
				backbufferG.drawString("FPS: " + fps, 10, 10);
			}
			if(renderMode==RENDER_MODE_MENU){
				backbufferG.setColor(Color.white);
				backbufferG.fillRect(0, 0, width, height);
				
				backbufferG.setColor(Color.black);
				backbufferG.drawString("FPS: " + fps, 10, 10);
			}
			if(renderMode==RENDER_MODE_SCORESCREEN){
				backbufferG.setColor(Color.orange);
				backbufferG.fillRect(0, 0, width, height);
				
				backbufferG.setColor(Color.black);
				backbufferG.setFont(new Font("ariel", Font.PLAIN, 25));
				String s = "";
				if(getGame().getFinalsKilled()>0){
					s = "Congratulations,";
					backbufferG.drawString(s, width/2-backbufferG.getFontMetrics().stringWidth(s)/2, 35);
					s = "you won the game!";
					backbufferG.drawString(s, width/2-backbufferG.getFontMetrics().stringWidth(s)/2, 70);
					s = "Final score: " + getGame().getFinalsKilled();
					backbufferG.drawString(s, width/2-backbufferG.getFontMetrics().stringWidth(s)/2, 105);
					if(!nameTF.getText().equals("")){backbufferG.setColor(Color.blue);}
					s = "Submit";
					backbufferG.drawString(s, width/2-backbufferG.getFontMetrics().stringWidth(s)/2, 210);
					if(!nameTF.getText().equals("")){backbufferG.drawLine(width/2-backbufferG.getFontMetrics().stringWidth(s)/2, 211, width/2+backbufferG.getFontMetrics().stringWidth(s)/2, 211);}
				}
				else if(getGame().getFinalsKilled()==-1){
					s = "Thank you for playing our game!";
					backbufferG.drawString(s, width/2-backbufferG.getFontMetrics().stringWidth(s)/2, 35);
					backbufferG.setColor(Color.blue);
					s = "Play again";
					backbufferG.drawString(s, width/2-backbufferG.getFontMetrics().stringWidth(s)/2, 105);
					backbufferG.drawLine(width/2-backbufferG.getFontMetrics().stringWidth(s)/2, 106, width/2+backbufferG.getFontMetrics().stringWidth(s)/2, 106);
				}
				else{
					s = "You lost!";
					backbufferG.drawString(s, width/2-backbufferG.getFontMetrics().stringWidth(s)/2, 35);
					s = "You were defeated on wave " + getGame().getWaveId();
					backbufferG.drawString(s, width/2-backbufferG.getFontMetrics().stringWidth(s)/2, 70);
					backbufferG.setColor(Color.blue);
					s = "Play again";
					backbufferG.drawString(s, width/2-backbufferG.getFontMetrics().stringWidth(s)/2, 105);
					backbufferG.drawLine(width/2-backbufferG.getFontMetrics().stringWidth(s)/2, 106, width/2+backbufferG.getFontMetrics().stringWidth(s)/2, 106);
				}
			}
		}
		frames++;
		backbuffer2G.drawImage(backbuffer, 0, 0, this);
		repaint();
	}

	@SuppressWarnings("static-access")
	public void run() {
		while (gameRunning) {
			double now = System.nanoTime();
		    
		    if (!isPaused()){
		    	double deltaUpdate = 0;
		    	double deltaRender = 0;
		    	
		    	if(!spedUp){
		    		deltaUpdate = (now - lastUpdateTime)/1000/1000;
		    		deltaRender = (now - lastRenderTime)/1000/1000;
		    	}
		    	else{
		    		deltaUpdate = 4*(now - lastUpdateTime)/1000/1000;
		    		deltaRender = 4*(now - lastRenderTime)/1000/1000;
		    	}
		    	
	    		logic((int) (deltaUpdate));
	    		events((int) (deltaUpdate));
				handleInput((int) (deltaUpdate));
				lastUpdateTime = now;

		        paintbb( (int) (deltaRender) );
		        lastRenderTime = now;
		         
	            int thisSecond = (int) (lastUpdateTime / 1000/1000/1000);
	            if (thisSecond > lastSecondTime)
	            {
	            	fps = frames;
	                frames = 0;
	                lastSecondTime = thisSecond;
	            }
	            
	            while ( now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < TIME_BETWEEN_UPDATES)
	            {
	               mainThread.yield();
	            
	               try {mainThread.sleep(1);} catch(Exception e) {} 
	            
	               now = System.nanoTime();
	            }
		    }
		}
	}
	
	
	public void logic(int delta){
		if(renderMode==RENDER_MODE_GAME && !logicPaused){
			game.logic(delta);
		}
	}
	
	public void events(int delta){
		if(!logicPaused){
			for(Event e : getEvents()){
				e.logic(delta);
			}
		}
	}
	
	public void handleInput (int delta) {
		if(renderMode==RENDER_MODE_GAME){
			game.handleInput(delta);
		}
		if(renderMode==RENDER_MODE_SCORESCREEN){
			if(mouseDown[MouseEvent.BUTTON1] && getGame().getFinalsKilled()>0 && !nameTF.getText().equals("")){
				String s = "Submit";
				if(mousePoint.x>=width/2-backbufferG.getFontMetrics().stringWidth(s)/2&&
				   mousePoint.x<=width/2+backbufferG.getFontMetrics().stringWidth(s)/2&&
				   mousePoint.y<=210&&
				   mousePoint.y>=195){
					try {
						String build = "";
						for(Tower t : getGame().getTowers()){
							build += "("+t.getTowerType().getId()+")";
						}
						URL url = new URL(getCodeBase(), "SubmitTehScorez.php?score="+getGame().getFinalsKilled()+"&name="+nameTF.getText()+"&build="+build);
						url.openStream().read();
					} catch (IOException e) {e.printStackTrace();}
					getGame().setFinalsKilled(-1);
					this.remove(nameTF);
				}
			}
			if(mouseDown[MouseEvent.BUTTON1] && getGame().getFinalsKilled()<=0){
				String s = "Play again";
				if(mousePoint.x>=width/2-backbufferG.getFontMetrics().stringWidth(s)/2&&
				   mousePoint.x<=width/2+backbufferG.getFontMetrics().stringWidth(s)/2&&
				   mousePoint.y<=105&&
				   mousePoint.y>=80){
					getGame().init();
					renderMode=RENDER_MODE_GAME;
					lastRenderTime = lastUpdateTime = System.nanoTime();
				}
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		if(renderMode==RENDER_MODE_GAME){
			game.keyPressed(e);
		}
		keyDown[e.getKeyCode()]=true;
	}

	public void keyReleased(KeyEvent e) {
		if(renderMode==RENDER_MODE_GAME){
			game.keyReleased(e);
		}
		keyDown[e.getKeyCode()]=false;
	}

	public void keyTyped(KeyEvent e) {
		if(renderMode==RENDER_MODE_GAME){
			game.keyTyped(e);
		}
		if(e.getKeyChar()=='p'){
			double now = System.nanoTime();
			lastUpdateTime = now;
			lastRenderTime = now;
			paused =! paused;
		}
		
		if(e.getKeyChar()=='e'){
			spedUp = !spedUp;
		}
		
		/*if(e.getKeyChar()=='l'){
			logicPaused = !logicPaused;
		}
		
		if(e.getKeyChar()=='g'){
			renderPaused = !renderPaused;
		}*/
	}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {
		if(renderMode==RENDER_MODE_GAME){
			getGame().mousePressed(e);
		}
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
	
	public void scoreScreen(){
		renderMode = RENDER_MODE_SCORESCREEN;
		if(getGame().getFinalsKilled()>0){
			nameTF = new TextField("");
			nameTF.setBounds(width/2-50,150,100,25);
			this.add(nameTF);
		}
		//nameTF.addKeyListener(this);
		//nameTF.addMouseListener(this);
	}
	
	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}
	
	public void setNewEvent(Event e){
		events.add(e);
	}
	
	public void destroyEvent(Event e){
		events.remove(e);
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public ArrayList<Event> getEvents() {
		return new ArrayList<Event>(events);
	}

	public void setEvents(ArrayList<Event> events) {
		this.events = events;
	}

}
