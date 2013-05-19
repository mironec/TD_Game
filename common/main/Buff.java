package main;

import java.util.HashMap;
import java.util.Map;

public class Buff {
	
	private int timeLeft;
	private int duration;
	private Buffable owner;
	private boolean over = false;
	
	public Buff(int static final String BUFF_SLOW = "buffSlow";
	
	@SuppressWarnings("rawtypes")
	public static final Map<String, Class> types = new HashMap<String, Class>();
	static{
		types.put(BUFF_SLOW, BuffSlow.class);
	}lic Buff(int dur, Buffable b){
		this.timeLeft = dur;
		this.duration = dur;
		this.owner = b;
		b.addBuff(this);
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

}
