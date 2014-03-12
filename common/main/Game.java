package main;

import graphics.Animation;
import graphics.Sprite;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import buff.BuffType;

import npc.NPC;
import npc.NPCFinal;
import npc.NPCResistant;
import npc.NPCRevive;
import npc.NPCType;

import tower.Projectile;
import tower.Tower;
import tower.TowerDebuffer;
import tower.TowerMultiAttack;
import tower.TowerSiege;
import tower.TowerType;

public class Game {
	
	private int tileWidth = 40;
	private BufferedImage black;
	private BufferedImage minimap;
	private Graphics minimapG;
	private TDMap currentMap;
	private static final String passable = "01110";
	private static final String buildable = "10000";
	
	private static final byte TILE_GRASS = 0;
	private static final byte TILE_DIRT = 1;
	private static final byte TILE_SPAWN = 2;
	private static final byte TILE_GOAL = 3;
	private static final byte TILE_VOID = 4;
	
	private int mapGraphicWidth;
	private int mapGraphicHeight;
	
	private double offsetXF = 0.0F, offsetYF = 0.0F;
	private int maxOffsetX, maxOffsetY;
	private int minOffsetX, minOffsetY;
	
	private Main m;
	
	private int panelWidth = 150;
	private int panelHeight = 200;
	private int marginMinimap = 5;
	
	private int numberNPC = 0;
	
	private ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
	private ArrayList<NPC> npcs = new ArrayList<NPC>();
	private ArrayList<Sprite> sprites = new ArrayList<Sprite>();
	private ArrayList<Tower> towers = new ArrayList<Tower>();
	private ArrayList<TowerType> towerTypes = new ArrayList<TowerType>();
	private ArrayList<NPCType> NPCTypes = new ArrayList<NPCType>();
	private ArrayList<BuffType> buffTypes = new ArrayList<BuffType>();
	private ArrayList<Animation> animations = new ArrayList<Animation>();
	private ArrayList<Button> buttons = new ArrayList<Button>();
	
	private boolean towerSelected;
	private Sprite towerSelectedSprite;
	private int towerSelectedX, towerSelectedY;
	private BufferedImage towerSelectedImage;
	private TowerType towerSelectedTowerType;
	
	private int money = 15*2;
	private int lives = 40;
	private Sprite tooltip;
	private String status = "";
	private String additionalStatus = "";
	private int finalsSpawned=0;
	private int finalsKilled=0;
	private Event nextWave;
	private Event currentWave;
	private BufferedImage imageSell;
	public static final String RES_DIR = "/res/";
	public static final String BUTTON_SELL_DESC = "<c>FontSize:16<c><c>Bold<c>Sell\n<c>FontSize:12<c><c>Plain<c>Sells the tower for 100%\nof invested money";
	
	private int waveId = 1;
	
	public Game (Main m) {
		black = new BufferedImage(tileWidth, tileWidth, BufferedImage.TYPE_INT_ARGB);
		Graphics g = black.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, tileWidth, tileWidth);
		g.dispose();
		maxOffsetX=1;
		maxOffsetY=1;
		minOffsetX=0;
		minOffsetY=0;
		towerSelected = false;
		towerSelectedSprite = null;
		
		this.m=m;
		currentMap = new TDMap(m);
	}
	
	public void init(){
		setVariables();
		loadMap("mapa.txt", TDMap.METHOD_LOAD_FILE);
		readMap();
		
		loadBuffTypes();
		loadTowerTypes();
		loadNPCTypes();
		loadAdditional();
		prepareButtons();
		
		resizeImages();
		
		nextWave = new Event(m,10000,1){
			public void run(int delta){
				spawnWave(findNPCTypeById(getWaveId()).getPerWave(),findNPCTypeById(getWaveId()).getBetweenSpawns(),delta);
			}
		};
		
		m.setNewEvent(nextWave);
		
	}
	
	public void setVariables(){
		setMoney(15);
		setWaveId(1);
		setLives(40);
		setFinalsKilled(0);
		
		setTowerSelected(false);
		
		setProjectiles(new ArrayList<Projectile>());
		setNpcs(new ArrayList<NPC>());
		setSprites(new ArrayList<Sprite>());
		setTowers(new ArrayList<Tower>());
		setTowerTypes(new ArrayList<TowerType>());
		setNPCTypes(new ArrayList<NPCType>());
		setBuffTypes(new ArrayList<BuffType>());
		setAnimations(new ArrayList<Animation>());
		setButtons(new ArrayList<Button>());
		m.setEvents(new ArrayList<Event>());
		nextWave = null;
		currentWave = null;
		
		setStatus("");
		setAdditionalStatus("");
		setFinalsSpawned(0);
	}
	
	private void loadAdditional() {
		try {imageSell = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "additional/sell.png"));}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
	}
	
	public void lastNpcDeath(){
		setWaveId(getWaveId()+1);
		
		if(findNPCTypeById(getWaveId())!=null){
			nextWave = new Event(m,5000,1){
				public void run(int delta){
					spawnWave(findNPCTypeById(getWaveId()).getPerWave(), findNPCTypeById(getWaveId()).getBetweenSpawns(),delta);
				}
			};
		}
		else{
			nextWave = new Event(m,5000,1){
				public void run(int delta){
					setAdditionalStatus("Finals killed: 0");
					setWaveId(-1);
					
					finalWave(delta);
				}
			};
		}
		m.setNewEvent(nextWave);
	}

	public void spawnWave(int number, int delays, int delta){		
		NPC npc = createNPC(findNPCTypeById(getWaveId()));
		npc.setX(findSpawn().x*tileWidth);
		npc.setY(findSpawn().y*tileWidth);
		npc.issueMoveCommand(findGoal().x, findGoal().y);
		npc.logic(delta);
		
		currentWave = new Event(m,delays,number-1){
			public void run(int delta){
				NPC npc = createNPC(findNPCTypeById(getWaveId()));
				npc.setX(findSpawn().x*tileWidth);
				npc.setY(findSpawn().y*tileWidth);
				npc.issueMoveCommand(findGoal().x, findGoal().y);
				npc.logic(delta);
			}
		};
		m.setNewEvent(currentWave);
		currentWave.logic(delta);
	}
	
	public void finalWave(int delta){
		NPC npc = createNPC(findNPCTypeById(-1));
		npc.setX(findSpawn().x*tileWidth);
		npc.setY(findSpawn().y*tileWidth);
		npc.issueMoveCommand(findGoal().x, findGoal().y);
		npc.logic(delta);
		
		currentWave = new Event(m,findNPCTypeById(-1).getBetweenSpawns(),1){
			public void run (int delta){
				finalWave(delta);
			}
		};
		m.setNewEvent(currentWave);
	}
	
	public Point findSpawn(){
		Point p = new Point(0,0);
		
		for(int loop = 4; loop < currentMap.getData().length; loop++){
			if(currentMap.getData()[loop] == TILE_SPAWN){
				p=new Point((loop-4)%currentMap.getWidth(),(loop-4)/currentMap.getHeight());
			}
		}
		
		return p;
	}
	
	public Point findGoal(){
		Point p = new Point(0,0);
		
		for(int loop = 4; loop < currentMap.getData().length; loop++){
			if(currentMap.getData()[loop] == TILE_GOAL){
				p=new Point((loop-4)%currentMap.getWidth(),(loop-4)/currentMap.getHeight());
			}
		}
		
		return p;
	}
	
	public void prepareButtons(){
		int posX = m.width-panelWidth+getMarginMinimap()*2;
		int posY = panelWidth+getMarginMinimap()*5;
		
		for(TowerType t : getTowerTypes()){
			if(t.getBase() == null){									//The Tower is not an upgrade from another Tower
				final int id = t.getId();
				Button b = new Button(m, posX, posY, getPanelWidth()/4, getPanelWidth()/4,
						resize(Animation.getImagePhase(t.getAnimationStand(), 0, m),getPanelWidth()/4,getPanelWidth()/4), this ) {
					public void run() {
						selectTower( findTowerTypeById(id) );
					}
				};
				b.setDes(t.getDescription());
				setNewButton(b);
				
				posX += getPanelWidth()/4 + getMarginMinimap();
				if(posX+getPanelWidth()/4>=m.width-panelWidth+getMarginMinimap()*2 + getPanelWidth()){
					posX = m.width-panelWidth+getMarginMinimap()*2;
					posY += getPanelWidth()/4 + getMarginMinimap();
				}
			}
		}
	}
	
	public void selectTower(TowerType towerType){
		if(isTowerSelected()){
			deselectTower(true);
		}
		if( getMoney() >= towerType.getCost() ){
			setMoney(getMoney()-towerType.getCost());
			setTowerSelected(true);
			setTowerSelectedImage(Animation.getImagePhase(towerType.getAnimationStand(), 0, m));
			setTowerSelectedTowerType(towerType);
		}
		else{
			setStatus("Not enough money!");
			m.setNewEvent(new Event(m, 2000, 1) {
				public void run(int delta) {
					setStatus("","Not enough money!");
				}
			});
		}
	}
	
	public void deselectTower(boolean refund){
		if(refund)
			setMoney(getMoney()+getTowerSelectedTowerType().getCost());
		setTowerSelected(false);
		destroySprite(getTowerSelectedSprite());
		setTowerSelectedImage(null);
	}
	
	public void resizeImages(){
		try {
		BufferedImage img;
		for(NPCType npc : getNPCTypes()){
			img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "npcs/" + npc.getId() + "-death.png"));
			npc.setAnimationDeath( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "npcs/" + npc.getId() + "-stand.png"));
			npc.setAnimationStand( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "npcs/" + npc.getId() + "-walk.png"));
			npc.setAnimationWalk( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			if(npc.getArg("animationRevive")!=null){
				img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "npcs/" + npc.getId() + "-revive.png"));
				npc.addArg( "animationRevive", resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			}
		}
		for(NPC npc : getNpcs()){
			npc.setAnimationDeath(npc.getNPCType().getAnimationDeath());
			npc.setAnimationStand(npc.getNPCType().getAnimationStand());
			npc.setAnimationWalk(npc.getNPCType().getAnimationWalk());
			if(npc instanceof NPCRevive)
				((NPCRevive)npc).setAnimationRevive((BufferedImage)npc.getNPCType().getArg("animationRevive"));
		}
		
		for(TowerType t : getTowerTypes()){
			img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + t.getId() + "-preAttack.png"));
			t.setAnimationPreAttack( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + t.getId() + "-postAttack.png"));
			t.setAnimationPostAttack( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + t.getId() + "-stand.png"));
			t.setAnimationStand( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + t.getId() + "-projectileStand.png"));
			t.setProjectileAnimationStand( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + t.getId() + "-projectileDeath.png"));
			t.setProjectileAnimationDeath( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
		}
		for(Tower t : getTowers()){
			t.setAnimationPreAttack(t.getTowerType().getAnimationPreAttack());
			t.setAnimationPostAttack(t.getTowerType().getAnimationPostAttack());
			t.setAnimationStand(t.getTowerType().getAnimationStand());
		}
		
		img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "additional/sell.png"));
		imageSell = resize(img,getTileWidth(),getTileWidth());
		
		}
		catch (MalformedURLException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	public static BufferedImage resize(BufferedImage img, int newW, int newH) {  
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType()==0?BufferedImage.TYPE_INT_ARGB:img.getType());
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        return dimg;
    }
	
	public static BufferedImage rotate(BufferedImage img, int amnout){
		BufferedImage ret = new BufferedImage(img.getWidth(),img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.rotate(-Math.PI/180.0D*(double)(amnout),img.getWidth()/2,img.getHeight()/2);
		Graphics2D g2d = (Graphics2D) ret.getGraphics();
        g2d.drawImage(img, at, null);
        g2d.dispose();
		return ret;
	}
	
	public boolean loadMap (String mapname, int method) {
		boolean done = true;
		
		try{
			currentMap = new TDMap(m);
			currentMap.loadMap(mapname, method);
		} catch (Exception e){System.out.println("PROBLEEEEEEEEEEEM!"); done=false;}
		
		return done;
	}
	
	public boolean loadMap () {
		boolean done = true;
		try{
			currentMap = new TDMap(m);
			currentMap.loadMap();
		} catch (Exception e){System.out.println("PROBLEEEEEEEEEEEM!"); done=false;}
		
		return done;
	}
	
	public String findValue(String s, String find){
		String val = "";
		find += "=";
		
		if(s.contains(find)){
			String key = s.substring( s.lastIndexOf(find) );
			val = key.substring(find.length(), key.indexOf(";"));
		}
		
		return val;
	}
	
	public void loadBuffTypes(){
		int x = 1;
		try {
			while(true){
				InputStream is = Main.class.getResourceAsStream(RES_DIR + "buffs/" + x + ".cfg");
				if(is==null){break;}
				byte[] b = new byte[Main.class.getResource(RES_DIR + "buffs/" + x + ".cfg").openConnection().getContentLength()];
				is.read(b);
				is.close();
				String s = new String(b);
				BuffType buff = new BuffType(m, x);
				
				buff.setType(findValue(s,"type"));
				buff.setDuration(Integer.parseInt(findValue(s,"duration")));
				buff.setStackingType(findValue(s,"stackingType"));
				buff.setGroupId(Integer.parseInt(findValue(s,"groupId")));
				
				if(buff.getType().equals(BuffType.BUFF_SLOW)){
					buff.addArg("slow", Double.parseDouble(findValue(s,"slow")));
				}
				if(buff.getType().equals(BuffType.BUFF_FIRE)){
					buff.addArg("damage", Double.parseDouble(findValue(s,"damage")));
				}
				
				setNewBuffType(buff);
				x++;
			}
		}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
	}
	
	public void loadTowerTypes(){
		int x = 1;
		try {
			while(true){
				InputStream is = Main.class.getResourceAsStream(RES_DIR + "towers/" + x + ".cfg");
				if(is==null){break;}
				byte[] b = new byte[Main.class.getResource(RES_DIR + "towers/" + x + ".cfg").openConnection().getContentLength()];
				is.read(b);
				is.close();
				String s = new String(b);
				TowerType t = new TowerType(m, x);
				
				t.setType(findValue(s,"type"));
				t.setAttackSpeed( Double.parseDouble(findValue(s,"attackSpeed")) );
				t.setDamage( Double.parseDouble(findValue(s,"damage")) );
				t.setProjectileSpeed( Double.parseDouble(findValue(s,"projectileSpeed")) );
				t.setProjectileAnimationStandDuration( Integer.parseInt(findValue(s, "projectileAnimationStandDuration")) );
				t.setProjectileAnimationDeathDuration( Integer.parseInt(findValue(s, "projectileAnimationDeathDuration")) );
				t.setRange( Double.parseDouble(findValue(s, "range")) );
				t.setAnimationStandDuration( Integer.parseInt(findValue(s, "animationStandDuration")) );
				t.setAnimationPreAttackDuration( Integer.parseInt(findValue(s, "animationPreAttackDuration")) );
				t.setAnimationPostAttackDuration( Integer.parseInt(findValue(s, "animationPostAttackDuration")) );
				t.setBase( findTowerTypeById(Integer.parseInt(findValue(s, "base"))) );
				t.setCost( Integer.parseInt(findValue(s, "cost")) );
				t.setDescription( findValue(s, "description") );
				t.setShootingAir( Boolean.parseBoolean(findValue(s, "shootingAir")) );
				if(t.getType().equals(TowerType.TOWER_TYPE_MULTI_TARGET))
					t.addArg("maxTargets", Integer.parseInt(findValue(s, "maxTargets")));
				if(t.getType().equals(TowerType.TOWER_TYPE_SIEGE))
					t.addArg("splashRadius", Double.parseDouble(findValue(s, "splashRadius")));
				if(t.getType().equals(TowerType.TOWER_TYPE_DEBUFFER)){
					String s2 = findValue(s, "debuffs");
					ArrayList<BuffType> debuffs = new ArrayList<BuffType>();
					for(String s3 : s2.split(":")){
						if(s3.equals("")) continue;
						debuffs.add(findBuffTypeById(Integer.parseInt(s3)));
					}
					t.addArg("debuffs", debuffs);
				}
				////////////////////////////////////////////////////////
				t.setAnimationStand( ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + x + "-stand.png")) );
				t.setAnimationPreAttack( ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + x + "-preAttack.png")) );
				t.setAnimationPostAttack( ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + x + "-postAttack.png")) );
				t.setProjectileAnimationStand( ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + x + "-projectileStand.png")) );
				t.setProjectileAnimationDeath( ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + x + "-projectileDeath.png")) );
				setNewTowerType(t);
				x++;
			}
		}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
	}
	
	public void loadNPCTypes(){
		int x = 1;
		boolean dothis=true;
		try {
			while(dothis){
				InputStream is = Main.class.getResourceAsStream(RES_DIR + "npcs/" + x + ".cfg");
				if(is==null){
					x=-1;
					is = Main.class.getResourceAsStream(RES_DIR + "npcs/" + x + ".cfg");
					dothis=false;
				}
				byte[] b = new byte[Main.class.getResource(RES_DIR + "npcs/" + x + ".cfg").openConnection().getContentLength()];
				is.read(b);
				is.close();
				String s = new String(b);
				NPCType npc = new NPCType(m, x);

				npc.setMaxHealth( Integer.parseInt(findValue(s, "maxHealth")) );
				npc.setMovementSpeed( Double.parseDouble(findValue(s, "movementSpeed")) );
				npc.setAnimationStandDuration( Integer.parseInt(findValue(s, "animationStandDuration")) );
				npc.setAnimationWalkDuration( Integer.parseInt(findValue(s, "animationWalkDuration")) );
				npc.setAnimationDeathDuration( Integer.parseInt(findValue(s, "animationDeathDuration")) );
				npc.setPerWave( Integer.parseInt(findValue(s, "perWave")) );
				npc.setBetweenSpawns( Integer.parseInt(findValue(s, "betweenSpawns")) );
				npc.setBounty( Integer.parseInt(findValue(s, "bounty")) );
				npc.setDescription( findValue(s, "description") );
				npc.setType( findValue(s, "type") );
				npc.setFlying( Boolean.parseBoolean(findValue(s, "flying")) );
				if(npc.getType().equals(NPCType.NPC_TYPE_REVIVE))
					npc.addArg( "reviveTimer", Integer.parseInt(findValue(s, "reviveTimer")) );
				if(npc.getType().equals(NPCType.NPC_TYPE_REVIVE))
					npc.addArg( "animationReviveDuration", Integer.parseInt(findValue(s, "animationReviveDuration")) );
				if(npc.getType().equals(NPCType.NPC_TYPE_FINAL))
					npc.addArg( "maxLife", Integer.parseInt(findValue(s, "maxLife")) );
				if(npc.getType().equals(NPCType.NPC_TYPE_FINAL))
					npc.addArg( "increment", Double.parseDouble(findValue(s, "increment")) );
				if(npc.getType().equals(NPCType.NPC_TYPE_RESISTANT)){
					String s2 = findValue(s, "against");
					Map<Integer, Double> Map = new HashMap<Integer, Double>();
					for(String s3 : s2.split(":")){
						if(s3.equals("")) continue;
						String buff = s3.split(">>")[0];
						String value = s3.split(">>")[1];
						Map.put(Integer.parseInt(buff),Double.parseDouble(value));
					}
					npc.addArg("against", Map);
				}
				////////////////////////////////////////////////////////
				npc.setAnimationStand( ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "npcs/" + x + "-stand.png")) );
				npc.setAnimationWalk( ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "npcs/" + x + "-walk.png")) );
				npc.setAnimationDeath( ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "npcs/" + x + "-death.png")) );
				if(npc.getType().equals(NPCType.NPC_TYPE_REVIVE))
					npc.addArg("animationRevive", ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "npcs/" + x + "-revive.png")));
				setNewNPCType(npc);
				x++;
			}
		}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
	}
	
	public void logic(int delta){
		logicNPCs(delta);
		logicTowers(delta);
		logicProjectiles(delta);
		logicOther(delta);
	}
	
	public boolean isCursorInPlayingField () {
		return m.mousePoint.x > 0 && m.mousePoint.x < m.width-getPanelWidth() && m.mousePoint.y > 0 && m.mousePoint.y < m.height-getPanelHeight();
	}
	
	public void logicOther(int delta){
		if(getLives()<=0){
			m.scoreScreen();
		}
		if( isTowerSelected() && getTowerSelectedImage()!=null){
			if(getTowerSelectedSprite() != null){
				destroySprite(getTowerSelectedSprite());
			}
			BufferedImage img = getTowerSelectedImage();
			if(isCursorInPlayingField()){
				setTowerSelectedX((int)Math.floor( (m.mousePoint.x + offsetXF) / getTileWidth() ));
				setTowerSelectedY((int)Math.floor( (m.mousePoint.y + offsetYF) / getTileWidth() ));
				setTowerSelectedSprite(new Sprite(m, getTowerSelectedX()*getTileWidth()-(int)offsetXF, getTowerSelectedY()*getTileWidth()-(int)offsetYF, img, this, false));
			}
			else{
				setTowerSelectedSprite(new Sprite(m, m.mousePoint.x-img.getWidth()/2, m.mousePoint.y-img.getHeight()/2, img, this, false));
			}
			setNewSprite(getTowerSelectedSprite());
		}
	}
	
	public void logicNPCs(int delta){
		for(NPC npc : getNpcs()){
			npc.logic(delta);
		}
	}
	
	public void logicTowers(int delta){
		for(Tower tower : getTowers()){
			tower.logic(delta);
		}
	}
	
	public void logicProjectiles(int delta){
		for(Projectile p : getProjectiles()){
			p.move(delta);
		}
	}
	
	public boolean readMap () {
		boolean done = true;
		
	    try{
	    	mapGraphicWidth = currentMap.getWidth()*tileWidth;
			mapGraphicHeight = currentMap.getHeight()*tileWidth;
	    	
			maxOffsetX = mapGraphicWidth-m.width+panelWidth;
			maxOffsetY = mapGraphicHeight-m.height+panelHeight;
			if(maxOffsetX<=0){maxOffsetX=minOffsetX=maxOffsetX/2;}
			if(maxOffsetY<=0){maxOffsetY=minOffsetY=maxOffsetY/2;}
			
			offsetXF = minOffsetX;
			offsetYF = minOffsetY;
			
			minimap = new BufferedImage(mapGraphicWidth, mapGraphicHeight, BufferedImage.TYPE_INT_ARGB);
			minimapG = minimap.getGraphics();
	    } catch (Exception e){done = false;}
		
		return done;
	}
	
	public int getTile(int x, int y){
		if(y>=0&&x>=0&&y<currentMap.getHeight()&&x<currentMap.getWidth()){
			return currentMap.getData()[y*currentMap.getWidth()+x+4];
		}
		else{
			return -1;
		}
	}
	
	public void render(int delta){
		renderMap(delta);
		renderObjects(delta);
		renderOverlay(delta);
		renderMiniMap(delta);
		renderAboveAll(delta);
	}
	
	public void renderObjects(int delta){
		Graphics g = m.backbufferG;
		
		for(NPC npc : getNpcs()){
			npc.drawLogic(delta);
		}
		for(Tower tower : getTowers()){
				tower.drawLogic(delta);
		}
		for(Projectile p : getProjectiles()){
				p.drawLogic(delta);
		}
		
		for( Sprite s : getSprites() ){
			if( ! (s.getOwner() instanceof Button) &&
				! (s.getOwner() instanceof Game) &&
				! (s.getOwner() instanceof Projectile)){
					s.draw(g, (int)offsetXF, (int)offsetYF);
			}
		}
		for( Sprite s : getSprites() ){
			if( (s.getOwner() instanceof Projectile) ){
					s.draw(g, (int)offsetXF, (int)offsetYF);
			}
		}
		
		for( Animation a : getAnimations() ){
			a.draw(g, (int)offsetXF, (int)offsetYF, delta);
		}
		
		for(NPC npc : getNpcs()){
			g.setColor(Color.red);
			g.fillRect((int) (npc.getX()-offsetXF),(int) (npc.getY()-offsetYF),getTileWidth(),getTileWidth()/5);
			g.setColor(Color.cyan);
			g.fillRect((int) (npc.getX()-offsetXF),(int) (npc.getY()-offsetYF),(int)((double)npc.getHealth()/npc.getMaxHealth()*getTileWidth()),getTileWidth()/5);
		}
	}
	
	public void renderAboveAll(int delta){
		Graphics g = m.backbufferG;
		
		for( Sprite s : getSprites() ){
			if( s.getOwner() instanceof Game ){
				s.draw(g, 0, 0);
			}
		}
	}
	
	public void renderMap(int delta){
		int offsetX=(int)offsetXF;
		int offsetY=(int)offsetYF;
		
		Graphics g = m.backbufferG;
		
		g.setColor(Color.black);
		g.fillRect(0, 0, m.width, m.height);
		
		if(currentMap.getImage() != null){
			g.drawImage(currentMap.getImage(), -offsetX, -offsetY, m);
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
				else{
					if(currentMap.getData()[index]==TILE_GRASS)
						g.setColor(Color.green);
					if(currentMap.getData()[index]==TILE_DIRT)
						g.setColor(Color.orange);
					if(currentMap.getData()[index]==TILE_SPAWN)
						g.setColor(Color.orange);
					if(currentMap.getData()[index]==TILE_GOAL)
						g.setColor(Color.orange);
					if(currentMap.getData()[index]==TILE_VOID)
						g.setColor(Color.black);
					g.fillRect((x-offsetXTiles)*tileWidth-offsetXMini, (y-offsetYTiles)*tileWidth-offsetYMini, tileWidth, tileWidth);
				}
			}
			}
		}
	}
	
	public void renderOverlay(int delta){
		Graphics g = m.backbufferG;
		
		g.setFont(new Font("arial",Font.PLAIN, 12));
		g.setColor(Color.darkGray);
		g.fillRect(m.width-panelWidth, 0, panelWidth, m.height);
		g.fillRect(0, m.height-panelHeight, m.width, panelHeight);
		
		g.setColor(Color.white);
		g.drawString("Money: " + getMoney(), m.width-panelWidth, panelWidth+getMarginMinimap()*2);
		g.drawString("Lives: " + lives, m.width-panelWidth+75, panelWidth+getMarginMinimap()*2);
		String nextWaveStatus = "";
		if(getWaveId()==-1){nextWaveStatus = "Try to stay alive as long as possible!";}
		else if(nextWave.getRepeat()==0){nextWaveStatus = "Slay all the creatures!";}
		else{nextWaveStatus = nextWave.getTime()/1000 + "s";}
		g.drawString("Next Wave: " + nextWaveStatus, getMarginMinimap()*2, m.height-panelHeight+getMarginMinimap()*2);
		g.drawString(getStatus(), marginMinimap*2, m.height-marginMinimap*4);
		g.drawString(getAdditionalStatus(), marginMinimap*2, m.height-panelHeight+marginMinimap*5);
		
		for( Button b : getButtons() ){
			b.drawLogic();
		}
		
		for( Sprite s : getSprites() ){
			if( s.getOwner() instanceof Button ){
				s.draw(g, 0, 0);
			}
		}
	}
	
	public void renderMiniMap(int delta){
		Graphics g = minimapG;
		
		for(int y=0;y<currentMap.getHeight();y++){
		for(int x=0;x<currentMap.getWidth();x++){
			int index = y*currentMap.getWidth()+x+4;
			if(currentMap.getData()[index]==TILE_GRASS)
				g.setColor(Color.green);
			if(currentMap.getData()[index]==TILE_DIRT)
				g.setColor(Color.orange);
			if(currentMap.getData()[index]==TILE_SPAWN)
				g.setColor(Color.orange);
			if(currentMap.getData()[index]==TILE_GOAL)
				g.setColor(Color.orange);
			if(currentMap.getData()[index]==TILE_VOID)
				g.setColor(Color.black);
			g.fillRect(x*tileWidth, y*tileWidth, tileWidth, tileWidth);
		}
		}
		
		for(NPC npc : getNpcs()){
			g.setColor(Color.red);
			g.fillRect( (int)(npc.getX()), (int)(npc.getY()), getTileWidth(), getTileWidth() );
		}
		for(Tower tower : getTowers()){
			g.setColor(Color.gray);
			g.fillRect( (int)(tower.getX()), (int)(tower.getY()), getTileWidth(), getTileWidth() );
		}
		
		m.backbufferG.drawImage(minimap, m.width-panelWidth+getMarginMinimap(), getMarginMinimap(), m.width-getMarginMinimap(), panelWidth-getMarginMinimap(),
				0, 0, mapGraphicWidth, mapGraphicHeight, m);
		m.backbufferG.setColor(Color.white);
		int minimapWidth = panelWidth-getMarginMinimap()*2;
		double modifierX = (double)minimapWidth/mapGraphicWidth;
		double modifierY = (double)minimapWidth/mapGraphicHeight;
		double x = offsetXF<0?0:offsetXF;
		double y = offsetYF<0?0:offsetYF;
		double width = m.width - panelWidth > mapGraphicWidth ? mapGraphicWidth : m.width - panelWidth;
		double height = m.height - panelHeight > mapGraphicHeight ? mapGraphicHeight : m.height - panelHeight;
		m.backbufferG.drawRect(m.width - panelWidth + getMarginMinimap() + (int)(x*modifierX), getMarginMinimap() + (int)(y*modifierY),(int)((double)width*modifierX),(int)((double)height*modifierY));
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
	
	public void keyTyped(KeyEvent e){
		
	}
	
	public void keyPressed(KeyEvent e) {
		for(Button b : getButtons()){
			b.key(e.getKeyCode());
		}
	}

	public void keyReleased(KeyEvent e) {}
	
	public void mousePressed(MouseEvent e){
		if(e.getButton() == MouseEvent.BUTTON1){
			destroySprite(getTooltip()); setTooltip(null);
			if(isTowerSelected()){
				if(isCursorInPlayingField()){
					if( isBuildable(getTile(getTowerSelectedX(), getTowerSelectedY())) &&
						!isTowerOn(getTowerSelectedX(), getTowerSelectedY()) ){
						deselectTower(false);
						createTower ( getTowerSelectedTowerType() ).setX(getTowerSelectedX()*getTileWidth()).setY(getTowerSelectedY()*getTileWidth());
					}
				}
				else{
					deselectTower(true);
				}
			}
			else{
				if(isCursorInPlayingField()){
					destroyUpgradeButtons();
					for(Tower t : getTowers()){
						if(e.getPoint().x>t.getX()-offsetXF&&
						   e.getPoint().x<t.getX()-offsetXF+getTileWidth()&&
						   e.getPoint().y>t.getY()-offsetYF&&
						   e.getPoint().y<t.getY()-offsetYF+getTileWidth()){
							createUpgradeButtons(t);
							constructTooltip(t.getTowerType().getDescription());
						}
					}
					for(NPC n : getNpcs()){
						if(e.getPoint().x>n.getX()-offsetXF&&
						   e.getPoint().x<n.getX()-offsetXF+getTileWidth()&&
						   e.getPoint().y>n.getY()-offsetYF&&
						   e.getPoint().y<n.getY()-offsetYF+getTileWidth()){
							constructTooltip(n.getNPCType().getDescription());
						}
					}
				}
			}
			
			for(Button b : getButtons()){
				b.click(e.getPoint().x, e.getPoint().y);
			}
		}
	}
	
	public void handleInput(int delta){
		//1 - Left Click
		//2 - Middle Click
		//3 - Right Click
		if(m.mouseDown[1]){
			if(m.mouseStart.x>=m.width-panelWidth+getMarginMinimap()&&
					m.mouseStart.x<=m.width-getMarginMinimap()&&
					m.mouseStart.y>=getMarginMinimap()&&
					m.mouseStart.y<=panelWidth-getMarginMinimap())
				{
					offsetXF = (m.mousePoint.x-m.width+panelWidth-getMarginMinimap()
							-0.5f*(m.width-panelWidth)/(double)mapGraphicWidth*(panelWidth-getMarginMinimap()*2)) //Drag the center of the minimap square
							*(mapGraphicWidth/(double)(panelWidth-getMarginMinimap()*2)); //How many times bigger is the main screen than the minimap
					if(offsetXF>=maxOffsetX){offsetXF=maxOffsetX;}
					if(offsetXF<=minOffsetX){offsetXF=minOffsetX;}
					
					offsetYF = (m.mousePoint.y-getMarginMinimap()
							-0.5f*(m.height-panelHeight)/(double)mapGraphicHeight*(panelWidth-getMarginMinimap()*2)) //Drag the center of the minimap square
							*(mapGraphicHeight/(double)(panelWidth-getMarginMinimap()*2)); //How many times bigger is the main screen than the minimap
					if(offsetYF>=maxOffsetY){offsetYF=maxOffsetY;}
					if(offsetYF<=minOffsetY){offsetYF=minOffsetY;}
				}
		}
		
		else{
			/*if(getTooltip()!=null){
				destroySprite(getTooltip());
				setTooltip(null);
			}*/
			for(Button b : getButtons()){
				if(b.isHere(m.mousePoint.x, m.mousePoint.y)){
					constructTooltip(b.getDes());
					break;
				}
			}
		}
		
		if(m.keyDown[KeyEvent.VK_LEFT]){
			offsetXF-=delta*m.keySensitivity;
			if(offsetXF<=minOffsetX){offsetXF=minOffsetX;}
		}
		if(m.keyDown[KeyEvent.VK_RIGHT]){
			offsetXF+=delta*m.keySensitivity;
			if(offsetXF>=maxOffsetX){offsetXF=maxOffsetX;}
		}
		if(m.keyDown[KeyEvent.VK_UP]){
			offsetYF-=delta*m.keySensitivity;
			if(offsetYF<=minOffsetY){offsetYF=minOffsetY;}
		}
		if(m.keyDown[KeyEvent.VK_DOWN]){
			offsetYF+=delta*m.keySensitivity;
			if(offsetYF>=maxOffsetY){offsetYF=maxOffsetY;}
		}
	}
	private void constructTooltip(String description){
		BufferedImage img = new BufferedImage(getPanelWidth()-marginMinimap*2, getPanelHeight()-marginMinimap*2, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		g.setColor(Color.yellow);
		g.fillRect(0, 0, getPanelWidth()-marginMinimap*2, getPanelWidth()-marginMinimap*2);
		g.setColor(Color.black);
		g.drawRect(0, 0, getPanelWidth()-marginMinimap*2, getPanelWidth()-marginMinimap*2);
		g.setFont(new Font("arial", Font.PLAIN, getPanelWidth()/10));
		int y = 2;
		int x = 2;
		for (String line : description.split("\n")){
			y += g.getFontMetrics().getHeight();
			x = 2;
			for(String command : line.split("<c>")){
				if(command.equals("")) continue;
				if(command.startsWith("FontSize:")){
					g.setFont(new Font("arial", g.getFont().getStyle(), Integer.parseInt(command.substring(command.indexOf(":")+1))));
				}
				else if(command.startsWith("Var:")){
					
					String com = command.replace("Var:", "");
					InputStream is = Main.class.getResourceAsStream(RES_DIR + com.substring(0, com.lastIndexOf("->")));
					byte[] b;
					String s = "";
					try {
						b = new byte[Main.class.getResource(RES_DIR + com.substring(0, com.lastIndexOf("->"))).openConnection().getContentLength()];
						is.read(b);
						is.close();
						s = new String(b);
					} catch (IOException e) {e.printStackTrace();}
					String val = findValue(s, com.substring(com.lastIndexOf("->")+2));
					
					g.drawString(val, x, y);
					x+=g.getFontMetrics().stringWidth(val);
				}
				else if(command.equals("Bold")){
					g.setFont(new Font("arial", Font.BOLD, g.getFont().getSize()));
				}
				else if(command.equals("Plain")){
					g.setFont(new Font("arial", Font.PLAIN, g.getFont().getSize()));
				}
				else if(command.equals("Green")){
					g.setColor(Color.green);
				}
				else if(command.equals("Black")){
					g.setColor(Color.black);
				}
				else if(command.equals("Red")){
					g.setColor(Color.red);
				}
				else{
					g.drawString(command, x, y);
					x+=g.getFontMetrics().stringWidth(command);
				}
			}
		}
		g.dispose();
		
		destroySprite(getTooltip());
		setTooltip(new Sprite(m, m.width-getPanelWidth()+marginMinimap, m.height-getPanelHeight()+marginMinimap, img, this, false));
		setNewSprite(getTooltip());
	}

	private void createUpgradeButtons(Tower t){
		if(t==null){return;}
		ArrayList<TowerType> upgrades = findUpgradesForTower(t.getTowerType());
		int buttonX = panelWidth;
		int buttonY = m.height-panelHeight+marginMinimap*4;
		final Tower t2=t;
		Button sell = new Button(m,buttonX,buttonY,getTileWidth(),getTileWidth(),imageSell,t){
			public void run() {
				t2.sell();
				destroyUpgradeButtons();
			}
		};
		sell.setKeybind(KeyEvent.VK_S);
		sell.setDes(BUTTON_SELL_DESC);
		setNewButton(sell);
		buttonX += getTileWidth()+marginMinimap;
		for(int loop=0;loop<upgrades.size();loop++){
			final TowerType t3=upgrades.get(loop);
			Button b = new Button(m,buttonX,buttonY,getTileWidth(),getTileWidth(),Animation.getImagePhase(upgrades.get(loop).getAnimationStand(),0,m),t){
				public void run() {
					destroyUpgradeButtons();
					createUpgradeButtons(t2.upgradeTo(t3));
				}
			};
			if(upgrades.size()==1){b.setKeybind(KeyEvent.VK_U);}
			b.setDes(t3.getDescription());
			setNewButton(b);
			buttonX += getTileWidth()+marginMinimap;
		}
	}
	
	private void destroyUpgradeButtons(){
		for(Button b : getButtons()){
			if(b.getOwner() instanceof Tower){
				b.destroy();
			}
		}
	}
	
	private ArrayList<TowerType> findUpgradesForTower(TowerType towerType){
		ArrayList<TowerType> ret = new ArrayList<TowerType>();
		
		for(TowerType t : getTowerTypes()){
			if(t.getBase()==towerType){
				ret.add(t);
			}
		}
		
		return ret;
	}
	
	private boolean isTowerOn(int x, int y) {
		for(Tower t : getTowers()){
			if(t.getX() / getTileWidth() == x &&
			   t.getY() / getTileWidth() == y){
				return true;
			}
		}
		return false;
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
	
	public static boolean isBuildable(int tileid){
		if(tileid>=0&&tileid<=buildable.length()){
		if(buildable.toCharArray()[tileid]=='1'){
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
	
	public TowerType findTowerTypeById (int id) {
		for(TowerType t : getTowerTypes()){
			if(t.getId()==id){return t;}
		}
		return null;
	}
	
	public NPCType findNPCTypeById (int id) {
		for(NPCType npc : getNPCTypes()){
			if(npc.getId()==id){return npc;}
		}
		return null;
	}
	
	public BuffType findBuffTypeById (int id) {
		for(BuffType b : getBuffTypes()){
			if(b.getId()==id){return b;}
		}
		return null;
	}

	public int getNumberNPC() {
		return numberNPC;
	}

	public void setNumberNPC(int numberNPC) {
		this.numberNPC = numberNPC;
	}
	
	public Tower createTower(TowerType towerType){
		String type = towerType.getType();
		Tower t;
		if( type.equals(TowerType.TOWER_TYPE_MULTI_TARGET) ){
			TowerMultiAttack to = new TowerMultiAttack(m,0,0,towerType,(Integer) towerType.getArg("maxTargets"));
			setNewTower(to);
			t = to;
		}
		else if( type.equals(TowerType.TOWER_TYPE_SIEGE) ){
			TowerSiege to = new TowerSiege(m,0,0,towerType,(Double) towerType.getArg("splashRadius"));
			setNewTower(to);
			t = to;
		}
		else if( type.equals(TowerType.TOWER_TYPE_DEBUFFER) ){
			@SuppressWarnings("unchecked")
			TowerDebuffer to = new TowerDebuffer(m,0,0,towerType,(ArrayList<BuffType>)towerType.getArg("debuffs"));
			setNewTower(to);
			t = to;
		}
		else{
			Tower to = new Tower(m,0,0,towerType);
			setNewTower(to);
			t = to;
		}
		t.copyFromTower(towerType);
		
		return t;
	}

	@SuppressWarnings("unchecked")
	public NPC createNPC(NPCType npcType){
		String type = npcType.getType();
		NPC npc;
		if( type.equals(NPCType.NPC_TYPE_REVIVE) ){
			NPCRevive npco = new NPCRevive(m,0,0,npcType,
					(Integer) npcType.getArg("reviveTimer"),
					(BufferedImage)npcType.getArg("animationRevive"),
					(Integer)npcType.getArg("animationReviveDuration"));
			setNewNPC(npco);
			npc = npco;
		}
		else if( type.equals(NPCType.NPC_TYPE_FINAL) ){
			NPCFinal npco = new NPCFinal(m,0,0,npcType,getFinalsSpawned());
			setNewNPC(npco);
			npc = npco;
		}
		else if( type.equals(NPCType.NPC_TYPE_RESISTANT) ){
			NPCResistant npco = new NPCResistant(m,0,0,npcType,(java.util.Map<Integer,Double>)npcType.getArg("against"));
			setNewNPC(npco);
			npc = npco;
		}
		else{
			NPC npco = new NPC(m,0,0,npcType);
			setNewNPC(npco);
			npc = npco;
		}
		npc.copyFromNPC(npcType);
		
		return npc;
	}
	
	public int getPanelWidth() {
		return panelWidth;
	}

	public void setPanelWidth(int panelWidth) {
		this.panelWidth = panelWidth;
	}

	public int getPanelHeight() {
		return panelHeight;
	}

	public void setPanelHeight(int panelHeight) {
		this.panelHeight = panelHeight;
	}

	public boolean isTowerSelected() {
		return towerSelected;
	}

	public void setTowerSelected(boolean towerSelected) {
		this.towerSelected = towerSelected;
	}

	public Sprite getTowerSelectedSprite() {
		return towerSelectedSprite;
	}

	public void setTowerSelectedSprite(Sprite towerSelectedSprite) {
		this.towerSelectedSprite = towerSelectedSprite;
	}

	public BufferedImage getTowerSelectedImage() {
		return towerSelectedImage;
	}

	public void setTowerSelectedImage(BufferedImage towerSelectedImage) {
		this.towerSelectedImage = towerSelectedImage;
	}

	public int getTowerSelectedX() {
		return towerSelectedX;
	}

	public void setTowerSelectedX(int towerSelectedX) {
		this.towerSelectedX = towerSelectedX;
	}

	public int getTowerSelectedY() {
		return towerSelectedY;
	}

	public void setTowerSelectedY(int towerSelectedY) {
		this.towerSelectedY = towerSelectedY;
	}

	public TowerType getTowerSelectedTowerType() {
		return towerSelectedTowerType;
	}

	public void setTowerSelectedTowerType(TowerType towerSelectedTowerType) {
		this.towerSelectedTowerType = towerSelectedTowerType;
	}

	public int getMoney() {
		return money/2;
	}

	public void setMoney(int money) {
		this.money = money*2;
	}

	public int getMarginMinimap() {
		return marginMinimap;
	}

	public void setMarginMinimap(int marginMinimap) {
		this.marginMinimap = marginMinimap;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status, String oldStatus) {
		if(getStatus().equals(oldStatus)){
			this.status = status;
		}
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public int getLives() {
		return lives;
	}

	public void setLives(int lives) {
		this.lives = lives;
	}

	public Sprite getTooltip() {
		return tooltip;
	}

	public void setTooltip(Sprite tooltip) {
		this.tooltip = tooltip;
	}

	public int getFinalsSpawned() {
		return finalsSpawned;
	}

	public void setFinalsSpawned(int finalsSpawned) {
		this.finalsSpawned = finalsSpawned;
	}

	public Event getNextWave() {
		return nextWave;
	}

	public void setNextWave(Event nextWave) {
		this.nextWave = nextWave;
	}

	public int getWaveId() {
		return waveId;
	}

	public void setWaveId(int waveId) {
		this.waveId = waveId;
	}

	public int getFinalsKilled() {
		return finalsKilled/2;
	}

	public void setFinalsKilled(int finalsKilled) {
		setAdditionalStatus("Finals killed: " + finalsKilled);
		this.finalsKilled = finalsKilled*2;
	}

	public String getAdditionalStatus() {
		return additionalStatus;
	}

	public void setAdditionalStatus(String additionalStatus) {
		this.additionalStatus = additionalStatus;
	}

	public ArrayList<NPC> getNpcs() {
		return new ArrayList<NPC>(npcs);
	}

	public void setNpcs(ArrayList<NPC> npcs) {
		this.npcs = npcs;
	}

	public ArrayList<Sprite> getSprites() {
		return new ArrayList<Sprite>(sprites);
	}

	public void setSprites(ArrayList<Sprite> sprites) {
		this.sprites = sprites;
	}

	public ArrayList<Tower> getTowers() {
		return new ArrayList<Tower>(towers);
	}

	public void setTowers(ArrayList<Tower> towers) {
		this.towers = towers;
	}

	public ArrayList<TowerType> getTowerTypes() {
		return new ArrayList<TowerType>(towerTypes);
	}

	public void setTowerTypes(ArrayList<TowerType> towerTypes) {
		this.towerTypes = towerTypes;
	}

	public ArrayList<NPCType> getNPCTypes() {
		return new ArrayList<NPCType>(NPCTypes);
	}

	public void setNPCTypes(ArrayList<NPCType> nPCTypes) {
		NPCTypes = nPCTypes;
	}

	public ArrayList<BuffType> getBuffTypes() {
		return new ArrayList<BuffType>(buffTypes);
	}

	public void setBuffTypes(ArrayList<BuffType> buffTypes) {
		this.buffTypes = buffTypes;
	}

	public ArrayList<Animation> getAnimations() {
		return new ArrayList<Animation>(animations);
	}

	public void setAnimations(ArrayList<Animation> animations) {
		this.animations = animations;
	}

	public ArrayList<Button> getButtons() {
		return new ArrayList<Button>(buttons);
	}

	public void setButtons(ArrayList<Button> buttons) {
		this.buttons = buttons;
	}
	
	public ArrayList<Projectile> getProjectiles() {
		return new ArrayList<Projectile>(projectiles);
	}

	public void setProjectiles(ArrayList<Projectile> projectiles) {
		this.projectiles = projectiles;
	}

	public void setNewTower(Tower t) {
		towers.add(t);
	}
	
	public void destroyTower(Tower t){
		towers.remove(t);
	}
	
	public void setNewTowerType(TowerType t) {
		towerTypes.add(t);
	}
	
	public void destroyTowerType(TowerType t) {
		towerTypes.remove(t);
	}
	
	public void setNewNPC(NPC n) {
		npcs.add(n);
	}
	
	public void destroyNPC(NPC n) {
		npcs.remove(n);
	}
	
	public void setNewNPCType(NPCType n) {
		NPCTypes.add(n);
	}
	
	public void destroyNPCType(NPCType n) {
		NPCTypes.remove(n);
	}
	
	public void setNewSprite(Sprite s) {
		sprites.add(s);
	}
	
	public void destroySprite(Sprite s) {
		sprites.remove(s);
	}
	
	public void setNewAnimation(Animation a){
		animations.add(a);
	}
	
	public void destroyAnimation(Animation a){
		animations.remove(a);
	}
	
	public void setNewButton(Button b){
		buttons.add(b);
	}
	
	public void destroyButton(Button b){
		buttons.remove(b);
	}
	
	public void setNewBuffType(BuffType b){
		buffTypes.add(b);
	}
	
	public void destroyBuffType(BuffType b){
		buffTypes.remove(b);
	}
	
	public void setNewProjectile(Projectile p){
		projectiles.add(p);
	}
	
	public void destroyProjectile(Projectile p){
		projectiles.remove(p);
	}

	public Event getCurrentWave() {
		return currentWave;
	}

	public void setCurrentWave(Event currentWave) {
		this.currentWave = currentWave;
	}
}
