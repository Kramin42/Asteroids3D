import org.lwjgl.util.vector.Vector3f;


public class Player extends entity{
	//brakePower must be between 0 and 1
	float enginePower, brakePower;
	boolean accelerating;
	boolean braking;
	Player()
	{
		super();
		//pitch=0;
		//yaw=0;
		enginePower= 0;
		brakePower = 0;
		accelerating=false;
		braking = false;
	}
	
	void update(float gameBounds)
	{
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
