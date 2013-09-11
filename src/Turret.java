import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import java.util.ArrayList;

import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Turret extends entity{
	Vector4f color;
	private float aimTolerance = 0.1f;//at a distance of 1, drops off with distance^2
	private float bulletSpeed = 10.0f;
	private float bulletFadeParam = 100000.0f;
	private int fireCounter;
	private int fireDelay;
	private Vector4f bulletColor;
	private float rotationSpeed = 0.08f;
	Sphere sphere;
	Cylinder cyl;
	
	int targetID;
	boolean targetDestroyed;
	
	private ArrayList<Bullet> blts;
	
	Turret()
	{
		super();
		color = new Vector4f(0.0f,0.0f,0.5f,1.0f);
		sphere = new Sphere();
		cyl = new Cylinder();
		blts = new ArrayList<Bullet>();
		fireCounter = 0;
		fireDelay = 50;
		bulletColor = new Vector4f(0.0f,1.0f,0.0f,1.0f);
		targetID = -1;
		targetDestroyed = false;
		//dir = new Vector3f(1,0,0);
		//up = new Vector3f(0,0,1);
	}
	
	void update(float gameBounds)
	{
		fireCounter++;
		
		updatePos();
		
		//check if Turret is out of bounds
		if (pos.x>gameBounds) {pos.x = gameBounds; prevPos.x = gameBounds;}
		if (pos.x<-gameBounds) {pos.x = -gameBounds; prevPos.x = -gameBounds;}
		if (pos.y>gameBounds) {pos.y = gameBounds; prevPos.y = gameBounds;}
		if (pos.y<-gameBounds) {pos.y = -gameBounds; prevPos.y = -gameBounds;}
		if (pos.z>gameBounds) {pos.z = gameBounds; prevPos.z = gameBounds;}
		if (pos.z<-gameBounds) {pos.z = -gameBounds; prevPos.z = -gameBounds;}
	}
	
	boolean aim(Vector3f pos, Vector3f prevPos){//TODO: predictive aiming
		//relativeRotate(0.0f, rotationSpeed);
		//relativeRotate(rotationSpeed, 0.0f);
		Vector3f targetDir = new Vector3f();
		Vector3f vel = new Vector3f();
		Vector3f.sub(pos, prevPos, vel);
		Vector3f.sub(pos, this.pos, targetDir);// no velocity prediction
		
		float a = Vector3f.dot(vel, vel) - bulletSpeed*bulletSpeed;
		float b = 2*Vector3f.dot(vel, targetDir);
		float c = targetDir.lengthSquared();
		
		float disc = b*b - 4*a*c;
		if (disc >= 0){
			float q = (float) ((b + Math.signum(b)*Math.sqrt(disc))/-2.0);
			float t1 = q/a;
			float t2 = c/q;
			float t = 0;
			if (t1>=0 && t2>=0)
				t = Math.min(t1, t2);
			else if (t1>=0)
				t = t1;
			else if (t2>=0)
				t = t2;
			else return false;
			
			targetDir.scale(1/t);
			Vector3f.add(targetDir, vel, targetDir);
		}
		
		float dist = targetDir.length();
		targetDir.normalise();
		Vector3f axis = new Vector3f();
		Vector3f.cross(targetDir, dir, axis);
		//System.out.println(axis.length());
		boolean fire = false;
		if (axis.length() <= aimTolerance/dist) fire = true; //return true if aiming maneuver complete
		float angle = (float) Math.asin(axis.length());
		axis.normalise();
		//System.out.println(angle);
		//System.out.println(axis);
		angle = angle*(rotationSpeed+1.0f/(100*angle+1.0f));
		rotate(angle,axis);
		//System.out.println(dir);
    	return fire;
	}
	
	
	
	void fire(){
		if (fireCounter >= fireDelay){
			fireCounter = 0;
			Bullet bullet = new Bullet();
			bullet.translate(pos);
			//bullet.translate(bullet.scalarProduct(player.dir, bulletSpeed));
			bullet.setVel(dir, bulletSpeed);
			bullet.setColor(bulletColor);
			blts.add(bullet);
		}
	}
	
	boolean canFire(){
		return fireCounter >= fireDelay;
	}
	
	void draw(){
		glPushMatrix();
        //glTranslatef(pos.x, pos.y, pos.z);
        glColor4f(color.x, color.y, color.z, color.w);
		glTranslatef(pos.x, pos.y, pos.z);
        glBegin(GL_LINES);
        glVertex3f(0.0f, 0.0f, 0.0f);
        glVertex3f(dir.x, dir.y, dir.z);
        glEnd();
		//glRotatef(-90, 1, 0, 0);
		//glRotatef(-90, 0, 1, 0);
		//gluLookAt(0.0f,0.0f,0.0f,dir.x,dir.y,dir.z,up.x,up.y,up.z);
        Vector3f axis = new Vector3f();
		Vector3f.cross(dir, new Vector3f(0,0,1), axis);
		if (axis.length() != 0){
			float angle = (float) Math.acos(Vector3f.dot(dir, new Vector3f(0,0,1)));
			//System.out.println(Math.toDegrees(-angle));
			glRotatef((float) Math.toDegrees(-angle), axis.x, axis.y, axis.z);
		}
        sphere.draw(0.5f, 8, 8);
        //glRotatef(90, 0, 1, 0);
        cyl.draw(0.2f, 0.2f, 1.0f, 8, 1);
        glPopMatrix();
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
						targetDestroyed = true;
						return true;
					}
					Vector3f.add(checkPos, diff, checkPos);
				}
			}
		}
		return false;
	}
}
