package buff;

import java.util.ArrayList;

public interface Buffable {
	
	public void removeBuff(Buff b);
	public void addBuff(Buff b);
	public ArrayList<Buff> getBuffs();
	public void setBuffs(ArrayList<Buff> buffs);
	
}
