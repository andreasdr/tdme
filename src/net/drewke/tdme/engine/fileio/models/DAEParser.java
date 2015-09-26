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

import net.drewke.tdme.engine.model.Animation;
import net.drewke.tdme.engine.model.AnimationSetup;
import net.drewke.tdme.engine.model.Face;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Group;
import net.drewke.tdme.engine.model.Joint;
import net.drewke.tdme.engine.model.JointWeight;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.model.ModelHelper;
import net.drewke.tdme.engine.model.Skinning;
import net.drewke.tdme.engine.model.TextureCoordinate;
import net.drewke.tdme.math.Matrix4x4;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Collada DAE parser
 * @author Andreas Drewke
 * @version $Id$
 */
public final class DAEParser {

	/**
	 * Parses a Collada DAE file
	 * @param pathName
	 * @param fileName
	 * @return Model instance
	 * @throws IOException
	 */
	public static Model parse(String pathName, String fileName) throws Exception {
		// load dae xml document
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = builder.parse(FileSystem.getInstance().getInputStream(pathName, fileName));
		Element xmlRoot = document.getDocumentElement();

		// 	create object
		Model model = new Model(pathName + File.separator + fileName, fileName);

		// determine up axis
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
			for(Element xmlAssetUnit: getChildrenByTagName(xmlAsset, "unit")) {
				String tmp = null;
				if ((tmp = xmlAssetUnit.getAttribute("meter")) != null) {
					model.getImportTransformationsMatrix().scale(Float.parseFloat(tmp));
				}
			}
		}

		// parse scene from xml
		String xmlSceneId = null;
		Element xmlScene = getChildrenByTagName(xmlRoot, "scene").get(0);
		for(Element xmlInstanceVisualscene: getChildrenByTagName(xmlScene, "instance_visual_scene")) {
			xmlSceneId = xmlInstanceVisualscene.getAttribute("url").substring(1);
		}

		// check for xml scene id
		if (xmlSceneId == null) {
			throw new ParserException("No scene id found");
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
					Group group = parseVisualSceneNode(pathName, model, xmlRoot, xmlNode, fps);
					if (group != null) {
						model.getSubGroups().put(group.getId(), group);
						model.getGroups().put(group.getId(), group);
					}
				}
			}
		}

		// determine joints and mark them as joints
		HashMap<String,Group> groups = model.getGroups();
		for (Group group: model.getSubGroups().getValuesIterator()) {
			Skinning skinning = group.getSkinning();
			// do we have a skinning
			if (skinning != null) {
				// yep
				for(Joint joint: skinning.getJoints()) {
					setJoint(groups.get(joint.getGroupId()));
				}
			}
		}

		// fix animation length
		AnimationSetup defaultAnimation = model.getAnimationSetup(Model.ANIMATIONSETUP_DEFAULT);
		if (defaultAnimation != null) {
			for (Group group: model.getSubGroups().getValuesIterator()) {
				fixAnimationLength(group, defaultAnimation.getFrames());
			}
		}

		// prepare for indexed rendering
		ModelHelper.prepareForIndexedRendering(model);

		//
		return model;
	}

	/**
	 * Sets up a group as joint taking all subgroups into account
	 * @param group
	 */
	private static void setJoint(Group root) {
		root.setJoint(true);
		for (Group group: root.getSubGroups().getValuesIterator()) {
			setJoint(group);
		}
	}

	/**
	 * Sets up a group as joint taking all subgroups into account
	 * @param group
	 */
	private static void fixAnimationLength(Group root, int frames) {
		Animation animation = root.getAnimation();
		Matrix4x4[] transformationsMatrices = new Matrix4x4[0];
		if (animation != null) {
			transformationsMatrices = root.getAnimation().getTransformationsMatrices();
		}
		animation = root.createAnimation(frames);
		for (int i = 0; i < transformationsMatrices.length; i++) {
			animation.getTransformationsMatrices()[i].set(transformationsMatrices[i]);
		}
		for (Group group: root.getSubGroups().getValuesIterator()) {
			fixAnimationLength(group, frames);
		}
	}

	/**
	 * Parses a DAE visual scene node
	 * @param pathName
	 * @param model
	 * @param xmlNode
	 * @param xmlRoot
	 * @param frames per second
	 */
	private static Group parseVisualSceneNode(String pathName, Model model, Element xmlRoot, Element xmlNode, float fps) throws Exception {
		List<Element> xmlInstanceControllers = getChildrenByTagName(xmlNode, "instance_controller");
		if (xmlInstanceControllers.isEmpty() == false) {
			return parseVisualSceneInstanceController(pathName, model, xmlRoot, xmlNode);
		} else {
			return parseNode(pathName, model, xmlRoot, xmlNode, fps);
		}
	}

	/**
	 * Parses a DAE visual scene group node
	 * @param pathName
	 * @param model
	 * @param xmlNode
	 * @param xmlRoot
	 * @param frames per seconds
	 */
	private static Group parseNode(String pathName, Model model, Element xmlRoot, Element xmlNode, float fps) throws Exception {
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
				if (xmlChannel.getAttribute("target").equals(xmlNodeId + "/matrix")) {
					xmlSamplerSource = xmlChannel.getAttribute("source").substring(1);
				}
	
				// check for sampler source
				if (xmlSamplerSource == null) {
					continue;
				}
	
				// parse animation output matrices
				String xmlSamplerOutputSource = null;
				Element xmlSampler = getChildrenByTagName(xmlAnimation, "sampler").get(0);
				for (Element xmlSamplerInput: getChildrenByTagName(xmlSampler, "input")) {
					if (xmlSamplerInput.getAttribute("semantic").equals("OUTPUT")) {
						xmlSamplerOutputSource = xmlSamplerInput.getAttribute("source").substring(1);
					}
				}
	
				// check for sampler source
				if (xmlSamplerOutputSource == null) {
					throw new ParserException("Could not fid xml sampler output source for animation for " + xmlNodeId);
				}
	
				// load animation output matrices
				for(Element xmlAnimationSource: getChildrenByTagName(xmlAnimation, "source")) {
					if (xmlAnimationSource.getAttribute("id").equals(xmlSamplerOutputSource)) {
						Element xmlFloatArray = getChildrenByTagName(xmlAnimationSource, "float_array").get(0);
						int frames = Integer.parseInt(xmlFloatArray.getAttribute("count")) / 16 - 1;

						// some models have animations without frames
						if (frames > 0) {

							// add default model animation setup
							AnimationSetup defaultAnimation = model.getAnimationSetup(Model.ANIMATIONSETUP_DEFAULT);
							if (defaultAnimation == null) {
								model.addAnimationSetup(Model.ANIMATIONSETUP_DEFAULT, 0, frames - 1, true);
							} else {
								// check default animation setup
								if (defaultAnimation.getStartFrame() != 0 || defaultAnimation.getEndFrame() != frames - 1) {
									System.out.println("Warning: default animation mismatch");
								}
								if (frames - 1 > defaultAnimation.getEndFrame()) {
									System.out.println("Warning: default animation mismatch, will be fixed");
									model.addAnimationSetup(Model.ANIMATIONSETUP_DEFAULT, 0, frames - 1, true);
								}
							}
	
							//
							Animation animation = group.createAnimation(frames);
							String valueString = xmlFloatArray.getTextContent();
							t = new StringTokenizer(valueString, " \n\r");
		
							// first frame is not a animation matrix
							Matrix4x4 frame0Matrix = new Matrix4x4(
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
								Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken())
							).transpose().invert();
		
							int frame = 0;
							Matrix4x4[] transformationsMatrices = animation.getTransformationsMatrices();
							while (t.hasMoreTokens()) {
								// set animation transformation matrix at frame
								transformationsMatrices[frame].set(
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()),
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken()), 
									Float.parseFloat(t.nextToken()), Float.parseFloat(t.nextToken())
								).transpose().multiply(frame0Matrix);
								frame++;
							}
						}
					}
				}
			}
		}

		// parse sub groups
		for(Element _xmlNode: getChildrenByTagName(xmlNode, "node")) {
			Group _group = parseVisualSceneNode(pathName, model, xmlRoot, _xmlNode, fps);
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
			parseGeometry(pathName, model, group, xmlRoot, xmlInstanceGeometryId, materialSymbols);

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
					Group _group = parseVisualSceneNode(pathName, model, xmlRoot, _xmlNode, fps);
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
					parseGeometry(pathName, model, group, xmlRoot, xmlGeometryId, materialSymbols);
				}
			}
		}

		//
		return group;
	}

	/**
	 * Parses a instance controller
	 * @param pathName
	 * @param model
	 * @param xmlRoot
	 * @param xmlNode
	 * @return
	 * @throws Exception
	 */
	private static Group parseVisualSceneInstanceController(String pathName, Model model, Element xmlRoot, Element xmlNode) throws Exception {
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
			throw new ParserException("skin not found for instance controller " + xmlNodeId);
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
		parseGeometry(pathName, model, group, xmlRoot, xmlGeometryId, materialSymbols);

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
			throw new ParserException("joint source not found for instance controller " + xmlNodeId);
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
			throw new ParserException("inverse bind matrices source not found for instance controller " + xmlNodeId);
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
					throw new ParserException("joint inverse bind matrices source do not match");
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
			throw new ParserException("xml vertext weight joint offset missing for node " + xmlNodeId);
		}
		if (xmlWeightOffset == -1) {
			throw new ParserException("xml vertext weight weight offset missing for node " + xmlNodeId);
		}
		if (xmlWeightsSource == null) {
			throw new ParserException("xml vertext weight weight source missing for node " + xmlNodeId);
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
	 * Parses a geometry
	 * @param pathName
	 * @param model
	 * @param group
	 * @param xmlRoot
	 * @param xmlNodeId
	 */
	public static void parseGeometry(String pathName, Model model, Group group, Element xmlRoot, String xmlNodeId, HashMap<String, String> materialSymbols) throws Exception {
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
								throw new ParserException("we only support triangles in " + xmlNodeId);
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
							material = parseMaterial(pathName, model, xmlRoot, xmlMaterialId);
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
						throw new ParserException("Could not determine triangles vertices source for '" + xmlNodeId + "'");
					}
	
					// check for triangles normals sources
					if (xmlNormalsSource == null) {
						throw new ParserException("Could not determine triangles normal source for '" + xmlNodeId + "'");
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
	 * Parses a material
	 * @param pathName
	 * @param model
	 * @param xmlRoot
	 * @param xmlNodeId
	 * @throws Exception
	 */
	public static Material parseMaterial(String pathName, Model model, Element xmlRoot, String xmlNodeId) throws Exception {
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
			System.out.println("DAEParser::makeDisplacementFilenameCandidate::" + ioe);
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
	 * Returns immediate children by tagnames of parent
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
