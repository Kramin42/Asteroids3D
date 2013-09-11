import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.glu.Sphere;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;


public class Asteroid extends entity{
	Vector4f color;
	float rotationSpeed, rotation;
	Vector3f rotationAxis;
	Sphere sphere;
	float radius;
	
	Asteroid(){
		super();
		color = new Vector4f(100/255.0f,89/255.0f,83/255.0f,1.0f);
		sphere = new Sphere();
		rotation = 0.0f;
		rotationSpeed = 1.0f;
		rotationAxis = new Vector3f(1.0f,0.0f,0.0f);
		radius = 1.0f;
	}
	
	void update(float gameBounds){
		if (exists){
			updatePos();

			// check if is out of bounds
			if (pos.x > gameBounds || pos.x < -gameBounds || pos.y > gameBounds
					|| pos.y < -gameBounds || pos.z > gameBounds
					|| pos.z < -gameBounds) {
				delete();
			}

			//update rotaion
			rotation += rotationSpeed;
			rotation %= 360;
		}
	}
	
	void draw(){
        glPushMatrix();
        glTranslatef(pos.x, pos.y, pos.z);
        glRotatef(rotation, rotationAxis.x, rotationAxis.y, rotationAxis.z);
        glColor4f(color.x, color.y, color.z, color.w);
        sphere.draw(radius, 8, 8);
        glPopMatrix();
	}
	
	void delete()
	{
		exists = false;
	}
}
