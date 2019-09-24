package main;

import vectors.Point2D;
import vectors.Point3D;

public class Particle3D {
	Point3D pos;
	Point3D oldpos;
	boolean pin;

	public Particle3D(Point3D position, Point3D oldposition) {
		pos = position;
		oldpos = oldposition;
		pin = false;
	}

	public Particle3D(Point3D position, Point3D oldposition, boolean pinned) {
		pos = position;
		oldpos = oldposition;
		pin = pinned;
	}

	public void update() {
		if (!pin) {
			Point3D vel = pos.add(oldpos.scale(-1));
			oldpos = pos;
			pos = pos.add(vel.scale(0.999));
		}else{
			pos = new Point3D(oldpos.x,oldpos.y, oldpos.z);
		}

	}
	public Point3D getVel(){
		return pos.add(oldpos.scale(-1));
	}
	public void setVel(Point3D vel){
		oldpos = pos.add(vel.scale(-1));
	}

}
