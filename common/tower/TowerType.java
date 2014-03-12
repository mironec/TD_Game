package tower;

import java.util.HashMap;
import java.util.Map;

import main.Main;

public class TowerType extends Tower{

	private int id;
	private TowerType base;
	private int cost;
	private String type;
	private Map<String,Object> args = new HashMap<String, Object>();
	private String description = "";
	
	public static final String TOWER_TYPE_MULTI_TARGET	= "multiAttack";
	public static final String TOWER_TYPE_SIEGE			= "siege";
	public static final String TOWER_TYPE_DEBUFFER		= "debuffer";
	
	public TowerType(Main m, int id) {
		super(m, 0, 0, null);
		this.id = id;
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
