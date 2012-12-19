import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Bullet extends entity{
	//Vector3f pos, vel, dir;
	Vector4f color;
	float length, speed;
	boolean exists = false;
	
	Bullet()
	{
		super();
		color = new Vector4f();
	}
	
	void update(float gameBounds)
	{
		if (exists){
			updatePos();

			// check if bullet is out of bounds
			if (pos.x > gameBounds || pos.x < -gameBounds || pos.y > gameBounds
					|| pos.y < -gameBounds || pos.z > gameBounds
					|| pos.z < -gameBounds) {
				delete();
			}

			// System.out.println("Direction:");
			// System.out.println(dir);
			// if (Vector3f.dot(pos, pos)>warpDist*warpDist)
			// pos.negate();
		}
	}
	
	void delete()
	{
		exists = false;
	}
}
