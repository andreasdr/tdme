package net.drewke.tdme.tests;

import net.drewke.tdme.math.Vector3;

public class AngleTest {

	public AngleTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		Vector3 a = new Vector3(0f,0f,1f).normalize();
		Vector3 b = new Vector3(-1f,0f,+1f).normalize();
		Vector3 n = new Vector3(0f,1f,0f);
		System.out.println(Vector3.computeAngle(a, b, n));
	}

}
