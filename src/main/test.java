package main;

public class test {

	public static void main(String[] args) {
		var a = new var(3);
		var2 b = new var2(a);
		b.val.val=100;
		System.out.println(a.val);
	}

	public static class var {
		public double val;
		public var(double value) {
			val = value;
		}
	}
	public static class var2 {
		public var val;
		public var2(var value) {
			val = value;
		}
	}
}
