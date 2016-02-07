package net.drewke.tdme.test;

import net.drewke.tdme.engine.Object3DModel;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.engine.primitives.ConvexMesh;
import net.drewke.tdme.engine.primitives.PrimitiveModel;
import net.drewke.tdme.math.Vector3;

public class ConvexMeshContainsPointTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// create bounding box
		BoundingBox bb = new BoundingBox(
			new Vector3(-1.0f,-1.0f, -1.0f),
			new Vector3(1.0f,1.0f, 1.0f)
		);
		// p in bb
		Vector3 pInBb = new Vector3(0.0f,0.0f,0.0f);
		System.out.println(pInBb + " in " + bb + ":" + bb.containsPoint(pInBb));
		// p not in bb
		Vector3 pNotinBb = new Vector3(-2.0f,0.0f,0.0f);
		System.out.println(pNotinBb + " in " + bb + ":" + bb.containsPoint(pNotinBb));

		// create model from bb, create convex mesh
		Model bbModel = PrimitiveModel.createBoundingBoxModel(bb, "bb");
		Object3DModel bbObject3dModel = new Object3DModel(bbModel);
		ConvexMesh bbConvexMesh = new ConvexMesh(bbObject3dModel);
		// p in cm
		System.out.println(pInBb + " in " + bbConvexMesh + ":" + bbConvexMesh.containsPoint(pInBb));
		// p not in cm
		System.out.println(pNotinBb + " in " + bbConvexMesh + ":" + bbConvexMesh.containsPoint(pNotinBb));

		// closests point
		Vector3 closestsPoint = new Vector3();
		// p in cm
		bbConvexMesh.computeClosestPointOnBoundingVolume(pInBb, closestsPoint);
		System.out.println(pInBb + " cp " + bbConvexMesh + ":" + closestsPoint);
		// p not in cm
		bbConvexMesh.computeClosestPointOnBoundingVolume(pNotinBb, closestsPoint);
		System.out.println(pNotinBb + " cp " + bbConvexMesh + ":" + closestsPoint);
	}

}
