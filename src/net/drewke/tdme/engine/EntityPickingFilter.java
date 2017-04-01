package net.drewke.tdme.engine;

/**
 * Entity picking filter
 * @author Andreas Drewke
 * @version $Id$
 */
public interface EntityPickingFilter {

	/**
	 * Filter entity
	 * @param entity
	 * @return if allowed or not
	 */
	public boolean filterEntity(Entity entity);

}
