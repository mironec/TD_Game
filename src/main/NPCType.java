package main;

public class NPCType extends NPC{

	private int id;
	private NPCType previous;
	private NPCType next;
	private String type;
	private Object[] args;
	private int maxArgs=0;
	
	public NPCType (Main m, int id){
		super(m, 0, 0, null);
		this.id = id;
		this.args = new Object[2];
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

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public int getMaxArgs() {
		return maxArgs;
	}

	public void setMaxArgs(int maxArgs) {
		this.maxArgs = maxArgs;
	}
	
	public void newArg(Object o){
		args[maxArgs]=o;
		maxArgs++;
	}
}