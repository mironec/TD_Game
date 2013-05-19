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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Game {
	
	private int tileWidth = 40;
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
	private int minOffsetX, minOffsetY;
	
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
	
	private int money = 50;
	private int lives = 40;
	private Sprite tooltip;
	private String status = "";
	private String additionalStatus = "";
	private int finalsSpawned=0;
	private int finalsKilled=0;
	private Event nextWave;
	private BufferedImage imageSell;
	public static final String RES_DIR = "/res/";
	public static final String BUTTON_SELL_DESC = "Sell\n%FontSize:11%Sells the tower\nfor 100 percent\nof invested money";
	
	private int waveId = 0;
	
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
		currentMap = new Map(m);
	}
	
	public void init(){
		loadMap("mapa.txt", Map.METHOD_LOAD_FILE);
		readMap();
		
		loadTowerTypes();
		loadNPCTypes();
		loadAdditional();
		prepareButtons();
		
		resizeImages();
		
		nextWave = new Event(m,5000,1){
			public void run(int delta){
				spawnWave(findNPCTypeById(waveId+1).getPerWave(),1000);
			}
		};
		
		m.setNewEvent(nextWave);
		
	}
	
	private void loadAdditional() {
		try {imageSell = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "additional/sell.png"));}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
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
		int posX = m.width-panelWidth+getMarginMinimap()*2;
		int posY = panelWidth+getMarginMinimap()*5;
		
		for(TowerType t = getLastTowerType();t!=null;t=t.getPrevious()){
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
				if(posX>=m.width-panelWidth+getMarginMinimap()*2 + getPanelWidth()){
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
		for(NPC npc = getLastNPC();npc!=null;npc=npc.getPrevious()){
			npc.setAnimationDeath(npc.getNPCType().getAnimationDeath());
			npc.setAnimationStand(npc.getNPCType().getAnimationStand());
			npc.setAnimationWalk(npc.getNPCType().getAnimationWalk());
			if(npc instanceof NPCRevive)
				((NPCRevive)npc).setAnimationRevive((BufferedImage)npc.getNPCType().getArg("animationRevive"));
		}
		
		for(TowerType t = getLastTowerType();t!=null;t=t.getPrevious()){
			img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + t.getId() + "-attack.png"));
			t.setAnimationAttack( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + t.getId() + "-stand.png"));
			t.setAnimationStand( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + t.getId() + "-projectileStand.png"));
			t.setProjectileAnimationStand( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
			
			img = ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + t.getId() + "-projectileDeath.png"));
			t.setProjectileAnimationDeath( resize(img,getTileWidth(),getTileWidth()*Animation.getImagePhases(img)) );
		}
		for(Tower t = getLastTower();t!=null;t=t.getPrevious()){
			t.setAnimationAttack(t.getTowerType().getAnimationAttack());
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
	
	public String findValue(String s, String find){
		String val = "";
		find += "=";
		
		if(s.contains(find)){
			String key = s.substring( s.lastIndexOf(find) );
			val = key.substring(find.length(), key.indexOf(";"));
		}
		
		return val;
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
				t.setAnimationAttackDuration( Integer.parseInt(findValue(s, "animationAttackDuration")) );
				t.setBase( findTowerTypeById(Integer.parseInt(findValue(s, "base"))) );
				t.setCost( Integer.parseInt(findValue(s, "cost")) );
				t.setDescription( findValue(s, "description") );
				t.setShootingAir( Boolean.parseBoolean(findValue(s, "shootingAir")) );
				if(t.getType().equals(TowerType.TOWER_TYPE_MULTI_TARGET))
					t.addArg("maxTargets", Integer.parseInt(findValue(s, "maxTargets")));
				if(t.getType().equals(TowerType.TOWER_TYPE_SIEGE))
					t.addArg("splashRadius", Double.parseDouble(findValue(s, "splashRadius")));
				if(t.getType().equals(TowerType.TOWER_TYPE_SLOW)){
					t.addArg("slow", Double.parseDouble(findValue(s, "slow")));
					t.addArg("slowDuration", Integer.parseInt(findValue(s, "slowDuration")));
				}
				////////////////////////////////////////////////////////
				t.setAnimationStand( ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + x + "-stand.png")) );
				t.setAnimationAttack( ImageIO.read(Main.class.getResourceAsStream(RES_DIR + "towers/" + x + "-attack.png")) );
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
				npc.setBounty( Integer.parseInt(findValue(s, "bounty")) );
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
		
		for(NPC npc = getLastNPC();npc!=null;npc=npc.getPrevious()){
			if(npc.getX()+npc.getAnimationStand().getWidth()>offsetXF&&
			   npc.getX()<offsetXF+m.width&&
			   npc.getY()+npc.getAnimationStand().getWidth()>offsetYF&&
			   npc.getY()<offsetYF+m.height)
			npc.drawLogic(delta);
		}
		for(Tower tower = getLastTower();tower!=null;tower=tower.getPrevious()){
			if(tower.getX()+tower.getAnimationStand().getWidth()>offsetXF&&
			   tower.getX()<offsetXF+m.width&&
			   tower.getY()+tower.getAnimationStand().getWidth()>offsetYF&&
			   tower.getY()<offsetYF+m.height)
				tower.drawLogic(delta);
		}
		for(Projectile p = getLastProjectile();p!=null;p=p.getPrevious()){
			if(p.getX()+p.getAnimationStand().getWidth()>offsetXF&&
			   p.getX()<offsetXF+m.width&&
			   p.getY()+p.getAnimationStand().getWidth()>offsetYF&&
			   p.getY()<offsetYF+m.height)
				p.drawLogic(delta);
		}
		
		for( Sprite s=getLastSprite(); s!=null; s=s.getPrevious() ){
			if( ! (s.getOwner() instanceof Button) &&
				! (s.getOwner() instanceof Game) &&
				! (s.getOwner() instanceof Projectile)){
				if(s.getX()+s.getImage().getWidth()>offsetXF&&
				   s.getX()<offsetXF+m.width&&
				   s.getY()+s.getImage().getHeight()>offsetYF&&
				   s.getY()<offsetYF+m.height)
					s.draw(g, (int)offsetXF, (int)offsetYF);
			}
		}
		for( Sprite s=getLastSprite(); s!=null; s=s.getPrevious() ){
			if( (s.getOwner() instanceof Projectile) ){
				if(s.getX()+s.getImage().getWidth()>offsetXF&&
				   s.getX()<offsetXF+m.width&&
				   s.getY()+s.getImage().getHeight()>offsetYF&&
				   s.getY()<offsetYF+m.height)
					s.draw(g, (int)offsetXF, (int)offsetYF);
			}
		}
		
		for( Animation a = getLastAnimation(); a != null; a = a.getPrevious() ){
			if(a.getX()+a.getImage().getWidth()>offsetXF&&
			   a.getX()<offsetXF+m.width&&
			   a.getY()+a.getImage().getWidth()>offsetYF&&
			   a.getY()<offsetYF+m.height)
			a.draw(g, (int)offsetXF, (int)offsetYF, delta);
		}
		
		for(NPC npc = getLastNPC();npc!=null;npc=npc.getPrevious()){
			g.setColor(Color.red);
			g.fillRect((int) (npc.getX()-offsetXF),(int) (npc.getY()-offsetYF),getTileWidth(),getTileWidth()/5);
			g.setColor(Color.cyan);
			g.fillRect((int) (npc.getX()-offsetXF),(int) (npc.getY()-offsetYF),(int)((double)npc.getHealth()/npc.getMaxHealth()*getTileWidth()),getTileWidth()/5);
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
		
		g.setColor(Color.darkGray);
		g.fillRect(m.width-panelWidth, 0, panelWidth, m.height);
		g.fillRect(0, m.height-panelHeight, m.width, panelHeight);
		
		g.setColor(Color.white);
		g.drawString("Money: " + money, m.width-panelWidth, panelWidth+getMarginMinimap()*2);
		g.drawString("Lives: " + lives, m.width-panelWidth+75, panelWidth+getMarginMinimap()*2);
		g.drawString("Next Wave: " + nextWave.getTime()/1000 + "s", getMarginMinimap()*2, m.height-panelHeight+getMarginMinimap()*2);
		g.drawString(getStatus(), marginMinimap*2, m.height-marginMinimap*4);
		g.drawString(getAdditionalStatus(), marginMinimap*2, m.height-panelHeight+marginMinimap*5);
		
		for(Button b = getLastButton();b!=null;b=b.getPrevious()){
			b.drawLogic();
		}
		
		for( Sprite s=getLastSprite(); s!=null; s=s.getPrevious() ){
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
			else{
				if(isCursorInPlayingField()){
					boolean isUpgrade = false;
					for(Tower t = getLastTower();t!=null;t=t.getPrevious()){
						if(m.mousePoint.x>t.getX()-offsetXF&&
						   m.mousePoint.x<t.getX()-offsetXF+getTileWidth()&&
						   m.mousePoint.y>t.getY()-offsetYF&&
						   m.mousePoint.y<t.getY()-offsetYF+getTileWidth()){
							isUpgrade = true;
							destroyUpgradeButtons();
							ArrayList<TowerType> upgrades = findUpgradesForTower(t.getTowerType());
							int buttonX = panelWidth;
							int buttonY = m.height-panelHeight+marginMinimap*2;
							final Tower t2=t;
							Button sell = new Button(m,buttonX,buttonY,getTileWidth(),getTileWidth(),imageSell,t){
								public void run() {
									t2.sell();
									destroyUpgradeButtons();
								}
							};
							sell.setDes(BUTTON_SELL_DESC);
							setNewButton(sell);
							buttonX += getTileWidth()+marginMinimap;
							for(int loop=0;loop<upgrades.size();loop++){
								final TowerType t3=upgrades.get(loop);
								Button b = new Button(m,buttonX,buttonY,getTileWidth(),getTileWidth(),Animation.getImagePhase(upgrades.get(loop).getAnimationStand(),0,m),t){
									public void run() {
										t2.upgradeTo(t3);
										destroyUpgradeButtons();
									}
								};
								setNewButton(b);
								buttonX += getTileWidth()+marginMinimap;
							}
						}
					}
					if(!isUpgrade){
						destroyUpgradeButtons();
					}
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
				if(offsetXF<=minOffsetX){offsetXF=minOffsetX;}
				
				offsetYF = (m.mousePoint.y-getMarginMinimap()
						-0.5f*(m.height-panelHeight)/(double)mapGraphicHeight*(panelWidth-getMarginMinimap()*2)) //Drag the center of the minimap square
						*(mapGraphicHeight/(double)(panelWidth-getMarginMinimap()*2)); //How many times bigger is the main screen than the minimap
				if(offsetYF>=maxOffsetY){offsetYF=maxOffsetY;}
				if(offsetYF<=minOffsetY){offsetYF=minOffsetY;}
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
					break;
				}
			}
		}
		
		if(m.keyDown[KeyEvent.VK_LEFT]||m.keyDown[KeyEvent.VK_A]){
			offsetXF-=delta*m.keySensitivity;
			if(offsetXF<=minOffsetX){offsetXF=minOffsetX;}
		}
		if(m.keyDown[KeyEvent.VK_RIGHT]||m.keyDown[KeyEvent.VK_D]){
			offsetXF+=delta*m.keySensitivity;
			if(offsetXF>=maxOffsetX){offsetXF=maxOffsetX;}
		}
		if(m.keyDown[KeyEvent.VK_UP]||m.keyDown[KeyEvent.VK_W]){
			offsetYF-=delta*m.keySensitivity;
			if(offsetYF<=minOffsetY){offsetYF=minOffsetY;}
		}
		if(m.keyDown[KeyEvent.VK_DOWN]||m.keyDown[KeyEvent.VK_S]){
			offsetYF+=delta*m.keySensitivity;
			if(offsetYF>=maxOffsetY){offsetYF=maxOffsetY;}
		}
	}
	
	private void destroyUpgradeButtons(){
		for(Button b=getLastButton();b!=null;b=b.getPrevious()){
			if(b.getOwner() instanceof Tower){
				b.destroy();
			}
		}
	}
	
	private ArrayList<TowerType> findUpgradesForTower(TowerType towerType){
		ArrayList<TowerType> ret = new ArrayList<TowerType>();
		
		for(TowerType t=getLastTowerType();t!=null;t=t.getPrevious()){
			if(t.getBase()==towerType){
				ret.add(t);
			}
		}
		
		return ret;
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
		else if( type.equals(TowerType.TOWER_TYPE_SLOW) ){
			TowerSlow to = new TowerSlow(m,0,0,towerType,(Double) towerType.getArg("slow"), (Integer) towerType.getArg("slowDuration"));
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
		if( type.equals(NPCType.NPC_TYPE_REVIVE) ){
			NPC npco = new NPCRevive(m,0,0,npcType,
					(Integer) npcType.getArg("reviveTimer"),
					(BufferedImage)npcType.getArg("animationRevive"),
					(Integer)npcType.getArg("animationReviveDuration"));
			setNewNPC(npco);
			npc = npco;
		}
		else if( type.equals(NPCType.NPC_TYPE_FINAL) ){
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
