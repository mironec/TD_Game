package buff;

import java.util.ArrayList;

public class Buff {
	
	private int timeLeft;
	private int duration;
	private int stackingType;
	private BuffType buffType;
	private Buffable owner;
	private boolean over = false;
	public static final int STACKING_REFRESH = 0;	//The buff refreshes with every application
	public static final int STACKING_DURATION = 1;	//The buff adds to its duration with every application
	public static final int STACKING_INTENSITY = 2;	//The buff applies without any restrictions
	
	public Buff(int dur, Buffable b, int stackingType, BuffType buffType){
		this.timeLeft = dur;
		this.duration = dur;
		this.stackingType = stackingType;
		this.buffType = buffType;
		this.owner = b;
		if(b!=null){
			if(stackingType==STACKING_REFRESH){
				ArrayList<Buff> destroy = new ArrayList<Buff>();
				for(Buff buff : b.getBuffs()){
					if(buff.getBuffType().getGroupId() == getBuffType().getGroupId()){
						destroy.add(buff);
					}
				}
				for(Buff buff : destroy){
					buff.destroy();
				}
			}
			if(stackingType==STACKING_DURATION){
				ArrayList<Buff> destroy = new ArrayList<Buff>();
				int bonusDur = 0;
				for(Buff buff : b.getBuffs()){
					if(buff.getBuffType().getGroupId() == getBuffType().getGroupId()){
						destroy.add(buff);
						bonusDur+=buff.getTimeLeft();
					}
				}
				for(Buff buff : destroy){
					buff.destroy();
				}
				this.duration += bonusDur;
				this.timeLeft  = duration;
			}
			b.addBuff(this);
		}
	}
	
	public void onCreate(){}
	public void onDestroy(){}
	
	public void logic (int delta){
		additionalLogic(delta);
		
		if(timeLeft==duration){onCreate();}
		timeLeft -= delta;
		if(timeLeft<=0){
			destroy();
		}
	}
	
	public void destroy () {
		owner.removeBuff(this);
		onDestroy();
	}
	
	public void additionalLogic(int delta){}
	
	public int getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(int timeLeft) {
		this.timeLeft = timeLeft;
	}

	public Buffable getOwner() {
		return owner;
	}

	public void setOwner(Buffable owner) {
		this.owner = owner;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public boolean isOver() {
		return over;
	}

	public void setOver(boolean over) {
		this.over = over;
	}

	public int getStackingType() {
		return stackingType;
	}

	public void setStackingType(int stackingType) {
		this.stackingType = stackingType;
	}

	public BuffType getBuffType() {
		return buffType;
	}

	public void setBuffType(BuffType buffType) {
		this.buffType = buffType;
	}

}
