package tower;

import java.util.ArrayList;

import buff.BuffType;

import main.Main;

public class TowerDebuffer extends Tower{

	private ArrayList<BuffType> debuffs;

	public TowerDebuffer(Main m, int x, int y, TowerType towerType, ArrayList<BuffType> debuffs) {
		super(m, x, y, towerType);
		this.debuffs = debuffs;
	}
	
	public void damage(Projectile p){
		if(canAttack(this, p.getTarget())){
			p.getTarget().damage(getDamage());
			
			for(BuffType b : debuffs){
				BuffType.newBuffFromBuffType(b, p.getTarget());
			}
		}
	}
	
	public ArrayList<BuffType> getDebuffs() {
		return debuffs;
	}

	public void setDebuffs(ArrayList<BuffType> debuffs) {
		this.debuffs = debuffs;
	}
	
}
