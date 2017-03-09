package net.drewke.tdme.engine.fileio.models;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.drewke.tdme.engine.ModelUtilities;
import net.drewke.tdme.engine.Rotation;
import net.drewke.tdme.engine.Transformations;
import net.drewke.tdme.engine.model.Animation;
import net.drewke.tdme.engine.model.Color4;
import net.drewke.tdme.engine.model.Face;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Group;
import net.drewke.tdme.engine.model.Joint;
import net.drewke.tdme.engine.model.JointWeight;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.model.Model.UpVector;
import net.drewke.tdme.engine.model.ModelHelper;
import net.drewke.tdme.engine.model.RotationOrder;
import net.drewke.tdme.engine.model.Skinning;
import net.drewke.tdme.engine.model.TextureCoordinate;
import net.drewke.tdme.engine.subsystems.object.ModelUtilitiesInternal.ModelStatistics;
import net.drewke.tdme.math.MathTools;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.tools.shared.files.LevelFileExport;
import net.drewke.tdme.tools.shared.model.LevelEditorLevel;
import net.drewke.tdme.tools.shared.model.LevelEditorModel;
import net.drewke.tdme.tools.shared.model.LevelEditorObject;
import net.drewke.tdme.tools.shared.model.PropertyModelClass;
import net.drewke.tdme.utils.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Collada DAE reader
 * @author Andreas Drewke
 * @version $Id$
 */
public final class DAEReader {

	private static Color4 BLENDER_AMBIENT_NONE = new Color4(0f,0f,0f,1f);
	private static float BLENDER_AMBIENT_FROM_DIFFUSE_SCALE = 0.7f;
	private static float BLENDER_DIFFUSE_SCALE = 0.8f;

	/**
	 * Authoring Tool
	 * @author Andreas Drewke
	 * @version $Id$
	 */
	private enum AuthoringTool {
		UNKNOWN,
		BLENDER
	}

	/**
	 * Reads Collada DAE file
	 * @param path name
	 * @param file name
	 * @throws Exception
	 * @return Model instance
	 */
	public static Model read(String pathName, String fileName) throws Exception {
		// load dae xml document
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(FileSystem.getInstance().getInputStream(pathName, fileName));
		Element xmlRoot = document.getDocumentElement();

		// authoring tool
		AuthoringTool authoringTool = getAuthoringTool(xmlRoot);

		// up vector and rotation order
		UpVector upVector = getUpVector(xmlRoot);
		RotationOrder rotationOrder = null;
		switch(upVector) {
			case Y_UP:
				rotationOrder = RotationOrder.ZYX;
			case Z_UP:
				rotationOrder = RotationOrder.YZX;
		}

		// 	create model
		Model model = new Model(pathName + File.separator + fileName, fileName, upVector, rotationOrder);

		// import matrix
		setupModelImportRotationMatrix(xmlRoot, model);
		setupModelImportScaleMatrix(xmlRoot, model);

		// parse scene from xml
		String xmlSceneId = null;
		Element xmlScene = getChildrenByTagName(xmlRoot, "scene").get(0);
		for(Element xmlInstanceVisualscene: getChildrenByTagName(xmlScene, "instance_visual_scene")) {
			xmlSceneId = xmlInstanceVisualscene.getAttribute("url").substring(1);
		}

		// check for xml scene id
		if (xmlSceneId == null) {
			throw new ModelFileIOException("No scene id found");
		}

		// parse visual scenes
		Element xmlLibraryVisualScenes = getChildrenByTagName(xmlRoot, "library_visual_scenes").get(0);
		for(Element xmlLibraryVisualScene: getChildrenByTagName(xmlLibraryVisualScenes, "visual_scene")) {
			String xmlVisualSceneId = xmlLibraryVisualScene.getAttribute("id");
			if (xmlVisualSceneId.equals(xmlSceneId)) {
				// default FPS
				float fps = 30f;

				// parse frames per second
				List<Element> xmlExtraNodes = getChildrenByTagName(xmlLibraryVisualScene, "extra");
				if (xmlExtraNodes.isEmpty() == false) {
					Element xmlExtraNode = xmlExtraNodes.get(0);
					for (Element xmlTechnique: getChildrenByTagName(xmlExtraNode, "technique")) {
						List<Element> xmlFrameRateNodes = getChildrenByTagName(xmlTechnique, "frame_rate");
						if (xmlFrameRateNodes.isEmpty() == false) {
							fps = Float.parseFloat(xmlFrameRateNodes.get(0).getTextContent());
							break;
						}
					}
				}

				// set up frames per seconds
				model.setFPS(fps);

				// visual scene root nodes
				for(Element xmlNode: getChildrenByTagName(xmlLibraryVisualScene, "node")) {
					Group group = readVisualSceneNode(authoringTool, pathName, model, xmlRoot, xmlNode, fps);
					if (group != null) {
						model.getSubGroups().put(group.getId(), group);
						model.getGroups().put(group.getId(), group);
					}
				}
			}
		}

		// set up joints
		ModelHelper.setupJoints(model);

		// fix animation length
		ModelHelper.fixAnimationLength(model);

		// prepare for indexed rendering
		ModelHelper.prepareForIndexedRendering(model);

		//
		return model;
	}

	/**
	 * Reads Collada DAE file level
	 * @param path name
	 * @param file name
	 * @throws Exception
	 * @return Model instance
	 */
	public static LevelEditorLevel readLevel(String pathName, String fileName) throws Exception {
		// (re)create tm files folder
		File tmFilesFolder = new File(pathName + "/" + fileName + "-models");
		if (tmFilesFolder.exists()) {
			tmFilesFolder.delete();
		}
		tmFilesFolder.mkdir();

		//
		LevelEditorLevel levelEditorLevel = new LevelEditorLevel();

		// load dae xml document
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(FileSystem.getInstance().getInputStream(pathName, fileName));
		Element xmlRoot = document.getDocumentElement();

		// authoring tool
		AuthoringTool authoringTool = getAuthoringTool(xmlRoot);

		// up vector and rotation order
		UpVector upVector = getUpVector(xmlRoot);
		RotationOrder rotationOrder = null;
		switch(upVector) {
			case Y_UP:
				rotationOrder = RotationOrder.ZYX;
			case Z_UP:
				rotationOrder = RotationOrder.YZX;
		}
		levelEditorLevel.setRotationOrder(rotationOrder);

		// parse scene from xml
		String xmlSceneId = null;
		Element xmlScene = getChildrenByTagName(xmlRoot, "scene").get(0);
		for(Element xmlInstanceVisualscene: getChildrenByTagName(xmlScene, "instance_visual_scene")) {
			xmlSceneId = xmlInstanceVisualscene.getAttribute("url").substring(1);
		}

		// check for xml scene id
		if (xmlSceneId == null) {
			throw new ModelFileIOException("No scene id found");
		}

		// parse visual scenes
		Element xmlLibraryVisualScenes = getChildrenByTagName(xmlRoot, "library_visual_scenes").get(0);
		for(Element xmlLibraryVisualScene: getChildrenByTagName(xmlLibraryVisualScenes, "visual_scene")) {
			String xmlVisualSceneId = xmlLibraryVisualScene.getAttribute("id");
			if (xmlVisualSceneId.equals(xmlSceneId)) {
				// default FPS
				float fps = 30f;

				// parse frames per second
				List<Element> xmlExtraNodes = getChildrenByTagName(xmlLibraryVisualScene, "extra");
				if (xmlExtraNodes.isEmpty() == false) {
					Element xmlExtraNode = xmlExtraNodes.get(0);
					for (Element xmlTechnique: getChildrenByTagName(xmlExtraNode, "technique")) {
						List<Element> xmlFrameRateNodes = getChildrenByTagName(xmlTechnique, "frame_rate");
						if (xmlFrameRateNodes.isEmpty() == false) {
							fps = Float.parseFloat(xmlFrameRateNodes.get(0).getTextContent());
							break;
						}
					}
				}

				// visual scene root nodes
				int nodeIdx = 0;
				for(Element xmlNode: getChildrenByTagName(xmlLibraryVisualScene, "node")) {
					// 	create model
					Model model = new Model(
						pathName + File.separator + fileName + '-' + xmlNode.getAttribute("id"), 
						fileName + '-' + xmlNode.getAttribute("id"),
						upVector,
						rotationOrder
					);

					// import matrix
					setupModelImportRotationMatrix(xmlRoot, model);
					Matrix4x4 modelImportRotationMatrix = new Matrix4x4(model.getImportTransformationsMatrix());
					setupModelImportScaleMatrix(xmlRoot, model);

					// translation, scaling, rotation
					Vector3 translation = new Vector3();
					Vector3 scale = new Vector3();
					Vector3 rotation = new Vector3();

					// set up local transformations matrix
					Matrix4x4 nodeTransformationsMatrix = null;
					List<Element> xmlMatrixElements = getChildrenByTagName(xmlNode, "matrix");
					if (xmlMatrixElements.size() == 1) {
						String xmlMatrix = xmlMatrixElements.get(0).getTextContent();
						StringTokenizer t = new StringTokenizer(xmlMatrix, " \n\r");

						// 
						nodeTransformationsMatrix = new Matrix4x4(
							Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
							Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
							Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
							Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
							Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
							Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
							Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
							Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken())
						).transpose();
					}

					// check if we have node transformations matrix
					if (nodeTransformationsMatrix == null) {
						throw new ModelFileIOException("missing node transformations matrix for node " + xmlNode.getAttribute("id"));
					}

					// extract coordinate axes
					Vector3 xAxis = new Vector3(
						nodeTransformationsMatrix.getArray()[0],
						nodeTransformationsMatrix.getArray()[1],
						nodeTransformationsMatrix.getArray()[2]
					);
					Vector3 yAxis = new Vector3(
						nodeTransformationsMatrix.getArray()[4],
						nodeTransformationsMatrix.getArray()[5],
						nodeTransformationsMatrix.getArray()[6]
					);
					Vector3 zAxis = new Vector3(
						nodeTransformationsMatrix.getArray()[8],
						nodeTransformationsMatrix.getArray()[9],
						nodeTransformationsMatrix.getArray()[10]
					);

					// translation
					translation.set(
						nodeTransformationsMatrix.getArray()[12],
						nodeTransformationsMatrix.getArray()[13],
						nodeTransformationsMatrix.getArray()[14]
					);

					// scale
					scale.set(
						xAxis.computeLength(),
						yAxis.computeLength(),
						zAxis.computeLength()
					);

					// normalize coordinate axes
					xAxis.normalize();
					yAxis.normalize();
					zAxis.normalize();

					// write back normalized x axis
					nodeTransformationsMatrix.getArray()[0] = xAxis.getX();
					nodeTransformationsMatrix.getArray()[1] = xAxis.getY();
					nodeTransformationsMatrix.getArray()[2] = xAxis.getZ();

					// write back normalized y axis
					nodeTransformationsMatrix.getArray()[4] = yAxis.getX();
					nodeTransformationsMatrix.getArray()[5] = yAxis.getY();
					nodeTransformationsMatrix.getArray()[6] = yAxis.getZ();

					// write back normalized z axis
					nodeTransformationsMatrix.getArray()[8] = zAxis.getX();
					nodeTransformationsMatrix.getArray()[9] = zAxis.getY();
					nodeTransformationsMatrix.getArray()[10] = zAxis.getZ();

					// check if negative scale and rotation
					if ((upVector == UpVector.Y_UP && Vector3.computeDotProduct(Vector3.computeCrossProduct(xAxis, yAxis), zAxis) < 0.0f) ||
						(upVector == UpVector.Z_UP && Vector3.computeDotProduct(Vector3.computeCrossProduct(xAxis, zAxis), yAxis) < 0.0f)) {
						// x axis
						nodeTransformationsMatrix.getArray()[0]*= -1f;
						nodeTransformationsMatrix.getArray()[1]*= -1f;
						nodeTransformationsMatrix.getArray()[2]*= -1f;

						// y axis
						nodeTransformationsMatrix.getArray()[4]*= -1f;
						nodeTransformationsMatrix.getArray()[5]*= -1f;
						nodeTransformationsMatrix.getArray()[6]*= -1f;

						// z axis
						nodeTransformationsMatrix.getArray()[8]*= -1f;
						nodeTransformationsMatrix.getArray()[9]*= -1f;
						nodeTransformationsMatrix.getArray()[10]*= -1f;

						// scale
						scale.scale(-1f);
					}

					// determine rotation
					computeEulerAngles(nodeTransformationsMatrix, rotation, 0, 1, 2, false);

					// apply model import matrix
					modelImportRotationMatrix.multiply(scale, scale);
					modelImportRotationMatrix.multiply(rotation, rotation);
					model.getImportTransformationsMatrix().multiply(translation, translation);

					// set up frames per seconds
					model.setFPS(fps);

					// read sub groups
					Group group = readVisualSceneNode(authoringTool, pathName, model, xmlRoot, xmlNode, fps);
					if (group != null) {
						group.getTransformationsMatrix().identity();
						model.getSubGroups().put(group.getId(), group);
						model.getGroups().put(group.getId(), group);
					}

					// set up joints
					ModelHelper.setupJoints(model);

					// fix animation length
					ModelHelper.fixAnimationLength(model);

					// prepare for indexed rendering
					ModelHelper.prepareForIndexedRendering(model);

					// check if empty model
					ModelStatistics modelStatistics = ModelUtilities.computeModelStatistics(model);
					if (modelStatistics.getOpaqueFaceCount() == 0 && modelStatistics.getTransparentFaceCount() == 0) {
						System.out.println("DAEReader::readLevel(): Skipping model '" + model.getName() + "' as is has no faces.");
						continue;
					}

					// level editor model
					LevelEditorModel levelEditorModel = null;

					// check if we have that model already
					for (int i = 0; i < levelEditorLevel.getModelLibrary().getModelCount(); i++) {
						LevelEditorModel levelEditorModelLibrary = levelEditorLevel.getModelLibrary().getModelAt(i);
						if (ModelUtilities.equals(model, levelEditorModelLibrary.getModel()) == true) {
							levelEditorModel = levelEditorModelLibrary;
							break;
						}
					}

					// create level editor model, if not yet exists
					if (levelEditorModel == null) {
						// save model
						TMWriter.write(model, pathName + "/" + fileName + "-models", xmlNode.getAttribute("id") + ".tm");

						// create level editor model
						levelEditorModel = levelEditorLevel.getModelLibrary().addModel(
							nodeIdx++,
							xmlNode.getAttribute("id"),
							xmlNode.getAttribute("id"),
							pathName + "/" + fileName + "-models",
							xmlNode.getAttribute("id") + ".tm",
							new Vector3()
						);
					}

					// level editor object transformations
					Transformations levelEditorObjectTransformations = new Transformations();
					levelEditorObjectTransformations.getTranslation().set(translation);
					levelEditorObjectTransformations.getRotations().add(new Rotation(rotation.getArray()[rotationOrder.getAxis0VectorIndex()], rotationOrder.getAxis0()));
					levelEditorObjectTransformations.getRotations().add(new Rotation(rotation.getArray()[rotationOrder.getAxis1VectorIndex()], rotationOrder.getAxis1()));
					levelEditorObjectTransformations.getRotations().add(new Rotation(rotation.getArray()[rotationOrder.getAxis2VectorIndex()], rotationOrder.getAxis2()));
					levelEditorObjectTransformations.getScale().set(scale);
					levelEditorObjectTransformations.update();

					// level editor object
					LevelEditorObject object = new LevelEditorObject(
						xmlNode.getAttribute("id"),
						xmlNode.getAttribute("id"),
						levelEditorObjectTransformations,
						levelEditorModel
					);

					// add object to level
					levelEditorLevel.addObject(object);
				}
			}
		}

		// save level
		LevelFileExport.export(pathName + "/" + fileName + ".tl", levelEditorLevel);

		//
		return levelEditorLevel;
	}

	/**
	 * Get authoring tool
	 * @param xml root
	 * @return authoring tool
	 */
	private static AuthoringTool getAuthoringTool(Element xmlRoot) {
		// determine up axis
		for(Element xmlAsset: getChildrenByTagName(xmlRoot, "asset")) {
			for(Element xmlContributer: getChildrenByTagName(xmlAsset, "contributor")) {
				for(Element xmlAuthoringTool: getChildrenByTagName(xmlContributer, "authoring_tool")) {
					if (xmlAuthoringTool.getTextContent().indexOf("Blender") != -1) {
						return AuthoringTool.BLENDER;
					}
				}
			}
		}
		return AuthoringTool.UNKNOWN;
	}

	/**
	 * Get Up vector
	 * @param xml root
	 * @return up vector
	 * @throws ModelFileIOException
	 */
	private static UpVector getUpVector(Element xmlRoot) throws ModelFileIOException {
		// determine up axis
		for(Element xmlAsset: getChildrenByTagName(xmlRoot, "asset")) {
			for(Element xmlAssetUpAxis: getChildrenByTagName(xmlAsset, "up_axis")) {
				String upAxis = xmlAssetUpAxis.getTextContent();
				if (upAxis.equalsIgnoreCase("Y_UP")) {
					return UpVector.Y_UP;
				} else 
				if (upAxis.equalsIgnoreCase("Z_UP")) {
					return UpVector.Z_UP;
				} else
				if (upAxis.equalsIgnoreCase("X_UP")) {
					throw new ModelFileIOException("X-Up is not supported");
				} else {
					throw new ModelFileIOException("Unknown Up vector");
				}
			}
		}
		throw new ModelFileIOException("Unknown Up vector");
	}

	/**
	 * Set up model import rotation matrix
	 * @param xml root
	 * @param model
	 */
	private static void setupModelImportRotationMatrix(Element xmlRoot, Model model) {
		// determine rotation matrix
		for(Element xmlAsset: getChildrenByTagName(xmlRoot, "asset")) {
			for(Element xmlAssetUpAxis: getChildrenByTagName(xmlAsset, "up_axis")) {
				String upAxis = xmlAssetUpAxis.getTextContent();
				if (upAxis.equalsIgnoreCase("Y_UP")) {
					// default: do nothing
				} else 
				if (upAxis.equalsIgnoreCase("Z_UP")) {
					// set up import transformation matrix
					model.getImportTransformationsMatrix().rotate(-90f, new Vector3(1f,0f,0f));
				} else
				if (upAxis.equalsIgnoreCase("X_UP")) {
					// set up import transformation matrix
					model.getImportTransformationsMatrix().rotate(-90f, new Vector3(0f,1f,0f));
				} else {
					System.out.println("Warning: Unknown up axis: " + upAxis);
				}
			}
		}
	}

	/**
	 * Set up model import scale matrix
	 * @param xml root
	 * @param model
	 */
	private static void setupModelImportScaleMatrix(Element xmlRoot, Model model) {
		// determine sclae
		for(Element xmlAsset: getChildrenByTagName(xmlRoot, "asset")) {
			for(Element xmlAssetUnit: getChildrenByTagName(xmlAsset, "unit")) {
				String tmp = null;
				if ((tmp = xmlAssetUnit.getAttribute("meter")) != null) {
					model.getImportTransformationsMatrix().scale(Float.parseFloat(tmp));
				}
			}
		}
	}

	/**
	 * Compute Euler angles (rotation around x, y, z axes)
	 * @see https://github.com/erich666/GraphicsGems/tree/master/gemsiv/euler_angle
	 * 
	 * 		This code repository predates the concept of Open Source, and predates most licenses along such lines. 
	 * 		As such, the official license truly is:
	 * 
	 * 		EULA: The Graphics Gems code is copyright-protected. 
	 * 		In other words, you cannot claim the text of the code as your own and resell it. 
	 * 		Using the code is permitted in any program, product, or library, non-commercial or commercial. 
	 * 		Giving credit is not required, though is a nice gesture. 
	 * 		The code comes as-is, and if there are any flaws or problems with any Gems code, 
	 * 		nobody involved with Gems - authors, editors, publishers, or webmasters - are to be held responsible. 
	 * 		Basically, don't be a jerk, and remember that anything free comes with no guarantee.
	 * 
	 * @param matrix
	 * @param euler
	 * @param axis 0
	 * @param axis 1
	 * @param axis 2
	 * @param axis invert
	 */
	public static void computeEulerAngles(Matrix4x4 matrix, Vector3 euler, int axis0, int axis1, int axis2, boolean invert) {
		float[] data = matrix.getArray();
		float[] eulerXYZ = euler.getArray();

		// compute euler angles in radians
		float cy = (float)Math.sqrt(data[axis0 + 4 * axis0] * data[axis0 + 4 * axis0] + data[axis1 + 4 * axis0] * data[axis1 + 4 * axis0]);
		if (cy > 16f * MathTools.EPSILON) {
			eulerXYZ[0] = (float)(Math.atan2(data[axis2 + 4 * axis1], data[axis2 + 4 * axis2]));
			eulerXYZ[1] = (float)(Math.atan2(-data[axis2 + 4 * axis0], cy));
			eulerXYZ[2] = (float)(Math.atan2(data[axis1 + 4 * axis0], data[axis0 + 4 * axis0]));
		} else {
			eulerXYZ[0] = (float)(Math.atan2(-data[axis1 + 4 * axis2], data[axis1 + 4 * axis1]));
			eulerXYZ[1] = (float)(Math.atan2(-data[axis2 + 4 * axis0], cy));
			eulerXYZ[2] = 0f;
		}

		// invert
		if (invert == true) {
			euler.scale(-1f);
		}

	    // convert to degrees
	    euler.scale((float)(180d / Math.PI));
	}

	/**
	 * Read a DAE visual scene node
	 * @param authoring tool
	 * @param path name
	 * @param model
	 * @param xml node
	 * @param xml root
	 * @param frames per second
	 * @throws Exception
	 * @return group
	 */
	private static Group readVisualSceneNode(AuthoringTool authoringTool, String pathName, Model model, Element xmlRoot, Element xmlNode, float fps) throws Exception {
		List<Element> xmlInstanceControllers = getChildrenByTagName(xmlNode, "instance_controller");
		if (xmlInstanceControllers.isEmpty() == false) {
			return readVisualSceneInstanceController(authoringTool, pathName, model, xmlRoot, xmlNode);
		} else {
			return readNode(authoringTool, pathName, model, xmlRoot, xmlNode, fps);
		}
	}

	/**
	 * Reads a DAE visual scene group node
	 * @param authoring tool
	 * @param path name
	 * @param model
	 * @param xml node
	 * @param xml root
	 * @param frames per seconds
	 * @throws Exception
	 * @return group
	 */
	private static Group readNode(AuthoringTool authoringTool, String pathName, Model model, Element xmlRoot, Element xmlNode, float fps) throws Exception {
		String xmlNodeId = xmlNode.getAttribute("id");
		String xmlNodeName = xmlNode.getAttribute("name");
		if (xmlNodeId.length() == 0) xmlNodeId = xmlNodeName;

		StringTokenizer t = null;

		// default node matrix
		Matrix4x4 transformationsMatrix = null;

		// set up local transformations matrix
		List<Element> xmlMatrixElements = getChildrenByTagName(xmlNode, "matrix");
		if (xmlMatrixElements.size() == 1) {
			String xmlMatrix = getChildrenByTagName(xmlNode, "matrix").get(0).getTextContent();
			t = new StringTokenizer(xmlMatrix, " \n\r");

			// 
			transformationsMatrix = new Matrix4x4(
				Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
				Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
				Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
				Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
				Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
				Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
				Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
				Float.parseFloat(t.nextToken()),  Float.parseFloat(t.nextToken())
			).transpose();
		}

		// tdme model definitions
		Group group = new Group(
			model,
			xmlNodeId,
			xmlNodeName
		);

		//
		if (transformationsMatrix != null) {
			group.getTransformationsMatrix().multiply(transformationsMatrix);
		}

		// parse animations
		List<Element> xmlAnimationsLibrary = getChildrenByTagName(xmlRoot, "library_animations");
		if (xmlAnimationsLibrary.isEmpty() == false) {
			List<Element> xmlAnimations = getChildrenByTagName(xmlAnimationsLibrary.get(0), "animation");
			for (Element xmlAnimation: xmlAnimations) {
				// older DAE has animation/animation xml nodes
				List<Element> _xmlAnimation = getChildrenByTagName(xmlAnimation, "animation");
				if (_xmlAnimation.isEmpty() == false) {
					xmlAnimation = _xmlAnimation.get(0);
				}

				// find sampler source
				String xmlSamplerSource = null;
				Element xmlChannel = getChildrenByTagName(xmlAnimation, "channel").get(0);
				if (xmlChannel.getAttribute("target").startsWith(xmlNodeId + "/")) {
					xmlSamplerSource = xmlChannel.getAttribute("source").substring(1);
				}

				// check for sampler source
				if (xmlSamplerSource == null) {
					continue;
				}

				// parse animation output matrices
				String xmlSamplerOutputSource = null;
				String xmlSamplerInputSource = null;
				Element xmlSampler = getChildrenByTagName(xmlAnimation, "sampler").get(0);
				for (Element xmlSamplerInput: getChildrenByTagName(xmlSampler, "input")) {
					if (xmlSamplerInput.getAttribute("semantic").equals("OUTPUT")) {
						xmlSamplerOutputSource = xmlSamplerInput.getAttribute("source").substring(1);
					} else
					if (xmlSamplerInput.getAttribute("semantic").equals("INPUT")) {
						xmlSamplerInputSource = xmlSamplerInput.getAttribute("source").substring(1);
					}

				}

				// check for sampler source
				if (xmlSamplerOutputSource == null) {
					throw new ModelFileIOException("Could not fid xml sampler output source for animation for " + xmlNodeId);
				}

				// load animation input matrices
				// TODO: check accessor "time"
				float keyFrameTimes[] = null;
				for(Element xmlAnimationSource: getChildrenByTagName(xmlAnimation, "source")) {
					if (xmlAnimationSource.getAttribute("id").equals(xmlSamplerInputSource)) {
						Element xmlFloatArray = getChildrenByTagName(xmlAnimationSource, "float_array").get(0);
						int frames = Integer.parseInt(xmlFloatArray.getAttribute("count"));
						String valueString = xmlFloatArray.getTextContent();
						int keyFrameIdx = 0;
						keyFrameTimes = new float[frames];
						t = new StringTokenizer(valueString, " \n\r");
						while (t.hasMoreTokens()) {
							keyFrameTimes[keyFrameIdx++] = Float.parseFloat(t.nextToken());
						}
					}
				}

				// load animation output matrices
				// TODO: check accessor "transform"
				Matrix4x4[] keyFrameMatrices = null; 
				for(Element xmlAnimationSource: getChildrenByTagName(xmlAnimation, "source")) {
					if (xmlAnimationSource.getAttribute("id").equals(xmlSamplerOutputSource)) {
						Element xmlFloatArray = getChildrenByTagName(xmlAnimationSource, "float_array").get(0);
						int keyFrames = Integer.parseInt(xmlFloatArray.getAttribute("count")) / 16 - 1;
						// some models have animations without frames
						if (keyFrames > 0) {
							String valueString = xmlFloatArray.getTextContent();
							t = new StringTokenizer(valueString, " \n\r");
		
							// first frame is not a animation matrix
							Matrix4x4 keyFrame0Matrix = new Matrix4x4(
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken())
							).transpose().invert();
		
							// parse key frame
							int keyFrameIdx = 0;
							keyFrameMatrices = new Matrix4x4[keyFrames];
							while (t.hasMoreTokens()) {
								// set animation transformation matrix at frame
								keyFrameMatrices[keyFrameIdx] = new Matrix4x4(
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken())
								).transpose().multiply(keyFrame0Matrix);
								keyFrameIdx++;
							}
						}
					}
				}

				// create linear animation by key frame times and key frames
				if (keyFrameTimes != null && keyFrameMatrices != null) {
					int frames = (int)Math.ceil(keyFrameTimes[keyFrameTimes.length - 1] * fps) + 1;

					// create default animation
					ModelHelper.createDefaultAnimation(model, frames);

					//
					Animation animation = group.createAnimation(frames);
					Matrix4x4[] transformationsMatrices = animation.getTransformationsMatrices();
					Matrix4x4 tansformationsMatrixLast = keyFrameMatrices[0];
					int keyFrameIdx = 0;
					int frameIdx = 0;
					float timeStampLast = 0.0f;
					for (float keyFrameTime: keyFrameTimes) {
						Matrix4x4 transformationsMatrixCurrent = keyFrameMatrices[(keyFrameIdx) % keyFrameMatrices.length];
						float timeStamp;
						for (timeStamp = timeStampLast; timeStamp < keyFrameTime; timeStamp+= 1.0f / fps) {
							if (frameIdx >= frames) {
								System.out.println("Warning: skipping frame: " + frameIdx);
								frameIdx++;
								continue;
							}
							Matrix4x4.interpolateLinear(
								tansformationsMatrixLast,
								transformationsMatrixCurrent,
								(timeStamp - timeStampLast) / (keyFrameTime - timeStampLast),
								transformationsMatrices[frameIdx]
							);
							frameIdx++;
						}
						timeStampLast = timeStamp;
						tansformationsMatrixLast = transformationsMatrixCurrent; 
						keyFrameIdx++;
					}
				}
			}
		}

		// parse sub groups
		for(Element _xmlNode: getChildrenByTagName(xmlNode, "node")) {
			Group _group = readVisualSceneNode(authoringTool, pathName, model, xmlRoot, _xmlNode, fps);
			if (_group != null) {
				group.getSubGroups().put(_group.getId(), _group);
				model.getGroups().put(_group.getId(), _group);
			}
		}

		// check for geometry data
		String xmlInstanceGeometryId = null;
		List<Element> xmlInstanceGeometryElements = getChildrenByTagName(xmlNode, "instance_geometry");
		if (xmlInstanceGeometryElements.isEmpty() == false) {
			Element xmlInstanceGeometryElement = xmlInstanceGeometryElements.get(0);

			// fetch instance geometry url
			xmlInstanceGeometryId = xmlInstanceGeometryElement.getAttribute("url").substring(1); 

			// determine bound materials
			HashMap<String, String> materialSymbols = new HashMap<String, String>(); 
			for (Element xmlBindMaterial: getChildrenByTagName(xmlInstanceGeometryElement, "bind_material"))
			for (Element xmlTechniqueCommon: getChildrenByTagName(xmlBindMaterial, "technique_common"))
			for (Element xmlInstanceMaterial: getChildrenByTagName(xmlTechniqueCommon, "instance_material")) {
				materialSymbols.put(
					xmlInstanceMaterial.getAttribute("symbol"),
					xmlInstanceMaterial.getAttribute("target")
				);
			}

			// parse geometry
			readGeometry(authoringTool, pathName, model, group, xmlRoot, xmlInstanceGeometryId, materialSymbols);

			//
			return group;
		}

		// otherwise check for "instance_node"
		String xmlInstanceNodeId = null;
		for (Element xmlInstanceNodeElement: getChildrenByTagName(xmlNode, "instance_node")) {
			xmlInstanceNodeId = xmlInstanceNodeElement.getAttribute("url").substring(1); 
		}

		// do we have a instance node id?
		if (xmlInstanceNodeId != null) {
			for (Element xmlLibraryNodes: getChildrenByTagName(xmlRoot, "library_nodes"))
			for (Element xmlLibraryNode: getChildrenByTagName(xmlLibraryNodes, "node"))
			if (xmlLibraryNode.getAttribute("id").equals(xmlInstanceNodeId)) {
				// parse sub groups
				for(Element _xmlNode: getChildrenByTagName(xmlLibraryNode, "node")) {
					Group _group = readVisualSceneNode(authoringTool, pathName, model, xmlRoot, _xmlNode, fps);
					if (_group != null) {
						group.getSubGroups().put(_group.getId(), _group);
						model.getGroups().put(_group.getId(), _group);
					}
				}

				// parse geometry 
				for (Element xmlInstanceGeometry: getChildrenByTagName(xmlLibraryNode, "instance_geometry")) {
					String xmlGeometryId = xmlInstanceGeometry.getAttribute("url").substring(1);
					// parse material symbols
					HashMap<String, String> materialSymbols = new HashMap<String, String>(); 
					for (Element xmlBindMaterial: getChildrenByTagName(xmlInstanceGeometry, "bind_material"))
					for (Element xmlTechniqueCommon: getChildrenByTagName(xmlBindMaterial, "technique_common"))
					for (Element xmlInstanceMaterial: getChildrenByTagName(xmlTechniqueCommon, "instance_material")) {
						materialSymbols.put(
							xmlInstanceMaterial.getAttribute("symbol"),
							xmlInstanceMaterial.getAttribute("target")
						);
					}

					// parse geometry
					readGeometry(authoringTool, pathName, model, group, xmlRoot, xmlGeometryId, materialSymbols);
				}
			}
		}

		//
		return group;
	}

	/**
	 * Reads a instance controller
	 * @param authoring tool
	 * @param path name
	 * @param model
	 * @param xml root
	 * @param xml node
	 * @return Group
	 * @throws Exception
	 */
	private static Group readVisualSceneInstanceController(AuthoringTool authoringTool, String pathName, Model model, Element xmlRoot, Element xmlNode) throws Exception {
		StringTokenizer t;

		String xmlNodeId = xmlNode.getAttribute("id");
		String xmlNodeName = xmlNode.getAttribute("name");

		HashMap<String, String> materialSymbols = new HashMap<String, String>(); 

		// geometry id
		String xmlGeometryId = null;

		// parse library controllers, find our controller
		List<Element> xmlInstanceControllers = getChildrenByTagName(xmlNode, "instance_controller");
		Element xmlSkin = null;
		Element xmlInstanceController = xmlInstanceControllers.get(0);

		// parse material symbols
		for (Element xmlBindMaterial: getChildrenByTagName(xmlInstanceController, "bind_material"))
		for (Element xmlTechniqueCommon: getChildrenByTagName(xmlBindMaterial, "technique_common"))
		for (Element xmlInstanceMaterial: getChildrenByTagName(xmlTechniqueCommon, "instance_material")) {
			materialSymbols.put(
				xmlInstanceMaterial.getAttribute("symbol"),
				xmlInstanceMaterial.getAttribute("target")
			);
		}

		String xmlInstanceControllerId = xmlInstanceController.getAttribute("url").substring(1);
		Element xmlLibraryControllers = getChildrenByTagName(xmlRoot, "library_controllers").get(0);
		for (Element xmlLibraryController: getChildrenByTagName(xmlLibraryControllers, "controller")) {
			// our controller ?
			if (xmlLibraryController.getAttribute("id").equals(xmlInstanceControllerId)) {
				// parse skin
				List<Element> xmlSkins = getChildrenByTagName(xmlLibraryController, "skin");
				if (xmlSkins.isEmpty() == false) {
					xmlSkin = xmlSkins.get(0);
				}
			}
		}

		// check for xml skin
		if (xmlSkin == null) {
			throw new ModelFileIOException("skin not found for instance controller " + xmlNodeId);
		}

		// get geometry id
		xmlGeometryId = xmlSkin.getAttribute("source").substring(1);

		// parse bind shape matrix
		String xmlMatrix = getChildrenByTagName(xmlSkin, "bind_shape_matrix").get(0).getTextContent();
		t = new StringTokenizer(xmlMatrix, " \n\r");

		// 
		Matrix4x4 bindShapeMatrix = new Matrix4x4(
			Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
			Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
			Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
			Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
			Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
			Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
			Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
			Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken())
		).transpose();

		// tdme model definitions
		Group group = new Group(
			model,
			xmlNodeId,
			xmlNodeName
		);

		// create skinning
		Skinning skinning = group.createSkinning();

		// parse geometry
		readGeometry(authoringTool, pathName, model, group, xmlRoot, xmlGeometryId, materialSymbols);

		// parse joints
		String xmlJointsSource = null;
		String xmlJointsInverseBindMatricesSource = null;
		Element xmlJoints = getChildrenByTagName(xmlSkin, "joints").get(0); 
		for(Element xmlJointsInput: getChildrenByTagName(xmlJoints, "input")) {
			if (xmlJointsInput.getAttribute("semantic").equals("JOINT")) {
				xmlJointsSource = xmlJointsInput.getAttribute("source").substring(1);
			} else
			if (xmlJointsInput.getAttribute("semantic").equals("INV_BIND_MATRIX")) {
				xmlJointsInverseBindMatricesSource = xmlJointsInput.getAttribute("source").substring(1);
			}
		}

		// check for joints sources
		if (xmlJointsSource == null) {
			throw new ModelFileIOException("joint source not found for instance controller " + xmlNodeId);
		}

		// parse joint ids
		ArrayList<Joint> joints = new ArrayList<Joint>();
		for (Element xmlSkinSource: getChildrenByTagName(xmlSkin, "source")) {
			if (xmlSkinSource.getAttribute("id").equals(xmlJointsSource)) {
				t = new StringTokenizer(getChildrenByTagName(xmlSkinSource, "Name_array").get(0).getTextContent(), " \n\r");
				while (t.hasMoreTokens()) {
					joints.add(new Joint(t.nextToken()));
				}
			}
		}
		skinning.setJoints(joints);

		// check for inverse bind matrices source
		if (xmlJointsInverseBindMatricesSource == null) {
			throw new ModelFileIOException("inverse bind matrices source not found for instance controller " + xmlNodeId);
		}

		// parse joints inverse bind matrix
		for (Element xmlSkinSource: getChildrenByTagName(xmlSkin, "source")) {
			if (xmlSkinSource.getAttribute("id").equals(xmlJointsInverseBindMatricesSource)) {
				t = new StringTokenizer(getChildrenByTagName(xmlSkinSource, "float_array").get(0).getTextContent(), " \n\r");
				Joint[] _joints = skinning.getJoints();
				for (int i = 0; i < _joints.length; i++) {
					_joints[i].getBindMatrix().set(
						bindShapeMatrix.clone().multiply(
							// inverse bind matrix
							new Matrix4x4(
								Float.parseFloat(t.nextToken()),Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()),Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()),Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()),Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()),Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()),Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()),Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()),Float.parseFloat(t.nextToken())
							).transpose()
						)
					);
				}
			}
		}

		// read vertex influences
		ArrayList<Float> weights = new ArrayList<Float>();
		int xmlJointOffset = -1;
		int xmlWeightOffset = -1;
		String xmlWeightsSource = null;
		Element xmlVertexWeights = getChildrenByTagName(xmlSkin, "vertex_weights").get(0);
		List<Element> xmlVertexWeightInputs = getChildrenByTagName(xmlVertexWeights, "input");
		for(Element xmlVertexWeightInput: xmlVertexWeightInputs) {
			if (xmlVertexWeightInput.getAttribute("semantic").equals("JOINT")) {
				if (xmlVertexWeightInput.getAttribute("source").substring(1).equals(xmlJointsSource) == false) {
					throw new ModelFileIOException("joint inverse bind matrices source do not match");
				}
				xmlJointOffset = Integer.parseInt(xmlVertexWeightInput.getAttribute("offset")); 
			} else
			if (xmlVertexWeightInput.getAttribute("semantic").equals("WEIGHT")) {
				xmlWeightOffset = Integer.parseInt(xmlVertexWeightInput.getAttribute("offset"));
				xmlWeightsSource = xmlVertexWeightInput.getAttribute("source").substring(1); 
			}
		}

		// check for vertex weight parameter 
		if (xmlJointOffset == -1) {
			throw new ModelFileIOException("xml vertext weight joint offset missing for node " + xmlNodeId);
		}
		if (xmlWeightOffset == -1) {
			throw new ModelFileIOException("xml vertext weight weight offset missing for node " + xmlNodeId);
		}
		if (xmlWeightsSource == null) {
			throw new ModelFileIOException("xml vertext weight weight source missing for node " + xmlNodeId);
		}

		// parse weights
		for (Element xmlSkinSource: getChildrenByTagName(xmlSkin, "source")) {
			if (xmlSkinSource.getAttribute("id").equals(xmlWeightsSource)) {
				t = new StringTokenizer(getChildrenByTagName(xmlSkinSource, "float_array").get(0).getTextContent(), " \n\r");
				while (t.hasMoreTokens()) {
					weights.add(new Float(Float.parseFloat(t.nextToken())));
				}
			}
		}
		skinning.setWeights(weights);

		// actually do parse joint influences of each vertex
		int xmlVertexWeightInputCount = xmlVertexWeightInputs.size();
		String vertexJointsInfluenceCountString = getChildrenByTagName(xmlVertexWeights, "vcount").get(0).getTextContent();
		String vertexJointsInfluencesString = getChildrenByTagName(xmlVertexWeights, "v").get(0).getTextContent();

		t = new StringTokenizer(vertexJointsInfluenceCountString, " \n\r");
		StringTokenizer t2 = new StringTokenizer(vertexJointsInfluencesString, " \n\r");
		int offset = 0;
		ArrayList<ArrayList<JointWeight>> verticesJointsWeights = new ArrayList<ArrayList<JointWeight>>();
		while (t.hasMoreTokens()) {
			// read joint influences for current vertex
			int vertexJointsInfluencesCount = Integer.parseInt(t.nextToken());
			ArrayList<JointWeight> vertexJointsWeights = new ArrayList<JointWeight>();
			for (int i = 0; i < vertexJointsInfluencesCount; i++) {
				int vertexJoint = -1;
				int vertexWeight = -1;
				while (vertexJoint == -1 || vertexWeight == -1) {
					int value = Integer.parseInt(t2.nextToken());
					if (offset % xmlVertexWeightInputCount == xmlJointOffset) {
						vertexJoint = value;
					} else
					if (offset % xmlVertexWeightInputCount == xmlWeightOffset) {
						vertexWeight = value;
					}
					offset++;
				}
				vertexJointsWeights.add(new JointWeight(vertexJoint, vertexWeight));
			}
			verticesJointsWeights.add(vertexJointsWeights);
		}
		skinning.setVerticesJointsWeights(verticesJointsWeights);
		return group;
	}

	/**
	 * Reads a geometry
	 * @param authoring tools
	 * @param path name
	 * @param model
	 * @param group
	 * @param xml root
	 * @param xml node id
	 * @param material symbols
	 * @throws Exception
	 */
	public static void readGeometry(AuthoringTool authoringTool, String pathName, Model model, Group group, Element xmlRoot, String xmlNodeId, HashMap<String, String> materialSymbols) throws Exception {
		StringTokenizer t;

		//
		FacesEntity facesEntity = null;
		ArrayList<FacesEntity> facesEntities = new ArrayList<FacesEntity>(Arrays.asList(group.getFacesEntities()));

		int verticesOffset = group.getVertices().length;
		ArrayList<Vector3> vertices = new ArrayList<Vector3>(Arrays.asList(group.getVertices()));

		int normalsOffset = group.getNormals().length;
		ArrayList<Vector3> normals = new ArrayList<Vector3>(Arrays.asList(group.getNormals()));

		int textureCoordinatesOffset =
			group.getTextureCoordinates() != null?
			group.getTextureCoordinates().length:
			0;
		ArrayList<TextureCoordinate> textureCoordinates =
			group.getTextureCoordinates() != null?
			new ArrayList<TextureCoordinate>(Arrays.asList(group.getTextureCoordinates())):
			new ArrayList<TextureCoordinate>();

		Element xmlLibraryGeometries = getChildrenByTagName(xmlRoot, "library_geometries").get(0);
		for (Element xmlGeometry: getChildrenByTagName(xmlLibraryGeometries, "geometry")) {
			if (xmlGeometry.getAttribute("id").equals(xmlNodeId)) {
				Element xmlMesh = getChildrenByTagName(xmlGeometry, "mesh").get(0);

				ArrayList<Element> xmlPolygonsList = new ArrayList<Element>(); 

				// try to read from triangles
				for (Element xmlTriangesElement: getChildrenByTagName(xmlMesh, "triangles")) {
					xmlPolygonsList.add(xmlTriangesElement);
				}

				// try to read from polylist
				for (Element xmlPolyListElement: getChildrenByTagName(xmlMesh, "polylist")) {
					xmlPolygonsList.add(xmlPolyListElement);
				}

				// try to read from polygons
				for (Element xmlPolygonsElement: getChildrenByTagName(xmlMesh, "polygons")) {
					xmlPolygonsList.add(xmlPolygonsElement);
				}

				// parse from xml polygons elements
				for(Element xmlPolygons: xmlPolygonsList) {
					ArrayList<Face>faces = new ArrayList<Face>();
					facesEntity = new FacesEntity(group, xmlNodeId);
					if (xmlPolygons.getNodeName().toLowerCase().equals("polylist")) {
						t = new StringTokenizer(getChildrenByTagName(xmlPolygons, "vcount").get(0).getTextContent());
						while (t.hasMoreTokens()) {
							int vertexCount = Integer.parseInt(t.nextToken());
							if (vertexCount != 3) {
								throw new ModelFileIOException("we only support triangles in " + xmlNodeId);
							}
						}
					}
					// parse triangles
					int xmlInputs = -1;
					int xmlVerticesOffset = -1;
					String xmlVerticesSource = null;
	
					int xmlNormalsOffset = -1;
					String xmlNormalsSource = null;
	
					int xmlTexCoordOffset = -1;
					String xmlTexCoordSource = null;

					int xmlColorOffset = -1;
					String xmlColorSource = null;

					// material
					String xmlMaterialId = xmlPolygons.getAttribute("material");
					String materialSymbol = materialSymbols.get(xmlMaterialId);
					if (materialSymbol != null) xmlMaterialId = materialSymbol.substring(1);
					if (xmlMaterialId != null && xmlMaterialId.length() > 0) {
						Material material = model.getMaterials().get(xmlMaterialId);
						if (material == null) {
							// parse material as we do not have it yet
							material = readMaterial(authoringTool, pathName, model, xmlRoot, xmlMaterialId);
						}
						// set it up
						facesEntity.setMaterial(material);
					}

					// parse input sources
					HashSet<Integer> xmlInputSet = new HashSet<Integer>();
					for (Element xmlTrianglesInput: getChildrenByTagName(xmlPolygons, "input")) {
						// check for vertices sources
						if (xmlTrianglesInput.getAttribute("semantic").equals("VERTEX")) {
							xmlVerticesOffset = Integer.parseInt(xmlTrianglesInput.getAttribute("offset"));
							xmlVerticesSource = xmlTrianglesInput.getAttribute("source").substring(1);
							xmlInputSet.add(xmlVerticesOffset);
						} else
						// check for normals sources
						if (xmlTrianglesInput.getAttribute("semantic").equals("NORMAL")) {
							xmlNormalsOffset = Integer.parseInt(xmlTrianglesInput.getAttribute("offset"));
							xmlNormalsSource = xmlTrianglesInput.getAttribute("source").substring(1);
							xmlInputSet.add(xmlNormalsOffset);
						}
						// check for texture coordinate sources
						if (xmlTrianglesInput.getAttribute("semantic").equals("TEXCOORD")) {
							xmlTexCoordOffset = Integer.parseInt(xmlTrianglesInput.getAttribute("offset"));
							xmlTexCoordSource = xmlTrianglesInput.getAttribute("source").substring(1);
							xmlInputSet.add(xmlTexCoordOffset);
						}
						// check for color coordinate sources
						if (xmlTrianglesInput.getAttribute("semantic").equals("COLOR")) {
							xmlColorOffset = Integer.parseInt(xmlTrianglesInput.getAttribute("offset"));
							xmlColorSource = xmlTrianglesInput.getAttribute("source").substring(1);
							xmlInputSet.add(xmlColorOffset);
						}
					}
					xmlInputs = xmlInputSet.size();

					// get vertices source
					for (Element xmlVertices: getChildrenByTagName(xmlMesh, "vertices")) {
						if (xmlVertices.getAttribute("id").equals(xmlVerticesSource)) {
							for (Element xmlVerticesInput: getChildrenByTagName(xmlVertices, "input")) {
								if (xmlVerticesInput.getAttribute("semantic").equalsIgnoreCase("position")) {
									xmlVerticesSource = xmlVerticesInput.getAttribute("source").substring(1);
								} else
								if (xmlVerticesInput.getAttribute("semantic").equalsIgnoreCase("normal")) {
									xmlNormalsSource = xmlVerticesInput.getAttribute("source").substring(1);
								}									
							}
						}
					}

					// check for triangles vertices sources
					if (xmlVerticesSource == null) {
						throw new ModelFileIOException("Could not determine triangles vertices source for '" + xmlNodeId + "'");
					}
	
					// check for triangles normals sources
					if (xmlNormalsSource == null) {
						throw new ModelFileIOException("Could not determine triangles normal source for '" + xmlNodeId + "'");
					}
	
					// load vertices, normals, texture coordinates
					for (Element xmlMeshSource: getChildrenByTagName(xmlMesh, "source")) {
						// vertices
						if (xmlMeshSource.getAttribute("id").equals(xmlVerticesSource)) {
							Element xmlFloatArray = getChildrenByTagName(xmlMeshSource, "float_array").get(0);
							String valueString = xmlFloatArray.getTextContent();
							t = new StringTokenizer(valueString, " \n\r");
							while (t.hasMoreTokens()) {
								Vector3 v = new Vector3(
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken())
								);
								vertices.add(v);
							}
						} else
						// normals
						if (xmlMeshSource.getAttribute("id").equals(xmlNormalsSource)) {
							Element xmlFloatArray = getChildrenByTagName(xmlMeshSource, "float_array").get(0);
							String valueString = xmlFloatArray.getTextContent();
							t = new StringTokenizer(valueString, " \n\r");
							while (t.hasMoreTokens()) {
								Vector3 v = new Vector3(
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken())
								);
								normals.add(v);
							}
						}
						// texture coordinates
						if (xmlTexCoordSource != null) {
							if (xmlMeshSource.getAttribute("id").equals(xmlTexCoordSource)) {
								Element xmlFloatArray = getChildrenByTagName(xmlMeshSource, "float_array").get(0);
								String valueString = xmlFloatArray.getTextContent();
								t = new StringTokenizer(valueString, " \n\r");
								while (t.hasMoreTokens()) {
									TextureCoordinate tc = new TextureCoordinate(
										Float.parseFloat(t.nextToken()),
										Float.parseFloat(t.nextToken())
									);
									textureCoordinates.add(tc);
								}
							}
						}
					}

					// load faces
					for(Element xmlPolygon: getChildrenByTagName(xmlPolygons, "p")) {
						String valueString = xmlPolygon.getTextContent();
						t = new StringTokenizer(valueString, " \n\r");
						int vi[] = new int[3];
						int viIdx = 0;
						int ni[] = new int[3];
						int niIdx = 0;
						int ti[] = xmlTexCoordSource == null?null:new int[3];
						int tiIdx = 0;
						int valueIdx = 0;
						boolean valid = true;
						while (t.hasMoreTokens()) {
							int value = Integer.parseInt(t.nextToken());
							if (valueIdx % xmlInputs == xmlVerticesOffset) {
								vi[viIdx++] = value;
								// validate
								if (value < 0 || value >= vertices.size() - verticesOffset) {
									valid = false;
								}
								// fix for some strange models
								if (xmlNormalsSource != null && xmlNormalsOffset == -1) {
									ni[niIdx++] = value;
									// validate
									if (value < 0 || value >= normals.size() - normalsOffset) {
										valid = false;
									}
								}
							}
							if (xmlNormalsOffset != -1 && valueIdx % xmlInputs == xmlNormalsOffset) {
								ni[niIdx++] = value;
								// validate
								if (value < 0 || value >= normals.size() - normalsOffset) {
									valid = false;
								}
							}
							if (xmlTexCoordOffset != -1 && valueIdx % xmlInputs == xmlTexCoordOffset) {
								ti[tiIdx++] = value;
								// validate
								if (value < 0 || value >= textureCoordinates.size() - textureCoordinatesOffset) {
									valid = false;
								}
							}
							if (viIdx == 3 && niIdx == 3 && (ti == null || tiIdx == 3)) {
								// only add valid faces
								if (valid) {
									// add face
									Face f = new Face(
										group,
										vi[0] + verticesOffset,
										vi[1] + verticesOffset,
										vi[2] + verticesOffset,
										ni[0] + normalsOffset,
										ni[1] + normalsOffset,
										ni[2] + normalsOffset
									);
									if (ti != null) {
										f.setTextureCoordinateIndices(
											ti[0] + textureCoordinatesOffset,
											ti[1] + textureCoordinatesOffset,
											ti[2] + textureCoordinatesOffset
										);
									}
									faces.add(f);
								}
								viIdx = 0;
								niIdx = 0;
								tiIdx = 0;
								valid = true;
							}
							valueIdx++;
						}
					}
					// add faces entities if we have any
					if (faces.isEmpty() == false) {
						facesEntity.setFaces(faces);
						facesEntities.add(facesEntity);
					}
				}
			}
		}

		// set up group
		group.setVertices(vertices);
		group.setNormals(normals);
		if (textureCoordinates.size() > 0) group.setTextureCoordinates(textureCoordinates);
		group.setFacesEntities(facesEntities);
		
		// create normal tangents and bitangents
		ModelHelper.createNormalTangentsAndBitangents(group);

		// determine features
		group.determineFeatures();
	}

	/**
	 * Reads a material
	 * @param authoring tool
	 * @param path name
	 * @param model
	 * @param xml root
	 * @param xml node id
	 * @return material
	 * @throws Exception
	 */
	public static Material readMaterial(AuthoringTool authoringTool, String pathName, Model model, Element xmlRoot, String xmlNodeId) throws Exception {
		// determine effect id
		String xmlEffectId = null;
		Element xmlLibraryMaterials = getChildrenByTagName(xmlRoot, "library_materials").get(0);
		for (Element xmlMaterial: getChildrenByTagName(xmlLibraryMaterials, "material")) {
			if (xmlMaterial.getAttribute("id").equals(xmlNodeId)) {
				Element xmlInstanceEffect = getChildrenByTagName(xmlMaterial, "instance_effect").get(0);
				xmlEffectId = xmlInstanceEffect.getAttribute("url").substring(1); 
			}
		}

		if (xmlEffectId == null) {
			System.out.println("Could not determine effect id for '" + xmlNodeId + "'");
			return null;
		}

		// parse effect
		Material material = new Material(xmlNodeId);
		String xmlDiffuseTextureId = null;
		String xmlSpecularTextureId = null;
		String xmlBumpTextureId = null;
		Element xmlLibraryEffects = getChildrenByTagName(xmlRoot, "library_effects").get(0);
		for (Element xmlEffect: getChildrenByTagName(xmlLibraryEffects, "effect")) {
			if (xmlEffect.getAttribute("id").equals(xmlEffectId)) {
				// diffuse texture
				Element xmlProfile = getChildrenByTagName(xmlEffect, "profile_COMMON").get(0);
				HashMap<String, String> samplerSurfaceMapping = new HashMap<String, String>();
				HashMap<String, String> surfaceImageMapping = new HashMap<String, String>();
				for(Element xmlNewParam: getChildrenByTagName(xmlProfile, "newparam")) {
					String xmlNewParamSID = xmlNewParam.getAttribute("sid");
					for(Element xmlSurface: getChildrenByTagName(xmlNewParam, "surface"))
					for(Element xmlSurfaceInitFrom: getChildrenByTagName(xmlSurface, "init_from")) {
						surfaceImageMapping.put(xmlNewParamSID, xmlSurfaceInitFrom.getTextContent());
					}
					for(Element xmlSampler2D: getChildrenByTagName(xmlNewParam, "sampler2D"))
					for(Element xmlSampler2DSource: getChildrenByTagName(xmlSampler2D, "source")) {
						samplerSurfaceMapping.put(xmlNewParamSID, xmlSampler2DSource.getTextContent());
					}					
				}

				// 
				for (Element xmlTechnique: getChildrenByTagName(xmlProfile, "technique")) {
					NodeList xmlTechniqueNodes = xmlTechnique.getChildNodes();
					for (int i = 0; i < xmlTechniqueNodes.getLength(); i++) {
						Node xmlTechniqueNode = xmlTechniqueNodes.item(i);

						// skip if not an element
						if (xmlTechniqueNode.getNodeType() != Node.ELEMENT_NODE) continue;

						// diffuse
						for (Element xmlDiffuse: getChildrenByTagName((Element)xmlTechniqueNode, "diffuse")) {
							// texture
							for (Element xmlTexture: getChildrenByTagName(xmlDiffuse, "texture")) {
								xmlDiffuseTextureId = xmlTexture.getAttribute("texture");
								String sample2Surface = samplerSurfaceMapping.get(xmlDiffuseTextureId);
								String surface2Image = null;
								if (sample2Surface != null) surface2Image = surfaceImageMapping.get(sample2Surface);
								if (surface2Image != null) xmlDiffuseTextureId = surface2Image; 
							}
							// color
							for (Element xmlColor: getChildrenByTagName(xmlDiffuse, "color")) {
								StringTokenizer t = new StringTokenizer(xmlColor.getTextContent(), " ");
								material.getDiffuseColor().set(
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken())
								);
							}
						}
						// ambient
						for (Element xmlAmbient: getChildrenByTagName((Element)xmlTechniqueNode, "ambient")) {
							// color
							for (Element xmlColor: getChildrenByTagName(xmlAmbient, "color")) {
								StringTokenizer t = new StringTokenizer(xmlColor.getTextContent(), " ");
								material.getAmbientColor().set(
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken())
								);
							}
						}
						// emission
						for (Element xmlEmission: getChildrenByTagName((Element)xmlTechniqueNode, "emission")) {
							// color
							for (Element xmlColor: getChildrenByTagName(xmlEmission, "color")) {
								StringTokenizer t = new StringTokenizer(xmlColor.getTextContent(), " ");
								material.getEmissionColor().set(
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken())
								);
							}
						}
						// specular
						boolean hasSpecularMap = false;
						boolean hasSpecularColor = false;
						for (Element xmlSpecular: getChildrenByTagName((Element)xmlTechniqueNode, "specular")) {
							// texture
							for (Element xmlTexture: getChildrenByTagName(xmlSpecular, "texture")) {
								xmlSpecularTextureId = xmlTexture.getAttribute("texture");
								String sample2Surface = samplerSurfaceMapping.get(xmlSpecularTextureId);
								String surface2Image = null;
								if (sample2Surface != null) surface2Image = surfaceImageMapping.get(sample2Surface);
								if (surface2Image != null) xmlSpecularTextureId = surface2Image; 
								hasSpecularMap = true;
							}
							// color
							for (Element xmlColor: getChildrenByTagName(xmlSpecular, "color")) {
								StringTokenizer t = new StringTokenizer(xmlColor.getTextContent(), " ");
								material.getSpecularColor().set(
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken())
								);
								hasSpecularColor = true;
							}
						}
						// set up specular color if not yet done but spec maps is available
						if (hasSpecularMap == true && hasSpecularColor == false) {
							material.getSpecularColor().set(1f,1f,1f,1f);
						}
						// shininess
						for (Element xmlShininess: getChildrenByTagName((Element)xmlTechniqueNode, "shininess")) {
							// color
							for (Element xmlFloat: getChildrenByTagName(xmlShininess, "float")) {
								material.setShininess(Float.parseFloat(xmlFloat.getTextContent()));
							}
						}
					}

					// bump / normal map
					for (Element xmlBumpExtra: getChildrenByTagName(xmlTechnique, "extra"))
					for (Element xmlBumpTechnique: getChildrenByTagName(xmlBumpExtra, "technique"))
					for (Element xmlBumpTechniqueBump: getChildrenByTagName(xmlBumpTechnique, "bump"))
					for (Element xmlBumpTexture: getChildrenByTagName(xmlBumpTechniqueBump, "texture")){
						xmlBumpTextureId = xmlBumpTexture.getAttribute("texture");
						String sample2Surface = samplerSurfaceMapping.get(xmlBumpTextureId);
						String surface2Image = null;
						if (sample2Surface != null) surface2Image = surfaceImageMapping.get(sample2Surface);
						if (surface2Image != null) xmlBumpTextureId = surface2Image; 

					}
				}
			}
		}

		// diffuse texture
		String xmlDiffuseTextureFilename = null;
		if (xmlDiffuseTextureId != null) {
			xmlDiffuseTextureFilename = getTextureFileNameById(xmlRoot, xmlDiffuseTextureId);
			// do we have a file name
			if (xmlDiffuseTextureFilename != null) {
				xmlDiffuseTextureFilename = makeFileNameRelative(xmlDiffuseTextureFilename); 
				// add texture
				material.setDiffuseTexture(pathName, xmlDiffuseTextureFilename);
			}
		}

		// specular texture
		String xmlSpecularTextureFilename = null;
		if (xmlSpecularTextureId != null) {
			xmlSpecularTextureFilename = getTextureFileNameById(xmlRoot, xmlSpecularTextureId);
			// do we have a file name
			if (xmlSpecularTextureFilename != null) {
				xmlSpecularTextureFilename = makeFileNameRelative(xmlSpecularTextureFilename); 
				// add texture
				material.setSpecularTexture(pathName, xmlSpecularTextureFilename);
			}
		}

		// normal map
		String xmlBumpTextureFilename = null;
		if (xmlBumpTextureId != null) {
			xmlBumpTextureFilename = getTextureFileNameById(xmlRoot, xmlBumpTextureId);
			// do we have a file name
			if (xmlBumpTextureFilename != null) {
				xmlBumpTextureFilename = makeFileNameRelative(xmlBumpTextureFilename);
				// add texture
				material.setNormalTexture(pathName, xmlBumpTextureFilename);
			}
		}

		// determine displacement map file name 
		String xmlDisplacementFilename = null;
		//  by diffuse file name
		if (xmlDiffuseTextureFilename != null) {
			xmlDisplacementFilename = determineDisplacementFilename(pathName, "diffuse", xmlDiffuseTextureFilename);
		}
		// 	by normal file name
		if (xmlDisplacementFilename == null && xmlBumpTextureFilename != null) {
			xmlDisplacementFilename = determineDisplacementFilename(pathName, "normal", xmlBumpTextureFilename);
		}

		// add texture
		if (xmlDisplacementFilename != null) {
			material.setDisplacementTexture(pathName, xmlDisplacementFilename);
		}

		// adjust ambient light with blender
		if (authoringTool == AuthoringTool.BLENDER && material.getAmbientColor().equals(BLENDER_AMBIENT_NONE)) {
			material.getAmbientColor().set(
				material.getDiffuseColor().getRed() * BLENDER_AMBIENT_FROM_DIFFUSE_SCALE, 
				material.getDiffuseColor().getGreen() * BLENDER_AMBIENT_FROM_DIFFUSE_SCALE, 
				material.getDiffuseColor().getBlue() * BLENDER_AMBIENT_FROM_DIFFUSE_SCALE, 
				1.0f
			);
			material.getDiffuseColor().set(
				material.getDiffuseColor().getRed() * BLENDER_DIFFUSE_SCALE,
				material.getDiffuseColor().getGreen() * BLENDER_DIFFUSE_SCALE,
				material.getDiffuseColor().getBlue() * BLENDER_DIFFUSE_SCALE,
				material.getDiffuseColor().getAlpha()
			);
		}

		// add material to library
		model.getMaterials().put(material.getId(), material);

		//
		return material;
	}

	/**
	 * Determine displacement filename 
	 * @param path
	 * @param map type
	 * @param file name
	 * @return displacement file name or null
	 */
	private static String determineDisplacementFilename(String path, String mapType, String fileName) {
		// filename to lower case
		String tmpFileNameCandidate = fileName.toLowerCase();
		// remove extension
		tmpFileNameCandidate = tmpFileNameCandidate.substring(0, tmpFileNameCandidate.lastIndexOf('.'));
		// normal - maptype
		if (tmpFileNameCandidate.endsWith(mapType)) tmpFileNameCandidate = tmpFileNameCandidate.substring(0, tmpFileNameCandidate.length() - mapType.length());
		// +displacment
		tmpFileNameCandidate+= "displacement";
		// final
		final String finalFilenameCandidate = tmpFileNameCandidate;
		
		// try to find file in path file listing
		try {
			String[] fileNameCandidates = FileSystem.getInstance().list(path, new FilenameFilter() {
				/*
				 * (non-Javadoc)
				 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
				 */
				public boolean accept(File dir, String name) {
					return
						name.equalsIgnoreCase(finalFilenameCandidate + ".png") ||
						name.equalsIgnoreCase(finalFilenameCandidate + ".tga") ||
						name.equalsIgnoreCase(finalFilenameCandidate + ".jpg");
				}
			});
			tmpFileNameCandidate = fileNameCandidates.length > 0?fileNameCandidates[0]:null;
		} catch (IOException ioe) {
			System.out.println("DAEReader::makeDisplacementFilenameCandidate::" + ioe);
		}
		
		// we are done
		return tmpFileNameCandidate;
	}

	/**
	 * Make file name relative
	 * @param file name
	 * @return file name
	 */
	private static String makeFileNameRelative(String fileName) {
		// check if absolute path
		if (fileName.startsWith("/") == true ||
			fileName.matches("^[A-Z]\\:\\\\.*$") == true) {					
			// yep, texture must be in the same path as DAE, so cut path off
			//	TODO: could be improved
			int indexSlash = fileName.lastIndexOf("/");
			int indexBackslash = fileName.lastIndexOf("\\");
			if (indexSlash != -1 || indexBackslash != -1) {
				if (indexSlash > indexBackslash) {
					fileName = fileName.substring(indexSlash + 1);
				} else {
					fileName = fileName.substring(indexBackslash + 1);
				}
			}
		}
		
		//
		return fileName;
	}
	
	/**
	 * Get texture file name by id
	 * @param xml root
	 * @param xml texture id
	 * @return xml texture file name
	 */
	private static String getTextureFileNameById(Element xmlRoot, String xmlTextureId) {
		String xmlTextureFilename = null;
		// yep
		Element xmlLibraryImages = getChildrenByTagName(xmlRoot, "library_images").get(0);
		for (Element xmlImage: getChildrenByTagName(xmlLibraryImages, "image")) {
			if (xmlImage.getAttribute("id").equals(xmlTextureId)) {
				xmlTextureFilename = getChildrenByTagName(xmlImage, "init_from").get(0).getTextContent();
				// cut "file://"
				if (xmlTextureFilename.startsWith("file://")) {
					xmlTextureFilename = xmlTextureFilename.substring(7); 
				}
				break;
			}
		}
		return xmlTextureFilename;
	}
	
	/**
	 * Returns immediate children by tag names of parent
	 * @param parent
	 * @param name
	 * @return children with given name
	 */
	public static List<Element> getChildrenByTagName(Element parent, String name) {
		List<Element> nodeList = new ArrayList<Element>();
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName())) {
				nodeList.add((Element) child);
			}
		}
		return nodeList;
	}

	/**
	 * Converts an element to string
	 * @param node
	 * @return string representation
	 */
	private static String nodeToString(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		String attributesString = "";
		for(int i = 0; i < attributes.getLength(); i++) {
			if (i > 0) {
				attributesString+=", ";
			}
			attributesString+= attributes.item(i).getNodeName();
			attributesString+= "=";
			attributesString+= attributes.item(i).getNodeValue();
		}
		return
			"[" +
			"name=" + node.getNodeName() +
			", attributes=[" + attributesString + "]" +
			"]";
	}

}
