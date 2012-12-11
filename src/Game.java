import java.nio.FloatBuffer;

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
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Vector3f;

// In the last lesson, we got a taste of the third dimension by rotating some flat planar objects
// around.  Now we're finally going to start building real three dimensional objects and
// pop that triangle and square into a sphere and cube and spin those around.

public class Game {
    private String windowTitle = "Asteroids3D";
    private int windowWidth  = 800;
    private int windowHeight = 600;
    private boolean quitRequested = false;
    
    public float minInputRad = 0.05f;
    public float sensitivity = 0.01f;

    private float sphereRotation = 0.0f;
    
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

    // The light is positioned back "behind" on the z axis.  The fourth component in the light
    // position is a scaling factor that will pretty much always be 1.0.
    private float[] lightPosition = {0.0f, 0.0f, 100.0f, 1.0f};
    
    
//    Vector3f camPos = new Vector3f(0.0f,0.0f,0.0f);
//    
//    Vector3f camDir = new Vector3f(-1.0f,0.0f,0.0f);
    Player player = new Player();
    
    Sphere sphere = new Sphere();

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
        gluPerspective(45.0f, (float) w / (float) h, 0.1f, 100.0f);
        glMatrixMode(GL_MODELVIEW);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glShadeModel(GL_SMOOTH);
        
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        // Here's where we define our light.  OpenGL supports up to eight lights at a time.
        FloatBuffer temp = BufferUtils.createFloatBuffer(4);
        glLight(GL_LIGHT1, GL_AMBIENT, (FloatBuffer) temp.put(lightAmbient).rewind());
        glLight(GL_LIGHT1, GL_DIFFUSE, (FloatBuffer) temp.put(lightDiffuse).rewind());
        glLight(GL_LIGHT1, GL_POSITION, (FloatBuffer) temp.put(lightPosition).rewind());
        glEnable(GL_LIGHT1);
        
        glEnable(GL_LIGHTING);
    }

    private void renderScene() {
        // The code should look vaguely familiar, but we're drawing a lot more shapes now

        sphereRotation += 1.0f;
        sphereRotation %= 360;
        
        //eyez+=0.01f;
        //eyex+=0.01f;

        // Now that we're dealing with depth, we have to reset a little more than before,
        // namely the depth buffer that we told OpenGL to support above.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glLoadIdentity();                          // Reset The Current Modelview Matrix

		gluLookAt(player.pos.x, player.pos.y, player.pos.z, player.pos.x
				+ player.dir.x, player.pos.y + player.dir.y, player.pos.z
				+ player.dir.z, player.up.x, player.up.y, player.up.z);

        FloatBuffer temp = BufferUtils.createFloatBuffer(4);
        glLight(GL_LIGHT1, GL_POSITION, (FloatBuffer) temp.put(lightPosition).rewind());
        
        glTranslatef(8.0f, 0.0f, 0.0f);
        glRotatef(sphereRotation, 0.0f, 1.0f, 0.0f);
        glColor3f(1.0f, 1.0f, 1.0f);
        sphere.draw(1.0f, 16, 16);
        glLoadIdentity();
        
        
    }

    // Everything below here is the same as it was in the previous lesson.


    /** Sets up OpenGL, runs the main loop of our app, and handles exiting */
    public void run() throws Exception {
        initialize();
        try {
            while (!quitRequested) {
                // This is the main loop of our application
                handleInput();      // Process input (e.g. keyboard, mouse, window events)
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
        initGL();       // Set options and initial projection
        player.calcDir();
        System.out.println("player dir x: "+player.dir.x+", y: "+player.dir.y+", z: "+player.dir.z);
        player.calcUp();
        System.out.println("player up x: "+player.up.x+", y: "+player.up.y+", z: "+player.up.z);
        player.pos.x = 0.0f;
        player.pos.y = 0.0f;
        player.pos.z = 0.0f;
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

                    case Keyboard.KEY_RETURN:
                        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
                            Display.setFullscreen(!Display.isFullscreen());
                        break;
                }
            }
        }
        
        while (Mouse.next())
        {
//        	if (Mouse.getEventButtonState()){
//        		if (Mouse.getEventButton() == 0){
//        			Mouse.setGrabbed(true);
//        		}
//        	}
//        	else {
//        		if (Mouse.getEventButton() == 0){
//        			Mouse.setGrabbed(false);
//        		}
//        	}
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
//        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
////        	System.out.println("camDir x: "+camDir.x+", y: "+camDir.y+", z: "+camDir.z);
//        	camDir = rotateVector(camDir, 0.0f, 0.02f);
////        	System.out.println("camDir x: "+camDir.x+", y: "+camDir.y+", z: "+camDir.z);
//        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){
//        	camDir = rotateVector(camDir, 0.0f, -0.02f);
//        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_UP)){
//        	camDir = rotateVector(camDir, -0.02f, 0.0f);
//        }
//        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
//        	camDir = rotateVector(camDir, 0.02f, 0.0f);
//        }
        if (Mouse.isButtonDown(0)){
        	//Vector3f mouse = new Vector3f();
//        	player.relativeRotate(Mouse.getDY()*0.001f, Mouse.getDX()*0.001f);
//        	player.dir.normalise();
//        	player.up.normalise();
//        	player.calcYaw();
//        	player.calcPitch();
        	//Mouse.setCursorPosition(windowWidth/2, windowHeight/2);
        	player.handleInput(2.0f*Mouse.getY()/windowHeight - 1.0f, 2.0f*Mouse.getX()/windowWidth - 1.0f, sensitivity, minInputRad);
        }
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


}