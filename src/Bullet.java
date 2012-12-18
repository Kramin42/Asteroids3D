import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Bullet {
	Vector3f pos, vel, dir;
	Vector4f color;
	float length, speed;
	
	Bullet()
	{
		pos = new Vector3f();
		vel = new Vector3f();
		dir = new Vector3f();
		color = new Vector4f();
		length = 0;
		speed = 0;
	}
	
	void update(float gameBounds)
	{
		Vector3f.add(pos, vel, pos);
		
		//check if bullet is out of bounds
		if (pos.x>gameBounds || pos.x<-gameBounds || pos.y>gameBounds || pos.y<-gameBounds || pos.z>gameBounds || pos.z<-gameBounds){
			delete();
		}
		
		//System.out.println("Direction:");
		//System.out.println(dir);
//		if (Vector3f.dot(pos, pos)>warpDist*warpDist)
//			pos.negate();
	}
	
	void delete()
	{
		
	}
}
