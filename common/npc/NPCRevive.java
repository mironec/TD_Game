package npc;

import graphics.Animation;
import graphics.Sprite;

import java.awt.image.BufferedImage;

import main.Game;
import main.Main;

public class NPCRevive extends NPC{
	
	private int reviveTimer;
	private BufferedImage animationRevive;
	private int animationReviveDuration;
	private boolean died = false;
	private boolean dead = false;
	
	

	public NPCRevive(Main m, double x, double y, NPCType NPCType, int reviveTimer, BufferedImage animationRevive, int animationReviveDuration) {
		super(m, x, y, NPCType);
		this.reviveTimer = reviveTimer;
		this.animationRevive = animationRevive;
		this.animationReviveDuration = animationReviveDuration;
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
	
	public void drawLogic (int delta) {
		int duration = getAnimationStandDuration();
		BufferedImage animation = getAnimationStand();
		
		if(getAnimation() == ANIMATION_WALK){
			duration = getAnimationWalkDuration();
			animation = getAnimationWalk();
		}
		
		if(getAnimation() == 3){
			duration = animationReviveDuration;
			animation = animationRevive;
		}
		
		setAnimationTime( (getAnimationTime()+delta) % duration );
		int phase = (int) ( (double)getAnimationTime() / (double)duration * (double)Animation.getImagePhases(animation) );
		if(getSprite()==null){
			Sprite s = new Sprite(m, (int)getX(), (int)getY(), Game.rotate(Animation.getImagePhase(animation,phase,m),getOrientation()*90), this, false);
			m.getGame().setNewSprite(s);
			setSprite(s);
		}
		else{
			if((getSprite().getX()==getX()&&
			   getSprite().getY()==getY()&&
			   getSprite().getImage()==Animation.getImagePhase(animation, phase, m)) )
			{}
			else{
				m.getGame().destroySprite(getSprite());
				setSprite(new Sprite(m, (int)getX(), (int)getY(), Game.rotate(Animation.getImagePhase(animation,phase,m),getOrientation()*90), this, false));
				m.getGame().setNewSprite(getSprite());
			}
		}
	}
	
	public void die(){
		if(died){
			destroy();
			m.getGame().setMoney(m.getGame().getMoney()+getNPCType().getBounty());
		}
		else{
			setUntargetable(true);
			setHealth(getMaxHealth());
			dead = true;
			died = true;
			setCommand(COMMAND_NOTHING);
			setAnimation(3);
		}
	}

	public int getReviveTimer() {
		return reviveTimer;
	}

	public void setReviveTimer(int reviveTimer) {
		this.reviveTimer = reviveTimer;
	}

	public BufferedImage getAnimationRevive() {
		return animationRevive;
	}

	public void setAnimationRevive(BufferedImage animationRevive) {
		this.animationRevive = animationRevive;
	}

	public int getAnimationReviveDuration() {
		return animationReviveDuration;
	}

	public void setAnimationReviveDuration(int animationReviveDuration) {
		this.animationReviveDuration = animationReviveDuration;
	}

	public boolean isDied() {
		return died;
	}

	public void setDied(boolean died) {
		this.died = died;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}
	
	

}
