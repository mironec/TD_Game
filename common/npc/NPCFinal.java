package npc;

import main.Main;

public class NPCFinal extends NPC {

	public NPCFinal(Main m, double x, double y, npc.NPCType NPCType, int number) {
		super(m, x, y, NPCType);
		double maxHP = (Integer)NPCType.getArg("maxLife");
		for(int loop=0;loop<number;loop++){
			maxHP = maxHP * (1.0D + (Double)NPCType.getArg("increment"));
		}
		NPCType.setMaxHealth((int)maxHP);
		m.getGame().setFinalsSpawned(number+1);
	}

	public void onDeath(){
		m.getGame().setFinalsKilled(m.getGame().getFinalsKilled()+1);
	}
}
