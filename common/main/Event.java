package main;

public abstract class Event {

	private int time; 		//in ms
	private int delay;		//in ms
	private int repeat;

	private Main m;
	
	public Event (Main m, int delay, int repeat) {
		this.m = m;
		this.delay = delay;
		this.time = delay;
		this.repeat = repeat;
	}
	
	public void logic (int delta) {
		time-=delta;
		
		if(time<=0){
			run(-time); repeat--;
			if(repeat != 0){time = delay+time;}
			else{m.destroyEvent(this);}
		}
	}
	
	public abstract void run (int delta);
	
	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getRepeat() {
		return repeat;
	}

	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}
}
