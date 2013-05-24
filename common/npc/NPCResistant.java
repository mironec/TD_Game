package npc;

import java.util.Map;

import main.Main;

public class NPCResistant extends NPC{
	
	@Suppresprivate Map<Integer, Double> against;
	
	public NPCResistant(Main m, double x, double y, npc.NPCType NPCType, Map<Integer, Double> against) {
		super(m, x, y, NPCType);
		this.against = against;
	}

	public Map<Integer, Double> getAgainst() {
		return against;
	}

	public void setAgainst(Map<Integer, Double> against) {
		this.against = against;
	}

}
