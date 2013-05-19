package main;

import java.util.HashMap;
import java.util.Map;

public class NPCType extends NPC{

	private int id;
	private NPCType previous;
	private NPCType next;
	private String type;
	private Map<String,Object> args = new HashMap<String, Object>();
	private int perWave;
	private int bounty;
	
	public static final String NPC_TYPE_REVIVE = "revive";
	public static final String NPC_TYPE_FINAL = "final";
	
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

	public NPCType getPrevious() {
		return previous;
	}

	public void setPrevious(NPCType previous) {
		this.previous = previous;
	}

	public NPCType getNext() {
		return next;
	}

	public void setNext(NPCType next) {
		this.next = next;
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
	
}