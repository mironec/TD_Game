package main;

import java.util.ArrayList;

public class TowerSiege extends Tower {
	
	private double splashRadius;

	public TowerSiege(Main m, int x, int y, TowerType towerType, double splashRadius) {
		super(m, x, y, towerType);
		this.splashRadius = splashRadius;
	}
	
	public void damageGround(Projectile p){
		for( NPC n : getNPCsCloseTo(p.getX(),p.getY() )){
			n.damage(getDamage());
		}
	}
	
	public void damage (Projectile p) {
		for( NPC n : getNPCsCloseTo(p.getX(),p.getY() )){
			n.damage(getDamage());
		}
	}
	
	public ArrayList<NPC> getNPCsCloseTo(double x, double y){
		ArrayList<NPC> result = new ArrayList<NPC>();
		
		for(NPC npc = m.getGame().getLastNPC(); npc != null; npc = npc.getPrevious()){
			if(getDistance(npc.getX(), npc.getY(), x, y) < getSplashRadius()*m.getGame().getTileWidth()){
				if(canAttackIgnoreRange(this,npc)){
					result.add(npc);
				}
			}
		}
		
		return result;
	}

	public double getSplashRadius() {
		return splashRadius;
	}

	public void setSplashRadius(double splashRadius) {
		this.splashRadius = splashRadius;
	}

}
