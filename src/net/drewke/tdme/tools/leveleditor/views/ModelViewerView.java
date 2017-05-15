package net.drewke.tdme.tools.leveleditor.views;

import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.shared.model.LevelEditorEntity;
import net.drewke.tdme.tools.shared.model.LevelEditorEntityLibrary;
import net.drewke.tdme.tools.shared.views.PopUps;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * Model viewer view
 * @author Andreas Drewke
 * @version $Id$ 
 */
public class ModelViewerView extends net.drewke.tdme.tools.shared.views.ModelViewerView {


	/**
	 * Public constructor
	 * @param pop ups view
	 */
	public ModelViewerView(PopUps popUps) {
		super(popUps);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.ModelViewerView#onSetEntityData()
	 */
	public void onSetEntityData() {
		TDMELevelEditor.getInstance().getLevelEditorEntityLibraryScreenController().setEntityLibrary();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.ModelViewerView#onLoadModel(net.drewke.tdme.tools.shared.model.LevelEditorEntity, net.drewke.tdme.tools.shared.model.LevelEditorEntity)
	 */
	public void onLoadModel(LevelEditorEntity oldModel, LevelEditorEntity model) {
		TDMELevelEditor.getInstance().getLevel().replaceEntity(oldModel.getId(), model.getId());
		TDMELevelEditor.getInstance().getEntityLibrary().removeEntity(oldModel.getId());
		TDMELevelEditor.getInstance().getLevelEditorEntityLibraryScreenController().setEntityLibrary();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.ModelViewerView#onInitAdditionalScreens()
	 */
	public void onInitAdditionalScreens() {
		engine.getGUI().addRenderScreen(TDMELevelEditor.getInstance().getLevelEditorEntityLibraryScreenController().getScreenNode().getId());
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.ModelViewerView#loadModel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, net.drewke.tdme.math.Vector3)
	 */
	protected LevelEditorEntity loadModel(String name, String description, String pathName, String fileName, Vector3 pivot) throws Exception {
		return TDMELevelEditor.getInstance().getEntityLibrary().addModel(
			LevelEditorEntityLibrary.ID_ALLOCATE, 
			name, 
			description, 
			pathName, 
			fileName, 
			pivot
		);
	}
}
