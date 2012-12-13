import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;


public class Player {
	Vector3f pos, vel, dir, up;
	float pitch, yaw;
	float acceleration, brakeAcceleration;
	boolean accelerating;
	boolean braking;
	Player()
	{
		pos = new Vector3f();
		vel = new Vector3f();
		dir = new Vector3f();
		up = new Vector3f();
		pitch=0;
		yaw=0;
		acceleration=0;
		brakeAcceleration = 0;
		accelerating=false;
		braking = false;
	}
	
	void update(float gameBounds)
	{
		if (accelerating){
			Vector3f.add(vel, scalarProduct(dir, acceleration), vel);
		}
		if (braking){
			//Vector3f temp = 
		}
		Vector3f.add(pos, vel, pos);
		
		//check if ship is out of bounds
		if (pos.x>gameBounds) {vel.x = 0; pos.x = gameBounds;}
		if (pos.x<-gameBounds) {vel.x = 0; pos.x = -gameBounds;}
		if (pos.y>gameBounds) {vel.y = 0; pos.y = gameBounds;}
		if (pos.y<-gameBounds) {vel.y = 0; pos.y = -gameBounds;}
		if (pos.z>gameBounds) {vel.z = 0; pos.z = gameBounds;}
		if (pos.z<-gameBounds) {vel.z = 0; pos.z = -gameBounds;}
		
		//System.out.println("Direction:");
		//System.out.println(dir);
//		if (Vector3f.dot(pos, pos)>warpDist*warpDist)
//			pos.negate();
	}
	
	void rotate(float pitchAmount, float yawAmount){
		pitch+=pitchAmount;
		if (pitch>Math.PI/2)
			pitch = (float) (Math.PI/2);
		if (pitch<-Math.PI/2)
			pitch = (float) (-Math.PI/2);
		
		yaw+=yawAmount;
		if (yaw>2*Math.PI)
			yaw -= 2*Math.PI;
		if (yaw<0)
			yaw += 2*Math.PI;
		
		calcDir();
		calcUp();
	}
	
	void relativeRotate(float pitchAmount, float yawAmount){
		Matrix3f identity = new Matrix3f();
		Matrix3f temp = new Matrix3f();
		identity.setIdentity();
		Matrix3f pitchTransform = new Matrix3f();
		Vector3f left = getLeft();
		pitchTransform = Matrix3f.add(
				Matrix3f.add(
						scalarProduct(identity, (float) Math.cos(pitchAmount)),
						scalarProduct(crossProdMat(left),
								(float) Math.sin(pitchAmount)), temp),
				scalarProduct(selfTensorProduct(left),
						1.0f - (float) Math.cos(pitchAmount)), temp);
		
		Matrix3f yawTransform = new Matrix3f();
		temp = new Matrix3f();
		yawTransform = Matrix3f.add(
				Matrix3f.add(
						scalarProduct(identity, (float) Math.cos(yawAmount)),
						scalarProduct(crossProdMat(up),
								(float) Math.sin(yawAmount)), temp),
				scalarProduct(selfTensorProduct(up),
						1.0f - (float) Math.cos(yawAmount)), temp);
		
		temp = Matrix3f.mul(pitchTransform, yawTransform, temp);
		dir = Matrix3f.transform(temp, dir, dir);
		up = Matrix3f.transform(temp, up, up);
	}
	
	void handleInput(float x, float y, float sensitivity, float minValue) // x and y should be between 0 and 1
	{
		//don't move the ship for very small inputs
		if (x<minValue && x>-minValue) x=0;
		if (y<minValue && y>-minValue) y=0;
		relativeRotate(x*sensitivity, y*sensitivity);
    	dir.normalise();
    	up.normalise();
    	calcYaw();
    	calcPitch();
	}
	
	void calcDir()
	{
    	dir.x = (float) (Math.cos(pitch)*Math.cos(yaw));
    	dir.y = (float) (Math.cos(pitch)*Math.sin(yaw));
    	dir.z = (float) (Math.sin(pitch));
	}
	
	void calcUp()
	{
		float upPitch = (float) (pitch + Math.PI/2);
		float upYaw = yaw;
		if (upPitch>Math.PI/2){
			upPitch = (float) (Math.PI-upPitch);
			upYaw = (float) (2*Math.PI - yaw);
		}
		up.x = (float) (Math.cos(upPitch)*Math.cos(upYaw));
    	up.y = (float) (Math.cos(upPitch)*Math.sin(upYaw));
    	up.z = (float) (Math.sin(upPitch));
	}
	
	void calcYaw()
	{
		yaw = (float) Math.atan2(dir.y, dir.x);
	}
	
	void calcPitch()
	{
		pitch = (float) Math.atan2(dir.z, Math.sqrt(dir.x*dir.x+dir.y*dir.y));
	}
	
	void translate (Vector3f v){
		pos = Vector3f.add(pos, v, pos);
	}
	
	Vector3f getLeft()
	{
		Vector3f left = new Vector3f();
		left = Vector3f.cross(up, dir, left);
		left.normalise();
		return left;
	}
	
	Vector3f getRight()
	{
		Vector3f right = new Vector3f();
		right = Vector3f.cross(dir, up, right);
		right.normalise();
		return right;
	}
	
	Vector3f scalarProduct(Vector3f v,float f){
		Vector3f vt = new Vector3f();
		vt.x=f*v.x;
		vt.y=f*v.y;
		vt.z=f*v.z;
		return vt;
	}
	
	Matrix3f scalarProduct(Matrix3f m,float f){
		Matrix3f mt = new Matrix3f();
		mt.m00=f*m.m00; mt.m01=f*m.m01; mt.m02=f*m.m02;
		mt.m10=f*m.m10; mt.m11=f*m.m11; mt.m12=f*m.m12;
		mt.m20=f*m.m20; mt.m21=f*m.m21; mt.m22=f*m.m22;
		return mt;
	}
	
	Matrix3f crossProdMat(Vector3f v){
		Matrix3f m = new Matrix3f();
		m.m01 = -v.z;
		m.m02 = v.y;
		m.m10 = v.z;
		m.m12 = -v.x;
		m.m20 = -v.y;
		m.m21 = v.x;
		return m;
	}
	
	Matrix3f selfTensorProduct(Vector3f v){
		Matrix3f m = new Matrix3f();
		m.m00 = v.x*v.x; m.m01 = v.x*v.y; m.m02 = v.x*v.z;
		m.m10 = v.x*v.y; m.m11 = v.y*v.y; m.m12 = v.y*v.z;
		m.m20 = v.x*v.z; m.m21 = v.y*v.z; m.m22 = v.z*v.z;
		return m;
	}
}
