package main;

import java.awt.Image;

public class Projectile {
	private double x;
	private double y;
	private Image image;
	private Main m;
	private NPC target;
	private double targetX;
	private double targetY;
	private double speed;			//Tiles per second
	private Tower tower;
	private Projectile previous;
	private Projectile next;
	
	public Projectile(Main m, double x, double y, double targetX, double targetY, Tower tower){
		this.m = m;
		this.setX(x);
		this.setY(y);
		this.setTower(tower);
		this.setTarget(null);
		this.setTargetPoint(targetX,targetY);
		this.setSpeed(0);
		this.setPrevious(null);
		this.setNext(null);
		this.setImage(null);
	}

	public Projectile(Main m, double x, double y, NPC target, Tower tower){
		this.m = m;
		this.setX(x);
		this.setY(y);
		this.setTower(tower);
		this.setTarget(target);
		this.setTargetPoint(0,0);
		this.setSpeed(0);
		this.setPrevious(null);
		this.setNext(null);
		this.setImage(null);
	}

	public void move(int delta){
		if(getTarget()!=null){
			NPC npc = getTarget();
			double distance = Math.pow( Math.pow(getX() - npc.getX(),2) + Math.pow(getY() - npc.getY(),2), 0.5D);
			double newDist = distance - getSpeed()*m.getGame().getTileWidth()*delta/1000D;
			double doDist = getSpeed()*m.getGame().getTileWidth()*delta/1000D;
			
			if(newDist <= 0){
				setX(npc.getX());
				setY(npc.getY());
				m.getGame().destroyProjectile(this);
				npc.damage(getTower().getDamage());
			}
			else{
				double newX = getX()+((npc.getX()-getX())*doDist/distance);
				double newY = getY()+((npc.getY()-getY())*doDist/distance);
				setX( newX );
				setY( newY );
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

	public Image getImage() {
		if(image==null){
			return getTower().getProjectileImage();
		}
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

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
}
