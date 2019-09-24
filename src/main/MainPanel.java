package main;

import static main.IntMath.abs;
import static main.IntMath.max;
import static main.IntMath.min;

import static java.lang.Math.*;

import java.awt.AWTException;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JFrame;

import vectors.Matrix3D;
import vectors.Point2D;
import vectors.Point3D;

public class MainPanel extends JPanel implements Runnable, MouseMotionListener, MouseListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int width;
	int height;
	BufferedImage buffer;
	int[] raster;
	int threadnum = 0;
	long time;
	Point2D Mouse = new Point2D(200, 200);
	ArrayList<Particle3D> particles = new ArrayList<Particle3D>();
	ArrayList<Beam3D> beams = new ArrayList<Beam3D>();
	boolean holding = false;
	Point3D maxCorner = new Point3D(256, 256, 256);
	Point3D minCorner = new Point3D(-256, -256, -10000);
	Point3D camera = new Point3D(0, 0, 1000);
	Matrix3D view = Matrix3D.identity();
	Matrix3D inverseview;
	double pitch;
	double yaw;
	boolean running = false;
	boolean mouselock = true;
	Container container;
	boolean[] ispressed = new boolean[65536];

	public MainPanel() {

	}

	public void Init() {
		Dimension d = getPreferredSize();
		width = d.width;
		height = d.height;
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		raster = new int[width * height];
		container = getParent();
		addKeyListener(this);
		((JFrame) container.getParent().getParent().getParent()).addKeyListener(this);
		addMouseMotionListener(this);
		container.addMouseMotionListener(this);
		addMouseListener(this);
		container.addMouseListener(this);
		generateWorld();
		Point2D shift = Point2D.origin();
		for (Container c = getParent(); c != null; c = c.getParent()) {
			shift.x += c.getX();
			shift.y += c.getY();
		}
		try {
			// TODO
			new Robot().mouseMove(width / 2 + getX() + (int) shift.x, height / 2 + getY() + (int) shift.y);
		} catch (AWTException e1) {
		}
		new Thread(this).start();
	}

	protected void generateWorld() {
		for (int y = -16; y < 16; y++) {
			for (int x = -16; x < 16; x++) {
				addParticle((x * 16 + 8) * 0.25, (y * 16 + 8) * 0.25, 256);
			}
		}
		for (int y = 0; y < 32 - 1; y++) {
			for (int x = 0; x < 32; x++) {
				addBeam(x + y * 32, x + (y + 1) * 32);
			}
		}
		for (int y = 0; y < 32; y++) {
			for (int x = 0; x < 32 - 1; x++) {
				addBeam(x + y * 32, x + y * 32 + 1);
			}
		}
	}

	private void addParticle(double x, double y, double z) {
		particles.add(new Particle3D(new Point3D(x, y, z), new Point3D(x, y, z)));
	}

	private void addBeam(int a, int b) {
		Point3D apos = particles.get(a).pos;
		Point3D bpos = particles.get(b).pos;
		double dist = apos.add(bpos.scale(-1)).dist();
		beams.add(new Beam3D(particles.get(a), particles.get(b), dist));
	}

	private void addBeam(int a, int b, double len) {
		beams.add(new Beam3D(particles.get(a), particles.get(b), len));
	}

	public void graphicsUpdate() {
		Arrays.fill(raster, -1);
		inverseview = view.inverse();
		Point2D ooo = project(new Point3D(minCorner.x, minCorner.y, minCorner.z));
		Point2D ooi = project(new Point3D(minCorner.x, minCorner.y, maxCorner.z));
		Point2D oio = project(new Point3D(minCorner.x, maxCorner.y, minCorner.z));
		Point2D oii = project(new Point3D(minCorner.x, maxCorner.y, maxCorner.z));
		Point2D ioo = project(new Point3D(maxCorner.x, minCorner.y, minCorner.z));
		Point2D ioi = project(new Point3D(maxCorner.x, minCorner.y, maxCorner.z));
		Point2D iio = project(new Point3D(maxCorner.x, maxCorner.y, minCorner.z));
		Point2D iii = project(new Point3D(maxCorner.x, maxCorner.y, maxCorner.z));
		drawLine((int) ioo.x, (int) ioo.y, (int) iio.x, (int) iio.y, 0xff000000);
		drawLine((int) iio.x, (int) iio.y, (int) iii.x, (int) iii.y, 0xff000000);
		drawLine((int) iii.x, (int) iii.y, (int) ioi.x, (int) ioi.y, 0xff000000);
		drawLine((int) ioi.x, (int) ioi.y, (int) ioo.x, (int) ioo.y, 0xff000000);
		drawLine((int) ooo.x, (int) ooo.y, (int) oio.x, (int) oio.y, 0xff000000);
		drawLine((int) oio.x, (int) oio.y, (int) oii.x, (int) oii.y, 0xff000000);
		drawLine((int) oii.x, (int) oii.y, (int) ooi.x, (int) ooi.y, 0xff000000);
		drawLine((int) ooi.x, (int) ooi.y, (int) ooo.x, (int) ooo.y, 0xff000000);
		drawLine((int) ooo.x, (int) ooo.y, (int) ioo.x, (int) ioo.y, 0xff000000);
		drawLine((int) oio.x, (int) oio.y, (int) iio.x, (int) iio.y, 0xff000000);
		drawLine((int) oii.x, (int) oii.y, (int) iii.x, (int) iii.y, 0xff000000);
		drawLine((int) ooi.x, (int) ooi.y, (int) ioi.x, (int) ioi.y, 0xff000000);
		for (Beam3D c : beams) {
			Point3D a = c.a.pos;
			Point3D b = c.b.pos;
			Point2D newa = project(a);
			Point2D newb = project(b);
			drawLine((int) newa.x, (int) newa.y, (int) newb.x, (int) newb.y, 0xff000000);
		}
		for (Particle3D p : particles) {
			Point2D a = project(p.pos);
			fillOval((int) a.x - 1, (int) a.y - 1, 3, 3, 0xffff0000);
		}

		buffer.setRGB(0, 0, width, height, raster, 0, width);
		repaint();

	}

	private Point2D project(Point3D a) {
		Point3D shifted = a.add(camera.scale(-1));
		Point3D transformed = inverseview.transform(shifted);
		Point2D workpoint = new Point2D(transformed.x, transformed.y);
		workpoint = workpoint.scale(min(width, height) / (transformed.z));
		Point2D output = new Point2D(workpoint.x + width / 2, workpoint.y + height / 2);
		return output;
	}

	public void contentUpdate() {
		updatePhysics();
		for (int i = 0; i < 16; i++) {
			floorConstraints();
			beamConstraints();
			ballConstraints();
		}
	}

	private void userInput() {
		view = Matrix3D.rotz(yaw).transform(Matrix3D.rotx(pitch));
		if (ispressed[KeyEvent.VK_W]) {
			camera = camera.add(view.transform(new Point3D(0, 0, -1)));
		}
		if (ispressed[KeyEvent.VK_S]) {
			camera = camera.add(view.transform(new Point3D(0, 0, 1)));
		}
		if (ispressed[KeyEvent.VK_A]) {
			camera = camera.add(view.transform(new Point3D(1, 0, 0)));
		}
		if (ispressed[KeyEvent.VK_D]) {
			camera = camera.add(view.transform(new Point3D(-1, 0, 0)));
		}
	}

	private void updatePhysics() {
		for (Particle3D p : particles) {
			p.update();
			p.pos.z -= 0.01;
		}
	}

	private void floorConstraints() {
		for (Particle3D p : particles) {
			if (p.pos.x > maxCorner.x) {
				double vel = (p.pos.x - p.oldpos.x);
				p.pos.x = maxCorner.x;
				p.oldpos.x = maxCorner.x + vel;
			}
			if (p.pos.x < minCorner.x) {
				double vel = (p.pos.x - p.oldpos.x);
				p.pos.x = minCorner.x;
				p.oldpos.x = minCorner.x + vel;
			}
			if (p.pos.y > maxCorner.y) {
				double vel = (p.pos.y - p.oldpos.y);
				p.pos.y = maxCorner.y;
				p.oldpos.y = maxCorner.y + vel;
			}
			if (p.pos.y < minCorner.y) {
				double vel = (p.pos.y - p.oldpos.y);
				p.pos.y = minCorner.y;
				p.oldpos.y = minCorner.y + vel;
			}
			if (p.pos.z > maxCorner.z) {
				double vel = (p.pos.z - p.oldpos.z);
				p.pos.z = maxCorner.z;
				p.oldpos.z = maxCorner.z + vel;
			}
			if (p.pos.z < minCorner.z) {
				double vel = (p.pos.z - p.oldpos.z);
				p.pos.z = minCorner.z;
				p.oldpos.z = minCorner.z + vel;
			}
		}
	}

	private void beamConstraints() {
		for (int i = 0; i < beams.size(); i++) {
			Beam3D c = beams.get(i);
			Point3D a = c.a.pos;
			Point3D b = c.b.pos;
			double len = c.length;
			Point3D mid = a.add(b).scale(0.5);
			Point3D adist = a.add(mid.scale(-1));
			Point3D bdist = b.add(mid.scale(-1));
			adist = adist.scale(len / adist.dist() * 0.5);
			bdist = bdist.scale(len / bdist.dist() * 0.5);
			if (!c.a.pin) {
				c.a.pos = mid.add(adist);
			}
			if (!c.b.pin) {
				c.b.pos = mid.add(bdist);
			}
		}
	}

	private void ballConstraints() {
		for (Particle3D p : particles) {
			Point3D pos = p.pos;
			if (pos.dist() < 100) {
				pos = pos.scale(100 / pos.dist());
			}
			p.pos = pos;
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(buffer, 0, 0, null);
	}

	public void fillRect(int x, int y, int w, int h, int color) {
		for (int ix = 0; ix < w; ix++) {
			for (int iy = 0; iy < h; iy++) {
				int dx = ix + x;
				int dy = iy + y;
				if ((dx < 0 != dx < width) && (dy < 0 != dy < height)) {
					raster[dx + dy * width] = color;
				}
			}
		}
	}

	public void fillOval(int x, int y, int w, int h, int color) {
		for (int ix = 0; ix < w; ix++) {
			for (int iy = 0; iy < h; iy++) {
				double px = ((double) ix / w) - 0.5;
				double py = ((double) iy / h) - 0.5;
				if ((px) * (px) + (py) * (py) < 0.25 ) {
					int dx = ix + x;
					int dy = iy + y;
					if ((dx < 0 != dx < width) && (dy < 0 != dy < height)) {
						raster[dx + dy * width] = color;
					}
				}
			}
		}
	}

	public void drawPixel(int x, int y, int color) {
		if ((x < 0 != x < width) && (y < 0 != y < height)) {
			raster[x + y * width] = color;
		}
	}

	public void drawLine(int x1, int y1, int x2, int y2, int color) {
		if (x1 == x2 && y1 == y2) {

			if ((x1 < 0 != x1 < width) && (y1 < 0 != y1 < height)) {
				raster[x1 + y1 * width] = color;
			}
		} else if (abs(x1 - x2) < abs(y1 - y2)) {
			drawLineY(x1, y1, x2, y2, color);
		} else {
			drawLineX(x1, y1, x2, y2, color);
		}

	}

	public void drawLineX(int x1, int y1, int x2, int y2, int color) {
		int minx = min(x1, x2);
		int maxx = max(x1, x2);
		int miny = x1 < x2 ? y1 : y2;
		int maxy = x1 > x2 ? y1 : y2;
		int difx = maxx - minx;
		for (int x = minx; !(x > maxx); x++) {
			float shiftx = ((float) x - minx) / difx;
			int y = (int) (maxy * shiftx + miny * (1.0f - shiftx) + 0.5f);
			if ((x < 0 != x < width) && (y < 0 != y < height)) {
				raster[x + y * width] = color;
			}
		}
	}

	public void drawLineY(int x1, int y1, int x2, int y2, int color) {
		int miny = min(y1, y2);
		int maxy = max(y1, y2);
		int minx = y1 < y2 ? x1 : x2;
		int maxx = y1 > y2 ? x1 : x2;
		int dify = maxy - miny;
		for (int y = miny; !(y > maxy); y++) {
			float shifty = ((float) y - miny) / dify;
			int x = (int) (maxx * shifty + minx * (1.0f - shifty) + 0.5f);
			if ((x < 0 != x < width) && (y < 0 != y < height)) {
				raster[x + y * width] = color;
			}
		}
	}

	@Override
	public void run() {
		int thread = threadnum;
		threadnum++;
		new Thread(this).start();
		if (thread == 0) {
			for (;;) {
				// try {
				// Thread.sleep(1);
				// } catch (InterruptedException e) {
				// }
				graphicsUpdate();
			}

		}
		if (thread == 1) {
			for (;;) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				if (running) {
					contentUpdate();
				}
				userInput();
			}

		}
		if (thread == 2) {
			for (;;) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
				time++;
			}

		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Mouse = new Point2D(e.getX(), e.getY());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (mouselock) {
			Point2D shift = Point2D.origin();
			for (Container c = getParent(); c != null; c = c.getParent()) {
				shift.x += c.getX();
				shift.y += c.getY();
			}
			yaw -= (e.getX() - width / 2) * 0.01;
			pitch += (e.getY() - height / 2) * 0.01;
			try {
				// TODO
				new Robot().mouseMove(width / 2 + getX() + (int) shift.x, height / 2 + getY() + (int) shift.y);
			} catch (AWTException e1) {
			}
		}
		// pitch+=(e.getX()-width/2)*0.1;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		Mouse = new Point2D(e.getX(), e.getY());
		holding = true;

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		holding = false;

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		ispressed[e.getKeyCode()] = true;
		System.out.println("hi");
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			running = !running;
		}
		if (e.getKeyCode() == KeyEvent.VK_E) {
			mouselock = !mouselock;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		ispressed[e.getKeyCode()] = false;
	}

}
