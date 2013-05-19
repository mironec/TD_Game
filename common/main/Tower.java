package main;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Tower implements Buffable{
	
	protected Main m;
	private double attackSpeed; //attacks per second
	private double damage;      //damage
	private double range;		//attack range
	private double AAcd;		//Auto attack cooldown
	private BufferedImage projectileAnimationStand;
	private BufferedImage projectileAnimationDeath;
	private int projectileAnimationStandDuration;
	private int projectileAnimationDeathDuration;
	private double projectileSpeed;
	public static final int ANIMATION_STAND=0;
	public static final int ANIMATION_ATTACK=1;
	private BufferedImage animationStand;
	private BufferedImage animationAttack;
	private int animationStandDuration;
	private int animationAttackDuration;
	private int animation;
	private int animationTime;
	private int x;
	private int y;
	private Tower previous;
	private Tower next;
	private Sprite sprite;
	private TowerType towerType;
	private boolean shootingAir;
	private boolean shootingGround;
	private ArrayList<Buff> buffs;
	
	public Tower(Main m, int x, int y, TowerType towerType){
		this.m = m;
		this.towerType = towerType;
		setX(x);
		setY(y);
		
		setAttackSpeed(0.0D);
		setDamage(0.0D);
		setRange(0.0D);
		setAAcd(0.0D);
		setProjectileAnimationDeath(null);
		setProjectileAnimationStand(null);
		setProjectileAnimationDeathDuration(1);
		setProjectileAnimationStandDuration(1);
		setProjectileSpeed(0.0D);
		setSprite(null);
		setAnimation(ANIMATION_STAND);
		setAnimationTime(0);
		setShootingAir(true);
		setShootingGround(true);
		setBuffs(new ArrayList<Buff>());
	}
	
	public void copyFromTower(Tower t){
		setAnimationAttack(t.getAnimationAttack());
		setAnimationAttackDuration(t.getAnimationAttackDuration());
		setAnimationStand(t.getAnimationStand());
		setAnimationStandDuration(t.getAnimationStandDuration());
		setAttackSpeed(t.getAttackSpeed());
		setDamage(t.getDamage());
		setProjectileAnimationDeath(t.getProjectileAnimationDeath());
		setProjectileAnimationStand(t.getProjectileAnimationStand());
		setProjectileAnimationDeathDuration(t.getProjectileAnimationDeathDuration());
		setProjectileAnimationStandDuration(t.getProjectileAnimationStandDuration());
		setProjectileSpeed(t.getProjectileSpeed());
		setRange(t.getRange());
		setShootingAir(t.isShootingAir());
		setShootingGround(t.isShootingGround());
	}
	
	public void logic (int delta) {
		additionalLogic(delta);
		
		ArrayList<Buff> buffs = new ArrayList<Buff>(this.buffs);
		for( Buff b : buffs ) {
			b.logic(delta);
		}
		
		if(isNpcInRange()){
			if(getAttackSpeed()>0&&
			   getRange()>0&&
			   getAAcd()==0){
				attack();
			}
			if(getAAcd()>0){
				setAAcd( (getAAcd()-delta/1000D)>0 ? (getAAcd()-delta/1000D) : 0 );
			}
		}
	}
	
	public void additionalLogic (int delta) {
		
	}
	
	public void drawLogic (int delta) {
		int duration = getAnimationStandDuration();
		BufferedImage animation = getAnimationStand();
		
		if(getAnimation() == ANIMATION_ATTACK){
			duration = getAnimationAttackDuration();
			animation = getAnimationAttack();
		}
		
		if(getAnimationTime()+delta > duration && getAnimation() == ANIMATION_ATTACK){ setAnimation(ANIMATION_STAND); }
		setAnimationTime( (getAnimationTime()+delta) % duration );
		int phase = (int) ( (double)getAnimationTime() / (double)duration * (double)Animation.getImagePhases(animation) );
		if(getSprite()==null){
			Sprite s = new Sprite(m, (int)getX(), (int)getY(), Animation.getImagePhase(animation,phase,m), this, false);
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
				Sprite s = new Sprite(m, (int)getX(), (int)getY(), Animation.getImagePhase(animation,phase,m), this, false);
				m.getGame().setNewSprite(s);
				setSprite(s);
			}
		}
	}
	
	public void sell(){
		destroy();
		int ret = 0;
		for(TowerType t = getTowerType();t!=null;t=t.getBase()){
			ret += t.getCost();
		}
		m.getGame().setMoney(m.getGame().getMoney() + ret);
	}
	
	public void upgradeTo(TowerType t){
		if(m.getGame().getMoney()>=t.getCost()){
			m.getGame().setMoney(m.getGame().getMoney()-t.getCost());
			Tower t2 = m.getGame().createTower(t);
			t2.setX(getX());
			t2.setY(getY());
			destroy();
		}
		else{
			m.getGame().setStatus("Not enough money!");
			m.setNewEvent(new Event(m, 2000, 1) {
				public void run(int delta) {
					m.getGame().setStatus("","Not enough money!");
				}
			});
		}
	}
	
	public void destroy(){
		m.getGame().destroyTower(this);
	}

	public double getAttackSpeed() {
		return attackSpeed;
	}

	public void setAttackSpeed(double attackSpeed) {
		this.attackSpeed = attackSpeed;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public int getX() {
		return x;
	}

	public Tower setX(int x) {
		this.x = x;
		return this;
	}

	public int getY() {
		return y;
	}

	public Tower setY(int y) {
		this.y = y;
		return this;
	}
	
	public void damageGround(Projectile p){
		NPC target = getClosestNPC(p.getX(),p.getY());
		
		if(target!=null)
			if( getDistance(target.getX(),target.getY(),p.getX(),p.getY()) < m.getGame().getTileWidth() )
				if( canAttack(this,target) )
					target.damage(getDamage());
	}
	
	public void damage(Projectile p){
		if(canAttack(this, p.getTarget()))
			p.getTarget().damage(getDamage());
	}
	
	public void attack(){
		NPC target = getClosestNPC();
		
		if(target!=null){
			setAAcd(1.0D/getAttackSpeed());
			setAnimation(ANIMATION_ATTACK);
			
			Projectile p = new Projectile(m, getX(), getY(), target, this);
			p.setAnimationDeath(getProjectileAnimationDeath());
			p.setAnimationStand(getProjectileAnimationStand());
			p.setAnimationDeathDuration(getProjectileAnimationDeathDuration());
			p.setAnimationStandDuration(getProjectileAnimationStandDuration());
			m.getGame().setNewProjectile( p );
		}
	}
	
	public NPC getClosestNPC(){
		return getClosestNPC(getX(),getY());
	}
	
	public static boolean canAttack(Tower t, NPC n){
		if(n.isUntargetable())
			return false;
		if(!t.isShootingAir() && n.isFlying())
			return false;
		if(!t.isShootingGround() && !n.isFlying())
			return false;
		if(getDistance(t.getX(),t.getY(),n.getX(),n.getY())>t.getRange()*Main.instance.getGame().getTileWidth())
			return false;
		
		return true;
	}
	
	public static boolean canAttackIgnoreRange(Tower t, NPC n){
		if(n.isUntargetable())
			return false;
		if(!t.isShootingAir() && n.isFlying())
			return false;
		if(!t.isShootingGround() && !n.isFlying())
			return false;
		
		return true;
	}
	
	public NPC getClosestNPC(double x, double y){
		double minDistance=Double.MAX_VALUE;
		NPC closest = null;
		
		if(m.getGame().getLastNPC()!=null){
		for(NPC npc = m.getGame().getLastNPC();npc!=null;npc=npc.getPrevious()){
			if(!canAttack(this,npc)){continue;}
			if(getDistance( npc.getX(),npc.getY(),x,y )<minDistance){
				minDistance=getDistance( npc.getX(),npc.getY(),x,y );
				closest = npc;
			}
		}
		}
		return closest;
	}
	
	public boolean isNpcInRange() {
		if(m.getGame().getLastNPC()!=null){
		for(NPC npc = m.getGame().getLastNPC();npc!=null;npc=npc.getPrevious()){
			if( isInRange( npc.getX(),npc.getY(),getX(),getY(),getRange()*m.getGame().getTileWidth() ) ){
				return true;
			}
		}
		}
		return false;
	}
	
	public boolean isInRange(double x, double y, double x2, double y2, double d){
		return Math.pow(x-x2, 2) + Math.pow(y-y2, 2) < Math.pow(d, 2);
	}
	
	public static double getDistance(double x, double y, double x2, double y2){
		return Math.pow(Math.pow(x-x2, 2) + Math.pow(y-y2, 2), 0.5);
	}

	public double getProjectileSpeed() {
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

	public Tower getPrevious() {
		return previous;
	}

	public void setPrevious(Tower previous) {
		this.previous = previous;
	}

	public Tower getNext() {
		return next;
	}

	public void setNext(Tower next) {
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

	public BufferedImage getAnimationAttack() {
		return animationAttack;
	}

	public void setAnimationAttack(BufferedImage animationAttack) {
		this.animationAttack = animationAttack;
	}

	public int getAnimationStandDuration() {
		return animationStandDuration;
	}

	public void setAnimationStandDuration(int animationStandDuration) {
		this.animationStandDuration = animationStandDuration;
	}

	public int getAnimationAttackDuration() {
		return animationAttackDuration;
	}

	public void setAnimationAttackDuration(int animationAttackDuration) {
		this.animationAttackDuration = animationAttackDuration;
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

	public BufferedImage getProjectileAnimationStand() {
		return projectileAnimationStand;
	}

	public void setProjectileAnimationStand(BufferedImage projectileAnimationStand) {
		this.projectileAnimationStand = projectileAnimationStand;
	}

	public BufferedImage getProjectileAnimationDeath() {
		return projectileAnimationDeath;
	}

	public void setProjectileAnimationDeath(BufferedImage projectileAnimationDeath) {
		this.projectileAnimationDeath = projectileAnimationDeath;
	}

	public int getProjectileAnimationStandDuration() {
		return projectileAnimationStandDuration;
	}

	public void setProjectileAnimationStandDuration(
			int projectileAnimationStandDuration) {
		this.projectileAnimationStandDuration = projectileAnimationStandDuration;
	}

	public int getProjectileAnimationDeathDuration() {
		return projectileAnimationDeathDuration;
	}

	public void setProjectileAnimationDeathDuration(
			int projectileAnimationDeathDuration) {
		this.projectileAnimationDeathDuration = projectileAnimationDeathDuration;
	}

	public TowerType getTowerType() {
		return towerType;
	}

	public void setTowerType(TowerType towerType) {
		this.towerType = towerType;
	}

	public boolean isShootingAir() {
		return shootingAir;
	}

	public void setShootingAir(boolean shootingAir) {
		this.shootingAir = shootingAir;
	}

	public boolean isShootingGround() {
		return shootingGround;
	}

	public void setShootingGround(boolean shootingGround) {
		this.shootingGround = shootingGround;
	}

	public void removeBuff(Buff buff) {
		this.buffs.remove(buff);
	}

	public void addBuff(Buff buff) {
		this.buffs.add(buff);
	}

	public ArrayList<Buff> getBuffs() {
		return buffs;
	}

	public void setBuffs(ArrayList<Buff> buffs) {
		this.buffs = buffs;
	}
}
