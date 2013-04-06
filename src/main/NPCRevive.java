package main;

public class NPCRevive extends NPC{
	
	int reviveTimer;
	boolean died = false;
	boolean dead = false;
	
	

	public NPCRevive(Main m, double x, double y, NPCType NPCType, int reviveTimer) {
		super(m, x, y, NPCType);
		this.reviveTimer = reviveTimer;
	}
	
	public void additionalLogic (int delta) {
		if(dead){
			setHealth(getMaxHealth());
			reviveTimer-=delta;
			setCommand(COMMAND_NOTHING);
			if(reviveTimer<=0){
				dead = false;
				setUntargetable(false);
				setCommand(COMMAND_MOVE);
			}
		}
	}
	
	public void die(){
		if(died){
			m.getGame().destroySprite(getSprite());
			m.getGame().setNewAnimation(new Animation(m, (int)getX(), (int)getY(), getAnimationDeath(), this, false, getAnimationDeathDuration()).setOrientation(getOrientation()*90));
			m.getGame().destroyNPC(this);
		}
		else{
			setUntargetable(true);
			setHealth(getMaxHealth());
			dead = true;
			died = true;
			setCommand(COMMAND_NOTHING);
		}
	}

}
