package main;

public class TowerType extends Tower{

	private int id;
	private TowerType base;
	private TowerType previous;
	private TowerType next;
	private int cost;
	private String type;
	private Object[] args;
	private int maxArgs=0;
	private String description="";
	
	public TowerType(Main m, int id) {
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

	public TowerType getBase() {
		return base;
	}

	public void setBase(TowerType base) {
		this.base = base;
	}

	public TowerType getPrevious() {
		return previous;
	}

	public void setPrevious(TowerType previous) {
		this.previous = previous;
	}

	public TowerType getNext() {
		return next;
	}

	public void setNext(TowerType next) {
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
	
	public void newArg(Object o){
		args[maxArgs]=o;
		maxArgs++;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
