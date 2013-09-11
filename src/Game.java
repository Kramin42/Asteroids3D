import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
//import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;
//import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;


import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

// In the last lesson, we got a taste of the third dimension by rotating some flat planar objects
// around.  Now we're finally going to start building real three dimensional objects and
// pop that triangle and square into a sphere and cube and spin those around.

public class Game {
    private String windowTitle = "Asteroids3D";
    private int windowWidth  = 1280;
    private int windowHeight = 720;
    private int crossHairSize = 20;
//    private DisplayMode[] displayModes;
//    private int currentDisplayMode = 0;
    private boolean quitRequested = false;
    
    public float minInputRad = 0.0f;
    public float sensitivity = 0.01f;
    
    private int numOfStars = 2000;
    private Vector3f[] stars = new Vector3f[numOfStars];
    private float starRange = 200.0f;
    private float starRangeSq = starRange*starRange;
    private float StarsFadeParam = starRangeSq/10;
    private float gameBounds = 10000000000000000000000000000000.0f;//practically infinity
    
    private float astRange = 200.0f;
    private float astSpeed = 1.0f;
    boolean targettingDemo = true;
    
    private float bulletSpeed = 20.0f;
    private Texture bulletEnd, bulletBody;
    
    private Random rand = new Random();
    
    // There's several kinds of lights, but two that we're concerned with here.  The first is
    // "ambient" light, which is essentially the "background light level" of the scene, lighting
    // everything uniformly in all directions.  The four components of the light are the standard
    // RGBA color values (alpha is almost always going to be 1).  With all components at .5,
    // this yields a dim light, but enough to see all the faces of the cube.
    private float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};

    // Diffuse light comes from a positioned source, and lights up surfaces that face the source
    // directly more than those that are faced away from it.  There's also specular light which
    // is like diffuse except it depends on the viewing angle to also be in the path of the
    // reflection (think glare on shiny objects).  We're not using specular light in this lesson.
    private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    
    private float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] matSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
    float shininess = 50.0f;

    // The light is positioned back "behind" on the z axis.  The fourth component in the light
    // position is a scaling factor that will pretty much always be 1.0.
    private float[] lightPosition = {0.0f, 10000000000000.0f, 10000000000000.0f, 1.0f};
    
    
//    Vector3f camPos = new Vector3f(0.0f,0.0f,0.0f);
//    
//    Vector3f camDir = new Vector3f(-1.0f,0.0f,0.0f);
    Player player = new Player();
    ArrayList<Asteroid> asts = new ArrayList<Asteroid>();
    ArrayList<Turret> trts = new ArrayList<Turret>();

    public static void main(String[] args) throws Exception {
        Game app = new Game();
        app.run();
    }


    private void initGL() {
        DisplayMode dm = Display.getDisplayMode();
        int w = dm.getWidth();
        int h = dm.getHeight();
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45.0f, (float) w / (float) h, 0.1f, 10000.0f);//need a large zFar in space
        glMatrixMode(GL_MODELVIEW);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glShadeModel(GL_SMOOTH);
        
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        glEnable(GL_BLEND);
        glAlphaFunc(GL_GREATER,0.1f);
        glEnable(GL_ALPHA_TEST);
        
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        // Here's where we define our light.  OpenGL supports up to eight lights at a time.
        FloatBuffer temp = BufferUtils.createFloatBuffer(4);
        glMaterial(GL_FRONT, GL_SPECULAR, (FloatBuffer) temp.put(matSpecular).rewind());                         // sets specular material color
        glMaterialf(GL_FRONT, GL_SHININESS, shininess);                                     // sets shininess
        glLight(GL_LIGHT0, GL_SPECULAR, (FloatBuffer) temp.put(lightSpecular).rewind());                            // sets specular light to white
        glLight(GL_LIGHT1, GL_AMBIENT, (FloatBuffer) temp.put(lightAmbient).rewind());
        glLight(GL_LIGHT1, GL_DIFFUSE, (FloatBuffer) temp.put(lightDiffuse).rewind());
        glLight(GL_LIGHT1, GL_POSITION, (FloatBuffer) temp.put(lightPosition).rewind());
        glEnable(GL_LIGHT1);
        
        glEnable(GL_LIGHTING);
    }

    private void renderScene() {
        
    	make3D();
        
        glEnable(GL_COLOR_MATERIAL);
        
        //eyez+=0.01f;
        //eyex+=0.01f;

        // Now that we're dealing with depth, we have to reset a little more than before,
        // namely the depth buffer that we told OpenGL to support above.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glLoadIdentity();                          // Reset The Current Modelview Matrix
        
        if (targettingDemo){
	        Turret trt = trts.get(0);
	        glTranslatef(5.0f, -2.0f, -10.0f);
			gluLookAt(trt.pos.x, trt.pos.y, trt.pos.z, trt.pos.x
					+ trt.dir.x, trt.pos.y + trt.dir.y, trt.pos.z
					+ trt.dir.z, trt.up.x, trt.up.y, trt.up.z);
        } else {
			gluLookAt(player.pos.x, player.pos.y, player.pos.z, player.pos.x
					+ player.dir.x, player.pos.y + player.dir.y, player.pos.z
					+ player.dir.z, player.up.x, player.up.y, player.up.z);
        }

        FloatBuffer temp = BufferUtils.createFloatBuffer(4);
        glLight(GL_LIGHT1, GL_POSITION, (FloatBuffer) temp.put(lightPosition).rewind());
        
        glDisable(GL_LIGHTING);
        glEnable(GL_BLEND);
        glEnable(GL_ALPHA_TEST);
        glPointSize(2);
        glBegin(GL_POINTS);
        glColor4f(1.0f,1.0f,1.0f,1.0f);
        float brightness = 0.0f;
        for (int i=0; i<numOfStars; i++){
        	brightness = StarsFadeParam/Vector3f.sub(player.pos, stars[i], null).lengthSquared();
        	brightness = brightness>1.0f ? 1.0f : brightness;
        	glColor4f(0.392f,0.349f,0.325f,brightness);
        	glVertex3f(stars[i].x, stars[i].y, stars[i].z);
        	//glVertex3f(stars[i].x-player.pos.x+player.prevPos.x, stars[i].y-player.pos.y+player.prevPos.y, stars[i].z-player.pos.z+player.prevPos.z);
        }
        glEnd();
        
        glPointSize(2);
        player.drawBullets();
        for (int i=trts.size()-1; i>=0;i--){
    		trts.get(i).drawBullets();
        }
        
		glEnable(GL_LIGHTING);
		glDisable(GL_BLEND);
		glDisable(GL_ALPHA_TEST);
//        glBegin(GL_LINES);
//        glColor4f(1.0f,0.0f,0.0f,1.0f);
//        glVertex3f(0.0f, 0.0f, 0.0f);
//        glVertex3f(10.0f, 0.0f, 0.0f);
//        glColor4f(0.0f,1.0f,0.0f,1.0f);
//        glVertex3f(0.0f, 0.0f, 0.0f);
//        glVertex3f(0.0f, 10.0f, 0.0f);
//        glColor4f(0.0f,0.0f,1.0f,1.0f);
//        glVertex3f(0.0f, 0.0f, 0.0f);
//        glVertex3f(0.0f, 0.0f, 10.0f);
//        glEnd();
        for (int i=asts.size()-1; i>=0;i--){
    		asts.get(i).draw();
        }
        for (int i=trts.size()-1; i>=0;i--){
    		trts.get(i).draw();
        }
        
        if (!targettingDemo){
        //draw the HUD
        make2D();
        //draw crosshairs in center TODO: change crosshair to an image
        glLineWidth(1);
        glColor4f(0.0f, 1.0f, 0.0f, 0.5f);
        glBegin(GL_LINES);
        	glVertex2f(Display.getWidth()/2-crossHairSize, Display.getHeight()/2);
        	glVertex2f(Display.getWidth()/2+crossHairSize-1, Display.getHeight()/2);
        	glVertex2f(Display.getWidth()/2, Display.getHeight()/2-crossHairSize+1);
        	glVertex2f(Display.getWidth()/2, Display.getHeight()/2+crossHairSize);
        glEnd();
        glPointSize(1);
        glBegin(GL_POINTS);
        	glVertex2i(Mouse.getX(), Mouse.getY());
        glEnd();
        }
    }
    
    private void updateGame()
    {
    	//update stars
    	for (int i=0;i<numOfStars;i++){
        	if (Vector3f.sub(player.pos, stars[i], null).lengthSquared() > starRangeSq) {
        		stars[i].x = 2*player.pos.x - stars[i].x;
        		stars[i].y = 2*player.pos.y - stars[i].y;
        		stars[i].z = 2*player.pos.z - stars[i].z;
        	}
        }
    	
    	player.update(gameBounds);
    	
    	player.updateBullets(gameBounds);
    	
    	for (int i=asts.size()-1; i>=0;i--){
    		asts.get(i).update(gameBounds);
    		//check for player bullet to asteroid collisions
    		if (player.checkBulletHit(asts.get(i).pos, asts.get(i).radius)){
    			asts.get(i).delete();
    		}
    		for (int j=trts.size()-1; j>=0;j--){
    			if (trts.get(j).checkBulletHit(asts.get(i).pos, asts.get(i).radius)){
        			asts.get(i).delete();
        		}
    		}
    		if (!asts.get(i).exists){
    			asts.remove(i);
    		}
        }
    	for (int i=trts.size()-1; i>=0;i--){
    		Turret trt = trts.get(i);
    		//if (trt.targetID == -1 || trt.targetID > asts.size()-1){trt.targetID = asts.size()-1;}
    		if (trt.targetDestroyed || trt.targetID == -1 || trt.targetID > asts.size()-1 || !asts.get(trt.targetID).exists){
    			//System.out.println(trt.targetID);
    			trt.targetID = -1;
    			float maxDot = 0.0f;
    			//System.out.println("Aquiring target:");
	    		for (int j=asts.size()-1; j>=0;j--){
	    			Asteroid ast = asts.get(j);
	    			if (ast.exists){
	    				Vector3f diff = new Vector3f();
	    				Vector3f.add(ast.pos, trt.pos, diff);
	    				diff.normalise();
	    				float dot = Vector3f.dot(diff, trt.dir);
	    				if (dot > maxDot){
		    				//System.out.println(dot);
		    				trt.targetDestroyed = false;
	    					trt.targetID = j;
	    					maxDot = dot;
	    				}
	    			}
	    		}
    		}
    		if (trt.targetID != -1 && trt.aim(asts.get(trt.targetID).pos, asts.get(trt.targetID).prevPos)){
    			trt.fire();
    		}
    		trt.update(gameBounds);
    		trt.updateBullets(gameBounds);
    		if (!trt.exists){
    			trts.remove(i);
    		}
        }
    }


    /** Sets up OpenGL, runs the main loop of our app, and handles exiting */
    public void run() throws Exception {
        initialize();
        try {
            while (!quitRequested) {
                // This is the main loop of our application
                handleInput();      // Process input (e.g. keyboard, mouse, window events)
                updateGame();
                renderScene();      // Render the frame to be drawn to the back buffer
                Display.update();   // Display the back buffer, then poll for input
                Display.sync(60);   // Sleep long enough for the app to run at 60FPS
                
            }
        } catch (Exception e) {
            Sys.alert(windowTitle, "An error occured -- now exiting.");
            e.printStackTrace();
            System.exit(0);
        } finally {
            cleanup();
        }
    }

    /** Sets up the window and sets up openGL options. */
    private void initialize() throws Exception {
        initDisplay();  // Get a display window
        DisplayMode[] displayModes = Display.getAvailableDisplayModes();

    	for (int i=0;i<displayModes.length;i++) {
    	    DisplayMode current = displayModes[i];
//    	    if (current.equals(Display.getDisplayMode())){
//    	    	currentDisplayMode = i;
//    	    }
    	    System.out.println(current.getWidth() + "x" + current.getHeight() + "x" +
    	                        current.getBitsPerPixel() + " " + current.getFrequency() + "Hz");
    	}
        initGL();       // Set options and initial projection
        
        //worry about joystick support later
//        Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
//
//        for(int i =0;i<ca.length;i++){
//
//            /* Get the name of the controller */
//            System.out.println(ca[i].getName());
//            System.out.println("Type: "+ca[i].getType().toString());
//
//            /* Get this controllers components (buttons and axis) */
//            Component[] components = ca[i].getComponents();
//            System.out.println("Component Count: "+components.length);
//            for(int j=0;j<components.length;j++){
//                
//                /* Get the components name */
//                System.out.println("Component "+j+": "+components[j].getName());
//                System.out.println("    Identifier: "+ components[j].getIdentifier().getName());
//                System.out.print("    ComponentType: ");
//                if (components[j].isRelative()) {
//                    System.out.print("Relative");
//                } else {
//                    System.out.print("Absolute");
//                }
//                if (components[j].isAnalog()) {
//                    System.out.print(" Analog");
//                } else {
//                    System.out.print(" Digital");
//                }
//            }
//        }
        
        player.dir.x = 1.0f;
        player.dir.y = 0.0f;
        player.dir.z = 0.0f;
        player.up.x = 0.0f;
        player.up.y = 0.0f;
        player.up.z = 1.0f;
        //player.calcDir();
        //System.out.println("player dir x: "+player.dir.x+", y: "+player.dir.y+", z: "+player.dir.z);
        //player.calcUp();
        //System.out.println("player up x: "+player.up.x+", y: "+player.up.y+", z: "+player.up.z);
        player.pos.x = 0.0f;
        player.pos.y = 0.0f;
        player.pos.z = 0.0f;
        player.enginePower = 0.001f;
        player.brakePower = 0.1f;
        
        //create stars
        for (int i=0;i<numOfStars;i++){
        	do {
        		stars[i] = new Vector3f((rand.nextFloat()*2-1.0f)*starRange,(rand.nextFloat()*2-1.0f)*starRange,(rand.nextFloat()*2-1.0f)*starRange);
        	} while (Vector3f.sub(player.pos, stars[i], null).lengthSquared() > starRangeSq);
        }
        
        //create initial asteroids
        for (int i=0; i<100; i++){
        	Asteroid ast = new Asteroid();
        	ast.pos = new Vector3f((rand.nextFloat()*2-1.0f)*astRange,(rand.nextFloat()*2-1.0f)*astRange,(rand.nextFloat()*2-1.0f)*astRange);
        	ast.prevPos = new Vector3f(ast.pos);
        	Vector3f.sub(ast.prevPos, new Vector3f((rand.nextFloat()*2-1.0f)*astSpeed,(rand.nextFloat()*2-1.0f)*astSpeed,(rand.nextFloat()*2-1.0f)*astSpeed), ast.prevPos);
        	asts.add(ast);
        }
        
        //create a turret
        Turret trt = new Turret();
        trt.pos = new Vector3f(10.0f,0.0f,0.0f);
        trt.prevPos = new Vector3f(trt.pos);
        trts.add(trt);
        
        //grab the mouse
        Mouse.setGrabbed(true);
    }

    /** Creates a new window and sets options on it */
    private void initDisplay() throws LWJGLException {
        DisplayMode mode = new DisplayMode(windowWidth, windowHeight);
        Display.setDisplayMode(mode);
        Display.setTitle(windowTitle);
        Display.setVSyncEnabled(true);
        Display.create();
    }

    /** Reads queued keyboard events and takes appropriate action on them. */
    private void handleInput() throws LWJGLException {
        if (Display.isCloseRequested())

        {
            // The display window is being closed
            quitRequested = true;
            return;
        }

        while (Keyboard.next())
        {
            int key = Keyboard.getEventKey();
            boolean isDown = Keyboard.getEventKeyState();
            if (isDown) {
                switch (key) {
                    case Keyboard.KEY_ESCAPE:
                        quitRequested = true;
                        break;
                    case Keyboard.KEY_SPACE:
                    	player.setEngines(true);
                    	break;
                    case Keyboard.KEY_LCONTROL:
                    	player.setBrakes(true);
                    	break;
                    case Keyboard.KEY_F11:
        		        setDisplayMode(windowWidth, windowHeight, !Display.isFullscreen());
                    	break;
                    case Keyboard.KEY_M:
                    	
                    	break;
                }
            } else {
            	switch (key) {
                case Keyboard.KEY_SPACE:
                	player.setEngines(false);
                	break;
                case Keyboard.KEY_LCONTROL:
                	player.setBrakes(false);
                	break;
            }
            }
        }
        
        while (Mouse.next())
        {
        	if (Mouse.getEventButtonState()){
        		if (Mouse.getEventButton() == 0){
        			player.fire();
        		}
        	}
        }
        
        //check held keys
        if (Keyboard.isKeyDown(Keyboard.KEY_W)){
        	//camPos.x-=0.1;
        	player.pos.translate(0.1f*player.dir.x, 0.1f*player.dir.y, 0.1f*player.dir.z);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)){
        	//camPos.x+=0.1;
        	player.pos.translate(-0.1f*player.dir.x, -0.1f*player.dir.y, -0.1f*player.dir.z);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A)){
        	Vector3f left = player.getLeft();
        	player.pos.translate(0.1f*left.x,0.1f*left.y,0.1f*left.z);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D)){
        	Vector3f right = player.getRight();
        	player.pos.translate(0.1f*right.x,0.1f*right.y,0.1f*right.z);
        }
        if (Mouse.isButtonDown(0)){
//        	Bullet bullet = new Bullet();
//			bullet.translate(player.pos);
//			//bullet.translate(bullet.scalarProduct(player.dir, bulletSpeed));
//			bullet.setVel(player.dir, bulletSpeed);
//			bullet.setColor(new Vector4f(1.0f,0.0f,0.0f,1.0f));
//			plyrBlts.add(bullet);
        	player.fire();
        }
        
        //move view
        player.handleInput(2.0f*Mouse.getY()/windowHeight - 1.0f, 2.0f*Mouse.getX()/windowWidth - 1.0f, sensitivity, minInputRad);
    }
    
//    private Vector3f rotateVector(Vector3f vec, float altitude, float azimuth)
//    {
//    	Vector3f sphVec = CartesianToSpherical(vec);
////    	System.out.println("old azimuth: "+Vector3f.angle(new Vector3f(vec.x,vec.y,0.0f), new Vector3f(1.0f,0.0f,0.0f)));
////    	azimuth+=Vector3f.angle(new Vector3f(vec.x,vec.y,0.0f), new Vector3f(1.0f,0.0f,0.0f));
////    	System.out.println("new azimuth: "+azimuth);
////    	altitude+=Vector3f.angle(vec, new Vector3f(0.0f,0.0f,1.0f));
////    	System.out.println("new altitude: "+altitude);
//    	sphVec.y+=altitude;
//    	sphVec.z+=azimuth;
////    	System.out.println("length: "+length);
//    	
////    	vec.x = (float) (length*Math.sin(altitude)*Math.cos(azimuth));
////    	vec.y = (float) (length*Math.sin(altitude)*Math.sin(azimuth));
////    	vec.z = (float) (length*Math.cos(altitude));
//    	
//    	vec = SphericalToCartesian(sphVec);
//    	return vec;
//    }
//    
//    private Vector3f CartesianToSpherical(Vector3f vec)
//    {
//    	Vector3f out = new Vector3f();
//    	// in spherical coords x=length, y=altitude, z=azimuth
//    	out.x = (float) (vec.length());
//    	out.y = (float) (Vector3f.angle(new Vector3f(0.0f,0.0f,1.0f), vec));
//    	if (vec.x!=0 || vec.y!=0)
//    		out.z = (float) (Math.atan2(vec.y, vec.x));
//    	else
//    		out.z = 0.0f;
//    	
////    	System.out.println("Cartesian To Spherical");
////    	System.out.println("input x: "+vec.x+", y: "+vec.y+", z: "+vec.z);
////    	System.out.println("output x: "+out.x+", y: "+out.y+", z: "+out.z);
//    	return out;
//    }
//    
//    private Vector3f SphericalToCartesian(Vector3f vec)
//    {
//    	Vector3f out = new Vector3f();
//    	// in spherical coords x=length, y=altitude, z=azimuth
//    	out.x = (float) (vec.x*Math.sin(vec.y)*Math.cos(vec.z));
//    	out.y = (float) (vec.x*Math.sin(vec.y)*Math.sin(vec.z));
//    	out.z = (float) (vec.x*Math.cos(vec.y));
////    	System.out.println("Spherical To Cartesian");
////    	System.out.println("input x: "+vec.x+", y: "+vec.y+", z: "+vec.z);
////    	System.out.println("output x: "+out.x+", y: "+out.y+", z: "+out.z);
//    	return out;
//    }

    /** Perform final actions to release resources. */
    private void cleanup() {
        Display.destroy();
    }
    
    private static void make2D() {
        //Remove the Z axis
        glDisable(GL_LIGHTING);
        glDisable(GL_DEPTH_TEST);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, Display.getWidth(), 0, Display.getHeight(), -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
    }

    private static void make3D() {
        //Restore the Z axis
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LIGHTING);
    }

    /**
     * Set the display mode to be used 
     * 
     * @param width The width of the display required
     * @param height The height of the display required
     * @param fullscreen True if we want fullscreen mode
     */
    public void setDisplayMode(int width, int height, boolean fullscreen) {

        // return if requested DisplayMode is already set
        if ((Display.getDisplayMode().getWidth() == width) && 
            (Display.getDisplayMode().getHeight() == height) && 
    	(Display.isFullscreen() == fullscreen)) {
    	    return;
        }

        try {
            DisplayMode targetDisplayMode = null;
    		
    	if (fullscreen) {
    	    DisplayMode[] modes = Display.getAvailableDisplayModes();
    	    int freq = 0;
    				
    	    for (int i=0;i<modes.length;i++) {
    	        DisplayMode current = modes[i];
    					
    		if ((current.getWidth() == width) && (current.getHeight() == height)) {
    		    if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
    		        if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
    			    targetDisplayMode = current;
    			    freq = targetDisplayMode.getFrequency();
                            }
                        }

    		    // if we've found a match for bpp and frequence against the 
    		    // original display mode then it's probably best to go for this one
    		    // since it's most likely compatible with the monitor
    		    if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel()) &&
                            (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
                                targetDisplayMode = current;
                                break;
                        }
                    }
                }
            } else {
                targetDisplayMode = new DisplayMode(width,height);
            }

            if (targetDisplayMode == null) {
                System.out.println("Failed to find value mode: "+width+"x"+height+" fs="+fullscreen);
                return;
            }

            Display.setDisplayMode(targetDisplayMode);
            Display.setFullscreen(fullscreen);
    			
        } catch (LWJGLException e) {
            System.out.println("Unable to setup mode "+width+"x"+height+" fullscreen="+fullscreen + e);
        }
    }
}