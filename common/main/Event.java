package main;

public abstract class Event {

	private Event previous;
	private Event next;
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

	public Event getPrevious() {
		return previous;
	}

	public void setPrevious(Event previous) {
		this.previous = previous;
	}

	public Event getNext() {
		return next;
	}

	public void setNext(Event next) {
		this.next = next;
	}
	
	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
}
