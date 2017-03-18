package net.drewke.tdme.tools.leveleditor.views;

import java.io.File;

import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.tools.leveleditor.TDMELevelEditor;
import net.drewke.tdme.tools.shared.model.LevelEditorModel;
import net.drewke.tdme.tools.shared.model.LevelEditorModelLibrary;
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
	 * @see net.drewke.tdme.tools.shared.views.ModelViewerView#init(com.jogamp.opengl.GLAutoDrawable)
	 */
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.ModelViewerView#onSetModelData()
	 */
	public void onSetModelData() {
		TDMELevelEditor.getInstance().getLevelEditorModelLibraryScreenController().setModelLibrary();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.ModelViewerView#onLoadModel(net.drewke.tdme.tools.shared.model.LevelEditorModel, net.drewke.tdme.tools.shared.model.LevelEditorModel)
	 */
	public void onLoadModel(LevelEditorModel oldModel, LevelEditorModel model) {
		TDMELevelEditor.getInstance().getLevel().replaceModel(oldModel.getId(), model.getId());
		TDMELevelEditor.getInstance().getModelLibrary().removeModel(oldModel.getId());
		TDMELevelEditor.getInstance().getLevelEditorModelLibraryScreenController().setModelLibrary();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.ModelViewerView#onDisplayAdditionalScreens(com.jogamp.opengl.GLAutoDrawable)
	 */
	public void onDisplayAdditionalScreens(GLAutoDrawable drawable) {
		engine.getGUI().render(TDMELevelEditor.getInstance().getLevelEditorModelLibraryScreenController().getScreenNode().getId());
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.gui.events.GUIInputEventHandler#handleInputEvents()
	 */
	public void handleInputEvents() {
		// handle level editor model library screen controller events
		engine.getGUI().handleEvents(TDMELevelEditor.getInstance().getLevelEditorModelLibraryScreenController().getScreenNode().getId(), null, false);

		//
		super.handleInputEvents();
	}

	/*
	 * (non-Javadoc)
	 * @see net.drewke.tdme.tools.shared.views.ModelViewerView#loadModel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, net.drewke.tdme.math.Vector3)
	 */
	protected LevelEditorModel loadModel(String name, String description, String pathName, String fileName, Vector3 pivot) throws Exception {
		return TDMELevelEditor.getInstance().getModelLibrary().addModel(
			LevelEditorModelLibrary.ID_ALLOCATE, 
			name, 
			description, 
			pathName, 
			fileName, 
			pivot
		);
	}
}
