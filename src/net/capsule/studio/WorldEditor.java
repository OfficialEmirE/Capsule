package net.capsule.studio;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse; // Mouse kontrolü için import

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.InputHandler;
import me.ramazanenescik04.diken.Vec2D;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.nodes.Decal;
import me.ramazanenescik04.diken.game.nodes.Part;
import me.ramazanenescik04.diken.game.nodes.Sky;
import me.ramazanenescik04.diken.game.nodes.SpawnLocation;
import me.ramazanenescik04.diken.game.nodes.Texture;
import me.ramazanenescik04.diken.game.nodes.Tool;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import net.capsule.Capsule;
import net.capsule.game.GameScreen;
import net.capsule.game.Player;
import net.capsule.util.Util;

public class WorldEditor extends Screen {
    
    protected World theWorld;
    protected EditorWorld editorPanel;
    private Node selectedNode = new Part(0, 0, 16, 16);
    protected boolean isPlayTestMode = false;
    
    // TOOL ID'leri:
    // 0 = Place (Yerleştirme - Henüz kullanmıyoruz ama kodda var)
    // 1 = Move (Taşıma)
    // 2 = Scale (Boyutlandırma)
    // 3 = Select (Sadece Seçim)
    protected int selectedTool = 3; // Varsayılan: Select Mode

    // Sürükleme işlemleri için değişkenler
    private boolean isDragging = false;
    private int dragOffsetX, dragOffsetY; // Tıklanan yer ile obje köşesi arasındaki fark

    protected Panel topPanel;
    protected Panel leftPanel;
    
    protected int gridSize = 16; // Izgara boyutu
    protected boolean gridEnabled = true; // Izgara açık mı?

    // Sayıyı en yakın ızgara katına yuvarlayan yardımcı metod
    private int snap(int value) {
        if (!gridEnabled) return value;
        return Math.round((float) value / gridSize) * gridSize;
    }
    
    private void deleteSelectedNode() {
        if (selectedNode != null && selectedNode != theWorld.root) {
            // Objeyi ebeveyninden (parent) kaldırıyoruz
            if (selectedNode.getParent() != null) {
                selectedNode.getParent().removeChild(selectedNode);
            } else {
                // Eğer ebeveyni yoksa direkt root'tan silmeyi dene
                theWorld.root.removeChild(selectedNode);
            }
            
            System.out.println(selectedNode.getName() + " silindi.");
            
            selectedNode = null; // Seçimi temizle
            updateNodeList();    // Sol paneli güncelle
        }
    }

    // ... (generateTestWorld metodu aynı kalıyor, buraya eklemiyorum yer kaplamasın diye) ...
    @SuppressWarnings("unused")
	private void generateTestWorld() {
        Part part = new Part(100, 100, 200, 200);
        part.setSolid(false);
        Texture texture = new Texture();
        texture.setTexture(((ArrayBitmap)theWorld.getResource("materials", EnumResource.IMAGE)).getBitmap(0, 5));
        part.addChild(texture);
        for (int i = 0; i < 3; i++) {
            Part frower = new Part(100 * i, 100, 32, 32);
            frower.setAnchored(false);
            frower.name = "Chest " + i; // İsimlendirme önemli
            Decal decal = new Decal();
            decal.setTexture(((ArrayBitmap)theWorld.getResource("materials", EnumResource.IMAGE)).getBitmap(11, 1));
            frower.addChild(decal);
            part.addChild(frower);
        }
        theWorld.addNode(part);
        theWorld.addNode(new SpawnLocation(100, 100, 16, 16));
        theWorld.addNode(new SpawnLocation(-100, -100, 16, 16));
        theWorld.addNode(new SpawnLocation(-100, 100, 16, 16));
        theWorld.addNode(new SpawnLocation(100, -100, 16, 16));
        
        Part part2 = new Part(300, 100, 200, 200);
        part2.name = "MaterialTest";
        part2.setSolid(false);
        Texture texture2 = new Texture();
        texture2.setTexture(((ArrayBitmap)theWorld.getResource("materials", EnumResource.IMAGE)).getBitmap(4, 2));
        part2.addChild(texture2);
        theWorld.addNode(part2);
        
        Tool sword = new Tool("Sword", 100, 0);
        sword.setIcon(((ArrayBitmap)theWorld.getResource("materials", EnumResource.IMAGE)).getBitmap(1, 14));
        theWorld.addNode(sword);
        
        Tool grass = new Tool("Grass", 200, 0);
        grass.setIcon(((ArrayBitmap)theWorld.getResource("materials", EnumResource.IMAGE)).getBitmap(0, 0));
        theWorld.addNode(grass);
        
        Tool tool = new Tool("Ball", 0, 0);
        tool.setIcon(((ArrayBitmap)theWorld.getResource("materials", EnumResource.IMAGE)).getBitmap(0, 14));
        theWorld.addNode(tool);
    }

    public WorldEditor(int gameID) {
    	if (gameID < 0) {
    		this.theWorld = new World("TestGame", 100, 100);
            theWorld.addResource("materials", ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "materials")));
            theWorld.addNode(new Sky(0xffcefbf9 + 0xff7d7d7d));
            
            Player tool = new Player(100, 0);
            tool.setName("NPC-Capsule");
            tool.setUserAvatar("Capsule");
            theWorld.addNode(tool);
            //generateTestWorld();
    	} else {
    		theWorld = Util.downloadGame(Capsule.instance.account.getApiKey().toString(), gameID, null);
			theWorld.addResource("materials", ResourceLocator.getResource(new ResourceLocator.ResourceKey("capsule", "materials")));
    	}
    }

    // ... (startPlayTest ve updateNodeList metodları aynı) ...
    public void startPlayTest() {
        this.engine.setCurrentScreen(new GameScreen(this, theWorld));
    }
    
    public void updateNodeList() {
        if (leftPanel == null) return;
        leftPanel.clear();

        int yOffset = 30;

        for (Node node : theWorld.root.getChildren()) {
            String nodeName = (node.name != null && !node.name.isEmpty())
                    ? node.name
                    : node.getClass().getSimpleName();

            // Ana node butonu
            Button nodeBtn = new Button(nodeName, 5, yOffset, 190 - 70, 20);
            nodeBtn.setRunnable(() -> {
                this.selectedNode = node;
            });
            leftPanel.add(nodeBtn);

            int btnX = 128; // 5 + 190 + 5 boşluk

            // 1. küçük buton
            Button btn1 = new Button("A", btnX, yOffset, 20, 20);
            btn1.setRunnable(() -> {
                System.out.println("Button A: " + nodeName);
            });
            leftPanel.add(btn1);

            // 2. küçük buton
            Button btn2 = new Button("B", btnX + 24, yOffset, 20, 20);
            btn2.setRunnable(() -> {
                System.out.println("Button B: " + nodeName);
            });
            leftPanel.add(btn2);

            // 3. küçük buton
            Button btn3 = new Button("C", btnX + 48, yOffset, 20, 20);
            btn3.setRunnable(() -> {
                System.out.println("Button C: " + nodeName);
            });
            leftPanel.add(btn3);

            yOffset += 25;
        }
    }


    public void openScreen() {
        isPlayTestMode = false;
        this.getContentPane().clear();
        
        // Üst Panel
        topPanel = new Panel(0, 0, engine.getWidth(), 52);
        topPanel.setBackground(new StaticBackground(Bitmap.createClearedBitmap(64, 64, 0xffffffff)));
        
        // Play Butonu
        topPanel.add(new Button("Play", 2, 2, 40, 20).setRunnable(() -> startPlayTest()));
        
        // Export Butonu
        topPanel.add(new Button("Export", 45, 2, 50, 20).setRunnable(() -> {
            if (!this.isPlayTestMode) {
                try {
                	File exportFile = EditorUtil.openSaveWorldDialog();
                	if (exportFile != null) {
                		 World.saveWorld(theWorld, exportFile);
                	} 
                } catch (IOException e) { e.printStackTrace(); }
            }
        }));
        
        topPanel.add(new Button("Import", 45, 24, 50, 20).setRunnable(() -> {
            if (!this.isPlayTestMode) {
                try {
                	File importFile = EditorUtil.openLoadWorldDialog();
                	if (importFile != null) {
                		theWorld = World.loadWorld(importFile);	
                	}
                } catch (IOException | ReflectiveOperationException e) { e.printStackTrace();}
                resetEditorPanel();
            }
        }));

        // --- YENİ MODE BUTTONLARI ---
        
        // SELECT MODE (Seçim)
        topPanel.add(new Button("Select", 110, 2, 50, 20).setRunnable(() -> {
            this.selectedTool = 3;
            System.out.println("Mode: Select");
        }));

        // MOVE MODE (Taşıma)
        topPanel.add(new Button("Move", 165, 2, 50, 20).setRunnable(() -> {
            this.selectedTool = 1;
            System.out.println("Mode: Move");
        }));

        // SCALE MODE (Boyutlandırma)
        topPanel.add(new Button("Scale", 220, 2, 50, 20).setRunnable(() -> {
            this.selectedTool = 2;
            System.out.println("Mode: Scale");
        }));
        
        topPanel.add(new Button("Place", 275, 2, 50, 20).setRunnable(() -> {
            this.selectedTool = 0;
            // Yeni bir parça oluştur ki yerleştirebilelim
            this.selectedNode = new Part(0, 0, 16, 16); 
            //this.selectedNode.setSolid(false);
            System.out.println("Mode: Place");
        }));

        // DELETE BUTTON
        topPanel.add(new Button("Delete", 330, 2, 50, 20).setRunnable(() -> {
        	deleteSelectedNode();
        }));

        // GRID TOGGLE (Izgarayı aç/kapat)
        topPanel.add(new Button("Grid", 385, 2, 40, 20).setRunnable(() -> {
            this.gridEnabled = !this.gridEnabled;
        }));
        
        topPanel.add(new Button("Publish", engine.getWidth() - 54, 2, 50, 20).setRunnable(() -> {
            this.engine.setCurrentScreen(new PublishGameScreen(this, this.theWorld));
        }));
        
        // Sol Panel
        leftPanel = new Panel(0, 52, 200, engine.getHeight() - 52);
        leftPanel.setBackground(new StaticBackground(Bitmap.createClearedBitmap(1, 1, 0xffdddddd)));
        
        // Dünya sınırlarını ayarla
        editorPanel = new EditorWorld();
        editorPanel.add(this.theWorld);
        editorPanel.resized();
        
        this.getContentPane().add(editorPanel);
        this.getContentPane().add(topPanel);
        this.getContentPane().add(leftPanel);
        
        updateNodeList();
    }
    
    private void resetEditorPanel() {
    	if (this.getContentPane().isVaild(editorPanel)) {
    		this.getContentPane().remove(editorPanel);
    	}
    	
    	editorPanel = new EditorWorld();
        editorPanel.add(this.theWorld);
        editorPanel.resized();
        
        this.getContentPane().add(editorPanel);
        
        updateNodeList();
    }
    
    @Override
	public void keyDown(char eventCharacter, int eventKey) {
		super.keyDown(eventCharacter, eventKey);
		
		if (!this.isPlayTestMode) {
			if (eventKey == Keyboard.KEY_DELETE || eventKey == Keyboard.KEY_BACK) {
	             deleteSelectedNode();
	        }
		}
	}

	// ... (closeScreen, render, tick, resized metodları aynı) ...
    public void closeScreen() { isPlayTestMode = true; }

    public void render(Bitmap bitmap) {
        super.render(bitmap);
        bitmap.drawLine(0, 52, engine.getWidth(), 52, 0xffffffff, 1); // Üst çizgi
        bitmap.drawLine(200, 52, 200, engine.getHeight(), 0xffffffff, 1); // Sol çizgi
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isPlayTestMode) {
            // Kamera hareketi
            if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP)) 
                this.theWorld.camera = this.theWorld.camera.add(new Vec2D(0, -4));
            if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT)) 
                this.theWorld.camera = this.theWorld.camera.add(new Vec2D(-4, 0));
            if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN)) 
                this.theWorld.camera = this.theWorld.camera.add(new Vec2D(0, 4));
            if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) 
                this.theWorld.camera = this.theWorld.camera.add(new Vec2D(4, 0));
        }
    }

    @Override
    public void resized() {
        if (topPanel != null) topPanel.setBounds(0, 0, engine.getWidth(), 52);
        if (leftPanel != null) leftPanel.setBounds(0, 52, 200, engine.getHeight() - 52);
        if (editorPanel != null) editorPanel.resized();
    }
    
    private class EditorWorld extends Panel {
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public void resized() {
			this.setBounds(200, 52, engine.getWidth() - 200, engine.getHeight() - 52);
			theWorld.setSize(width, height);
		}
    	
    	@Override
        public void tick(DikenEngine engine) {
            super.tick(engine);
            
            if (this.isActive()) {
                // MOUSE SÜRÜKLEME MANTIĞI (MOVE & SCALE)
                // Sol tık basılıysa ve bir node seçiliyse
                if (Mouse.isButtonDown(0) && selectedNode != null && isDragging) {
                    
                    // Mouse'un dünyadaki konumu
                    int worldMouseX = (int) (InputHandler.getMousePosition().x + theWorld.camera.x()) - this.x;
                    int worldMouseY = (int) (InputHandler.getMousePosition().y + theWorld.camera.y()) - this.y;

                    if (selectedTool == 1) { // MOVE
                        selectedNode.x = snap(worldMouseX - dragOffsetX);
                        selectedNode.y = snap(worldMouseY - dragOffsetY);
                    }
                    else if (selectedTool == 2) { // SCALE MODE
                        // Yeni genişlik/yükseklik hesapla (Mouse konumu - Obje başlangıç konumu)
                        int newWidth = snap(worldMouseX - selectedNode.x);
                        int newHeight = snap(worldMouseY - selectedNode.y);
                        
                        // Minimum boyut kontrolü (ters dönmemesi için)
                        if (newWidth < 8) newWidth = 8;
                        if (newHeight < 8) newHeight = 8;
                        
                        if (newWidth < gridSize) newWidth = gridSize;
                        if (newHeight < gridSize) newHeight = gridSize;

                        // Not: Node sınıfında width/height public varsayıyorum. 
                        // Eğer değilse cast etmen gerekir: ((Part)selectedNode).width = ...
                        selectedNode.setAABB(newWidth, newHeight);
                    }
                } else {
                    // Mouse bırakıldıysa sürüklemeyi bitir
                    isDragging = false;
                }
            }
        }

        @Override
        public void mouseClicked(int relMouseX, int relMouseY, int button, boolean isTouch2) {
            super.mouseClicked(relMouseX, relMouseY, button, isTouch2);
            
            this.setActive(isTouch2);
            
            if (button == 0 && this.isActive()) {
                // Mouse'un dünyadaki konumu
                int worldMouseX = (int) (InputHandler.getMousePosition().x + theWorld.camera.x()) - this.x;
                int worldMouseY = (int) (InputHandler.getMousePosition().y + theWorld.camera.y()) - this.y;

                // 1. ADIM: Zaten seçili olan objeye mi tıkladık? Kontrol edelim.
                boolean clickedOnCurrentSelection = false;
                if (selectedNode != null) {
                	if (selectedNode.getGlobalAABB() == null) {
                		return;
                	}
                    // Basit AABB (Kutu) çarpışma kontrolü
                    if (worldMouseX >= selectedNode.x && worldMouseX <= selectedNode.x + selectedNode.getGlobalAABB().width &&
                        worldMouseY >= selectedNode.y && worldMouseY <= selectedNode.y + selectedNode.getGlobalAABB().height) {
                        clickedOnCurrentSelection = true;
                    }
                }

                if (selectedTool == 0) { // PLACE MODE
                	if (selectedNode != null) {
                		Node newNode = selectedNode.copy();
                        if (newNode.getGlobalAABB() != null) {
                        	newNode.x = snap(worldMouseX - (newNode.getGlobalAABB().width / 2));
                            newNode.y = snap(worldMouseY - (newNode.getGlobalAABB().height / 2));
                        }
                        
                        theWorld.addNode(newNode);
                        updateNodeList();
                	}          
                } else if (clickedOnCurrentSelection && (selectedTool == 1 || selectedTool == 2)) {
                    
                    startDragAction(worldMouseX, worldMouseY);
                    
                } else {
                    // 3. ADIM: Yeni seçim yapma mantığı (Select modu veya boşluğa/başka objeye tıklama)
                    Hitbox mouseHitbox = InputHandler.getMouseHitbox();
                    mouseHitbox.x = worldMouseX;
                    mouseHitbox.y = worldMouseY;

                    List<Node> results = theWorld.root.findInArea(mouseHitbox);
                    
                    if (results.size() > 0) {
                        // DÜZELTME: results.get(0) yerine listenin SON elemanını alıyoruz.
                        // Çünkü genellikle son eklenen/çizilen eleman (en üstteki) listenin sonundadır.
                        // Eğer motorun ters çalışıyorsa burayı tekrar 0 yapabilirsin ama %90 sonuncudur.
                        selectedNode = results.get(results.size() - 1);
                        
                        // Seçim değiştiği an eğer mod Move/Scale ise hemen sürüklemeye başlasın
                        if (selectedTool == 1 || selectedTool == 2) {
                            startDragAction(worldMouseX, worldMouseY);
                        }
                    } else {
                        // Boşluğa tıklayınca seçimi kaldır (İsteğe bağlı, kaldırmak istemiyorsan sil)
                        selectedNode = null;
                    }
                }
            }
        }
        
        // Kod tekrarını önlemek için yardımcı metod
        private void startDragAction(int worldMouseX, int worldMouseY) {
            if (selectedTool == 1) { // Move
                dragOffsetX = worldMouseX - selectedNode.x;
                dragOffsetY = worldMouseY - selectedNode.y;
                isDragging = true;
            } else if (selectedTool == 2) { // Scale
            	if (selectedNode.getGlobalAABB() == null) {
            		return;
            	}
            	
                isDragging = true;
            }
        }

        @Override
        public Bitmap render() {
            var bitmap = super.render();
            
            if (gridEnabled) {
                int color = 0x22ffffff; // Çok hafif şeffaf beyaz
                // Dikey çizgiler
                for (int x = snap((int)theWorld.camera.x()); x < theWorld.camera.x() + this.width; x += gridSize) {
                    bitmap.drawLine((int)(x - theWorld.camera.x()), 0, (int)(x - theWorld.camera.x()), this.height, color, 1);
                }
                // Yatay çizgiler
                for (int y = snap((int)theWorld.camera.y()); y < theWorld.camera.y() + this.height; y += gridSize) {
                    bitmap.drawLine(0, (int)(y - theWorld.camera.y()), this.width, (int)(y - theWorld.camera.y()), color, 1);
                }
            }
            
            // Seçili nesnenin etrafına kutu çizme
            if (selectedNode != null) {
                int color = 0xffffff00; // Sarı
                
                // Modlara göre renk değiştirelim ki anlaşılsın
                if (selectedTool == 3) color = 0xff00ff00; // Select: Yeşil
                if (selectedTool == 1) color = 0xffff0000; // Move: Kırmızı
                if (selectedTool == 2) color = 0xff0000ff; // Scale: Mavi
                
                if (selectedNode.getGlobalAABB() != null) {
                	bitmap.box((int) (selectedNode.getGlobalX() - theWorld.camera.x()), (int) (selectedNode.getGlobalY() - theWorld.camera.y()),
                            (int) (selectedNode.getGlobalX() - theWorld.camera.x()) + selectedNode.getGlobalAABB().width, 
                            (int) (selectedNode.getGlobalY() - theWorld.camera.y()) + selectedNode.getGlobalAABB().height, 
                            color);
            	}
            }
            
            return bitmap;
        }
        
        
    }
}