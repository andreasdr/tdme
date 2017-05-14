package net.drewke.tdme.tools.leveleditor.views;

import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.shared.views.PopUps;

/**
 * Particle System View
 * @author Andreas Drewke
 * @version $Id$
 */
public class ParticleSystemView extends net.drewke.tdme.tools.shared.views.ParticleSystemView {

	/**
	 * Public constructor
	 * @param pop ups
	 */
	public ParticleSystemView(PopUps popUps) {
		super(popUps);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.ParticleSystemView#onSetEntityData()
	 */
	public void onSetEntityData() {
		TDMELevelEditor.getInstance().getLevelEditorEntityLibraryScreenController().setEntityLibrary();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.ParticleSystemView#onInitAdditionalScreens()
	 */
	public void onInitAdditionalScreens() {
		engine.getGUI().addRenderScreen(TDMELevelEditor.getInstance().getLevelEditorEntityLibraryScreenController().getScreenNode().getId());
	}

}
