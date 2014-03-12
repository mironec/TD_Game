package npc;

import java.util.HashMap;
import java.util.Map;

import main.Main;

public class NPCType extends NPC{

	private int id;
	private String type;
	private Map<String,Object> args = new HashMap<String, Object>();
	private int perWave;
	private int betweenSpawns;
	private int bounty;
	private String description = "";
	
	public static final String NPC_TYPE_REVIVE    = "revive";
	public static final String NPC_TYPE_FINAL     = "final";
	public static final String NPC_TYPE_RESISTANT = "resistant";
	
	public NPCType (Main m, int id){
		super(m, 0, 0, null);
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String,Object> getArgs() {
		return args;
	}

	public void setArgs(Map<String,Object> args) {
		this.args = args;
	}
	
	public void addArg(String s, Object o){
		args.put(s, o);
	}
	
	public Object getArg(String s){
		return args.get(s);
	}

	public int getPerWave() {
		return perWave;
	}

	public void setPerWave(int perWave) {
		this.perWave = perWave;
	}

	public int getBounty() {
		return bounty;
	}

	public void setBounty(int bounty) {
		this.bounty = bounty;
	}

	public int getBetweenSpawns() {
		return betweenSpawns;
	}

	public void setBetweenSpawns(int betweenSpawns) {
		this.betweenSpawns = betweenSpawns;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}