package main;

import java.awt.Image;

public class Tower {
	private Main m;
	private double attackSpeed; //attacks per second
	private double damage;      //damage
	private double range;		//attack range
	private double AAcd;		//Auto attack cooldown
	private Image projectileImage;
	private double projectileSpeed;
	private Image image;
	private int x;
	private int y;
	private TowerType towerType;
	
	public Tower(Main m, TowerType towerType, int x, int y){
		this.m = m;
		setTowerType(towerType);
		setX(x);
		setY(y);
		
		setAttackSpeed(0.0D);
		setDamage(0.0D);
		setRange(0.0D);
		setAAcd(0.0D);
		setImage(null);
		setProjectileImage(null);
		setProjectileSpeed(0.0D);
	}

	public double getAttackSpeed() {
		if(attackSpeed==0){
			return getTowerType().getAttackSpeed();
		}
		else{
			return attackSpeed;
		}
	}

	public void setAttackSpeed(double attackSpeed) {
		this.attackSpeed = attackSpeed;
	}

	public double getDamage() {
		if(damage==0){
			return getTowerType().getDamage();
		}
		else{
			return damage;
		}
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getRange() {
		if(range==0){
			return getTowerType().getRange();
		}
		else{
			return range;
		}
	}

	public void setRange(double range) {
		this.range = range;
	}

	public Image getImage() {
		if(image==null){
			return getTowerType().getImage();
		}
		else{
			return image;
		}
	}

	public TowerType getTowerType() {
		return towerType;
	}

	public void setTowerType(TowerType towerType) {
		this.towerType = towerType;
	}

	public void setImage(Image image) {
		this.image = image;
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
	
	public void attack(){
		NPC target = getClosestNPC();
		
		setAAcd(1.0D/getAttackSpeed());
		
		m.getGame().setNewProjectile( new Projectile(m, getX(), getY(), target, this) );
	}
	
	public NPC getClosestNPC(){
		double minDistance=Double.MAX_VALUE;
		NPC closest = null;
		
		if(m.getGame().getLastNPC()!=null){
		for(NPC npc = m.getGame().getLastNPC();npc!=null;npc=npc.getPrevious()){
			if(getDistance( npc.getX(),npc.getY(),getX(),getY() )<minDistance){
				minDistance=getDistance( npc.getX(),npc.getY(),getX(),getY() );
				closest = npc;
			}
		}
		}
		return closest;
	}
	
	public boolean isNpcInRange() {
		
		if(m.getGame().getLastNPC()!=null){
		for(NPC npc = m.getGame().getLastNPC();npc!=null;npc=npc.getPrevious()){
			if( isInRange( npc.getX(),npc.getY(),getX(),getY(),getRange() ) ){
				return true;
			}
		}
		}
		return false;
	}
	
	public boolean isInRange(double x, double y, double x2, double y2, double d){
		return Math.pow(x-x2, 2) + Math.pow(y-y2, 2) < Math.pow(d, 2);
	}
	
	public double getDistance(double x, double y, double x2, double y2){
		return Math.pow(Math.pow(x-x2, 2) + Math.pow(y-y2, 2), 0.5);
	}

	public Image getProjectileImage() {
		if(projectileImage==null){
			return getTowerType().getProjectileImage();
		}
		return projectileImage;
	}

	public void setProjectileImage(Image projectileImage) {
		this.projectileImage = projectileImage;
	}

	public double getProjectileSpeed() {
		if(projectileSpeed==0){
			return getTowerType().getProjectileSpeed();
		}
		return projectileSpeed;
	}

	public void setProjectileSpeed(double projectileSpeed) {
		this.projectileSpeed = projectileSpeed;
	}

	public double getAAcd() {
		return AAcd;
	}

	public void setAAcd(double aAcd) {
		AAcd = aAcd;
	}
}
