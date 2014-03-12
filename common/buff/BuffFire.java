package buff;

import npc.NPC;
import npc.NPCResistant;

public class BuffFire extends Buff{
	
	private NPC npc;
	private double damage;
	private double tick;

	public BuffFire(int dur, NPC n, int stackingType, BuffType buffType, double damage) {
		super(dur, n, stackingType, buffType);
		this.damage = damage;
		this.npc = n;
		this.tick = 0;
	}
	
	public void additionalLogic(int delta){
		tick += delta;
		while(tick>1D/damage*1000D){
			tick-=1D/damage*1000D;
			if(npc instanceof NPCResistant){
				NPCResistant npcr = (NPCResistant)npc;
				npcr.damage(1.0D*(1.0D-npcr.getAgainst().get(getBuffType().getGroupId())));
			}
			else{
				npc.damage(1);
			}
		}
	}
	
	public void onCreate(){
		/*if(npc instanceof NPCResistant){
			NPCResistant npc = ((NPCResistant)(this.npc));
			if(npc.getAgainst().containsKey(getBuffType().getGroupId())){
				npc.setMovementSpeedMultiplier(
						npc.getMovementSpeedMultiplier()*(
								(1.0D-slow*(
										1.0D-npc.getAgainst().get(getBuffType().getGroupId()))
										)
								)
						);
			}
			else{
				npc.setMovementSpeedMultiplier(npc.getMovementSpeedMultiplier()*(1.0D-slow));
			}
		}
		else{
			npc.setMovementSpeedMultiplier(npc.getMovementSpeedMultiplier()*(1.0D-slow));
		}*/
	}
	
	public void onDestroy(){
		/*if(npc instanceof NPCResistant){
			NPCResistant npc = ((NPCResistant)(this.npc));
			if(npc.getAgainst().containsKey(getBuffType().getGroupId())){
				npc.setMovementSpeedMultiplier(
						npc.getMovementSpeedMultiplier()/(
								(1.0D-slow*(
										1.0D-npc.getAgainst().get(getBuffType().getGroupId()))
										)
								)
						);
			}
			else{
				npc.setMovementSpeedMultiplier(npc.getMovementSpeedMultiplier()/(1.0D-slow));
			}
		}
		else{
			npc.setMovementSpeedMultiplier(npc.getMovementSpeedMultiplier()/(1.0D-slow));
		}*/
	}
	
}
