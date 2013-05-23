package buff;

import java.util.HashMap;
import java.util.Map;

import main.Main;
import npc.NPC;

public class BuffType extends Buff {

	private String type;
	private int id;
	@SuppressWarnings("unused")
	private Main m;
	private int duration;
	private int stackingType;
	private BuffType previous;
	private BuffType next;
	private Map<String,Object> args = new HashMap<String, Object>();
	
	public static final String BUFF_SLOW = "buffSlow";
	
	@SuppressWarnings("rawtypes")
	public static final Map<String, Class> types = new HashMap<String, Class>();
	static{
		types.put(BUFF_SLOW, BuffSlow.class);
	}
	
	public BuffType(Main m, int id) {
		super(0, null, 0, null);
		this.m = m;
		this.id = id;
	}

	public static void newBuff(String type, int dur, Buffable target, int stackingType, BuffType buffType, Map<String, Object> args){
		if(type.equals(BUFF_SLOW)){
			new BuffSlow(dur, (NPC) target, stackingType, buffType, (Double) args.get("slow"));
		}
	}
	
	public static void newBuffFromBuffType(BuffType b, Buffable target){
		newBuff(b.getType(), b.getDuration(), target, b.getStackingType(), b, b.getArgs());
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	

	public int getDuration() {
		return duration;
	}
	

	public void setDuration(int duration) {
		this.duration = duration;
	}
	

	public int getStackingType() {
		return stackingType;
	}
	

	public void setStackingType(int stackingType) {
		this.stackingType = stackingType;
	}

	
	public BuffType getPrevious() {
		return previous;
	}

	
	public void setPrevious(BuffType previous) {
		this.previous = previous;
	}

	public BuffType getNext() {
		return next;
	}

	public void setNext(BuffType next) {
		this.next = next;
	}

}