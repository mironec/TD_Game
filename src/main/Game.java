package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

public class Game {
	
	private int tileWidth = 30;
	private BufferedImage tiles[];
	private int maxTiles = 1;
	private BufferedImage black;
	private BufferedImage minimap;
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
	
	private int numberNPC = 0;
	private Projectile lastProjectile;
	private NPC lastNPC;
	private Sprite lastSprite;
	private Tower lastTower;
	private TowerType lastTowerType;
	private NPCType lastNPCType;
	private Animation lastAnimation;
	private Button lastButton;
	
	public Game (Main m) {
		tiles = new BufferedImage[maxTiles+1];
		black = new BufferedImage(tileWidth, tileWidth, BufferedImage.TYPE_INT_ARGB);
		Graphics g = black.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, tileWidth, tileWidth);
		g.dispose();
		maxOffsetX=1;
		maxOffsetY=1;
		this.m=m;
		currentMap = new Map(m);
	}
	
	public void init(){
		loadMapTiles();
		loadTowerTypes();
		loadNPCTypes();
		
		resizeImages();
		
		Event e = new Event(m,200,50){
			public void run() {
				NPC npc = createNPC(findNPCTypeById(1));
				npc.issueMoveCommand(19,19);
			}
		};
		
		Button b = new Button(m, 10, 10, 50, 50, findTowerTypeById(1).getAnimationStand()) {
			public void run() {
				Tower t = createTower(findTowerTypeById(1));
				t.setX(3*getTileWidth());
				t.setY(2*getTileWidth());
			}
		}; 
		setNewButton(b);
		
		Tower t = createTower(findTowerTypeById(1));
		t.setX(2*getTileWidth());
		t.setY(2*getTileWidth());
		
		m.setNewEvent(e);
		
		
	}
	
	public void selectTower(){
		
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
		}
		for(NPC npc = getLastNPC();npc!=null;npc=npc.getPrevious()){
			npc.setAnimationDeath(npc.getNPCType().getAnimationDeath());
			npc.setAnimationStand(npc.getNPCType().getAnimationStand());
			npc.setAnimationWalk(npc.getNPCType().getAnimationWalk());
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
		catch (MalformedURLException e) {System.out.println("An Image couldn't be found.");} 
		catch (IOException e) {System.out.println("An Image couldn't be found.");}
	}
	
	public BufferedImage resize(BufferedImage img, int x, int y){
		BufferedImage ret;
		ret = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
		ret.getGraphics().drawImage(img, 0, 0, x, y, 0, 0, img.getWidth(), img.getHeight(), m);
		ret.getGraphics().dispose();
		return ret;
	}
	
	public static BufferedImage rotate(BufferedImage img, int amnout){
		BufferedImage ret = new BufferedImage(img.getWidth(),img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.rotate(-Math.PI/180.0D*(double)(amnout),img.getWidth()/2,img.getHeight()/2);
		Graphics2D g2d = (Graphics2D) ret.getGraphics();
        g2d.drawImage(img, at, null);
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
		catch (IOException e) {System.out.println("An Image couldn't be found.");}
		
	}
	
	public void loadNPCTypes(){
		int x = 1;
		URL url;
		try {
			while(true){
				url = new URL(m.getCodeBase() + "npcs/" + x + ".cfg");
				URLConnection con = url.openConnection();
				if(con.getContentLength()<=0){break;}
				byte[] b = new byte[con.getContentLength()];
				con.getInputStream().read(b);
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
				npc.newArg( Integer.parseInt(value) ); }
				////////////////////////////////////////////////////////
				npc.setAnimationStand( ImageIO.read(new URL(m.getCodeBase() + "npcs/" + x + "-stand.png")) );
				npc.setAnimationWalk( ImageIO.read(new URL(m.getCodeBase() + "npcs/" + x + "-walk.png")) );
				npc.setAnimationDeath( ImageIO.read(new URL(m.getCodeBase() + "npcs/" + x + "-death.png")) );
				setNewNPCType(npc);
				x++;
			}
		}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {System.out.println("An Image couldn't be found.");}
		
	}
	
	public void logic(int delta){
		logicNPCs(delta);
		logicTowers(delta);
		logicProjectiles(delta);
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
	
	public void render(int delta){
		renderMap(delta);
		renderObjects(delta);
		renderOverlay(delta);
		renderMiniMap(delta);
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
			if( ! (s.getOwner() instanceof Button) ){
				s.draw(g, (int)offsetXF, (int)offsetYF);
			}
		}
		
		for( Animation a = getLastAnimation(); a != null; a = a.getPrevious() ){
			a.draw(g, (int)offsetXF, (int)offsetYF, delta);
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
				else{g.drawImage(tiles[currentMap.getData()[index]], (x-offsetXTiles)*tileWidth-offsetXMini, (y-offsetYTiles)*tileWidth-offsetYMini, m);}
			}
			}
		}
	}
	
	public void renderOverlay(int delta){
		Graphics g = m.backbufferG;
		
		g.setColor(Color.darkGray);
		g.fillRect(m.width-panelWidth, 0, panelWidth, m.height);
		g.fillRect(0, m.height-panelHeight, m.width, panelHeight);
		
		for(Button b = getLastButton();b!=null;b=b.getPrevious()){
			b.drawLogic();
		}
		
		for( Sprite s=getLastSprite(); s!=null; s=s.getPrevious() ){
			if( s.getOwner() instanceof Button ){
				/*System.out.println("j");*/
				s.draw(g, -m.width+panelWidth, -m.height+panelHeight);
			}
		}
	}
	
	public void renderMiniMap(int delta){
		Graphics g = minimapG;
		
		for(int y=0;y<currentMap.getHeight();y++){
		for(int x=0;x<currentMap.getWidth();x++){
			int index = y*currentMap.getWidth()+x+4;
			g.drawImage(tiles[currentMap.getData()[index]], x*tileWidth, y*tileWidth, (x+1)*tileWidth, (y+1)*tileWidth, 0, 0, tiles[currentMap.getData()[index]].getWidth(), tiles[currentMap.getData()[index]].getHeight(), m);
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
			
			for(Button b = getLastButton();b!=null;b=b.getPrevious()){
				b.click(m.mousePoint.x, m.mousePoint.y);
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
			NPC npco = new NPCRevive(m,0,0,npcType,(Integer) npcType.getArgs()[0]);
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
}
