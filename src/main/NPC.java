package main;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class NPC {

	private double x;
	private double y;
	private int tileX;
	private int tileY;
	private int wait; //in ms
	protected Main m;
	private int command;
	public static final int COMMAND_NOTHING=0;
	public static final int COMMAND_MOVE=1;
	
	public static final int ANIMATION_DEATH=0;
	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public static final int ANIMATION_STAND=1;
	public static final int ANIMATION_WALK=2;
	private BufferedImage animationDeath;
	private BufferedImage animationStand;
	private BufferedImage animationWalk;
	private int animation;
	private int animationTime;
	private int animationDeathDuration;			//in ms
	private int animationStandDuration;			//in ms
	private int animationWalkDuration;			//in ms
	
	private int targetX;
	private int targetY;
	private ArrayList<Point> path;
	private int pathIndex=-1;
	private int health;
	private int maxHealth;
	private double movementSpeed; //tiles per second
	private NPC previous;
	private NPC next;
	private Sprite sprite;
	private NPCType NPCType;
	private int orientation = ORIENTATION_EAST;
	public final static int ORIENTATION_EAST = 0;
	public final static int ORIENTATION_NORTH = 1;
	public final static int ORIENTATION_WEST = 2;
	public final static int ORIENTATION_SOUTH = 3;
	private boolean untargetable;
	
	public NPC (Main m, double x, double y, NPCType NPCType){
		setX(x);
		setY(y);
		this.m = m;
		this.NPCType = NPCType;
		
		//DEFAULT
		
		setAnimation(ANIMATION_STAND);
		setAnimationTime(0);
		setAnimationDeath(null);
		setAnimationStand(null);
		setAnimationWalk(null);
		setAnimationDeathDuration(0);
		setAnimationStandDuration(0);
		setAnimationWalkDuration(0);
		setWait(0);
		setCommand(COMMAND_NOTHING);
		setTargetX(getTileX());
		setTargetY(getTileY());
		setHealth(1);
		setMovementSpeed(1);
		setUntargetable(false);
		
		m.getGame().setNumberNPC(m.getGame().getNumberNPC()+1);
	}
	
	public void copyFromNPC(NPC npc){
		setAnimationDeath(npc.getAnimationDeath());
		setAnimationDeathDuration(npc.getAnimationDeathDuration());
		setAnimationStand(npc.getAnimationStand());
		setAnimationStandDuration(npc.getAnimationStandDuration());
		setAnimationWalk(npc.getAnimationWalk());
		setAnimationWalkDuration(npc.getAnimationWalkDuration());
		setMaxHealth(npc.getMaxHealth());
		setMovementSpeed(npc.getMovementSpeed());
		setUntargetable(npc.isUntargetable());
		//////////////////////////////////////////////////////////
		setAnimation(ANIMATION_STAND);
		setAnimationTime(0);
		setCommand(COMMAND_NOTHING);
		setHealth(getMaxHealth());
		setTargetX(0);
		setTargetY(0);
		setWait(0);
	}
	
	public void logic (int delta) {
		additionalLogic(delta);
		if(getCommand()==NPC.COMMAND_MOVE){
			goToTarget(delta);
		}
	}
	
	public void additionalLogic (int delta) {
		
	}
	
	public void drawLogic (int delta) {
		int duration = getAnimationStandDuration();
		BufferedImage animation = getAnimationStand();
		
		if(getAnimation() == ANIMATION_WALK){
			duration = getAnimationWalkDuration();
			animation = getAnimationWalk();
		}
		
		setAnimationTime( (getAnimationTime()+delta) % duration );
		int phase = (int) ( (double)getAnimationTime() / (double)duration * (double)Animation.getImagePhases(animation) );
		if(getSprite()==null){
			Sprite s = new Sprite(m, (int)getX(), (int)getY(), Game.rotate(Animation.getImagePhase(animation,phase,m),orientation*90), this, false);
			m.getGame().setNewSprite(s);
			setSprite(s);
		}
		else{
			if((getSprite().getX()==getX()&&
			   getSprite().getY()==getY()&&
			   getSprite().getImage()==Animation.getImagePhase(animation, phase, m)) )
			{}
			else{
				m.getGame().destroySprite(getSprite());
				Sprite s = new Sprite(m, (int)getX(), (int)getY(), Game.rotate(Animation.getImagePhase(animation,phase,m),orientation*90), this, false);
				m.getGame().setNewSprite(s);
				setSprite(s);
			}
		}
	}
	
	public void damage(double damage){
		health-=damage;
		if(health<=0){
			die();
		}
	}
	
	public void die(){
		m.getGame().destroySprite(getSprite());
		m.getGame().setNewAnimation(new Animation(m, (int)getX(), (int)getY(), Game.rotate(getAnimationDeath(),orientation*90), this, false, getAnimationDeathDuration()));
		m.getGame().destroyNPC(this);
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
	
	public void goToTarget(int delta) {
		if(path==null||path.isEmpty()){
			path = findPath(tileX, tileY, new ArrayList<Point>(),new ArrayList<Point>(), targetX, targetY);
			pathIndex=1;
		}
		else{
			for(int loop=0;loop<delta;loop++){
				if(wait==0){
					setAnimation(ANIMATION_WALK);
					if(tileX==targetX&&tileY==targetY){
						command = COMMAND_NOTHING;
						setAnimation(ANIMATION_STAND);
						return;
					}
					
					x=tileX*m.getGame().getTileWidth();
					y=tileY*m.getGame().getTileWidth();
					
					if(tileX<path.get(pathIndex).x){orientation = ORIENTATION_EAST;}
					if(tileY>path.get(pathIndex).y){orientation = ORIENTATION_NORTH;}
					if(tileX>path.get(pathIndex).x){orientation = ORIENTATION_WEST;}
					if(tileY<path.get(pathIndex).y){orientation = ORIENTATION_SOUTH;}
					
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

	public BufferedImage getAnimationDeath() {
		return animationDeath;
	}

	public void setAnimationDeath(BufferedImage animationDeath) {
		this.animationDeath = animationDeath;
	}

	public BufferedImage getAnimationStand() {
		return animationStand;
	}

	public void setAnimationStand(BufferedImage animationStand) {
		this.animationStand = animationStand;
	}

	public BufferedImage getAnimationWalk() {
		return animationWalk;
	}

	public void setAnimationWalk(BufferedImage animationWalk) {
		this.animationWalk = animationWalk;
	}

	public int getAnimation() {
		return animation;
	}

	public void setAnimation(int animation) {
		if(animation != this.animation){setAnimationTime(0);}
		this.animation = animation;
	}

	public int getAnimationTime() {
		return animationTime;
	}

	public void setAnimationTime(int animationTime) {
		this.animationTime = animationTime;
	}

	public Sprite getSprite() {
		return sprite;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	public int getAnimationDeathDuration() {
		return animationDeathDuration;
	}

	public void setAnimationDeathDuration(int animationDeathDuration) {
		this.animationDeathDuration = animationDeathDuration;
	}

	public int getAnimationStandDuration() {
		return animationStandDuration;
	}

	public void setAnimationStandDuration(int animationStandDuration) {
		this.animationStandDuration = animationStandDuration;
	}

	public int getAnimationWalkDuration() {
		return animationWalkDuration;
	}

	public void setAnimationWalkDuration(int animationWalkDuration) {
		this.animationWalkDuration = animationWalkDuration;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}

	public NPCType getNPCType() {
		return NPCType;
	}

	public void setNPCType(NPCType nPCType) {
		NPCType = nPCType;
	}

	public boolean isUntargetable() {
		return untargetable;
	}

	public void setUntargetable(boolean untargetable) {
		this.untargetable = untargetable;
	}
}
