package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.*;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapFont.Align;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.CartoonEdgeFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication implements PhysicsCollisionListener, PhysicsCollisionGroupListener {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    
    //instance fields
            
    //physics
    
    private BulletAppState bulletAppState;    
         
    //materials + loaded assets
    
    private Material platformMaterial;
    private Material starMat;
    private Spatial runCharacter;
    private Spatial runCharacter2;
    private Spatial runCharacter3;

    
    //instances
    
    private Terrain terrain;
    
    private Player user; //user
    private ArrayList<Player> aiList; //all ai
    private Player bestAI; //best ai(loaded from text file), not in aiList
    private NeuralNet bestNN; //neural network of the best ai
    
    private Player playerFollowing; //pointer to the best player
    
    public static final float[][] COLOR_LIST = {{(float)(83.0/255.0), (float)(217.0/255.0), (float)(142.0/255.0)}, 
    {(float)(250.0/255.0), (float)(110.0/255.0), (float)(66.0/255.0)}, {(float)(26.0/255.0), (float)(149.0/255.0), (float)(182.0/255.0)}
        }; //list of the colors of the players
    
    private ArrayList<Star> starList; //stars
    private float starZDistance = 0; //used for spawning
    
    //numbers + booleans
    
    
    //settings
    
    public static final int NUM_PLATFORMS = 10;
    public static final int NUM_AI = 30;
    public static final Vector3f STARTING_POSITION = new Vector3f(0, 5, -30);
    public static final int YOUNG_BONUS_AGE_THRESHOLD = 10;
    public static final double YOUNG_FITNESS_BONUS = 1.2;
    public static final int OLD_AGE_THRESHOLD = 50;
    public static final double OLD_AGE_PENALTY = 0.7;
    public static final int NUM_GENS_ALLOWED_NO_IMPROVEMENT = 3;
    public static final double CROSSOVER_RATE = 0.6;
    public static final int MAX_PERMITTED_NEURONS = 12;
    public static final double CHANCE_ADD_NODE = 0.05;
    public static final int NUM_TRYS_TO_FIND_OLD_LINK = 10;
    public static final double CHANCE_ADD_LINK = 0.1;
    public static final double CHANCE_ADD_RECURRENT_LINK = 0.0;
    public static final int NUM_TRYS_TO_FIND_LOOPED_LINK = 0;
    public static final int NUM_ADD_LINK_ATTEMPTS = 10;
    public static final double MUTATION_RATE = 0.3;
    public static final double PROBABILITY_WEIGHT_REPLACED = 0.1;
    public static final double MAX_WEIGHT_PERTUBATION = 0.5;
    public static final double ACTIVATION_MUTATION_RATE = 0.1;
    public static final double MAX_ACTIVATION_PERTUBATION = 0.5;
    public static final float BOTTOM_LIMIT = 2.5f;
    public static final float TOP_LIMIT = 25f;
    public static final float STAR_START = 500;
    
    
    private Ga NEAT; //neat object
    private int maxGenerations;
    
    private int time;
    private int round;
    private boolean pause;
    
    //text
    
    private BitmapText genText;
    private BitmapText scoreText;
    private BitmapText infoText;
    private BitmapText titleText;
    private BitmapText neatText;
    
    private boolean hidingNeatText;
    
    //stuff for the neural network
    private ArrayList<Geometry> nodeList;
    private ArrayList<Geometry> lineList;
    
    //stuff for graph
    private ArrayList<Geometry> pointList;
    private ArrayList<Geometry> connectList;
    
    //text file
    
    private TextFileReader genomeReader;
    
    //for deciding what gamemode is currently going on
    //gamemodes
    //only player plays - 1
    //comp + best ai - 2
    //training - 3
    
    private int gamemode;
    
    //for switching between scenes
    
    private boolean isPlaying; //if you are playing
    
    private int gamesPlayed;
    private int userWon;
    
    //best score ever, since the program was started
    private int bestScore;
    
        
    //methods

    @Override
    public void simpleInitApp() { //initialize everything
       
        
        //initialize all instance fields
        bestScore = 0;
        gamesPlayed = 0;
        userWon = 0;
        gamemode = 3;
        isPlaying = true;
        time = 0;
        pause = false;
        nodeList = new ArrayList<>();
        lineList = new ArrayList<>();
        connectList = new ArrayList<>();
        pointList = new ArrayList<>();
        round = 1;
        
        //initialize materials and loaded assets
        
        platformMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        runCharacter = assetManager.loadModel("Models/runcharacter1.j3o");
        runCharacter2 = assetManager.loadModel("Models/runcharacter2.j3o");
        runCharacter3 = assetManager.loadModel("Models/runcharacter3.j3o");

        //initialize physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -40f, 0));
        
        //camera position and rotation set
        
        cam.setLocation(new Vector3f(0, 0, -5));
        cam.setRotation(new Quaternion(0.08f, -0.007f, 0, 1f));      
        cam.setFrustumPerspective(45, settings.getWidth()/settings.getHeight(), 1, 1000000);
        
        
        //lighting
        
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(0f,0.5f,1f).normalizeLocal());
        rootNode.addLight(sun); 
        
        //cool thing, adds borders to everything
        
        FilterPostProcessor fpp=new FilterPostProcessor(assetManager);

        fpp.addFilter(new CartoonEdgeFilter());

        viewPort.addProcessor(fpp);
        
        //initailize graphics + objects
        initTerrain();
        initUser();
        initBest();
        initAI();
        initStars();
        
        //keyboard
                
        inputManager.addMapping("moveRight",  new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("moveLeft",  new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("jump",  new KeyTrigger(KeyInput.KEY_SPACE));
        
        
        inputManager.addMapping("skip",  new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("pause",  new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("gamemode1",  new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("gamemode2",  new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping("gamemode3",  new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping("hide",  new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("hide",  new KeyTrigger(KeyInput.KEY_H));

        inputManager.addMapping("restart", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("back", new KeyTrigger(KeyInput.KEY_B));
        inputManager.addMapping("continue", new KeyTrigger(KeyInput.KEY_C));

        inputManager.addMapping("regular", new KeyTrigger(KeyInput.KEY_4));
        inputManager.addMapping("pirate", new KeyTrigger(KeyInput.KEY_5));
        inputManager.addMapping("hat", new KeyTrigger(KeyInput.KEY_6));
        inputManager.addMapping("name", new KeyTrigger(KeyInput.KEY_N));
        
        inputManager.addMapping("hideneat", new KeyTrigger(KeyInput.KEY_Q));

        
        inputManager.addListener(actionListener, "hideneat", "name", "regular", "pirate", "hat","pause", "skip", "gamemode1", "gamemode2", "gamemode3", "hide", "continue", "back", "restart");
        inputManager.addListener(analogListener, "moveRight", "moveLeft", "jump");
        
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
        bulletAppState.getPhysicsSpace().addCollisionGroupListener(this, 1);
        
        //text
        
        initText();
        
        //text file
        
        genomeReader = new TextFileReader("src/textfiles/encoding.txt");

        bestNN = genomeReader.getGenome(); //loads the current genome saved
        
        //neat
        NEAT = new Ga(Main.NUM_AI, 2, 3);
        maxGenerations = 10;
        
        neatText.setText("Neural Network of AI Still Alive\n\nBest Saved AI Score: " + (int)genomeReader.getCurrentFitness() + "\nNumber of Members: " + Main.NUM_AI + "\nNumber of Species: " + NEAT.getNumSpecies() + "\nNumber of Innovations: " + NEAT.getNumInnovations() + "\n\nPress q to hide text");

        
        user.setAlwaysDead(true);
        bestAI.setAlwaysDead(true);
        for(Player p : aiList) {
            p.setAlwaysDead(false);
        }
        
        System.out.println("called*****************************************************************");
                
        
    }
    
    //stars
    
    private void initStars() {
        starMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        starMat.setColor("Color",ColorRGBA.White);
        starList = new ArrayList<>();
        createStars(STAR_START);
    }
    
    private void createStars(float max) { //creates stars until some max

        while(starZDistance < max) {
            
            float x;
            do {
                x = Rand.randomInt(-200, 200);
            } while(x > -40 && x < 40);
            
            float y;
            do {
                y = Rand.randomInt(-50, 150);
            } while(y > 0 && y < 40);
            
            Star s = new Star(starMat, new Vector3f(x,y , starZDistance));
            rootNode.attachChild(s.body);
            starZDistance += Rand.randomNumber(5, 10);
            starList.add(s);
        }
    }
    
    //text
    
    private void initText() {
        BitmapFont bigFont = assetManager.loadFont("Interface/Fonts/Oswald.fnt");
        BitmapFont comicSans = assetManager.loadFont("Interface/Fonts/ErasBoldITC.fnt");
        
        genText = new BitmapText(bigFont, false);
        genText.setSize(50);      // font size
        genText.setColor(ColorRGBA.White);                             // font color
        genText.setText("Generation: 0");             // the text
        genText.setLocalTranslation(this.settings.getWidth() / 2 - 100, this.settings.getHeight() - 35, 0); // position
        guiNode.attachChild(genText);
        
        scoreText = new BitmapText(bigFont, false);
        scoreText.setSize(50);      // font size
        scoreText.setColor(ColorRGBA.White);                             // font color
        scoreText.setText("Generation: 0");             // the text
        scoreText.setLocalTranslation(this.settings.getWidth() / 2 - 100, this.settings.getHeight() - 125, 0); // position
        guiNode.attachChild(scoreText);
        
        titleText = new BitmapText(bigFont, false);
        titleText.setSize(65);      // font size
        titleText.setColor(new ColorRGBA(0, 0.749f, 1, 1));                             // font color
        titleText.setText("NeuroRun");             // the text
        titleText.setLocalTranslation(50, this.settings.getHeight() - 200, 0); // position
        guiNode.attachChild(titleText);
        
        infoText = new BitmapText(bigFont, false);
        infoText.setSize(30);      // font size
        infoText.setColor(ColorRGBA.White);                             // font color
        infoText.setText("Welcome, the AI are training right now\nso they can beat you later\n\nClick p to pause\nClick s to skip gen\nClick c to have the training stop\nClick r to restart all training\nClick h to hide/show");             // the text
        infoText.setLocalTranslation(50, this.settings.getHeight() - 300, 0); // position
        guiNode.attachChild(infoText);
        
        neatText = new BitmapText(bigFont, false);
        neatText.setSize(30);      // font size
        neatText.setColor(ColorRGBA.White);                             // font color
        neatText.setText("");
        neatText.setLocalTranslation(this.settings.getWidth() - 450, this.settings.getHeight() - 450, 0); // position
        guiNode.attachChild(neatText);
        
    }
    
    //graphics + objects
    
    private void initTerrain() { //initialize the terrain
        terrain = new Terrain();
                
        //put on screen
        for(int i = 0; i < NUM_PLATFORMS; i++) {
                       
            Platform created = terrain.addPlatform(platformMaterial, true, gamemode == 2);
            //puts on screen
            rootNode.attachChild(created.geometry);
            
            //add to physics space
            
            bulletAppState.getPhysicsSpace().add(created.rb);
        
        }
    }
    
    //initialize the user
    private void initUser() {
        
        //does graphic
        
        Spatial userGraphic = runCharacter.clone();
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        mat.setColor("Color", new ColorRGBA(COLOR_LIST[0][0], COLOR_LIST[0][1], COLOR_LIST[0][2], 1));
            
        userGraphic.setMaterial(mat);
        userGraphic.rotate(new Quaternion (.707f, 0, 0, 0.707f));
        userGraphic.setLocalScale(0.25f);
        userGraphic.setName("user");
        
        //initialize
        
        user = new Player("ashay1", 0, false, STARTING_POSITION, userGraphic, -1, COLOR_LIST[0], "pirate");
                       
        //finialize and add
                
        rootNode.attachChild(user.geometry);
        
        bulletAppState.getPhysicsSpace().add(user.rb);
        
        user.rb.setLinearVelocity(new Vector3f(0, 0, 30));

        

    }
    
    //initialize the best ai
    private void initBest() {
        //does graphic
       
        Spatial userGraphic3 = runCharacter.clone();
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        
        mat.setColor("Color", new ColorRGBA(COLOR_LIST[1][0], COLOR_LIST[1][1], COLOR_LIST[1][2], 255));
        
        
        userGraphic3.setMaterial(mat);
        
        userGraphic3.rotate(new Quaternion (.707f, 0, 0, 0.707f));
        userGraphic3.setLocalScale(0.25f);
        userGraphic3.setName("best");
        
        //initialize
        
        bestAI = new Player("ashay2", 0, true, STARTING_POSITION, userGraphic3, terrain.getPlatform(1).getNumber(), COLOR_LIST[1], "pirate");
                       
        //finialize and add
                
        rootNode.attachChild(bestAI.geometry);
        
        bulletAppState.getPhysicsSpace().add(bestAI.rb);
        
        bestAI.rb.setLinearVelocity(new Vector3f(0, 0, 30));
    }
    
    //initializes the neat ai
    private void initAI() {
        
        aiList = new ArrayList<>();
        
        //create all the ai 
        for(int i = 0; i < NUM_AI; i++) {
            Spatial userGraphic2 = runCharacter.clone();
           
            
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                        
            mat.setColor("Color", new ColorRGBA(COLOR_LIST[2][0], COLOR_LIST[2][1], COLOR_LIST[2][2], 1));
            
            userGraphic2.setMaterial(mat);
            
            userGraphic2.rotate(new Quaternion (.707f, 0, 0, 0.707f));
            userGraphic2.setLocalScale(0.25f);
            userGraphic2.setName("player" + i + 1);
        
            
            //initialize
        
            Player player = new Player("gamer", 0, true, STARTING_POSITION, userGraphic2, terrain.getPlatform(1).getNumber(), COLOR_LIST[2], "pirate");
                       
            //finialize and add
                
            rootNode.attachChild(player.geometry);
        
            bulletAppState.getPhysicsSpace().add(player.rb);
        
            player.rb.setLinearVelocity(new Vector3f(0, 0, 30));
            
            aiList.add(player);
        }
        
        
    }
    
    //keyboard
    
    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            
            if(keyPressed) return;
            
            
            //applies to all scenes      
            if(name.equals("hide")) { //hides the text
                System.out.println("yeeted");
                if(titleText.getCullHint().equals(CullHint.Never)) { //text is shown, make invisible
                    titleText.setCullHint(CullHint.Always);
                    infoText.setCullHint(CullHint.Always);
                    
                } else { //text is hidden, show it
                    titleText.setCullHint(CullHint.Never);
                    infoText.setCullHint(CullHint.Never);
                }
            }
            
            if(!isPlaying) { //currently frozen
                
                //changing gamemode 
                
            if(name.equals("gamemode1") && !keyPressed) { //user
                freezeGraphics(false);
                gamemode = 1;
                changeInfoText(1);
                for(Player p : aiList) {
                    p.setAlwaysDead(true);
                }
                user.setAlwaysDead(false);
                bestAI.setAlwaysDead(true);
                round = 1;
                resetGame();
                isPlaying = true;
                deleteNN();
            } else if(name.equals("gamemode2") && !keyPressed) { //user + best
                freezeGraphics(false);
                gamemode = 2;
                changeInfoText(2);
                round = 1;
                bestNN = genomeReader.getGenome();
                bestAI.setAlwaysDead(false);
                user.setAlwaysDead(false);
                for(Player p : aiList) {
                    p.setAlwaysDead(true);
                }
                resetGame();
                isPlaying = true;
            } else if(name.equals("gamemode3") && !keyPressed) { //neat
                freezeGraphics(false);
                gamemode = 3;
                changeInfoText(3);
                round = 1;
                for(Player p : aiList) {
                    p.setAlwaysDead(false);
                }
                bestAI.setAlwaysDead(true);
                user.setAlwaysDead(true);
                NEAT = new Ga(Main.NUM_AI, 2, 3);
                resetGame();
                isPlaying = true;
                userWon = 0;
                gamesPlayed = 0;
                hidingNeatText = false;
                neatText.setText("Neural Network of AI Still Alive\n\nBest Saved AI Score: " + (int)genomeReader.getCurrentFitness() + "\nNumber of Members: " + Main.NUM_AI + "\nNumber of Species: " + NEAT.getNumSpecies() + "\nNumber of Innovations: " + NEAT.getNumInnovations() + "\n\nPress q to hide text");

            } else if(name.equals("regular")) {
                //removes the previous geometry and rigidbody
                rootNode.detachChild(user.geometry);
                
                bulletAppState.getPhysicsSpace().remove(user.rb);

                //creates new graphic/spatial/geometry
                Spatial userGraphic = runCharacter.clone();
        
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
                mat.setColor("Color", new ColorRGBA(COLOR_LIST[0][0], COLOR_LIST[0][1], COLOR_LIST[0][2], 1));
            
                userGraphic.setMaterial(mat);
                userGraphic.rotate(new Quaternion (.707f, 0, 0, 0.707f));
                userGraphic.setLocalScale(0.25f);
                
                //sets geometry to the new one, and adds both geometry and rb to the scene
                user.changeSpatial(userGraphic);
                rootNode.attachChild(user.geometry);
                bulletAppState.getPhysicsSpace().add(user.rb);
            } else if(name.equals("pirate")) {//pirate
                
                rootNode.detachChild(user.geometry);
                
                bulletAppState.getPhysicsSpace().remove(user.rb);

                Spatial userGraphic = runCharacter3.clone();
        
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
                mat.setColor("Color", new ColorRGBA(COLOR_LIST[0][0], COLOR_LIST[0][1], COLOR_LIST[0][2], 1));
            
                userGraphic.setMaterial(mat);
                userGraphic.rotate(new Quaternion (.707f, 0, 0, 0.707f));
                userGraphic.setLocalScale(0.25f);
                
                user.changeSpatial(userGraphic);
                rootNode.attachChild(user.geometry);
                bulletAppState.getPhysicsSpace().add(user.rb);
                
            } else if(name.equals("hat")) { //hat(runcharacter 2)
                rootNode.detachChild(user.geometry);
                
                bulletAppState.getPhysicsSpace().remove(user.rb);

                Spatial userGraphic = runCharacter2.clone();
        
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
                mat.setColor("Color", new ColorRGBA(COLOR_LIST[0][0], COLOR_LIST[0][1], COLOR_LIST[0][2], 1));
            
                userGraphic.setMaterial(mat);
                userGraphic.rotate(new Quaternion (.707f, 0, 0, 0.707f));
                userGraphic.setLocalScale(0.25f);
                
                user.changeSpatial(userGraphic);
                rootNode.attachChild(user.geometry);
                bulletAppState.getPhysicsSpace().add(user.rb);

            } else if(name.equals("name")) { //cool feature
                if(infoText.getText().substring(infoText.getText().length()-1).equals(")")) { //remove aryan name
                    changeInfoText(4);
                } else { //add aryan name
                    infoText.setText(infoText.getText() + "\n\nName: Aryan (how did i know?)");
                }
            }
                
                
                
                
            } else if(gamemode == 1) {
                if (name.equals("pause") && !keyPressed) {
                
                    pause = !pause;

                    if(pause) { //on pause, must freeze
                        for(Player p : aiList) {
                         p.rb.setKinematic(true);
                        }
                        user.rb.setKinematic(true);
                        bestAI.rb.setKinematic(true);
                    } else { //not on pause, must unfreeze all
                        for(Player p : aiList) {
                            p.rb.setKinematic(false);
                        }
                        user.rb.setKinematic(false);
                        bestAI.rb.setKinematic(false);
                    
                    }
                } else if(name.equals("back")) {
                    
                    isPlaying = false;
                    freezeGraphics(true);
                    changeInfoText(4);
                    
                }
            } else if(gamemode == 2) {
                if (name.equals("pause") && !keyPressed) {
                
                    pause = !pause;

                    if(pause) { //on pause, must freeze
                        for(Player p : aiList) {
                         p.rb.setKinematic(true);
                        }
                        user.rb.setKinematic(true);
                        bestAI.rb.setKinematic(true);
                    } else { //not on pause, must unfreeze all
                        for(Player p : aiList) {
                            p.rb.setKinematic(false);
                        }
                        user.rb.setKinematic(false);
                        bestAI.rb.setKinematic(false);
                    
                    }
                }
            } else if(gamemode == 3) {
                if (name.equals("skip")) {
                    for(Player p : aiList) {
                        p.setDead();
                    }
                    user.setDead();
                } else if(name.equals("hideneat")) {
                    hidingNeatText = !hidingNeatText;
                    if(hidingNeatText) {
                        deleteNN();
                        neatText.setText("");
                    }
                }
                
                if (name.equals("pause") && !keyPressed) {
                
                    pause = !pause;

                    if(pause) { //on pause, must freeze
                        for(Player p : aiList) {
                         p.rb.setKinematic(true);
                        }
                        user.rb.setKinematic(true);
                        bestAI.rb.setKinematic(true);
                    } else { //not on pause, must unfreeze all
                        for(Player p : aiList) {
                            p.rb.setKinematic(false);
                        }
                        user.rb.setKinematic(false);
                        bestAI.rb.setKinematic(false);
                    
                    }
                } else if(name.equals("continue") && NEAT.generation > 1) {
                    gamemode = 2;
                    round = 1;
                    bestNN = genomeReader.getGenome();
                    bestAI.setAlwaysDead(false);
                    user.setAlwaysDead(false);
                    for(Player p : aiList) {
                        p.setAlwaysDead(true);
                    }
                    freezeGraphics(false);
                    changeInfoText(2);
                    resetGame();
                    deleteNN();
                    isPlaying = true;
                } else if(name.equals("restart")) {
                    genomeReader.clearSavedGenome();
                    NEAT.clearInnovations();
                    NEAT = new Ga(Main.NUM_AI, 2, 3);
                    neatText.setText("Neural Network of AI Still Alive\n\nBest Saved AI Score: " + (int)genomeReader.getCurrentFitness() + "\nNumber of Members: " + Main.NUM_AI + "\nNumber of Species: " + NEAT.getNumSpecies() + "\nNumber of Innovations: " + NEAT.getNumInnovations());
                    resetGame();
                }
            }
                  
        }
    };
    
    private final AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {
            
            Vector3f currentVelocity = user.rb.getLinearVelocity();
            
            if(!isPlaying || gamemode == 3) return; //not playing or the gamemode is 3(ai training)
            
            switch (name) {
                case "moveRight": //moving the user to the right
                    if(!user.getDead()) {
                        System.out.println("moved");
                        user.move(-75f);
                        //user.moveAI(-.1f);
                    }
                    break;
                case "moveLeft": //moving user to the left
                    if(!user.getDead()) {
                        System.out.println("moved");
                        user.move(75f);
                       // user.moveAI(0.1f);
                    }
                    break;
                case "jump": //must be on a platform to jump
                    if(user.getDead()) break;
                    
                    for(int i = 0; i < terrain.getNumPlatforms(); i++) { //each platform
                        CollisionResults results = new CollisionResults();
                        if(user.geometry.collideWith((Collidable)terrain.getPlatform(i).box, results) > 0) { //there is collision
                            System.out.println("jumped");
                            user.jump();
                            break;
                        }
                    }
                    break;
                default:
                    break;
            }
              
        }
    };
    
    
    //update stuff

    @Override
    public void simpleUpdate(float tpf) { //called every frame
        //TODO: add update code
        
        if(isPlaying) { //currently playing, not in between
        
            time++;
                
            if(round == 1 && time == 1) {
                resetGame();
            }
        
            if(needRestart()) { //need to restart
            
                
                if(time / 30 > bestScore && gamemode == 2) { //updates the highscore, as long as the gamemode is AI vs player
                    bestScore = time / 30;
                }
        
                
                //get all fitnesses
                if(gamemode == 3) {
                    ArrayList<Double> allFitnesses = new ArrayList<>();
                    System.out.println("fitnesses");
                    double bestScore = 0;
                    int bestIndex = -1;
                    for(int i = 0; i < aiList.size(); i++) {
                        allFitnesses.add((double)aiList.get(i).getFitness());
                        if((double)aiList.get(i).getScore()> bestScore) {
                            bestIndex = i;
                            bestScore = (double)aiList.get(i).getScore();
                        }
                    }
            
                    System.out.println("best index: " + bestIndex + "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            
                    //before epoch, updates the best genome
                    if(bestIndex != -1)
                        genomeReader.saveGenome(NEAT.getGenome(bestIndex), NEAT.getGenomeDepth(bestIndex), bestScore);
            
            
                    //epoch
            
                    NEAT.Epoch(allFitnesses);
                    
                    
                    String text = "Neural Network of AI Still Alive\n\nBest Saved AI Score: " + (int)genomeReader.getCurrentFitness() + "\nNumber of Members: " + Main.NUM_AI + "\nNumber of Species: " + NEAT.getNumSpecies() + "\nNumber of Innovations: " + NEAT.getNumInnovations();
                    
                    //finds the rounds average fitness, largest fitness, and smallest fitness overall

                    if(NEAT.generation >= 3) {
                        ArrayList<Double> avgFitness = NEAT.getAverageFitnesses();
                        
                        double min = Double.MAX_VALUE;
                        int i1 = -1;
                        double max = Double.MIN_VALUE;
                        int i2 = -1;
                        
                        double avg = 0;
                        
                        for(int i = 0; i < avgFitness.size(); i++) {
                            avg += avgFitness.get(i);
                            if(avgFitness.get(i) < min) {
                                min = avgFitness.get(i);
                                i1 = i;
                            }
                            if(avgFitness.get(i) > max) {
                                max = avgFitness.get(i);
                                i2 = i;
                            }
                        }
                        avg /= avgFitness.size();
                        
                        text += "\nPrevious Round Avg Fitness: " + avgFitness.get(avgFitness.size()-1).intValue() + "\nWorst Avg Fitness: " + (int)min + " (gen " + (i1 + 1) + ")\nBest Avg Fitness: " + (int)max + " (gen " + (i2 + 1) + ")" + "\nAvg of the Avg Fitnesses: " + (int)avg;
                        
                    }
                    
                    text += "\n\nPress q to hide text";
                    
                    //updates the neat text, so the user can see
                    if(!hidingNeatText) neatText.setText(text);
                    
                    //best ai saved fitness
                    //number of species
                    //number of innovations made
                }
            
            
                //now for restarting, if the gamemode is 1 or 3, you restart; in the actual game, you go back to the menu
            
                if(gamemode == 2) { //in between now
                    freezeGraphics(true);
                    isPlaying = false;
                    
                    gamesPlayed++;
                    if(getWinner() == 1) {
                        userWon++;
                    }
                    
                    changeInfoText(4);
                } else {
                    resetGame(); //reset

                }
            
                        
            
            } else if(!pause) { //game is running, update everything  
                setFollowedPlayer(); //sets the player to be followed
                if(gamemode == 1) { //only player plays
                    updateUser();
                } else if(gamemode == 2) { //player + best ai
                    updateUser();
                    bestMovement();
                    updateBest();
                    
                } else if(gamemode == 3) { //neat
                    //neat
                    aiMovement();
                    updateAI();  
                    setFollowedPlayer(); //in case the player died
                    if(!hidingNeatText) displayNN(playerFollowing);
                }

                //other stuff
                updateTerrain(playerFollowing.geometry.getLocalTranslation().z - 50);
                createStars(playerFollowing.geometry.getLocalTranslation().z + 500);
                updateCamera(playerFollowing.geometry.getLocalTranslation());            
                displayText();
            
            }
        
        }   
    }
    
    //update methods
    
    private void updateCamera(Vector3f followVector) { //paramter is vector to follow
        cam.setLocation(new Vector3f(followVector.x, 20, followVector.z - 40));
    }
    
    private void updateAI() { //updates the ai
        for(Player p : aiList) {
                        
            if(p.getDead()) {
                p.geometry.setCullHint(CullHint.Always);
                continue;
            } //if the player is dead, don't do anything
            
            //updates the target platform
            
            for(int j = 0; j < terrain.getNumPlatforms(); j++) { //each platform
                CollisionResults results = new CollisionResults();
                if(p.geometry.collideWith((Collidable)terrain.getPlatform(j).box, results) > 0) { //there is collision
                    p.changePlatform(terrain.getPlatform(j+1).getNumber());
                    boolean changed = terrain.getPlatform(j).changeColor(p.getColor());
                    //extra incentive for fitness: if you are the first to a platform, you get extra points
                    if(terrain.getPlatform(j).getNumber() != 1) { //makes sure it is not the first platform
                        p.increaseFitness(300); //big bonus
                    }
                    break;
                }
            }
            
            
            //increase score
            p.increaseScore();
            p.increaseFitness(2);
            
            p.updateCanJump();
            
            Vector3f angV = p.rb.getAngularVelocity();
            p.rb.setAngularVelocity(new Vector3f(0, 0, 0));
            p.geometry.rotate(new Quaternion (.707f, 0, 0, 0.707f));
        
            //now moves it forward(constant speed)
            p.rb.setLinearVelocity(new Vector3f(p.rb.getLinearVelocity().x, p.rb.getLinearVelocity().y, 30));
            
            if(p.geometry.getLocalTranslation().y < Main.BOTTOM_LIMIT || p.geometry.getLocalTranslation().y > Main.TOP_LIMIT) {
                p.setDead();
            }
        }
        
    }
    
    
    private void updateUser() { //updates the user by applying forwarwd force and other physics stuff
                
        if(user.getDead()) {
            user.geometry.setCullHint(CullHint.Always);
            return;
        }
        
        //updates the color
        for(int j = 0; j < terrain.getNumPlatforms(); j++) { //each platform
            CollisionResults results = new CollisionResults();
            if(user.geometry.collideWith((Collidable)terrain.getPlatform(j).box, results) > 0) { //there is collision
                terrain.getPlatform(j).changeColor(user.getColor());
                if(!terrain.getPlatform(j).checkSameColor(user.getColor()) && gamemode == 2 && terrain.getPlatform(j).getNumber() != 1) { //user is on the platform of another player
                    user.setDead();
                    user.geometry.setCullHint(CullHint.Always);
                    return;
                }
                break;
            }
        }
                
        //prevents user from flipping over(way of locking rotation)
        Vector3f angV = user.rb.getAngularVelocity();
        user.rb.setAngularVelocity(new Vector3f(0, 0, 0));
        user.geometry.rotate(new Quaternion (.707f, 0, 0, 0.707f));
        
        //now moves it forward(constant speed)
        user.rb.setLinearVelocity(new Vector3f(user.rb.getLinearVelocity().x, user.rb.getLinearVelocity().y, 30));
        
        //sets dead
        
        if(user.geometry.getLocalTranslation().y < Main.BOTTOM_LIMIT || user.geometry.getLocalTranslation().y > Main.TOP_LIMIT) {
            user.setDead();
        }
    }
    
    private void updateTerrain(double minZ) { //adds and removes appropriate platforms
        
        //first checks for removing
        
        int index = 0;
        //removes all platforms that fall behind
        while(index < terrain.getNumPlatforms()) {
            if(terrain.getPlatform(index).geometry.getLocalTranslation().z < minZ) { //below min z
                //removes from the screen
                rootNode.detachChild(terrain.getPlatform(index).geometry);
                bulletAppState.getPhysicsSpace().remove(terrain.getPlatform(index).rb);
                //removes from array
                terrain.removePlatform(index);
                
                //if you remove a platform, you add a new one
                
                boolean change = true;
                
                if(gamemode == 2) {
                    change = ((terrain.getPlatform(terrain.getNumPlatforms()-1).getNumber()) - 1) % 3 == 0; //if the platform is the third platform in a line, its number mod 3 will be 0
                }
                
                Platform created = terrain.addPlatform(platformMaterial, change, gamemode == 2);
                
//                System.out.println("new: " + created.geometry.getLocalTranslation().z);
//                System.out.println("user at: " + user.geometry.getLocalTranslation().z);
//                
                rootNode.attachChild(created.geometry);
            
                //add to physics space
            
                bulletAppState.getPhysicsSpace().add(created.rb);
            } else { //keep checking
                index++;
            }
        }
        
        
    }
    
    //another update for best ai
    
    private void updateBest() {
        if(bestAI.getDead()) {
            bestAI.geometry.setCullHint(CullHint.Always);
            return;
        } //if the player is dead, don't do anything
            
        //updates the color of a platform
        
        boolean onPlatform = false;
        
        int platformIndex = -1;
        
        for(int j = 0; j < terrain.getNumPlatforms(); j++) { //each platform
            CollisionResults results = new CollisionResults();
            if(bestAI.geometry.collideWith((Collidable)terrain.getPlatform(j).box, results) > 0) { //there is collision
                terrain.getPlatform(j).changeColor(bestAI.getColor());
                onPlatform = true;
                platformIndex = j;
                if(!terrain.getPlatform(j).checkSameColor(bestAI.getColor()) && gamemode == 2 && terrain.getPlatform(j).getNumber() != 1) { //bestAI is on the platform of another player(not the starting platform)
                    bestAI.setDead();
                    bestAI.geometry.setCullHint(CullHint.Always);
                    return;
                }
                
                break;
            }
        }
        
        //updates the target for the ai
        
        //make sure closest platform is chosen
        
        if(onPlatform) { //on a platform, but not the first
            //on a platform, can change to the best platform in the row in front of the current platform
            
            int currentRow = (terrain.getPlatform(platformIndex).getNumber() - 2) / 3; //row currently in
            int newRow = currentRow + 1; //row in front
            
            if(terrain.getPlatform(platformIndex).getNumber() == 1) { //first platform
                newRow = 0;
            }
            
            double minDistance = Double.MAX_VALUE;
            int bestIndex = -1;
            
            for(int j = 0; j < terrain.getNumPlatforms(); j++) {
                if((terrain.getPlatform(j).getNumber() - 2) / 3 == newRow) {
                    //calculate distance

                    double d = Math.abs(terrain.getPlatform(j).geometry.getLocalTranslation().x - bestAI.rb.getPhysicsLocation().x);
                    if(d < minDistance && !terrain.getPlatform(j).checkSameColor(user.getColor()) && terrain.getPlatform(j).getNumber() != 1) { //smaller distance, not chosen by user, not first platform
                        minDistance = d;
                        bestIndex = j;
                    }
                }
            }
            
            System.out.println("-----------");
            
            if(bestIndex >= 0) {
                System.out.println("changed to: " + terrain.getPlatform(bestIndex).getNumber());
                bestAI.changePlatform(terrain.getPlatform(bestIndex).getNumber());
            }
            
        } else { //in air, check to see if the platform you chose was already taken
            //figure out if the platform was chosen
            
            Platform target = terrain.getPlatformFromNumber(bestAI.getNextPlatform());
            
            if(target.checkSameColor(user.getColor())) { //user already chose, pick a new one
                int row = (target.getNumber() - 2) / 3;
                
                double minDistance = Double.MAX_VALUE;
                int bestIndex = -1;
            
                for(int j = 0; j < terrain.getNumPlatforms(); j++) {
                    if((terrain.getPlatform(j).getNumber() - 2) / 3 == row) {
                        //calculate distance

                        double d = Math.abs(terrain.getPlatform(j).geometry.getLocalTranslation().x - bestAI.rb.getPhysicsLocation().x);
                        if(d < minDistance && !terrain.getPlatform(j).checkSameColor(user.getColor()) && terrain.getPlatform(j).getNumber() != 1 && terrain.getPlatform(j).getNumber() != target.getNumber()) { //smaller distance, not chosen by user, not first or current platform
                            minDistance = d;
                            bestIndex = j;
                        }
                    }
                }
            
                if(bestIndex >= 0) {
                    System.out.println("changed to: " + terrain.getPlatform(bestIndex).getNumber());
                    bestAI.changePlatform(terrain.getPlatform(bestIndex).getNumber());
                }
            }
        }
        
        System.out.println("platform: " + bestAI.getNextPlatform());
        
        

            
//        //increase score
//        bestAI.increaseScore();
//        bestAI.increaseFitness();            
        

        Vector3f angV = bestAI.rb.getAngularVelocity();
        bestAI.rb.setAngularVelocity(new Vector3f(0, 0, 0));
        bestAI.geometry.rotate(new Quaternion (.707f, 0, 0, 0.707f));
        
        //now moves it forward(constant speed)
        bestAI.rb.setLinearVelocity(new Vector3f(bestAI.rb.getLinearVelocity().x, bestAI.rb.getLinearVelocity().y, 30));
            
        if(bestAI.geometry.getLocalTranslation().y < Main.BOTTOM_LIMIT || bestAI.geometry.getLocalTranslation().y > Main.TOP_LIMIT) {
            bestAI.setDead();
        }
    }
    
    
    //updates the best ai movement
    
    private void bestMovement() {
         if(bestAI.getDead()) { return; } //continue if the ai is dead
            
            
        //gets the inputs
            
        ArrayList<Double> inputs = new ArrayList<>();
            
        int platformNumber = bestAI.getNextPlatform(); //number of the platform the ai is targeting
            
        int platformIndex = -1;
        //gets the index
        for(int z = 0; z < terrain.getNumPlatforms(); z++) {
            if(terrain.getPlatform(z).getNumber() == platformNumber) {
                platformIndex = z;
                break;
            }
        }
            
            
        if(platformIndex == -1) { //ai messed up, its target doesn't exist
            return;
        } else { //finds the inputs
            double xDistance = bestAI.geometry.getLocalTranslation().x - terrain.getPlatform(platformIndex).geometry.getLocalTranslation().x;
            double zDistance = terrain.getPlatform(platformIndex).geometry.getLocalTranslation().z - bestAI.geometry.getLocalTranslation().z;
                
            //now normalize
                
            xDistance /= 30; //distance from side to side
            zDistance /= 80; //divide by max distance from two platforms
            zDistance = 1 - zDistance;
                
            inputs.add(xDistance);
            inputs.add(zDistance);
        }
        
        
        
        ArrayList<Double> outputs = bestNN.Update(inputs, "snapshot");
            
        //for moving, you can only move in the air
                        
        boolean inAir = true;
        //check if the ai is in the air
        for(int j = 0; j < terrain.getNumPlatforms(); j++) { //each platform
            CollisionResults results = new CollisionResults();
            if(bestAI.geometry.collideWith((Collidable)terrain.getPlatform(j).box, results) > 0) { //there is collision
                inAir = false;
                break;
            }
        }
                        
        if(inAir) { //can move
                
            double movement = outputs.get(1) - outputs.get(0);
            bestAI.moveAI(clamp((float)movement, -0.1f, 0.1f));
                
        } else { //can jump
            if(outputs.get(2) > 0.385f && time > 2) {
                bestAI.jump();
            }
        }
        
        
        
    }
    
    //applies gravity to everything
    
    private void applyGravity() {
        for(Player p : aiList)            
            p.rb.applyCentralForce(new Vector3f(0, -40, 0)); //apply gravity
        
        user.rb.applyCentralForce(new Vector3f(0, -40, 0)); //apply gravity

    }
    
    //goes through movement for the ai
    
    private void aiMovement() {
        
        //updates movement with NN
        for(int i = 0; i < aiList.size(); i++) {
            
            if(aiList.get(i).getDead()) { continue; } //continue if the ai is dead
            
            
            //gets the inputs
            
            ArrayList<Double> inputs = new ArrayList<>();
            
            int platformNumber = aiList.get(i).getNextPlatform(); //number of the platform the ai is targeting
            
            int platformIndex = -1;
            //gets the index
            for(int z = 0; z < terrain.getNumPlatforms(); z++) {
                if(terrain.getPlatform(z).getNumber() == platformNumber) {
                    platformIndex = z;
                    break;
                }
            }
            
            //System.out.println("target: " + platformIndex);
            
            if(platformIndex == -1) { //ai messed up, its target doesn't exist
                continue;
            } else { //finds the inputs
                double xDistance = aiList.get(i).geometry.getLocalTranslation().x - terrain.getPlatform(platformIndex).geometry.getLocalTranslation().x;
                double zDistance = terrain.getPlatform(platformIndex).geometry.getLocalTranslation().z - aiList.get(i).geometry.getLocalTranslation().z;
                
                //now normalize
                
                xDistance /= 30; //distance from side to side
                zDistance /= 80; //divide by max distance from two platforms
                zDistance = 1 - zDistance;
                
                inputs.add(xDistance);
                inputs.add(zDistance);
            }
                     
            ArrayList<Double> outputs = NEAT.UpdateMember(i, inputs);
            
            //for moving, you can only move in the air
                        
            boolean inAir = true;
            //check if the ai is in the air
            for(int j = 0; j < terrain.getNumPlatforms(); j++) { //each platform
                CollisionResults results = new CollisionResults();
                if(aiList.get(i).geometry.collideWith((Collidable)terrain.getPlatform(j).box, results) > 0) { //there is collision
                    inAir = false;
                    break;
                }
             }
            
            
            
            if(inAir) { //can move
                
                double movement = outputs.get(1) - outputs.get(0);
                aiList.get(i).moveAI(clamp((float)movement, -0.1f, 0.1f));
                
            } else { //can jump
                if(outputs.get(2) > 0.42f && time > 2 && aiList.get(i).getCanJump()) {
                    aiList.get(i).jump();
                }
            }
            
            
        }
        
    }
    
    //displays the NN
    
    private void deleteNN() {
        while(!nodeList.isEmpty()) {
            rootNode.detachChild(nodeList.remove(0));
        }
        
        while(!lineList.isEmpty()) {
            rootNode.detachChild(lineList.remove(0));
        }
        
    }
    
    private void displayNN(Player player) { //displays the neural network on the screen; parameter is the player whose NN is displayed
                
        //delete the NN on the screen
        
        deleteNN();
        
        if(player.equals(user)) { return; } //cannot display NN of user, since it doesn't have one
        
        
        else if(player.equals(bestAI)) { //best ai
            
            //offsets to place the neural network on the correct side
            
            float zPos = player.geometry.getLocalTranslation().z + 5; //side to side
            float xOffset = cam.getLocation().x - 10;
            float yOffset = cam.getLocation().y;
        
            for (int i = 0; i < bestNN.getNNsize(); i++) //for each of the nodes
            {
                Vector2 pos = bestNN.getNodePosition(i); //gets the position of the node

                //create the graphic
                Sphere s = new Sphere(30, 30, 1);
                Box b = new Box(Vector3f.ZERO, 1, 1, 1);
                Geometry geom = new Geometry("Box", s);
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.setColor("Color", ColorRGBA.Red);
                geom.setMaterial(mat);
           
                geom.setLocalTranslation(-pos.getX() + xOffset, pos.getY() + yOffset, zPos); //sets the location
           
                //adds to the arraylist and the rootnode(screen)
                rootNode.attachChild(geom);
                nodeList.add(geom);

                //create its connections

                ArrayList<PLink> connectionList = bestNN.getNeuronFromIndex(i).getVecLinksOut(); //gets every link

                for(PLink link : connectionList) //creates the lines
                {
                    //gets the start and end position 
                    Vector2 startPos = pos;
                    Vector2 endPos = bestNN.getNodePositionFromID(link.getPOut().getNeuronId()); //neuron connecting to the current neuron
               
                    //create a line
                    Line line = new Line(new Vector3f(-startPos.getX() + xOffset, startPos.getY() + yOffset, zPos), new Vector3f(-endPos.getX() + xOffset, endPos.getY() + yOffset, zPos));
                
                    //the weight
                    float weight = (float)Math.abs(link.getWeight()) * 5;

                    if(weight < 1) {
                        weight = 1;
                    }
                
                
                    line.setLineWidth(weight);
                
                    //graphic
                    
                    Geometry geometry = new Geometry("Bullet", line);
                    Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                
                    
                    //sets color to differentiate positive vs negative
                    if(link.getWeight() < 0) {
                        mat2.setColor("Color", ColorRGBA.Red); //red is negative
                    } else {
                        mat2.setColor("Color", ColorRGBA.Blue); //blue is positive
                    }
                
                    //adds tp screen
                    geometry.setMaterial(mat2);                  
                    rootNode.attachChild(geometry);
                    lineList.add(geometry);
                }
                
            }
        } else {  //an ai  
        //displays on screen
        
        //gets the index of the player
        int position = 0;
        
        for(int i = 0; i < aiList.size(); i++) {
            if(aiList.get(i).equals(player)) {
                position = i;
                break;
            }
        }
        
        //the position of the AI is only needed in order to get the data about the neural network of the player
        
        float zPos = player.geometry.getLocalTranslation().z + 5;
        float xOffset = cam.getLocation().x - 15;
        float yOffset = cam.getLocation().y - 5;
        
        for (int i = 0; i < NEAT.getNNNodeSize(position); i++) //for each of the nodes
        {
           //construct the node

           int id = NEAT.getNNId(position, i);

           Vector2 pos = NEAT.getNNNodePosFromID(position, id);

           
           Sphere s = new Sphere(30, 30, 1);
           Box b = new Box(Vector3f.ZERO, 1, 1, 1);
           Geometry geom = new Geometry("Box", s);
           Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
           mat.setColor("Color", ColorRGBA.Red);
           geom.setMaterial(mat);
           
           geom.setLocalTranslation(-pos.getX() + xOffset, pos.getY() + yOffset, zPos);
           
           rootNode.attachChild(geom);
           nodeList.add(geom);

           //construct its connections

           ArrayList<PLink> connectionList = NEAT.getNNConnections(position, i);

           for(PLink link : connectionList) //creates the lines
           {
               
                Vector2 startPos = pos;
                Vector2 endPos = NEAT.getNNNodePosFromID(position, link.getPOut().getNeuronId());
               
                Line line = new Line(new Vector3f(-startPos.getX() + xOffset, startPos.getY() + yOffset, zPos), new Vector3f(-endPos.getX() + xOffset, endPos.getY() + yOffset, zPos));
                
                float weight = (float)Math.abs(link.getWeight()) * 5;

                if(weight < 1) {
                    weight = 1;
                }
                
                
                line.setLineWidth(weight);
                
                Geometry geometry = new Geometry("Bullet", line);
                Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                
                if(link.getWeight() < 0) {
                    mat2.setColor("Color", ColorRGBA.Red); //red is negative
                } else {
                    mat2.setColor("Color", ColorRGBA.Blue); //blue is positive
                }
                
                geometry.setMaterial(mat2);                  
                rootNode.attachChild(geometry);
                lineList.add(geometry);
           }

        }   
        }
        
        //weight thickness
        //color based on weight
        
        
    }
   
    
    //displays text
    
    private void displayText() {
        if(gamemode == 3) 
            genText.setText("Generation: " + NEAT.generation);
        else 
            genText.setText("");
        scoreText.setText("Score: " + time / 30);
    }
    
    
    //finds the player that should be followed
    
    private void setFollowedPlayer() {        
        if(gamemode == 3) { //neat
            for(Player p : aiList) {
                if(!p.getDead()) {
                    playerFollowing = p;
                }
            }
        } else if(gamemode == 2) { //player + ai plays
            if(!user.getDead()) {
                playerFollowing = user;
            } else {
                playerFollowing = bestAI;
            }
        } else { //user plays only
            playerFollowing = user; 
        }
        
    }
    
    //reset method
    
    private void resetGame() { //resets players, terrain, camera
        //updates best score
        
        time = 0;
        round++;
        pause = false;

        //reset the players
            user.rb.setLinearVelocity(Vector3f.ZERO);
            user.geometry.setLocalTranslation(STARTING_POSITION);
            user.rb.setPhysicsLocation(STARTING_POSITION);
            user.rb.setPhysicsRotation(new Quaternion (.707f, 0, 0, 0.707f));
            user.reset();
            
            for(Player p : aiList) {
                p.rb.setLinearVelocity(Vector3f.ZERO);
                p.geometry.setLocalTranslation(STARTING_POSITION);
                p.rb.setPhysicsLocation(STARTING_POSITION);
                p.rb.setPhysicsRotation(new Quaternion (.707f, 0, 0, 0.707f));
                p.reset();
            }
            
            bestAI.rb.setLinearVelocity(Vector3f.ZERO);
            bestAI.geometry.setLocalTranslation(STARTING_POSITION);
            bestAI.rb.setPhysicsLocation(STARTING_POSITION);
            bestAI.rb.setPhysicsRotation(new Quaternion (.707f, 0, 0, 0.707f));
            bestAI.reset();
            
            
            //reset the terrain
            
            for(int i = 0; i < terrain.getNumPlatforms(); i++) {
                //removes from the screen
                rootNode.detachChild(terrain.getPlatform(i).geometry);
                bulletAppState.getPhysicsSpace().remove(terrain.getPlatform(i).rb);
            }
            
            terrain.deleteTerrain();
            
            //put on screen the new terrain
            
            //initial platform
            
            Platform created = terrain.addPlatform(platformMaterial, true, gamemode == 2);
            
            //puts on screen
            rootNode.attachChild(created.geometry);
            
            //add to physics space
            
            bulletAppState.getPhysicsSpace().add(created.rb);
            
            
            for(int i = 0; i < NUM_PLATFORMS-1; i++) { //rest of the platforms
                
                int times;
                if(gamemode == 2) {
                    times = 3;
                } else {
                    times = 1;
                }
                
                boolean changeZ = true;
                for(int j = 0; j < times; j++) {
                    Platform created2 = terrain.addPlatform(platformMaterial, changeZ, gamemode == 2);
                    changeZ = false;
            
                    //puts on screen
                    rootNode.attachChild(created2.geometry);
            
                    //add to physics space
            
                    bulletAppState.getPhysicsSpace().add(created2.rb);
                }
        
            }
            //resets the stars and creates new ones
            
            starZDistance = 0;
            for(int i = 0; i < starList.size(); i++) {
                rootNode.detachChild(starList.get(i).body);
            }
            
            starList = new ArrayList<>();
            
            createStars(STAR_START);
            
            
            //reset the camera
            
            updateCamera(new Vector3f(0, 5, 0));
            //sets linear velocities
            user.rb.setLinearVelocity(new Vector3f(0, 0, 30));
            for(Player p : aiList) {
                p.rb.setLinearVelocity(new Vector3f(0, 0, 30));
            }
            bestAI.rb.setLinearVelocity(new Vector3f(0, 0, 30));
    }
    
    
    
    //checking for restart
    
    private boolean needRestart() { //returns true if restart is needed
        switch (gamemode) {
            case 1:
                //only player
                return user.getDead();
            case 2:
                //player + best player
                return user.getDead() || bestAI.getDead();
            default: //neat
                boolean allDead = true; //assume all dead
                
                for(Player player : aiList) {
                    allDead = allDead && player.getDead(); //once false will make it true
                }
                return allDead;
        }
    }
    
    //returns the winner, between the user(1) or the ai(2)
    
    private int getWinner() {
        if(user.getDead()) {
            return 2; //user dead, ai won
        } else if(bestAI.getDead()) {
            return 1; //other way around, user won
        }
        return -1;
    }
    
    //clamp method
    
    private float clamp(float f, float min, float max) {
        if(f < min) {
            return min;
        } else if(f > max) {
            return max;
        }
        return f;
    } 
    
    
    //collision
    
    @Override
    public void collision(PhysicsCollisionEvent event) {
                
        
    }
    
    @Override
    public boolean collide(PhysicsCollisionObject nodeA, PhysicsCollisionObject nodeB) { //prevents collision between players
        int count = 0;
        
        //collisions between a player won't be registered
        for(Player p : aiList) {
            if(nodeA.equals(p.rb) || nodeB.equals(p.rb)) {
                count++;
            }
        }
        
        if(nodeA.equals(user.rb) || nodeB.equals(user.rb))
            count++;
        
        if(nodeA.equals(bestAI.rb) || nodeB.equals(bestAI.rb))
            count++;
        
        return count != 2;
        
    }
    
    //sets the info text to certain text, based on number passed in
    
    private void changeInfoText(int num) {
        switch(num) {
            case 1: //practice
                infoText.setText("You are practicing noob\n\nland on the platforms\nClick a to go left\nClick d to go right\nClick space to jump\nClick p to pause\nClick b to go back\nClick h to hide/show text");
                neatText.setText("");
                break;
            case 2: //game
                infoText.setText("Why are you reading this,\nyou should be playing\n\nHOW TO PLAY\nLand on platforms that\nthe AI hasn't landed on\nMake the AI touch your platform\nor fall in the void to win\n\nClick a to go left\nClick d to go right\nClick space to jump\nClick p to pause\nClick h to hide/show text");
                neatText.setText("");
                break;
            case 3: //neat
                infoText.setText("The AI are training, so wait\n\nClick p to pause\nClick s to skip gen\nClick c to continue\nClick r to restart all training\nClick h to hide/show text");
                
                break;
            case 4: //in between
                neatText.setText("");
                if(gamemode == 1) {
                    infoText.setText("Click something to do something\n\nClick 1 to practice\nClick 2 to play\nClick 3 to train the AI\nClick 4 to make the character regular\nClick 5 to make the character a pirate\nClick 6 to make the character have a hat\nClick h to hide/show text" 
                            + "\n\nHighest Score: " + bestScore + "\nUser Win Percentage: " + (Math.round(((double)userWon / (double)gamesPlayed) * 1000)/10.0) + "%");
                    
                } else if(gamemode == 2) {
                    if(getWinner() == 1) {
                    infoText.setText("YOU WIN\n(but you won't again)\nClick 1 to practice\nClick 2 to play\nClick 3 to train the AI\nClick 4 to make the character regular\nClick 5 to make the character a pirate\nClick 6 to make the character have a hat\nClick h to hide/show text" 
                            + "\n\nHighest Score: " + bestScore + "\nUser Win Percentage: " + (Math.round((double)userWon / (double)gamesPlayed * 1000)/10.0) + "%");
                        
                    } else if(getWinner() == 2) {
                    infoText.setText("YOU LOSE\n(even pyrus would have won)\n\nClick 1 to practice\nClick 2 to play\nClick 3 to train the AI\nClick 4 to make the character regular\nClick 5 to make the character a pirate\nClick 6 to make the character have a hat\nClick h to hide/show text" 
                            + "\n\nHighest Score: " + bestScore + "\nUser Win Percentage: " + (Math.round((double)userWon / (double)gamesPlayed * 1000)/10.0) + "%");
                        
                    }
                                        
                }
                
                System.out.println(userWon + " " + gamesPlayed);
                break;
            default:
                break;
        }
    }
    
    private void freezeGraphics(boolean freeze) { //freezes graphics if boolean passed in is true; else, it unfreezes
        for(Player p : aiList) {
            p.rb.setKinematic(freeze);
        }
        user.rb.setKinematic(freeze);
        bestAI.rb.setKinematic(freeze);
    }

    @Override
    public void simpleRender(RenderManager rm) { //render code
        //TODO: add render code
    }
    
          
        
    
    //figured it all out:
    
    //problem was genome was returned as reference, not copy
    
    //now to do:
    
    //fix jumping super high
    
    //finishing touches
            
    
    
}
