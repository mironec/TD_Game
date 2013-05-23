package npc;

import java.util.Map;

import main.Main;

public class NPCResistant extends NPC{
	
	@SuppressWarnings("rawtypes")
	private Map<Class, Double> against;
	
	@SuppressWarnings("rawtypes")
	public NPCResistant(Main m, double x, double y, npc.NPCType NPCType, Map<Class, Double> against) {
		super(m, x, y, NPCType);
		this.against = against;
	}

	@SuppressWarnings("rawtypes")
	public Map<Class, Double> getAgainst() {
		return against;
	}

	@SuppressWarnings("rawtypes")
	public void setAgainst(Map<Class, Double> against) {
		this.against = against;
	}

}
