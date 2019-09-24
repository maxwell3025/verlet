package main;

import vectors.Point2D;
import vectors.Point3D;

public class Particle2D {
	Point2D pos;
	Point2D oldpos;
	boolean pin;

	public Particle2D(Point2D position, Point2D oldposition) {
		pos = position;
		oldpos = oldposition;
		pin = false;
	}

	public Particle2D(Point2D position, Point2D oldposition, boolean pinned) {
		pos = position;
		oldpos = oldposition;
		pin = pinned;
	}

	public void update() {
		if (!pin) {
			Point2D vel = pos.add(oldpos.scale(-1));
			oldpos = pos;
			pos = pos.add(vel);
		} else {
			pos = new Point2D(oldpos.x, oldpos.y);
		}

	}

	public Point2D getVel() {
		return pos.add(oldpos.scale(-1));
	}

	public void setVel(Point2D vel) {
		oldpos = pos.add(vel.scale(-1));
	}

}