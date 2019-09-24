package main;

import static main.IntMath.abs;
import static main.IntMath.max;
import static main.IntMath.min;

import static java.lang.Math.*;

import java.awt.AWTException;
import java.awt.Color;
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

import vectors.Matrix2D;
import vectors.Point2D;
import vectors.Point3D;
import vectors.Point2D;

public class MainPanel3 extends JPanel implements Runnable, MouseMotionListener, MouseListener, KeyListener {
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
	ArrayList<Particle2D> particles = new ArrayList<Particle2D>();
	ArrayList<Beam2D> beams = new ArrayList<Beam2D>();
	boolean holding = false;
	boolean running = false;
	Container container;
	boolean[] ispressed = new boolean[65536];

	public MainPanel3() {

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
		for(int i = 0;i<128;i++){
			addParticle(i*4,0);
		}
		for(int i = 0;i<127;i++){
			addBeam(i,i+1);
		}
		particles.get(127).pin=true;
	}

	private void addParticle(double x, double y) {
		particles.add(new Particle2D(new Point2D(x, y), new Point2D(x, y)));
	}
	private void addBeam(int a,int b){
		beams.add(new Beam2D(particles.get(a),particles.get(b), particles.get(a).pos.add(particles.get(b).pos.scale(-1)).dist()));
	}

	public void graphicsUpdate() {
		Arrays.fill(raster, -1);
		for (Beam2D c : beams) {
			Point2D a = c.a.pos;
			Point2D b = c.b.pos;
			drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y, 0xff000000);
		}
		for (Particle2D p : particles) {
			float vel = (float)p.getVel().dist()/8;
			int color = Color.getHSBColor(vel, 1, 1).getRGB();
			fillOval((int) (p.pos.x - 2), (int) (p.pos.y - 2), 4, 4, color);
			Point2D pos = p.pos;
			Point2D oldpos = p.pos.add(p.getVel().scale(10));
			drawLine((int) pos.x, (int) pos.y, (int) oldpos.x, (int) oldpos.y, color);
		}
		buffer.setRGB(0, 0, width, height, raster, 0, width);
		repaint();

	}

	public void contentUpdate() {
		updatePhysics();
		for (int i = 0; i < 1; i++) {
			floorConstraints();
			beamConstraints();
		}
	}
	private void beamConstraints() {
		for (int i = 0; i < beams.size(); i++) {
			Beam2D c = beams.get(i);
			Point2D a = c.a.pos;
			Point2D b = c.b.pos;
			double len = c.length;
			Point2D mid = a.add(b).scale(0.5);
			Point2D adist = a.add(mid.scale(-1));
			Point2D bdist = b.add(mid.scale(-1));
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
	private void userInput() {
	}

	private void updatePhysics() {
		for (Particle2D p : particles) {
			p.update();
			p.pos.y += 0.0001;
		}
	}

	private void floorConstraints() {
		for (Particle2D p : particles) {
			if (p.pos.x > width) {
				double vel = (p.pos.x - p.oldpos.x);
				p.pos.x = width;
				p.oldpos.x = width + vel;
			}
			if (p.pos.x < 0) {
				double vel = (p.pos.x - p.oldpos.x);
				p.pos.x = 0;
				p.oldpos.x = vel;
			}
			if (p.pos.y > height) {
				double vel = (p.pos.y - p.oldpos.y);
				p.pos.y = height;
				p.oldpos.y = height + vel;
			}
			if (p.pos.y < 0) {
				double vel = (p.pos.y - p.oldpos.y);
				p.pos.y =0;
				p.oldpos.y = vel;
			}
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
				if ((px) * (px) + (py) * (py) < 0.25) {
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
	}

	@Override
	public void keyReleased(KeyEvent e) {
		ispressed[e.getKeyCode()] = false;
	}

}
