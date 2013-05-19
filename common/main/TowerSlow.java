package main;

import java.util.ArrayList;

public class TowerSlow extends Tower{

	private double slow;
	private int dur;
	
	public TowerSlow(Main m, int x, int y, TowerType towerType, double slow, int dur) {
		super(m, x, y, towerType);
		this.slow = slow;
		this.dur = dur;
	}
	
	public void damage(Projectile p){
		if(canAttack(this, p.getTarget())){
			p.getTarget().damage(getDamage());
			
			ArrayList<Buff> destroy = new ArrayList<Buff>();
			for( Buff b : p.getTarget().getBuffs() ) {
				if(b instanceof BuffSlow)
					destroy.add(b);
			}
			for( Buff b : destroy ){
				b.destroy();
			}
			
			new BuffSlow(dur, p.getTarget(), slow);
		}
	}

	public double getSlow() {
		return slow;
	}

	public void setSlow(double slow) {
		this.slow = slow;
	}

	public int getDur() {
		return dur;
	}

	public void setDur(int dur) {
		this.dur = dur;
	}
	
}
