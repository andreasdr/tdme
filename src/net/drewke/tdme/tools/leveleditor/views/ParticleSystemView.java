package net.drewke.tdme.tools.leveleditor.views;

import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.shared.files.ModelMetaDataFileImport;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityLibrary;
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
	 * @see net.drewke.tdme.tools.shared.views.ParticleSystemView#onLoadParticleSystem(net.drewke.tdme.tools.shared.model.LevelEditorEntity, net.drewke.tdme.tools.shared.model.LevelEditorEntity)
	 */
	public void onLoadParticleSystem(LevelEditorEntity oldEntity, LevelEditorEntity newEntity) {
		TDMELevelEditor.getInstance().getLevel().replaceEntity(oldEntity.getId(), newEntity.getId());
		TDMELevelEditor.getInstance().getEntityLibrary().removeEntity(oldEntity.getId());
		TDMELevelEditor.getInstance().getLevelEditorEntityLibraryScreenController().setEntityLibrary();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.ParticleSystemView#loadParticleSystem(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	protected LevelEditorEntity loadParticleSystem(String name, String description, String pathName, String fileName) throws Exception {
		if (fileName.toLowerCase().endsWith(".tps")) {
			LevelEditorEntity levelEditorEntity = ModelMetaDataFileImport.doImport(
				LevelEditorEntityLibrary.ID_ALLOCATE, 
				pathName, 
				fileName
			);
			levelEditorEntity.setDefaultBoundingVolumes();
			TDMELevelEditor.getInstance().getEntityLibrary().addEntity(levelEditorEntity);
			return levelEditorEntity;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.ParticleSystemView#onInitAdditionalScreens()
	 */
	public void onInitAdditionalScreens() {
		engine.getGUI().addRenderScreen(TDMELevelEditor.getInstance().getLevelEditorEntityLibraryScreenController().getScreenNode().getId());
	}

}
