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
		System.out.println(slow);
		if(npc instanceof NPCResistant){
			NPCResistant npc = ((NPCResistant)(this.npc));
			if(npc.getAgainst().containsKey(getClass())){
				npc.setMovementSpeedMultiplier(
						npc.getMovementSpeedMultiplier()*(
								(1.0D-slow*(
										1.0D-npc.getAgainst().get(getClass()))
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
		System.out.println(npc.getMovementSpeedMultiplier());
	}
	
	public void onDestroy(){
		if(npc instanceof NPCResistant){
			NPCResistant npc = ((NPCResistant)(this.npc));
			if(npc.getAgainst().containsKey(getClass())){
				npc.setMovementSpeedMultiplier(
						npc.getMovementSpeedMultiplier()/(
								(1.0D-slow*(
										1.0D-npc.getAgainst().get(getClass()))
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
