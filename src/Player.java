import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;


public class Player extends entity{
	private float bulletSpeed = 10.0f;
	private float bulletFadeParam = 100000.0f;
	private int fireCounter;
	private int fireDelay = 10;
	
	
	//brakePower must be between 0 and 1
	float enginePower, brakePower;
	boolean accelerating;
	boolean braking;
	private ArrayList<Bullet> blts;
	
	Player()
	{
		super();
		//pitch=0;
		//yaw=0;
		enginePower= 0;
		brakePower = 0;
		accelerating=false;
		braking = false;
		blts = new ArrayList<Bullet>();
		fireCounter = 0;
	}
	
	void update(float gameBounds)
	{
		fireCounter++;
		
		if (braking){
			Vector3f temp = Vector3f.sub(pos, prevPos, null);
			prevPos = Vector3f.add(prevPos, scalarProduct(temp, brakePower), null);
		}
		if (accelerating)
			acc = scalarProduct(dir, enginePower);
		
		updatePos();
		
		//check if ship is out of bounds
		if (pos.x>gameBounds) {pos.x = gameBounds; prevPos.x = gameBounds;}
		if (pos.x<-gameBounds) {pos.x = -gameBounds; prevPos.x = -gameBounds;}
		if (pos.y>gameBounds) {pos.y = gameBounds; prevPos.y = gameBounds;}
		if (pos.y<-gameBounds) {pos.y = -gameBounds; prevPos.y = -gameBounds;}
		if (pos.z>gameBounds) {pos.z = gameBounds; prevPos.z = gameBounds;}
		if (pos.z<-gameBounds) {pos.z = -gameBounds; prevPos.z = -gameBounds;}
	}
	
	void handleInput(float x, float y, float sensitivity, float minValue) // x and y should be between 0 and 1
	{
		//don't move the ship for very small inputs
		if (x<minValue && x>-minValue) x=0;
		if (y<minValue && y>-minValue) y=0;
		relativeRotate(x*sensitivity, y*sensitivity);
    	dir.normalise();
    	up.normalise();
    	//calcYaw();
    	//calcPitch();
	}
	
	void fire(){
		if (fireCounter >= fireDelay){
			fireCounter = 0;
			Bullet bullet = new Bullet();
			bullet.translate(pos);
			//bullet.translate(bullet.scalarProduct(player.dir, bulletSpeed));
			bullet.setVel(dir, bulletSpeed);
			bullet.setColor(new Vector4f(1.0f,0.0f,0.0f,1.0f));
			blts.add(bullet);
		}
	}
	
	void updateBullets(float gameBounds){
    	for (int i=blts.size()-1; i>=0;i--){
    		blts.get(i).update(gameBounds);
    		if (!blts.get(i).exists){
    			blts.remove(i);//TODO: check for slowdown when removing large numbers of bullets
    		}
    		//System.out.println("x: "+blts.get(i).pos.x+", y: "+blts.get(i).pos.y+", z: "+blts.get(i).pos.z);
    		//System.out.println("px: "+blts.get(i).prevPos.x+", py: "+blts.get(i).prevPos.y+", pz: "+blts.get(i).prevPos.z);
    		//System.out.print(blts.get(i).exists);
        }
	}
	
	void drawBullets(){
		//draw the bullets
        glBegin(GL_LINES);
        //glColor4f(1.0f,1.0f,1.0f,1.0f);
        float brightness = 0.0f;
        for (int i=0; i<blts.size();i++){
        	brightness = bulletFadeParam/Vector3f.sub(pos, blts.get(i).pos, null).lengthSquared();
        	brightness = brightness>1.0f ? 1.0f : brightness;
        	glColor4f(blts.get(i).color.x,blts.get(i).color.y,blts.get(i).color.z,blts.get(i).color.w*brightness);
        	glVertex3f(blts.get(i).prevPos.x,blts.get(i).prevPos.y,blts.get(i).prevPos.z);
        	glVertex3f(blts.get(i).pos.x,blts.get(i).pos.y,blts.get(i).pos.z);
        }
        glEnd();
        glBegin(GL_POINTS);
        //glColor4f(1.0f,1.0f,1.0f,1.0f);
        for (int i=0; i<blts.size();i++){
        	brightness = bulletFadeParam/Vector3f.sub(pos, blts.get(i).pos, null).lengthSquared();
        	brightness = brightness>1.0f ? 1.0f : brightness;
        	glColor4f(blts.get(i).color.x,blts.get(i).color.y,blts.get(i).color.z,blts.get(i).color.w*brightness);
        	//glVertex3f(blts.get(i).prevPos.x,blts.get(i).prevPos.y,blts.get(i).prevPos.z);
        	glVertex3f(blts.get(i).pos.x,blts.get(i).pos.y,blts.get(i).pos.z);
        }
        glEnd();
	}
	
	boolean checkBulletHit(Vector3f pos, float radius){
		for (int i=0; i<blts.size();i++){
			Bullet blt = blts.get(i);
			Vector3f diff = new Vector3f();
			Vector3f.sub(blt.pos, pos, diff);
			if (diff.lengthSquared() < (bulletSpeed-radius)*(bulletSpeed-radius)){//initial rough check
				//System.out.println("passed rough check");
				Vector3f checkPos = new Vector3f(blt.pos);
				Vector3f.sub(blt.pos, blt.prevPos, diff);
				diff.scale(0.1f);
				for (int j=0; j<10; j++){
					Vector3f d = new Vector3f();
					Vector3f.sub(pos, checkPos, d);
					//System.out.println(d.length());
					if (d.lengthSquared() < radius*radius){
						blt.delete();
						return true;
					}
					Vector3f.add(checkPos, diff, checkPos);
				}
			}
		}
		return false;
	}
	
	void draw(){
		
	}
	
	void setEngines(boolean state)
	{
		accelerating = state;
		if (!state)
			acc.x = 0; acc.y = 0; acc.z = 0;
	}
	
	void setBrakes(boolean state)
	{
		braking = state;
	}
	
//	void rotate(float pitchAmount, float yawAmount){
//	pitch+=pitchAmount;
//	if (pitch>Math.PI/2)
//		pitch = (float) (Math.PI/2);
//	if (pitch<-Math.PI/2)
//		pitch = (float) (-Math.PI/2);
//	
//	yaw+=yawAmount;
//	if (yaw>2*Math.PI)
//		yaw -= 2*Math.PI;
//	if (yaw<0)
//		yaw += 2*Math.PI;
//	
//	calcDir();
//	calcUp();
//}
	
//	void calcDir()
//	{
//    	dir.x = (float) (Math.cos(pitch)*Math.cos(yaw));
//    	dir.y = (float) (Math.cos(pitch)*Math.sin(yaw));
//    	dir.z = (float) (Math.sin(pitch));
//	}
//	
//	void calcUp()
//	{
//		float upPitch = (float) (pitch + Math.PI/2);
//		float upYaw = yaw;
//		if (upPitch>Math.PI/2){
//			upPitch = (float) (Math.PI-upPitch);
//			upYaw = (float) (2*Math.PI - yaw);
//		}
//		up.x = (float) (Math.cos(upPitch)*Math.cos(upYaw));
//    	up.y = (float) (Math.cos(upPitch)*Math.sin(upYaw));
//    	up.z = (float) (Math.sin(upPitch));
//	}
//	
//	void calcYaw()
//	{
//		yaw = (float) Math.atan2(dir.y, dir.x);
//	}
//	
//	void calcPitch()
//	{
//		pitch = (float) Math.atan2(dir.z, Math.sqrt(dir.x*dir.x+dir.y*dir.y));
//	}
}
