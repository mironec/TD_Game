package main;

import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;

public class NPC {

	private double x;
	private double y;
	private int tileX;
	private int tileY;
	private int wait; //in ms
	private Image image;
	private Main m;
	private NPCType npcType;
	private int command;
	public static final int COMMAND_NOTHING=0;
	public static final int COMMAND_MOVE=1;
	private int targetX;
	private int targetY;
	private ArrayList<Point> path;
	private int pathIndex=-1;
	private int health;
	private double movementSpeed; //tiles per second
	private NPC previous;
	private NPC next;
	
	public NPC (Main m, double x, double y, NPCType npcType){
		setX(x);
		setY(y);
		this.m = m;
		this.npcType = npcType;
		
		//DEFAULT
		
		setWait(0);
		setImage(null);
		setCommand(COMMAND_NOTHING);
		setTargetX(getTileX());
		setTargetY(getTileY());
		setHealth(1);
		setMovementSpeed(1);
	}

	public Image getImage() {
		if(image==null)
			return getNpcType().getImage();
		else
			return image;
	}
	
	public void damage(double damage){
		health-=damage;
		if(health<=0){
			m.getGame().destroyNPC(this);
		}
	}
	
	public ArrayList<Point> findPath(int x, int y, ArrayList<Point> beenTo, ArrayList<Point> path, int tx, int ty) {
		int paths=0;
		boolean left,right,up,down;
		left = right = down = up = false;
		
		beenTo.add(new Point(x,y));
		path.add(new Point(x,y));
		
		if(x==tx&&y==ty){
			return path;
		}
		
		if(!beenTo.contains(new Point(x-1,y))&&Game.isPassable(m.getGame().getTile(x-1, y))){
			paths++; left = true;
		}
		if(!beenTo.contains(new Point(x+1,y))&&Game.isPassable(m.getGame().getTile(x+1, y))){
			paths++; right = true;
		}
		if(!beenTo.contains(new Point(x,y-1))&&Game.isPassable(m.getGame().getTile(x, y-1))){
			paths++; up = true;
		}
		if(!beenTo.contains(new Point(x,y+1))&&Game.isPassable(m.getGame().getTile(x, y+1))){
			paths++; down = true;
		}
		
		if(paths<=0){
			return new ArrayList<Point>();
		}
		if(paths==1){
			if(up)
				return findPath(x,y-1,new ArrayList<Point>(beenTo),new ArrayList<Point>(path),tx,ty);
			if(down)
				return findPath(x,y+1,new ArrayList<Point>(beenTo),new ArrayList<Point>(path),tx,ty);
			if(left)
				return findPath(x-1,y,new ArrayList<Point>(beenTo),new ArrayList<Point>(path),tx,ty);
			if(right)
				return findPath(x+1,y,new ArrayList<Point>(beenTo),new ArrayList<Point>(path),tx,ty);
		}
		if(paths>1){
			ArrayList<Point> path1 = new ArrayList<Point>();
			ArrayList<Point> path2 = new ArrayList<Point>();
			ArrayList<Point> path3 = new ArrayList<Point>();
			ArrayList<Point> path4 = new ArrayList<Point>();
			if(up)
				path1=findPath(x,y-1,new ArrayList<Point>(beenTo),new ArrayList<Point>(path),tx,ty);
			if(down)
				path2=findPath(x,y+1,new ArrayList<Point>(beenTo),new ArrayList<Point>(path),tx,ty);
			if(left)
				path3=findPath(x-1,y,new ArrayList<Point>(beenTo),new ArrayList<Point>(path),tx,ty);
			if(right)
				path4=findPath(x+1,y,new ArrayList<Point>(beenTo),new ArrayList<Point>(path),tx,ty);
			int min = Math.min(path4.toArray().length==0?Integer.MAX_VALUE:path4.toArray().length,
					  Math.min(path3.toArray().length==0?Integer.MAX_VALUE:path3.toArray().length,
					  Math.min(path2.toArray().length==0?Integer.MAX_VALUE:path2.toArray().length,
							   path1.toArray().length==0?Integer.MAX_VALUE:path1.toArray().length)));
			if(min==path1.toArray().length){
				return path1;
			}
			if(min==path2.toArray().length){
				return path2;
			}
			if(min==path3.toArray().length){
				return path3;
			}
			if(min==path4.toArray().length){
				return path4;
			}
		}
		return new ArrayList<Point>();
	}
	
	public void goAlongPath(ArrayList<Point> path){
		
	}
	
	public void goToTarget(int delta) {
		if(path==null||path.isEmpty()){
			path = findPath(tileX, tileY, new ArrayList<Point>(),new ArrayList<Point>(), targetX, targetY);
			pathIndex=0;
		}
		else{
			for(int loop=0;loop<delta;loop++){
				if(wait==0){
					if(tileX==targetX&&tileY==targetY){
						command = COMMAND_NOTHING;
						return;
					}
					
					x=tileX*m.getGame().getTileWidth();
					y=tileY*m.getGame().getTileWidth();
					
					tileX = path.get(pathIndex).x;
					tileY = path.get(pathIndex).y;
					
					
					pathIndex++;
					
					wait = (int) (1000/getMovementSpeed());
				}
				else{
					if(tileX*m.getGame().getTileWidth()>x){
						x=((double)tileX-(double)wait*getMovementSpeed()/1000D )*(double)m.getGame().getTileWidth();
					}
					if(tileX*m.getGame().getTileWidth()<x){
						x=((double)tileX+(double)wait*getMovementSpeed()/1000D )*(double)m.getGame().getTileWidth();
					}
					if(tileY*m.getGame().getTileWidth()>y){
						y=((double)tileY-(double)wait*getMovementSpeed()/1000D )*(double)m.getGame().getTileWidth();
					}
					if(tileY*m.getGame().getTileWidth()<y){
						y=((double)tileY+(double)wait*getMovementSpeed()/1000D )*(double)m.getGame().getTileWidth();
					}
					
					wait--;
				}
			}
		}
	}
	
	public void setImage(Image img) {
		this.image = img;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public int getTileX() {
		return tileX;
	}

	public void setTileX(int tileX) {
		this.tileX = tileX;
	}

	public int getTileY() {
		return tileY;
	}

	public void setTileY(int tileY) {
		this.tileY = tileY;
	}

	public int getWait() {
		return wait;
	}

	public void setWait(int wait) {
		this.wait = wait;
	}

	public int getCommand() {
		return command;
	}

	public void setCommand(int command) {
		this.command = command;
	}

	public void issueMoveCommand(int x, int y){
		targetX = x;
		targetY = y;
		command = COMMAND_MOVE;
	}
	
	public int getTargetX() {
		return targetX;
	}

	public void setTargetX(int targetX) {
		this.targetX = targetX;
	}

	public int getTargetY() {
		return targetY;
	}

	public void setTargetY(int targetY) {
		this.targetY = targetY;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public double getMovementSpeed() {
		return movementSpeed;
	}

	public void setMovementSpeed(double movementSpeed) {
		this.movementSpeed = movementSpeed;
	}

	public NPCType getNpcType() {
		return npcType;
	}

	public void setNpcType(NPCType npcType) {
		this.npcType = npcType;
	}

	public NPC getPrevious() {
		return previous;
	}

	public void setPrevious(NPC previous) {
		this.previous = previous;
	}

	public NPC getNext() {
		return next;
	}

	public void setNext(NPC next) {
		this.next = next;
	}
}
