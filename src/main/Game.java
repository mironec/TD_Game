package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

public class Game {
	
	private int tileWidth = 25;
	private Image tiles[];
	private int maxTiles = 1;
	private Image black;
	private Image minimap;
	private Graphics minimapG;
	private Map currentMap;
	private static final String passable = "10";
	
	private int mapGraphicWidth;
	private int mapGraphicHeight;
	
	private double offsetXF = 0.0F, offsetYF = 0.0F;
	private int maxOffsetX, maxOffsetY;
	
	private Main m;
	
	private int panelWidth = 150;
	private int panelHeight = 200;
	private int marginMinimap = 5;
	
	private NPCType npcTypes[];
	private TowerType towerTypes[];
	private Tower towers[];
	private int towersCount;
	private Projectile lastProjectile;
	private NPC lastNPC;
	
	public Game (Main m) {
		tiles = new Image[maxTiles+1];
		black = m.createImage(tileWidth,tileWidth);
		Graphics g = black.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, tileWidth, tileWidth);
		maxOffsetX=1;
		maxOffsetY=1;
		this.m=m;
		currentMap = new Map(m);
	}
	
	public void init(){
		setTowersCount(0);
		
		npcTypes = new NPCType[1];
		npcTypes[0] = new NPCType(m);
		
		Event e = new Event(m,1000,10){
			public void run() {
				setNewNPC(new NPC(m,0,0,npcTypes[0]));
				getLastNPC().issueMoveCommand(19,19);
				getLastNPC().setHealth(5);
				getLastNPC().setMovementSpeed(1);
			}
		};
		m.setNewEvent(e);
		
		towerTypes = new TowerType[1];
		towerTypes[0] = new TowerType(m);
		towerTypes[0].setAttackSpeed(5);
		towerTypes[0].setDamage(1);
		towerTypes[0].setRange(2000);
		towerTypes[0].setProjectileSpeed(3);
		towers = new Tower[2];
		towers[0] = new Tower(m,towerTypes[0],0,0);
		towers[0].setX(50);
		towers[0].setY(50);
		setTowersCount(1);
		
		setLastProjectile(null);
		
		loadMapTiles();
		loadNpcImages();
		loadTowerImages();
	}
	
	public boolean loadMap (String mapname, int method) {
		boolean done = true;
		
		try{
			currentMap = new Map(m);
			currentMap.loadMap(mapname, method);
		} catch (Exception e){System.out.println("PROBLEEEEEEEEEEEM!"); done=false;}
		
		return done;
	}
	
	public boolean loadMap () {
		boolean done = true;
		
		try{
			currentMap = new Map(m);
			currentMap.loadMap();
		} catch (Exception e){System.out.println("PROBLEEEEEEEEEEEM!"); done=false;}
		
		return done;
	}
	
	public void loadTowerImages(){
		for(int x=0;x<m.getGame().getTowerTypes().length;x++){
			try {
				URL url = new URL(m.getCodeBase() + "towers/" + x + ".png");
				URL url2 = new URL(m.getCodeBase() + "projectiles/" + x + ".png");
				towerTypes[x].setImage(ImageIO.read(url));
				towerTypes[x].setProjectileImage(ImageIO.read(url2));
			}
			catch (MalformedURLException e) {e.printStackTrace();} 
			catch (IOException e) {System.out.println("An Image couldn't be found.");}
		}
	}
	
	public void loadNpcImages(){
		for(int x=0;x<m.getGame().getNpcTypes().length;x++){
			try {
				URL url = new URL(m.getCodeBase() + "npcs/" + x + ".png");
				npcTypes[x].setImage(ImageIO.read(url));
			}
			catch (MalformedURLException e) {e.printStackTrace();} 
			catch (IOException e) {System.out.println("An Image couldn't be found.");}
		}
	}
	
	public void logic(int delta){
		logicNPCs(delta);
		logicTowers(delta);
		logicProjectiles(delta);
	}
	
	public void logicNPCs(int delta){
		if(getLastNPC()!=null){
		for(NPC npc = getLastNPC();npc!=null;npc=npc.getPrevious()){
			if(npc.getCommand()==NPC.COMMAND_MOVE){
				npc.goToTarget(delta);
			}
		}
		}
	}
	
	public void logicTowers(int delta){
		for(int loop=0;loop<getTowersCount();loop++){
			Tower tower = getTowers()[loop];
			if(tower.isNpcInRange()){
				if(tower.getAttackSpeed()>0&&
				   tower.getDamage()>0&&
				   tower.getRange()>0&&
				   tower.getAAcd()==0){
					tower.attack();
				}
				if(tower.getAAcd()>0){
					tower.setAAcd( (tower.getAAcd()-delta/1000D)>0 ? (tower.getAAcd()-delta/1000D) : 0 );
				}
			}
		}
	}
	
	public void logicProjectiles(int delta){
		if(getLastProjectile()!=null){
		for(Projectile p = getLastProjectile();p!=null;p=p.getPrevious()){
			p.move(delta);
		}
		}
	}
	
	public boolean readMap () {
		boolean done = true;
		
	    try{
	    	mapGraphicWidth = currentMap.getWidth()*tileWidth;
			mapGraphicHeight = currentMap.getHeight()*tileWidth;
	    	
			maxOffsetX = mapGraphicWidth-m.width+panelWidth;
			maxOffsetY = mapGraphicHeight-m.height+panelHeight;
			if(maxOffsetX<=0){maxOffsetX=0;}
			if(maxOffsetY<=0){maxOffsetY=0;}
			
			minimap = m.createImage(mapGraphicWidth, mapGraphicHeight);
			minimapG = minimap.getGraphics();
	    } catch (Exception e){done = false;}
		
		return done;
	}
	
	public void loadMapTiles(){
		for(int x=0;x<tiles.length;x++){
			try {
				URL url = new URL(m.getCodeBase() + "tiles/" + x + ".png");
				tiles[x]=ImageIO.read(url);
			}
			catch (MalformedURLException e) {e.printStackTrace();} 
			catch (IOException e) {e.printStackTrace();}
		}
	}
	
	public int getTile(int x, int y){
		if(y>=0&&x>=0&&y<currentMap.getHeight()&&x<currentMap.getWidth()){
			return currentMap.getData()[y*currentMap.getWidth()+x+4];
		}
		else{
			return -1;
		}
	}
	
	public void render(){
		renderMap();
		renderObjects();
		renderOverlay();
		renderMiniMap();
	}
	
	public void renderObjects(){
		Graphics g = m.backbufferG;
		if(getLastNPC()!=null){
		for(NPC npc = getLastNPC();npc!=null;npc=npc.getPrevious()){
			g.drawImage(npc.getImage(),(int)(npc.getX()-offsetXF),(int)(npc.getY()-offsetYF), m);
		}
		}
		for(int loop=0;loop<getTowersCount();loop++){
			Tower tower = getTowers()[loop];
			g.drawImage(tower.getImage(),(int)(tower.getX()-offsetXF),(int)(tower.getY()-offsetYF),m);
		}
		if(getLastProjectile()!=null){
		for(Projectile p = getLastProjectile();p!=null;p=p.getPrevious()){
			g.drawImage(p.getImage(),(int)(p.getX()-offsetXF),(int)(p.getY()-offsetYF),m);
		}
		}
	}
	
	public void renderMap(){
		int offsetX=(int)offsetXF;
		int offsetY=(int)offsetYF;
		
		Graphics g = m.backbufferG;
		
		g.setColor(Color.white);
		g.fillRect(0, 0, m.width, m.height);
		
		if(currentMap.getImage() != null){
			g.drawImage(currentMap.getImage(), 0-offsetX, 0-offsetY, m);
		}
		else{
			int offsetXTiles=(int)Math.floor(offsetX/tileWidth);
			int offsetYTiles=(int)Math.floor(offsetY/tileWidth);
			int offsetXMini=offsetX - offsetXTiles*tileWidth;
			int offsetYMini=offsetY - offsetYTiles*tileWidth;
			
			int maxY = (int)Math.ceil(m.height/tileWidth)+offsetYTiles;
			int maxX = (int)Math.ceil(m.width/tileWidth)+offsetXTiles;
			
			for(int y=offsetY<0?(offsetYTiles-1):(offsetYTiles);y<((offsetY>0)?(maxY+1):(maxY));y++){
			for(int x=offsetX<0?(offsetXTiles-1):(offsetXTiles);x<((offsetX>0)?(maxX+1):(maxX));x++){
				int index = y*currentMap.getWidth()+x+4;
				if(y<0||x<0||x>=currentMap.getWidth()||y>=currentMap.getHeight()){g.drawImage(black, (x-offsetXTiles)*tileWidth-offsetXMini, (y-offsetYTiles)*tileWidth-offsetYMini, m);}
				else{g.drawImage(tiles[currentMap.getData()[index]], (x-offsetXTiles)*tileWidth-offsetXMini, (y-offsetYTiles)*tileWidth-offsetYMini, m);}
			}
			}
		}
	}
	
	public void renderOverlay(){
		Graphics g = m.backbufferG;
		
		g.setColor(Color.darkGray);
		g.fillRect(m.width-panelWidth, 0, panelWidth, m.height);
		g.fillRect(0, m.height-panelHeight, m.width, panelHeight);
	}
	
	public void renderMiniMap(){
		Graphics g = minimapG;
		
		for(int y=0;y<currentMap.getHeight();y++){
		for(int x=0;x<currentMap.getWidth();x++){
			int index = y*currentMap.getWidth()+x+4;
			g.drawImage(tiles[currentMap.getData()[index]], x*tileWidth, y*tileWidth, m);
		}
		}
		
		if(getLastNPC()!=null){
		for(NPC npc = getLastNPC();npc!=null;npc=npc.getPrevious()){
			g.drawImage(npc.getImage(),(int)(npc.getX()),(int)(npc.getY()),m);
		}
		}
		for(int loop=0;loop<getTowersCount();loop++){
			Tower tower = getTowers()[loop];
			g.drawImage(tower.getImage(),(int)(tower.getX()),(int)(tower.getY()),m);
		}
		
		m.backbufferG.drawImage(minimap, m.width-panelWidth+marginMinimap, marginMinimap, m.width-marginMinimap, panelWidth-marginMinimap,
				0, 0, mapGraphicWidth, mapGraphicHeight, m);
		m.backbufferG.setColor(Color.white);
		int minimapWidth = panelWidth-marginMinimap*2;
		double modifierX = (double)minimapWidth/mapGraphicWidth;
		double modifierY = (double)minimapWidth/mapGraphicHeight;
		m.backbufferG.drawRect(m.width - panelWidth + marginMinimap + (int)(offsetXF*modifierX), marginMinimap + (int)(offsetYF*modifierY),(int)((double)(m.width-panelWidth)*modifierX),(int)((double)(m.height-panelHeight)*modifierY));
	}
	
	public String printMap(int x, int y){
		char[] map = new char[x*y+4];
		map[0]=(char)((int)Math.floor(x/256));
		map[1]=(char)(x % 256);
		map[2]=(char)((int)Math.floor(y/256));
		map[3]=(char)(y % 256);
		for(int loop=0;loop<x*y;loop++){
			map[loop+4]=(char)((int)(Math.random()*2D));
		}
		
		return String.valueOf(map);
	}
	
	public void handleInput(int delta){
		//1 - Left Click
		//2 - Middle Click
		//3 - Right Click
		if(m.mouseDown[1]){
			if(m.mouseStart.x>=m.width-panelWidth+marginMinimap&&
				m.mouseStart.x<=m.width-marginMinimap&&
				m.mouseStart.y>=marginMinimap&&
				m.mouseStart.y<=panelWidth-marginMinimap)
			{
				offsetXF = (m.mousePoint.x-m.width+panelWidth-marginMinimap //X relative to the minimap
						-0.5f*(m.width-panelWidth)/(double)mapGraphicWidth*(panelWidth-marginMinimap*2)) //Drag the center of the minimap square
						*(m.width/(double)(panelWidth-marginMinimap*2)); //How many times bigger is the main screen than the minimap
				if(offsetXF>=maxOffsetX){offsetXF=maxOffsetX;}
				if(offsetXF<=0){offsetXF=0;}
				
				offsetYF = (m.mousePoint.y-marginMinimap //Y relative to the minimap
						-0.5f*(m.height-panelHeight)/(double)mapGraphicHeight*(panelWidth-marginMinimap*2)) //Drag the center of the minimap square
						*(m.height/(double)(panelWidth-marginMinimap*2)); //How many times bigger is the main screen than the minimap
				if(offsetYF>=maxOffsetY){offsetYF=maxOffsetY;}
				if(offsetYF<=0){offsetYF=0;}
			}
		}
		
		if(m.keyDown[KeyEvent.VK_LEFT]||m.keyDown[KeyEvent.VK_A]){
			offsetXF-=delta*m.keySensitivity;
			if(offsetXF<=0.0f){offsetXF=0.0f;}
		}
		if(m.keyDown[KeyEvent.VK_RIGHT]||m.keyDown[KeyEvent.VK_D]){
			offsetXF+=delta*m.keySensitivity;
			if(offsetXF>=maxOffsetX){offsetXF=maxOffsetX;}
		}
		if(m.keyDown[KeyEvent.VK_UP]||m.keyDown[KeyEvent.VK_W]){
			offsetYF-=delta*m.keySensitivity;
			if(offsetYF<=0.0f){offsetYF=0.0f;}
		}
		if(m.keyDown[KeyEvent.VK_DOWN]||m.keyDown[KeyEvent.VK_S]){
			offsetYF+=delta*m.keySensitivity;
			if(offsetYF>=maxOffsetY){offsetYF=maxOffsetY;}
		}
	}
	
	public static boolean isPassable(int tileid){
		if(tileid>=0&&tileid<=passable.length()){
		if(passable.toCharArray()[tileid]=='1'){
			return true;
		}
		else{return false;}
		}
		else{return false;}
	}
	
	public int getTileWidth(){
		return tileWidth;
	}
	
	public void setTileWidth(int tileWidth){
		this.tileWidth = tileWidth;
	}
	
	public NPCType[] getNpcTypes() {
		return npcTypes;
	}

	public void setNpcTypes(NPCType[] npcTypes) {
		this.npcTypes = npcTypes;
	}

/*	public NPC[] getNpcs() {
		return npcs;
	}

	public void setNpcs(NPC[] npcs) {
		this.npcs = npcs;
	}*/

	public TowerType[] getTowerTypes() {
		return towerTypes;
	}

	public void setTowerTypes(TowerType towerTypes[]) {
		this.towerTypes = towerTypes;
	}

	public Tower[] getTowers() {
		return towers;
	}

	public void setTowers(Tower towers[]) {
		this.towers = towers;
	}

/*	public int getNpcsCount() {
		return npcsCount;
	}

	public void setNpcsCount(int npcsCount) {
		this.npcsCount = npcsCount;
	}*/

	public int getTowersCount() {
		return towersCount;
	}

	public void setTowersCount(int towersCount) {
		this.towersCount = towersCount;
	}

	public Projectile getLastProjectile() {
		return lastProjectile;
	}

	public void setLastProjectile(Projectile lastProjectile) {
		this.lastProjectile = lastProjectile;
	}

	public void setNewProjectile(Projectile p){
		if(getLastProjectile()==null){
			setLastProjectile( p );
		}
		else{
			getLastProjectile().setNext(p);
			p.setPrevious(getLastProjectile());
			setLastProjectile(p);
		}
	}
	
	public void setNewNPC(NPC npc){
		if(getLastNPC()==null){
			setLastNPC( npc );
		}
		else{
			getLastNPC().setNext(npc);
			npc.setPrevious(getLastNPC());
			setLastNPC(npc);
		}
		
		/*getNpcs()[getNpcsCount()]=npc;
		setNpcsCount(getNpcsCount()+1);*/
	}
	
	public void destroyProjectile(Projectile p){
		if(getLastProjectile().equals(p)){
			setLastProjectile(p.getPrevious());
		}
		if(p.getPrevious()!=null){p.getPrevious().setNext(p.getNext());}
		if(p.getNext()!=null){p.getNext().setPrevious(p.getPrevious());}
	}
	
	public void destroyNPC(NPC npc){
		if(getLastProjectile()!=null){
		for(Projectile p = getLastProjectile();p!=null;p=p.getPrevious()){
			if(p.getTarget().equals(npc)){destroyProjectile(p);}
		}
		}
		
		if(getLastNPC().equals(npc)){
			setLastNPC(npc.getPrevious());
		}
		if(npc.getPrevious()!=null){npc.getPrevious().setNext(npc.getNext());}
		if(npc.getNext()!=null){npc.getNext().setPrevious(npc.getPrevious());}
	}

	public NPC getLastNPC() {
		return lastNPC;
	}

	public void setLastNPC(NPC lastNPC) {
		this.lastNPC = lastNPC;
	}
}
