package main;

import vectors.Point2D;

public class Ball2D extends Particle2D {
	double rad;
	public Ball2D(Point2D position, Point2D oldposition, double radius) {
		super(position, oldposition);
		rad = radius;
	}


}
