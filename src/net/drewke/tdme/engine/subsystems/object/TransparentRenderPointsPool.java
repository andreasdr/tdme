package net.drewke.tdme.engine.subsystems.object;

import java.util.ArrayList;

import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.utils.ArrayListIterator;
import net.drewke.tdme.utils.QuickSort;

/**
 * Transparent render points pool
 * @author andreas.drewke
 * @version $Id$
 */
public final class TransparentRenderPointsPool {

	private ArrayList<TransparentRenderPoint> transparentRenderPoints = null;
	private ArrayListIterator<TransparentRenderPoint> transparentRenderPointsIterator = null;
	private int poolIdx = 0;

	/**
	 * Default constructor
	 */
	public TransparentRenderPointsPool(int pointsMax) {
		transparentRenderPoints = new ArrayList<TransparentRenderPoint>();
		for (int i = 0; i < pointsMax; i++) {
			TransparentRenderPoint point = new TransparentRenderPoint();
			point.acquired = false;
			point.point = new Vector3();
			point.color = new Color4();
			transparentRenderPoints.add(point);
		}
		transparentRenderPointsIterator = new ArrayListIterator<TransparentRenderPoint>(transparentRenderPoints);
	}

	/**
	 * Creates an transparent render point entity in pool
	 * @param point
	 * @param color
	 */
	public void addPoint(Vector3 point, Color4 color, float distanceFromCamera) {
		// check for pool overflow
		if (poolIdx >= transparentRenderPoints.size()) {
			System.out.println("TransparentRenderPointsPool::createTransparentRenderPoint(): Too many transparent render points");
			return;
		}

		// create point in pool
		TransparentRenderPoint transparentRenderPoint = transparentRenderPoints.get(poolIdx++);
		transparentRenderPoint.acquired = true;
		transparentRenderPoint.point.set(point);
		transparentRenderPoint.color.set(color);
		transparentRenderPoint.distanceFromCamera = distanceFromCamera;
	}

	/**
	 * Merge another pool into this pool
	 * @param pool
	 */
	public void merge(TransparentRenderPointsPool pool2) {
		int p = 0;
		for (TransparentRenderPoint point: pool2.transparentRenderPointsIterator) {

			// skip if point is not in use
			if (point.acquired == false) break;

			// check for pool overflow
			if (poolIdx >= transparentRenderPoints.size()) {
				System.out.println("TransparentRenderPointsPool::merge(): Too many transparent render points");
				break;
			}

			// create point in pool
			TransparentRenderPoint transparentRenderPoint = transparentRenderPoints.get(poolIdx++);
			transparentRenderPoint.acquired = true;
			transparentRenderPoint.point.set(point.point);
			transparentRenderPoint.color.set(point.color);
			transparentRenderPoint.distanceFromCamera = point.distanceFromCamera;
			p++;
		}
	}

	/**
	 * Reset
	 */
	public void reset() {
		poolIdx = 0;
		for (TransparentRenderPoint point: transparentRenderPointsIterator) {
			point.acquired = false;
		}
	}

	/**
	 * @return transparent render points vector
	 */
	public ArrayList<TransparentRenderPoint> getTransparentRenderPoints() {
		return transparentRenderPoints;
	}

	/**
	 * @return transparent render points iterator
	 */
	public ArrayListIterator<TransparentRenderPoint> getTransparentRenderPointsIterator() {
		return transparentRenderPointsIterator;
	}

	/**
	 * Sort transparent render points
	 */
	public void sort() {
		QuickSort.sort(transparentRenderPoints);
	}

}
