package main;

import java.awt.Image;

public class TowerType {
	
	private Image image;
	private Main m;
	private double range;
	private double damage;
	private double attackSpeed;
	private Image projectileImage;
	private double projectileSpeed;
	
	public TowerType(Main m){
		this.m = m;
		
		setImage( this.m.createImage(m.getGame().getTileWidth(),m.getGame().getTileWidth()) );
		setProjectileImage( this.m.createImage(m.getGame().getTileWidth()/5,m.getGame().getTileWidth()/5) );
		setRange(0);
		setDamage(0);
		setAttackSpeed(0);
		setProjectileSpeed(0);
		setProjectileImage(null);
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getAttackSpeed() {
		return attackSpeed;
	}

	public void setAttackSpeed(double attackSpeed) {
		this.attackSpeed = attackSpeed;
	}

	public Image getProjectileImage() {
		return projectileImage;
	}

	public void setProjectileImage(Image projectileImage) {
		this.projectileImage = projectileImage;
	}

	public double getProjectileSpeed() {
		return projectileSpeed;
	}

	public void setProjectileSpeed(double projectileSpeed) {
		this.projectileSpeed = projectileSpeed;
	}
}
