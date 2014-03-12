package buff;

import npc.NPC;
import npc.NPCResistant;

public class BuffSlow extends Buff{
	
	private NPC npc;
	private double slow;

	public BuffSlow(int dur, NPC n, int stackingType, BuffType buffType, double slow) {
		super(dur, n, stackingType, buffType);
		this.slow = slow;
		this.npc = n;
	}
	
	public void onCreate(){
		if(npc instanceof NPCResistant){
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
		}
	}
	
	public void onDestroy(){
		if(npc instanceof NPCResistant){
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
		}
	}
	
}
