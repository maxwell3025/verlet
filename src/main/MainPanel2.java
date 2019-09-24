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

import vectors.Matrix2D;
import vectors.Point2D;
import vectors.Point2D;

public class MainPanel2 extends JPanel implements Runnable, MouseMotionListener, MouseListener, KeyListener {
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
	ArrayList<Ball2D> particles = new ArrayList<Ball2D>();
	// ArrayList<Beam2D> beams = new ArrayList<Beam2D>();
	boolean holding = false;
	boolean running = false;
	Container container;
	boolean[] ispressed = new boolean[65536];

	public MainPanel2() {

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
		for(int x = 4;x<width-4;x+=16){
			for(int y = 4;y<height-4;y+=16){
				addParticle(x,y,4);
			}
		}
		particles.get(0).oldpos = new Point2D(-1,-1);
	}

	private void addParticle(double x, double y, double rad) {
		particles.add(new Ball2D(new Point2D(x, y), new Point2D(x, y), rad));
	}

	public void graphicsUpdate() {
		Arrays.fill(raster, -1);
		for (Ball2D p : particles) {
			int color = particles.indexOf(p) == 0 ? 0xffff0000 : 0xff0000ff;
			fillOval((int) (p.pos.x - p.rad), (int) (p.pos.y - p.rad), (int) (p.rad * 2), (int) (p.rad * 2), color);
		}

		buffer.setRGB(0, 0, width, height, raster, 0, width);
		repaint();

	}

	public void contentUpdate() {
		updatePhysics();
		for (int i = 0; i < 1; i++) {
			floorConstraints();
			ballConstraints();
		}
	}

	private void userInput() {
	}

	private void updatePhysics() {
		for (Particle2D p : particles) {
			p.update();
			p.pos.y += 0.01;
			//p.pos.x += (random() - 0.5) * 0.1;
			//p.pos.y += (random() - 0.5) * 0.1;
		}
	}

	private void floorConstraints() {
		for (Ball2D p : particles) {
			if (p.pos.x + p.rad > width) {
				double vel = (p.pos.x - p.oldpos.x);
				p.pos.x = width - p.rad;
				p.oldpos.x = width + vel - p.rad;
			}
			if (p.pos.x - p.rad < 0) {
				double vel = (p.pos.x - p.oldpos.x);
				p.pos.x = p.rad;
				p.oldpos.x = vel + p.rad;
			}
			if (p.pos.y + p.rad > height) {
				double vel = (p.pos.y - p.oldpos.y);
				p.pos.y = height - p.rad;
				p.oldpos.y = height + vel - p.rad;
			}
			if (p.pos.y - p.rad < 0) {
				double vel = (p.pos.y - p.oldpos.y);
				p.pos.y = p.rad;
				p.oldpos.y = vel + p.rad;
			}
		}
	}

	private void ballConstraints() {
		for (Ball2D a : particles) {
			for (Ball2D b : particles) {
				if (a != b) {
					Point2D dif = a.pos.add(b.pos.scale(-1));
					if (abs(dif.x) + abs(dif.y) < (a.rad + b.rad) * 2) {
						Point2D mid = a.pos.add(b.pos).scale(-0.5);
						Point2D adif = a.pos.add(mid);
						Point2D bdif = b.pos.add(mid);
						if (dif.dist() < a.rad + b.rad) {
							double desireddist = (a.rad + b.rad) * 0.5;
							adif = adif.scale(desireddist / adif.dist());
							bdif = bdif.scale(desireddist / bdif.dist());
							Point2D avel = a.pos.add(a.oldpos.scale(-1));
							Point2D bvel = b.pos.add(b.oldpos.scale(-1));
							a.pos = mid.scale(-1).add(adif);
							b.pos = mid.scale(-1).add(bdif);
							a.oldpos = mid.scale(-1).add(adif);
							b.oldpos = mid.scale(-1).add(bdif);
							Matrix2D collision = new Matrix2D(dif,new Point2D(-dif.y,dif.x));
							Matrix2D inverse = collision.inverse();
							Point2D achange = inverse.transform(avel);
							Point2D bchange = inverse.transform(bvel);
							b.oldpos = b.oldpos.add(collision.x.scale(-achange.x).add(collision.y.scale(-bchange.y)));
							a.oldpos = a.oldpos.add(collision.x.scale(-bchange.x).add(collision.y.scale(-achange.y)));
						}
					}
				}
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
