package main;

import java.awt.image.BufferedImage;

public class Projectile {
	private double x;
	private double y;
	private BufferedImage animationStand;
	private BufferedImage animationDeath;
	private int animationStandDuration;
	private int animationDeathDuration;
	private int animationTime;
	private Main m;
	private NPC target;
	private double targetX;
	private double targetY;
	private double speed;			//Tiles per second
	private Tower tower;
	private Projectile previous;
	private Projectile next;
	private Sprite sprite;
	private int orientation = 0;
	
	
	private Projectile(Main m, double x, double y, Tower tower){
		this.m = m;
		this.setX(x);
		this.setY(y);
		this.setTower(tower);
		this.setSpeed(0);
		this.setPrevious(null);
		this.setNext(null);
		this.setAnimationStand(null);
		this.setAnimationDeath(null);
		this.setAnimationStandDuration(1);
		this.setAnimationDeathDuration(1);
		this.setAnimationTime(0);
		this.setSprite(null);
	}
	
	public Projectile(Main m, double x, double y, double targetX, double targetY, Tower tower){
		this(m, x, y, tower);
		this.setTarget(null);
		this.setTargetPoint(targetX,targetY);
	}

	public Projectile(Main m, double x, double y, NPC target, Tower tower){
		this(m, x, y, tower);
		this.setTarget(target);
		this.setTargetPoint(0,0);
	}

	public void move(int delta){
		if(getTarget()!=null){
			NPC npc = getTarget();
			double distance = Math.pow( Math.pow(getX() - npc.getX(),2) + Math.pow(getY() - npc.getY(),2), 0.5D);
			double newDist = distance - getSpeed()*m.getGame().getTileWidth()*delta/1000D;
			double doDist = getSpeed()*m.getGame().getTileWidth()*delta/1000D;
			orientation = (int) Math.toDegrees(Math.acos( (npc.getX() - getX()) / Math.pow(Math.pow(npc.getX() - getX(), 2) + Math.pow(npc.getY()-getY(), 2), 0.5) ));
			//System.out.println((npc.getX() - getX()) / Math.pow(Math.pow(npc.getX() - getX(), 2) + Math.pow(npc.getY()-getY(), 2), 0.5));
			orientation = 90;
			
			if(newDist <= 0){
				setX(npc.getX());
				setY(npc.getY());
				m.getGame().destroySprite(getSprite());
				m.getGame().destroyProjectile(this);
				m.getGame().setNewAnimation(new Animation(m, (int)getX(), (int)getY(), getAnimationDeath(), this, false, getAnimationDeathDuration()));
				npc.damage(getTower().getDamage());
			}
			else{
				double newX = getX()+((npc.getX()-getX())*doDist/distance);
				double newY = getY()+((npc.getY()-getY())*doDist/distance);
				setX( newX );
				setY( newY );
			}
		}
		
		else{
			double distance = Math.pow( Math.pow(getX() - getTargetX(),2) + Math.pow(getY() - getTargetY(),2), 0.5D);
			double newDist = distance - getSpeed()*m.getGame().getTileWidth()*delta/1000D;
			double doDist = getSpeed()*m.getGame().getTileWidth()*delta/1000D;
			orientation = (int) Math.toDegrees(Math.acos( (getTargetX() - getX()) / Math.pow(Math.pow(getTargetX() - getX(), 2) + Math.pow(getTargetY()-getY(), 2), 0.5) ));
			
			if(newDist <= 0){
				setX(getTargetX());
				setY(getTargetY());
				m.getGame().destroySprite(getSprite());
				m.getGame().destroyProjectile(this);
				m.getGame().setNewAnimation(new Animation(m, (int)getX(), (int)getY(), getAnimationDeath(), this, false, getAnimationDeathDuration()));
				tower.damageGround(this);
			}
			else{
				double newX = getX()+((getTargetX()-getX())*doDist/distance);
				double newY = getY()+((getTargetY()-getY())*doDist/distance);
				setX( newX );
				setY( newY );
			}
		}
	}
	
	public void drawLogic (int delta) {
		setAnimationTime( (getAnimationTime() + delta) % getAnimationStandDuration() );
		int phase = (int) ( (double)getAnimationTime() / (double)getAnimationStandDuration() * (double)Animation.getImagePhases(getAnimationStand()) );
		BufferedImage img = Game.rotate(Animation.getImagePhase(getAnimationStand(),phase,m), orientation);
		//System.out.println(orientation);
		
		if(getSprite() != null &&
		   (getSprite().getX()==getX()&&
		   getSprite().getY()==getY()&&
		   getSprite().getImage()==img ) ){}
		else{
			if(getSprite()!=null){m.getGame().destroySprite(getSprite());}
			Sprite s =  new Sprite(m, (int)getX(), (int)getY(), img, this, true);
			m.getGame().setNewSprite( s );
			setSprite(s);
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

	/*public BufferedImage getImage() {
		if(image==null){
			return getTower().getProjectileImage();
		}
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}*/

	public NPC getTarget() {
		return target;
	}

	public void setTarget(NPC target) {
		this.target = target;
	}

	public double getTargetX() {
		return targetX;
	}

	public void setTargetX(double targetX) {
		this.targetX = targetX;
	}

	public double getTargetY() {
		return targetY;
	}

	public void setTargetY(double targetY) {
		this.targetY = targetY;
	}
	
	public void setTargetPoint(double targetX, double targetY){
		this.targetX = targetX;
		this.targetY = targetY;
	}

	public double getSpeed() {
		if(speed==0){
			return getTower().getProjectileSpeed();
		}
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public Tower getTower() {
		return tower;
	}

	public void setTower(Tower tower) {
		this.tower = tower;
	}

	public Projectile getPrevious() {
		return previous;
	}

	public void setPrevious(Projectile previous) {
		this.previous = previous;
	}

	public Projectile getNext() {
		return next;
	}

	public void setNext(Projectile next) {
		this.next = next;
	}

	public Sprite getSprite() {
		return sprite;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	public BufferedImage getAnimationStand() {
		return animationStand;
	}

	public void setAnimationStand(BufferedImage animationStand) {
		this.animationStand = animationStand;
	}

	public BufferedImage getAnimationDeath() {
		return animationDeath;
	}

	public void setAnimationDeath(BufferedImage animationDeath) {
		this.animationDeath = animationDeath;
	}

	public int getAnimationStandDuration() {
		return animationStandDuration;
	}

	public void setAnimationStandDuration(int animationStandDuration) {
		this.animationStandDuration = animationStandDuration;
	}

	public int getAnimationDeathDuration() {
		return animationDeathDuration;
	}

	public void setAnimationDeathDuration(int animationDeathDuration) {
		this.animationDeathDuration = animationDeathDuration;
	}

	public int getAnimationTime() {
		return animationTime;
	}

	public void setAnimationTime(int animationTime) {
		this.animationTime = animationTime;
	}
}
