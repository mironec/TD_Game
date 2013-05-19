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
		npc.setMovementSpeedMultiplier(npc.getMovementSpeedMultiplier()*(1.0D-slow));
	}
	
	public void onDestroy(){
		npc.setMovementSpeedMultiplier(npc.getMovementSpeedMultiplier()/(1.0D-slow));
	}
	
}
