import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

public class entity {
	Vector3f pos, prevPos, acc, dir, up;
	boolean exists;
	entity()
	{
		pos = new Vector3f(0,0,0);
		prevPos = new Vector3f(0,0,0);
		acc = new Vector3f(0,0,0);
		dir = new Vector3f(1,0,0);
		up = new Vector3f(0,0,1);
		exists = true;
	}
	
	void updatePos()
	{
		// using verlet integration pos += pos - prevPos + acc
		Vector3f temp = new Vector3f();
		Vector3f.add(pos, Vector3f.sub(pos, prevPos, null), temp);
		Vector3f.add(temp, acc, temp);
		prevPos = pos;
		pos = temp;
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
								(float) Math.sin(pitchAmount)), null),
				scalarProduct(selfTensorProduct(left),
						1.0f - (float) Math.cos(pitchAmount)), null);
		
		Matrix3f yawTransform = new Matrix3f();
		yawTransform = Matrix3f.add(
				Matrix3f.add(
						scalarProduct(identity, (float) Math.cos(yawAmount)),
						scalarProduct(crossProdMat(up),
								(float) Math.sin(yawAmount)), null),
				scalarProduct(selfTensorProduct(up),
						1.0f - (float) Math.cos(yawAmount)), null);
		
		temp = Matrix3f.mul(pitchTransform, yawTransform, temp);
		dir = Matrix3f.transform(temp, dir, dir);
		up = Matrix3f.transform(temp, up, up);
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
	
	void setVel(Vector3f dir, float speed){
		Vector3f vel = scalarProduct(dir, speed);
		prevPos = Vector3f.sub(pos, vel, null);
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
