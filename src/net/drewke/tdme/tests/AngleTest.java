package net.drewke.tdme.tests;

import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.Console;

public class AngleTest {

	public AngleTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		Vector3 a = new Vector3(0f,0f,1f).normalize();
		Vector3 b = new Vector3(-1f,0f,+1f).normalize();
		Vector3 n = new Vector3(0f,1f,0f);
		Console.println(Vector3.computeAngle(a, b, n));
	}

}
