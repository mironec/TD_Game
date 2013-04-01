package main;

public class TowerMultiAttack extends Tower{

	private int maxTargets;
	
	public TowerMultiAttack(Main m, int x, int y, TowerType towerType, int maxTargets) {
		super(m, x, y, towerType);
		this.maxTargets = maxTargets;
	}
	
	public void attack(){
		NPC targets [] = getClosestNPC(maxTargets);
		
		setAAcd(1.0D/getAttackSpeed());
		setAnimation(ANIMATION_ATTACK);
		
		for(int loop=0;loop<targets.length;loop++){
			if(targets[loop]==null){continue;}
			Projectile p = new Projectile(m, getX(), getY(), targets[loop], this);
			p.setAnimationDeath(getProjectileAnimationDeath());
			p.setAnimationStand(getProjectileAnimationStand());
			p.setAnimationDeathDuration(getProjectileAnimationDeathDuration());
			p.setAnimationStandDuration(getProjectileAnimationStandDuration());
			m.getGame().setNewProjectile( p );
		}
	}

	public NPC[] getClosestNPC(int number){
		NPC all [] = new NPC[m.getGame().getNumberNPC()];
		int npcs = 0;
		
		if(m.getGame().getLastNPC()!=null){
		for(NPC npc = m.getGame().getLastNPC();npc!=null;npc=npc.getPrevious()){
			if(npc.isUntargetable()){continue;}
			if(getDistance( npc.getX(),npc.getY(),getX(),getY() )<getRange()*m.getGame().getTileWidth()){
				all[npcs]=npc;
				npcs++;
			}
		}
		}
		NPC all2 [] = new NPC[npcs];
		
		for(int loop2 = 0;loop2<npcs;loop2++){
			double minDistance=Double.MAX_VALUE;
			for(int loop = 0;loop<npcs;loop++){
				if(all[loop]==null) continue;
				double dist = getDistance(all[loop].getX(),all[loop].getY(),getX(),getY());
				if(dist < minDistance){
					minDistance = dist;
					all2[loop2] = all[loop];
				}
			}
			
			for(int loop = 0;loop<npcs;loop++){
				if(all[loop]==null) continue;
				if(getDistance(all[loop].getX(),all[loop].getY(),getX(),getY()) == minDistance){
					all[loop] = null;
				}
			}
		}
		
		NPC closest [] = new NPC[number];
		for(int loop = 0;loop<npcs&&loop<number;loop++){
			closest[loop] = all2[loop];
		}
		
		return closest;
	}
	
	public int getMaxTargets() {
		return maxTargets;
	}

	public void setMaxTargets(int maxTargets) {
		this.maxTargets = maxTargets;
	}
}