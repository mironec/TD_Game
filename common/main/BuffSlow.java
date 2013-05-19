package main;

public class BuffSlow extends Buff{
	
	private NPC npc;
	private double slow;

	public BuffSlow(int dur, NPC n, double slow) {
		super(dur, n);
		this.slow = slow;
		this.npc = n;
	}
	
	public void onCreate(){
		npc.setMovemenif(npc instanceof NPCResistant){
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
		entSpeedMultiplier(npc.getMovementSpeedMultiplier()*(1.0D-slow));
	}
	
	public voi		}
		}
		else{
	entSpeedMultiplier(npc.getMovementSpeedMultiplier()*(1.0D-slow));
	}
	
	public voi	}
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
