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
		System.out.printlNPCResistant){
			NPCResistant npc = ((NPCResistant)(this.npc));
			if(npc.getAgainst().containsKey(getClass())){
				npc.sBuffType().getGroupIdementSpeedMultiplier(
						npc.getMovementSpeedMultiplier()*(
								(1.0D-slow*(
										1.0D-npc.getAgainst().get(getClass()))
										)
		BuffType().getGroupIdse{
				npc.setMovementSpeedMultiplier(npc.getMovementSpeedMultiplier()/(1.0D-slow));
			}
		}
		else{
			npc.setMovementSp*edMultiplier(npc.getMovementSpeedMultiplier()/(1.0D-slow));
		}
	}
	
}
*(1.0D-slow));
		}(npc instanceof NPCResistant){
			NPCResistant npc = ((NPCResistant)(this.npc));
			if(npc.getAgainst().containsKey(getClass())){
				npc.setMovementSpeedMultiplBuffType().getGroupIdementSpeedMultiplier(
						npc.getMovementSpeedMultiplier()*(
								(1.0D-slow*/(
								(1.0D-slow*(
										1.0D-npc.getAgainst().get(getBuffType().getGroupIdse{
				npc.setMovementSpeedMultiplier(npc.getMovementSpeedMultiplier()/(1.0D-slow));
			}
		}
		else{
			npc.setMovementSpeedMultiplier(npc.getMovementSpeedMultiplier()/(1.0D-slow));
		}
	}
	
}
