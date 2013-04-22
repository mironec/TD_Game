package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

public class Game {
	
	private int tileWidth = 40;
	//private BufferedImage tiles[];
	//private int maxTiles = 1;
	private BufferedImage black;
	private BufferedImage minimap;
	private Graphics minimapG;
	private Map currentMap;
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
	
	private Main m;
	
	private int panelWidth = 150;
	private int panelHeight = 200;
	private int marginMinimap = 5;
	
	private int numberNPC = 0;
	private Projectile lastProjectile;
	private NPC lastNPC;
	private Sprite lastSprite;
	private Tower lastTower;
	private TowerType lastTowerType;
	private NPCType lastNPCType;
	private Animation lastAnimation;
	private Button lastButton;
	
	private boolean towerSelected;
	private Sprite towerSelectedSprite;
	private int towerSelectedX, towerSelectedY;
	private BufferedImage towerSelectedImage;
	private TowerType towerSelectedTowerType;
	
	private int money = 30;
	private int lives = 40;
	private Sprite tooltip;
	private String status = "";
	//private String nextWaveStatus = "";
	private String additionalStatus = "";
	private int finalsSpawned=0;
	private int finalsKilled=0;
	private Event nextWave;
	
	private int waveId = 1;
	
	public Game (Main m) {
		black = new BufferedImage(tileWidth, tileWidth, BufferedImage.TYPE_INT_ARGB);
		Graphics g = black.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, tileWidth, tileWidth);
		g.dispose();
		maxOffsetX=1;
		maxOffsetY=1;
		towerSelected = false;
		towerSelectedSprite = null;
		
		this.m=m;
		currentMap = new Map(m);
	}
	
	public void init(){
		loadMap("mapa.txt", Map.METHOD_LOAD_FILE);
		readMap();
		
		loadTowerTypes();
		loadNPCTypes();
		prepareButtons();
		
		resizeImages();
		
		//setStatus("Time to next wave: 5 seconds");
		nextWave = new Event(m,5000,1){
			public void run(int delta){
				spawnWave(findNPCTypeById(waveId+1).getPerWave(),1000);
			}
		};
				
		m.setNewEvent(nextWave);
	}
	
	public void spawnWave(int number, int delays){
		
		setWaveId(getWaveId()+1);
		final int waveId = getWaveId();
		
		NPC npc = createNPC(findNPCTypeById(waveId));
		npc.setX(findSpawn().x*tileWidth);
		npc.setY(findSpawn().y*tileWidth);
		npc.issueMoveCommand(findGoal().x, findGoal().y);
		
		Event e = new Event(m,delays,number-1){
			public void run(int delta){
				NPC npc = createNPC(findNPCTypeById(waveId));
				npc.setX(findSpawn().x*tileWidth);
				npc.setY(findSpawn().y*tileWidth);
				npc.issueMoveCommand(findGoal().x, findGoal().y);
				npc.logic(delta);
			}
		};
		m.setNewEvent(e);
		
		
		if(findNPCTypeById(waveId+1)!=null){
			nextWave = new Event(m,delays*number+10000,1){
				public void run(int delta){
					spawnWave(findNPCTypeById(waveId+1).getPerWave(), 1000);
				}
			};
			m.setNewEvent(nextWave);
		}
		else{
			nextWave = new Event(m,delays*number+10000,1){
				public void run(int delta){
					setAdditionalStatus("Finals killed: 0");
					
					finalWave(delta);
				}
			};
			m.setNewEvent(nextWave);
		}
	}
	
	public void finalWave(int delta){
		NPC npc = createNPC(findNPCTypeById(-1));
		npc.setX(findSpawn().x*tileWidth);
		npc.setY(findSpawn().y*tileWidth);
		npc.issueMoveCommand(findGoal().x, findGoal().y);
		npc.logic(delta);
		
		Event e = new Event(m,1500,1){
			public void run (int delta){
				finalWave(delta);
			}
		};
		m.setNewEvent(e);
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
		int posX = getMarginMinimap()*2;
		int posY = getMarginMinimap()*2;
		
		for(TowerType t = getLastTowerType();t!=null;t=t.getPrevious()){
			if(t.getBase() == null){									//The Tower is not an upgrade from another Tower
				final int id = t.getId();
				Button b = new Button(m, posX, posY, getPanelWidth()/4, getPanelWidth()/4,
						resize(Animation.getImagePhase(t.getAnimationStand(), 0, m),getPanelWidth()/4,getPanelWidth()/4) ) {
					public void run() {
						selectTower( findTowerTypeById(id) );
					}
				};
				b.setDes(t.getDescription());
				setNewButton(b);
				
				posX += getPanelWidth()/4 + getMarginMinimap();
				if(posX>=getPanelWidth()){
					posX = getMarginMinimap()*2;
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
		for(NPCType npc = getLastNPCType();npc!=null;npc=npc.getPrevious()){
			img = ImageIO.read(new URL(m.getCodeBase() + "npcs/" + npc.getId() + "-death.png"));
			npc.setAnimationDeath( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(new URL(m.getCodeBase() + "npcs/" + npc.getId() + "-stand.png"));
			npc.setAnimationStand( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(new URL(m.getCodeBase() + "npcs/" + npc.getId() + "-walk.png"));
			npc.setAnimationWalk( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			if(npc.getArg("animationRevive")!=null){
				img = ImageIO.read(new URL(m.getCodeBase() + "npcs/" + npc.getId() + "-revive.png"));
				npc.addArg( "animationRevive", resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			}
		}
		for(NPC npc = getLastNPC();npc!=null;npc=npc.getPrevious()){
			npc.setAnimationDeath(npc.getNPCType().getAnimationDeath());
			npc.setAnimationStand(npc.getNPCType().getAnimationStand());
			npc.setAnimationWalk(npc.getNPCType().getAnimationWalk());
			if(npc instanceof NPCRevive)
				((NPCRevive)npc).setAnimationRevive((BufferedImage)npc.getNPCType().getArg("animationRevive"));
		}
		
		for(TowerType t = getLastTowerType();t!=null;t=t.getPrevious()){
			img = ImageIO.read(new URL(m.getCodeBase() + "towers/" + t.getId() + "-attack.png"));
			t.setAnimationAttack( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(new URL(m.getCodeBase() + "towers/" + t.getId() + "-stand.png"));
			t.setAnimationStand( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(new URL(m.getCodeBase() + "towers/" + t.getId() + "-projectileStand.png"));
			t.setProjectileAnimationStand( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(new URL(m.getCodeBase() + "towers/" + t.getId() + "-projectileDeath.png"));
			t.setProjectileAnimationDeath( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
		}
		for(Tower t = getLastTower();t!=null;t=t.getPrevious()){
			t.setAnimationAttack(t.getTowerType().getAnimationAttack());
			t.setAnimationStand(t.getTowerType().getAnimationStand());
		}
		
		}
		catch (MalformedURLException e) {e.printStackTrace(); /*System.out.println("An Image couldn't be found.");*/} 
		catch (IOException e) {e.printStackTrace(); /*System.out.println("An Image couldn't be found.");*/}
	}
	
	public static BufferedImage resize(BufferedImage img, int newW, int newH) {  
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
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
	
	public void loadTowerTypes(){
		int x = 1;
		URL url;
		try {
			while(true){
				url = new URL(m.getCodeBase() + "towers/" + x + ".cfg");
				URLConnection con = url.openConnection();
				if(con.getContentLength()<=0){break;}
				byte[] b = new byte[con.getContentLength()];
				con.getInputStream().read(b);
				con.getInputStream().close();
				String s = new String(b);
				TowerType t = new TowerType(m, x);
				String key;
				String value;
				String find;
				////////////////////////////////////////////////////////
				find = "type=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.setType(value); }
				////////////////////////////////////////////////////////
				find = "attackSpeed=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.setAttackSpeed( Double.parseDouble(value) ); }
				////////////////////////////////////////////////////////
				find = "damage=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.setDamage( Double.parseDouble(value) ); }
				////////////////////////////////////////////////////////
				find = "projectileSpeed=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.setProjectileSpeed( Double.parseDouble(value) ); }
				////////////////////////////////////////////////////////
				find = "projectileAnimationStandDuration=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.setProjectileAnimationStandDuration( Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "projectileAnimationDeathDuration=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.setProjectileAnimationDeathDuration( Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "range=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.setRange( Double.parseDouble(value) ); }
				////////////////////////////////////////////////////////
				find = "animationStandDuration=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.setAnimationStandDuration( Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "animationAttackDuration=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.setAnimationAttackDuration( Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "base=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.setBase( findTowerTypeById(Integer.parseInt(value)) ); }
				////////////////////////////////////////////////////////
				find = "cost=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.setCost( Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "description=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.setDescription( value ); }
				////////////////////////////////////////////////////////
				find = "maxTargets=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				t.newArg( Integer.parseInt(value) );}
				////////////////////////////////////////////////////////
				t.setAnimationStand( ImageIO.read(new URL(m.getCodeBase() + "towers/" + x + "-stand.png")) );
				t.setAnimationAttack( ImageIO.read(new URL(m.getCodeBase() + "towers/" + x + "-attack.png")) );
				t.setProjectileAnimationStand( ImageIO.read(new URL(m.getCodeBase() + "towers/" + x + "-projectileStand.png")) );
				t.setProjectileAnimationDeath( ImageIO.read(new URL(m.getCodeBase() + "towers/" + x + "-projectileDeath.png")) );
				setNewTowerType(t);
				x++;
			}
		}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();/*System.out.println("An Image couldn't be found.");*/}
		
	}
	
	/*public void loadFinal(){
		try{
			URL url = new URL(m.getCodeBase() + "npcs/final.cfg");
			URLConnection con = url.openConnection();
			byte[] b = new byte[con.getContentLength()];
			con.getInputStream().read(b);
			con.getInputStream().close();
			String s = new String(b);
			NPCType npc = new NPCType(m, -1);
			String key;
			String value;
			String find;
			////////////////////////////////////////////////////////
			find = "maxHealth=";
			if(s.contains(find)){
			key = s.substring( s.lastIndexOf(find) );
			value = key.substring(find.length(), key.indexOf(";"));
			npc.setMaxHealth( Integer.parseInt(value) ); }
			////////////////////////////////////////////////////////
			find = "movementSpeed=";
			if(s.contains(find)){
			key = s.substring( s.lastIndexOf(find) );
			value = key.substring(find.length(), key.indexOf(";"));
			npc.setMovementSpeed( Double.parseDouble(value) ); }
			////////////////////////////////////////////////////////
			find = "animationStandDuration=";
			if(s.contains(find)){
			key = s.substring( s.lastIndexOf(find) );
			value = key.substring(find.length(), key.indexOf(";"));
			npc.setAnimationStandDuration( Integer.parseInt(value) ); }
			////////////////////////////////////////////////////////
			find = "animationWalkDuration=";
			if(s.contains(find)){
			key = s.substring( s.lastIndexOf(find) );
			value = key.substring(find.length(), key.indexOf(";"));
			npc.setAnimationWalkDuration( Integer.parseInt(value) ); }
			////////////////////////////////////////////////////////
			find = "animationDeathDuration=";
			if(s.contains(find)){
			key = s.substring( s.lastIndexOf(find) );
			value = key.substring(find.length(), key.indexOf(";"));
			npc.setAnimationDeathDuration( Integer.parseInt(value) ); }
			////////////////////////////////////////////////////////
			find = "perWave=";
			if(s.contains(find)){
			key = s.substring( s.lastIndexOf(find) );
			value = key.substring(find.length(), key.indexOf(";"));
			npc.setPerWave( Integer.parseInt(value) ); }
			////////////////////////////////////////////////////////
			find = "bounty=";
			if(s.contains(find)){
			key = s.substring( s.lastIndexOf(find) );
			value = key.substring(find.length(), key.indexOf(";"));
			npc.setBounty( Integer.parseInt(value) ); }
			////////////////////////////////////////////////////////
			find = "type=";
			if(s.contains(find)){
			key = s.substring( s.lastIndexOf(find) );
			value = key.substring(find.length(), key.indexOf(";"));
			npc.setType( value ); }
			////////////////////////////////////////////////////////
			find = "reviveTimer=";
			if(s.contains(find)){
			key = s.substring( s.lastIndexOf(find) );
			value = key.substring(find.length(), key.indexOf(";"));
			npc.addArg( "reviveTimer", Integer.parseInt(value) ); }
			////////////////////////////////////////////////////////
			find = "animationReviveDuration=";
			if(s.contains(find)){
			key = s.substring( s.lastIndexOf(find) );
			value = key.substring(find.length(), key.indexOf(";"));
			npc.addArg( "animationReviveDuration", Integer.parseInt(value) ); }
			////////////////////////////////////////////////////////
			npc.setAnimationStand( ImageIO.read(new URL(m.getCodeBase() + "npcs/final-stand.png")) );
			npc.setAnimationWalk( ImageIO.read(new URL(m.getCodeBase() + "npcs/final-walk.png")) );
			npc.setAnimationDeath( ImageIO.read(new URL(m.getCodeBase() + "npcs/final-death.png")) );
			if(new URL(m.getCodeBase() + "npcs/final-revive.png").openConnection().getContentLength()>0){
				npc.addArg("animationRevive", ImageIO.read(new URL(m.getCodeBase() + "npcs/final-revive.png")));
			}
			setNewNPCType(npc);
		}
		catch(Exception e){e.printStackTrace();}
	}*/
	
	public void loadNPCTypes(){
		int x = 1;
		URL url;
		boolean dothis=true;
		try {
			while(dothis){
				url = new URL(m.getCodeBase() + "npcs/" + x + ".cfg");
				URLConnection con = url.openConnection();
				if(con.getContentLength()<=0){
					x=-1;
					url = new URL(m.getCodeBase() + "npcs/" + x + ".cfg");
					con = url.openConnection();
					dothis=false;
				}
				byte[] b = new byte[con.getContentLength()];
				con.getInputStream().read(b);
				con.getInputStream().close();
				String s = new String(b);
				NPCType npc = new NPCType(m, x);
				String key;
				String value;
				String find;
				////////////////////////////////////////////////////////
				find = "maxHealth=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				npc.setMaxHealth( Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "movementSpeed=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				npc.setMovementSpeed( Double.parseDouble(value) ); }
				////////////////////////////////////////////////////////
				find = "animationStandDuration=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				npc.setAnimationStandDuration( Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "animationWalkDuration=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				npc.setAnimationWalkDuration( Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "animationDeathDuration=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				npc.setAnimationDeathDuration( Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "perWave=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				npc.setPerWave( Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "bounty=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				npc.setBounty( Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "type=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				npc.setType( value ); }
				////////////////////////////////////////////////////////
				find = "reviveTimer=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				npc.addArg( "reviveTimer", Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "animationReviveDuration=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				npc.addArg( "animationReviveDuration", Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "maxLife=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				npc.addArg( "maxLife", Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				find = "increment=";
				if(s.contains(find)){
				key = s.substring( s.lastIndexOf(find) );
				value = key.substring(find.length(), key.indexOf(";"));
				npc.addArg( "increment", Double.parseDouble(value) ); }
				////////////////////////////////////////////////////////
				npc.setAnimationStand( ImageIO.read(new URL(m.getCodeBase() + "npcs/" + x + "-stand.png")) );
				npc.setAnimationWalk( ImageIO.read(new URL(m.getCodeBase() + "npcs/" + x + "-walk.png")) );
				npc.setAnimationDeath( ImageIO.read(new URL(m.getCodeBase() + "npcs/" + x + "-death.png")) );
				if(new URL(m.getCodeBase() + "npcs/" + x + "-revive.png").openConnection().getContentLength()>0){
					npc.addArg("animationRevive", ImageIO.read(new URL(m.getCodeBase() + "npcs/" + x + "-revive.png")));
				}
				setNewNPCType(npc);
				x++;
			}
		}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();/*System.out.println("An Image couldn't be found.");*/}
		
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
			m.renderMode = Main.RENDER_MODE_MENU;
		}
		if( isTowerSelected() ){
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
		if(getLastNPC()!=null){
		for(NPC npc = getLastNPC();npc!=null;npc=npc.getPrevious()){
			npc.logic(delta);
		}
		}
	}
	
	public void logicTowers(int delta){
		for(Tower tower = getLastTower();tower!=null;tower=tower.getPrevious()){
			tower.logic(delta);
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
		
		for(NPC npc = getLastNPC();npc!=null;npc=npc.getPrevious()){
			npc.drawLogic(delta);
		}
		for(Tower tower = getLastTower();tower!=null;tower=tower.getPrevious()){
			tower.drawLogic(delta);
		}
		for(Projectile p = getLastProjectile();p!=null;p=p.getPrevious()){
			p.drawLogic(delta);
		}
		
		for( Sprite s=getLastSprite(); s!=null; s=s.getPrevious() ){
			if( ! (s.getOwner() instanceof Button) &&
				! (s.getOwner() instanceof Game) ){
				s.draw(g, (int)offsetXF, (int)offsetYF);
			}
		}
		
		for( Animation a = getLastAnimation(); a != null; a = a.getPrevious() ){
			a.draw(g, (int)offsetXF, (int)offsetYF, delta);
		}
		
		for(NPC npc = getLastNPC();npc!=null;npc=npc.getPrevious()){
			g.setColor(Color.red);
			g.fillRect((int) (npc.getX()-offsetXF),(int) (npc.getY()-offsetYF),50,10);
			g.setColor(Color.cyan);
			g.fillRect((int) (npc.getX()-offsetXF),(int) (npc.getY()-offsetYF),(int)((double)npc.getHealth()/npc.getMaxHealth()*50D),10);
			g.setColor(Color.orange);
			g.drawString(npc.getHealth() + "/" + npc.getMaxHealth(), (int) (npc.getX()-offsetXF)+10,(int) (npc.getY()-offsetYF)+9);
		}
	}
	
	public void renderAboveAll(int delta){
		Graphics g = m.backbufferG;
		
		for( Sprite s=getLastSprite(); s!=null; s=s.getPrevious() ){
			if( s.getOwner() instanceof Game ){
				s.draw(g, 0, 0);
			}
		}
	}
	
	public void renderMap(int delta){
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
				else{
					//g.drawImage(tiles[currentMap.getData()[index]], (x-offsetXTiles)*tileWidth-offsetXMini, (y-offsetYTiles)*tileWidth-offsetYMini, m);
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
		
		g.setColor(Color.darkGray);
		g.fillRect(m.width-panelWidth, 0, panelWidth, m.height);
		g.fillRect(0, m.height-panelHeight, m.width, panelHeight);
		
		g.setColor(Color.white);
		g.drawString("Money: " + money, m.width-panelWidth, panelWidth+getMarginMinimap()*2);
		g.drawString("Lives: " + lives, m.width-panelWidth+75, panelWidth+getMarginMinimap()*2);
		g.drawString("Next Wave: " + nextWave.getTime()/1000 + "s", getMarginMinimap()*2, m.height-panelHeight+getMarginMinimap()*2);
		g.drawString(getStatus(), marginMinimap*2, m.height-marginMinimap*4);
		
		for(Button b = getLastButton();b!=null;b=b.getPrevious()){
			b.drawLogic();
		}
		
		for( Sprite s=getLastSprite(); s!=null; s=s.getPrevious() ){
			if( s.getOwner() instanceof Button ){
				s.draw(g, -m.width+panelWidth, -panelWidth-getMarginMinimap()*3);
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
			//g.drawImage(tiles[currentMap.getData()[index]], x*tileWidth, y*tileWidth, (x+1)*tileWidth, (y+1)*tileWidth, 0, 0, tiles[currentMap.getData()[index]].getWidth(), tiles[currentMap.getData()[index]].getHeight(), m);
		}
		}
		
		for(NPC npc = getLastNPC();npc!=null;npc=npc.getPrevious()){
			g.setColor(Color.red);
			g.fillRect( (int)(npc.getX()), (int)(npc.getY()), getTileWidth(), getTileWidth() );
		}
		for(Tower tower = getLastTower();tower!=null;tower=tower.getPrevious()){
			g.setColor(Color.gray);
			g.fillRect( (int)(tower.getX()), (int)(tower.getY()), getTileWidth(), getTileWidth() );
		}
		
		m.backbufferG.drawImage(minimap, m.width-panelWidth+getMarginMinimap(), getMarginMinimap(), m.width-getMarginMinimap(), panelWidth-getMarginMinimap(),
				0, 0, mapGraphicWidth, mapGraphicHeight, m);
		m.backbufferG.setColor(Color.white);
		int minimapWidth = panelWidth-getMarginMinimap()*2;
		double modifierX = (double)minimapWidth/mapGraphicWidth;
		double modifierY = (double)minimapWidth/mapGraphicHeight;
		m.backbufferG.drawRect(m.width - panelWidth + getMarginMinimap() + (int)(offsetXF*modifierX), getMarginMinimap() + (int)(offsetYF*modifierY),(int)((double)(m.width-panelWidth)*modifierX),(int)((double)(m.height-panelHeight)*modifierY));
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
			
			if(m.mouseStart.x>=m.width-panelWidth+getMarginMinimap()&&
				m.mouseStart.x<=m.width-getMarginMinimap()&&
				m.mouseStart.y>=getMarginMinimap()&&
				m.mouseStart.y<=panelWidth-getMarginMinimap())
			{
				offsetXF = (m.mousePoint.x-m.width+panelWidth-getMarginMinimap()
						-0.5f*(m.width-panelWidth)/(double)mapGraphicWidth*(panelWidth-getMarginMinimap()*2)) //Drag the center of the minimap square
						*(mapGraphicWidth/(double)(panelWidth-getMarginMinimap()*2)); //How many times bigger is the main screen than the minimap
				if(offsetXF>=maxOffsetX){offsetXF=maxOffsetX;}
				if(offsetXF<=0){offsetXF=0;}
				
				offsetYF = (m.mousePoint.y-getMarginMinimap()
						-0.5f*(m.height-panelHeight)/(double)mapGraphicHeight*(panelWidth-getMarginMinimap()*2)) //Drag the center of the minimap square
						*(mapGraphicHeight/(double)(panelWidth-getMarginMinimap()*2)); //How many times bigger is the main screen than the minimap
				if(offsetYF>=maxOffsetY){offsetYF=maxOffsetY;}
				if(offsetYF<=0){offsetYF=0;}
			}
			
			for(Button b = getLastButton();b!=null;b=b.getPrevious()){
				b.click(m.mousePoint.x, m.mousePoint.y);
			}
		}
		
		else{
			if(getTooltip()!=null){
				destroySprite(getTooltip());
				setTooltip(null);
			}
			for(Button b = getLastButton();b!=null;b=b.getPrevious()){
				if(b.isHere(m.mousePoint.x, m.mousePoint.y)){
					BufferedImage img = new BufferedImage(getPanelWidth()-marginMinimap*2, getPanelHeight()-marginMinimap*2, BufferedImage.TYPE_INT_ARGB);
					Graphics g = img.getGraphics();
					g.setColor(Color.yellow);
					g.fillRect(0, 0, getPanelWidth()-marginMinimap*2, getPanelWidth()-marginMinimap*2);
					g.setColor(Color.black);
					g.drawRect(0, 0, getPanelWidth()-marginMinimap*2, getPanelWidth()-marginMinimap*2);
					g.setFont(new Font("monospaced", Font.PLAIN, getPanelWidth()/10));
					int y = 2;
					int x = 2;
					for (String line : b.getDes().split("\n")){
						y += g.getFontMetrics().getHeight();
						x = 2;
						for(String command : line.split("%")){
							if(command.startsWith("FontSize:")){
								g.setFont(new Font(g.getFont().getFontName(), g.getFont().getStyle(), Integer.parseInt(command.substring(command.indexOf(":")+1))));
							}
							else{
								g.drawString(command, x, y);
								x+=command.length()*g.getFontMetrics().getWidths()[0];
							}
						}
					}
					g.dispose();
					setTooltip(new Sprite(m, m.width-getPanelWidth()+marginMinimap, m.height-getPanelHeight()+marginMinimap, img, this, false));
					setNewSprite(getTooltip());
				}
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
	
	private boolean isTowerOn(int x, int y) {
		for(Tower t = getLastTower();t!=null;t = t.getPrevious()){
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

	public Projectile getLastProjectile() {
		return lastProjectile;
	}

	public void setLastProjectile(Projectile lastProjectile) {
		this.lastProjectile = lastProjectile;
	}

	public void setNewProjectile(Projectile p){
		if(getLastProjectile()!=null){
			getLastProjectile().setNext(p);
			p.setPrevious(getLastProjectile());
		}
		setLastProjectile(p);
	}
	
	public void setNewNPC(NPC npc){
		if(getLastNPC()!=null){
			getLastNPC().setNext(npc);
			npc.setPrevious(getLastNPC());
		}
		setLastNPC(npc);
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
			if(p.getTarget()!=null){
				if(p.getTarget().equals(npc)){
					p.setTarget(null); p.setTargetX(npc.getX()); p.setTargetY(npc.getY());
				}
			}
		}
		}
		
		for(Sprite s = getLastSprite();s!=null;s=s.getPrevious()){
			if(s.getOwner().equals(npc)){destroySprite(s);}
		}
		
		if(getLastNPC().equals(npc)){
			setLastNPC(npc.getPrevious());
		}
		if(npc.getPrevious()!=null){npc.getPrevious().setNext(npc.getNext());}
		if(npc.getNext()!=null){npc.getNext().setPrevious(npc.getPrevious());}
	}
	
	public void setNewSprite(Sprite s){
		if(getLastSprite()!=null){
			getLastSprite().setNext(s);
			s.setPrevious(getLastSprite());
		}
		setLastSprite(s);
	}
	
	public void destroySprite(Sprite s){
		if(getLastSprite().equals(s)){
			setLastSprite(s.getPrevious());
		}
		if(s.getPrevious()!=null){s.getPrevious().setNext(s.getNext());}
		if(s.getNext()!=null){s.getNext().setPrevious(s.getPrevious());}
		s.setImage(null);
	}
	
	public void setNewTower(Tower t){
		if(getLastTower()!=null){
			getLastTower().setNext(t);
			t.setPrevious(getLastTower());
		}
		setLastTower(t);
	}
	
	public void destroyTower(Tower t){
		if(getLastTower().equals(t)){
			setLastTower(t.getPrevious());
		}
		if(t.getPrevious()!=null){t.getPrevious().setNext(t.getNext());}
		if(t.getNext()!=null){t.getNext().setPrevious(t.getPrevious());}
		
		for(Sprite s = getLastSprite();s!=null;s=s.getPrevious()){
			if(s.getOwner().equals(t)){destroySprite(s);}
		}
	}
	
	public void setNewAnimation(Animation a){
		if(getLastAnimation()!=null){
			getLastAnimation().setNext(a);
			a.setPrevious(getLastAnimation());
			
		}
		setLastAnimation(a);
	}
	
	public void destroyAnimation(Animation a){
		if(getLastAnimation().equals(a)){
			setLastAnimation(a.getPrevious());
		}
		if(a.getPrevious()!=null){a.getPrevious().setNext(a.getNext());}
		if(a.getNext()!=null){a.getNext().setPrevious(a.getPrevious());}
		a.setImage(null);
	}

	public void setNewTowerType(TowerType t){
		if(getLastTowerType()!=null){
			getLastTowerType().setNext(t);
			t.setPrevious(getLastTowerType());
		}
		setLastTowerType(t);
	}
	
	public void destroyTowerType(TowerType t){
		if(getLastTowerType().equals(t)){
			setLastTowerType(t.getPrevious());
		}
		if(t.getPrevious()!=null){t.getPrevious().setNext(t.getNext());}
		if(t.getNext()!=null){t.getNext().setPrevious(t.getPrevious());}
	}
	
	public void destroyNPCType(NPCType npc){
		if(getLastNPCType().equals(npc)){
			setLastNPCType(npc.getPrevious());
		}
		if(npc.getPrevious()!=null){npc.getPrevious().setNext(npc.getNext());}
		if(npc.getNext()!=null){npc.getNext().setPrevious(npc.getPrevious());}
	}
	
	public void setNewNPCType(NPCType npc){
		if(getLastNPCType()!=null){
			getLastNPCType().setNext(npc);
			npc.setPrevious(getLastNPCType());
		}
		setLastNPCType(npc);
	}

	public NPC getLastNPC() {
		return lastNPC;
	}

	public void setLastNPC(NPC lastNPC) {
		this.lastNPC = lastNPC;
	}

	public Sprite getLastSprite() {
		return lastSprite;
	}

	public void setLastSprite(Sprite lastSprite) {
		this.lastSprite = lastSprite;
	}

	public Tower getLastTower() {
		return lastTower;
	}

	public void setLastTower(Tower lastTower) {
		this.lastTower = lastTower;
	}

	public Animation getLastAnimation() {
		return lastAnimation;
	}

	public void setLastAnimation(Animation lastAnimation) {
		this.lastAnimation = lastAnimation;
	}

	public TowerType getLastTowerType() {
		return lastTowerType;
	}

	public void setLastTowerType(TowerType lastTowerType) {
		this.lastTowerType = lastTowerType;
	}
	
	public TowerType findTowerTypeById (int id) {
		for(TowerType t=getLastTowerType();t!=null;t=t.getPrevious()){
			if(t.getId()==id){return t;}
		}
		return null;
	}
	
	public NPCType findNPCTypeById (int id) {
		for(NPCType npc=getLastNPCType();npc!=null;npc=npc.getPrevious()){
			if(npc.getId()==id){return npc;}
		}
		return null;
	}

	public NPCType getLastNPCType() {
		return lastNPCType;
	}

	public void setLastNPCType(NPCType lastNPCType) {
		this.lastNPCType = lastNPCType;
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
		if( type.equals("multiAttack") ){
			TowerMultiAttack to = new TowerMultiAttack(m,0,0,towerType,(Integer) towerType.getArgs()[0]);
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
	
	public NPC createNPC(NPCType npcType){
		String type = npcType.getType();
		NPC npc;
		if( type.equals("revive") ){
			NPC npco = new NPCRevive(m,0,0,npcType,
					(Integer) npcType.getArg("reviveTimer"),
					(BufferedImage)npcType.getArg("animationRevive"),
					(Integer)npcType.getArg("animationReviveDuration"));
			setNewNPC(npco);
			npc = npco;
		}
		else if( type.equals("final") ){
			NPC npco = new NPCFinal(m,0,0,npcType,getFinalsSpawned());
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
	
	public void destroyButton(Button b){
		if(getLastButton().equals(b)){
			setLastButton(b.getPrevious());
		}
		if(b.getPrevious()!=null){b.getPrevious().setNext(b.getNext());}
		if(b.getNext()!=null){b.getNext().setPrevious(b.getPrevious());}
	}
	
	public void setNewButton(Button b){
		if(getLastButton()!=null){
			getLastButton().setNext(b);
			b.setPrevious(getLastButton());
		}
		setLastButton(b);
	}

	public Button getLastButton() {
		return lastButton;
	}

	public void setLastButton(Button lastButton) {
		this.lastButton = lastButton;
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
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
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
		return finalsKilled;
	}

	public void setFinalsKilled(int finalsKilled) {
		setAdditionalStatus("Finals killed: " + finalsKilled);
		this.finalsKilled = finalsKilled;
	}

	public String getAdditionalStatus() {
		return additionalStatus;
	}

	public void setAdditionalStatus(String additionalStatus) {
		this.additionalStatus = additionalStatus;
	}
}
