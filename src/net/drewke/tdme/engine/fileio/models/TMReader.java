package net.drewke.tdme.engine.fileio.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.drewke.tdme.engine.model.Animation;
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
import net.drewke.tdme.engine.primitives.BoundingBox;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.os.FileSystem;
import net.drewke.tdme.utils.HashMap;

/**
 * TDME model reader
 * @author Andreas Drewke
 * @version $Id$
 */
public class TMReader {

	/**
	 * TDME model format reader
	 * @param path name
	 * @param file name
	 * @throws IOException
	 * @throws ModelIOException
	 * @return model
	 */
	public static Model read(String pathName, String fileName) throws IOException, ModelFileIOException {
		InputStream is = null;
		try {
			is = FileSystem.getInstance().getInputStream(pathName, fileName);

			// version major.minor = 1.0
			String fileId = readString(is);
			if (fileId == null || fileId.equals("TDME Model") == false) {
				throw new ModelFileIOException("File is not a TDME model file, file id = '" + fileId + "'");
			}
			byte[] version = new byte[3];
			version[0] = readByte(is);
			version[1] = readByte(is);
			version[2] = readByte(is);
			if (version[0] != 1 || version[1] != 0 || version[2] != 0) {
				throw new ModelFileIOException("Version mismatch, should be 1.0.0, but is " + version[0] + "."  + version[1] + "."  + version[2]);
			}
			
			// meta data
			String name = readString(is);

			// up vector, rotation order, bounding box
			UpVector upVector = UpVector.valueOf(readString(is));
			RotationOrder rotationOrder = RotationOrder.valueOf(readString(is));
			BoundingBox boundingBox = new BoundingBox(new Vector3(readFloatArray(is)), new Vector3(readFloatArray(is)));

			// 	create object
			Model model = new Model(pathName + File.separator + fileName, fileName, upVector, rotationOrder, boundingBox);

			// set additional data
			model.setFPS(readFloat(is));
			model.getImportTransformationsMatrix().set(readFloatArray(is));

			// materials
			int materialCount = readInt(is);
			for (int i = 0; i < materialCount; i++) {
				Material material = readMaterial(is);
				model.getMaterials().put(material.getId(), material);
			}

			// sub groups
			readSubGroups(is, model, model.getSubGroups());

			//
			return model;
		} catch (IOException ioe) {
			throw ioe;
		} catch (ModelFileIOException mfioe) {
			throw mfioe;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	/**
	 * Reads a boolean from input stream
	 * @param input stream
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return boolean
	 */
	private static boolean readBoolean(InputStream is) throws IOException, ModelFileIOException {
		return readByte(is) == 1;
	}

	/**
	 * Reads a byte from input stream
	 * @param output stream
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return byte
	 */
	private static byte readByte(InputStream is) throws IOException, ModelFileIOException {
		int i = is.read();
		if (i == -1) {
			throw new ModelFileIOException("End of stream");
		}
		return (byte)i;
	}

	/**
	 * Reads a integer from input stream
	 * @param input stream
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return int
	 */
	private static int readInt(InputStream is) throws IOException, ModelFileIOException {
		int i = 
			(((int)readByte(is) & 0xFF) << 24) +
			(((int)readByte(is) & 0xFF) << 16) +
			(((int)readByte(is) & 0xFF) << 8) +
			(((int)readByte(is) & 0xFF) << 0);
		return i;
	}

	/**
	 * Reads a float from input stream
	 * @param input stream
	 * @throws IOException
	 * @throws ModelIOException
	 * @return float
	 */
	private static float readFloat(InputStream is) throws IOException, ModelFileIOException {
		int i = readInt(is);
		return Float.intBitsToFloat(i);
	}

	/**
	 * Reads a string from input stream
	 * @param input stream
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return string
	 */
	private static String readString(InputStream is) throws IOException, ModelFileIOException {
		if (readBoolean(is) == false) {
			return null;
		} else {
			int l = readInt(is);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < l; i++) {
				sb.append((char)readByte(is));
			}
			return sb.toString();
		}
	}

	/**
	 * Reads a float array from input stream
	 * @param input stream
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return float array
	 */
	private static float[] readFloatArray(InputStream is) throws IOException, ModelFileIOException {
		float[] f = new float[readInt(is)];
		for (int i = 0; i < f.length; i++) {
			f[i] = readFloat(is);
		}
		return f;
	}

	/**
	 * Read material
	 * @param input stream
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return material
	 */
	private static Material readMaterial(InputStream is) throws IOException, ModelFileIOException {
		String id = readString(is);
		Material m = new Material(id);
		m.getAmbientColor().set(readFloatArray(is));
		m.getDiffuseColor().set(readFloatArray(is));
		m.getSpecularColor().set(readFloatArray(is));
		m.getEmissionColor().set(readFloatArray(is));
		m.setShininess(readFloat(is));
		String diffuseTexturePathName = readString(is);
		String diffuseTextureFileName = readString(is);
		if (diffuseTextureFileName != null && diffuseTexturePathName != null) {
			m.setDiffuseTexture(diffuseTexturePathName, diffuseTextureFileName);
		}
		String specularTexturePathName = readString(is);
		String specularTextureFileName = readString(is);
		if (specularTextureFileName != null && specularTexturePathName != null) {
			m.setSpecularTexture(specularTexturePathName, specularTextureFileName);
		}
		String normalTexturePathName = readString(is);
		String normalTextureFileName = readString(is);
		if (normalTextureFileName != null && normalTexturePathName != null) {
			m.setNormalTexture(normalTexturePathName, normalTextureFileName);
		}
		String displacementTexturePathName = readString(is);
		String displacementTextureFileName = readString(is);
		if (displacementTextureFileName != null && displacementTexturePathName != null) {
			m.setDisplacementTexture(displacementTexturePathName, displacementTextureFileName);
		}
		return m;
	}

	/**
	 * Read vertices from input stream
	 * @param input stream
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return vector3 array
	 */
	private static Vector3[] readVertices(InputStream is) throws IOException, ModelFileIOException {
		if (readBoolean(is) == false) {
			return null;
		} else {
			Vector3[] v = new Vector3[readInt(is)];
			for (int i = 0; i < v.length; i++) {
				v[i] = new Vector3(readFloatArray(is));
			}
			return v;
		}
	}

	/**
	 * Read texture coordinates from input stream
	 * @param input stream
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return texture coordinates array
	 */
	private static TextureCoordinate[] readTextureCoordinates(InputStream is) throws IOException, ModelFileIOException {
		if (readBoolean(is) == false) {
			return null;
		} else {
			TextureCoordinate[] tc = new TextureCoordinate[readInt(is)];
			for (int i = 0; i < tc.length; i++) {
				tc[i] = new TextureCoordinate(readFloatArray(is));
			}
			return tc;
		}
	}

	/**
	 * Read indices from input stream
	 * @param input stream
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return indicies / int array
	 */
	private static int[] readIndices(InputStream is) throws IOException, ModelFileIOException {
		if (readBoolean(is) == false) {
			return null;
		} else {
			int indices[] = new int[readInt(is)];
			for (int i = 0; i < indices.length; i++) {
				indices[i] = readInt(is);
			}
			return indices;
		}
	}

	/**
	 * Read animation from input stream into group
	 * @param input stream
	 * @throws IOException 
	 * @throws ModelFileIOException
	 * @return Animation
	 */
	private static Animation readAnimation(InputStream is, Group g) throws IOException, ModelFileIOException {
		if (readBoolean(is) == false) {
			return null;
		} else {
			g.createAnimation(readInt(is));
			for (int i = 0; i < g.getAnimation().getTransformationsMatrices().length; i++) {
				g.getAnimation().getTransformationsMatrices()[i].set(readFloatArray(is));
			}

			// create default animation
			ModelHelper.createDefaultAnimation(g.getModel(), g.getAnimation().getTransformationsMatrices().length);

			//
			return g.getAnimation();
		}
	}

	/**
	 * Read faces entities from input stream
	 * @param input stream
	 * @param group
	 * @throws IOException
	 * @throws ModelIOException
	 */
	private static void readFacesEntities(InputStream is, Group g) throws IOException, ModelFileIOException {
		FacesEntity[] facesEntities = new FacesEntity[readInt(is)];
		for (int i = 0; i < facesEntities.length; i++) {
			facesEntities[i] = new FacesEntity(g, readString(is));
			if (readBoolean(is) == true) {
				facesEntities[i].setMaterial(g.getModel().getMaterials().get(readString(is)));
			}
			Face[] faces = new Face[readInt(is)];
			for (int j = 0; j < faces.length; j++) {
				int[] vertexIndices = readIndices(is);
				int[] normalIndices = readIndices(is);
				faces[j] = new Face(
					g, 
					vertexIndices[0],
					vertexIndices[1],
					vertexIndices[2],
					normalIndices[0],
					normalIndices[1],
					normalIndices[2]
				);
				int[] textureCoordinateIndices = readIndices(is);
				if (textureCoordinateIndices != null && textureCoordinateIndices.length > 0) {
					faces[j].setTextureCoordinateIndices(
						textureCoordinateIndices[0], 
						textureCoordinateIndices[1], 
						textureCoordinateIndices[2]
					);
				}
				int[] tangentIndices = readIndices(is);
				int[] bitangentIndices = readIndices(is);
				if (tangentIndices != null && tangentIndices.length > 0 &&
					bitangentIndices != null && bitangentIndices.length > 0) {
					faces[j].setTangentIndices(
						tangentIndices[0], 
						tangentIndices[1], 
						tangentIndices[2]
					);
					faces[j].setBitangentIndices(
						bitangentIndices[0], 
						bitangentIndices[1], 
						bitangentIndices[2]
					);
				}
			}
			facesEntities[i].setFaces(faces);
		}
		g.setFacesEntities(facesEntities);
	}

	/**
	 * Read skinning joint
	 * @param input stream
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return joint
	 */
	private static Joint readSkinningJoint(InputStream is) throws IOException, ModelFileIOException {
		Joint joint = new Joint(readString(is));
		joint.getBindMatrix().set(readFloatArray(is));
		return joint;
	}

	/**
	 * Read skinning joint weight
	 * @param input stream
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return joint weight
	 */
	private static JointWeight readSkinningJointWeight(InputStream is) throws IOException, ModelFileIOException {
		return new JointWeight(readInt(is), readInt(is));
	}

	/**
	 * Read skinning from input stream
	 * @param input stream
	 * @param group
	 * @throws IOException
	 */
	private static void readSkinning(InputStream is, Group g) throws IOException, ModelFileIOException {
		if (readBoolean(is) == true) {
			Skinning skinning = g.createSkinning();
			skinning.setWeights(readFloatArray(is));
			Joint[] joints = new Joint[readInt(is)];
			for (int i = 0; i < joints.length; i++) {
				joints[i] = readSkinningJoint(is);
			}
			skinning.setJoints(joints);
			JointWeight[][] verticesJointsWeight = new JointWeight[readInt(is)][];
			for (int i = 0; i < verticesJointsWeight.length; i++) {
				verticesJointsWeight[i] = new JointWeight[readInt(is)];
				for (int j = 0; j < verticesJointsWeight[i].length; j++) {
					verticesJointsWeight[i][j] = readSkinningJointWeight(is); 
				}
			}
			skinning.setVerticesJointsWeights(verticesJointsWeight);
		}
	}

	/**
	 * Read sub groups
	 * @param input stream
	 * @param model
	 * @param sub groups
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return group
	 */
	private static void readSubGroups(InputStream is, Model model, HashMap<String, Group> subGroups) throws IOException, ModelFileIOException {
		int subGroupCount = readInt(is);
		for (int i = 0; i < subGroupCount; i++) {
			Group subGroup = readGroup(is, model);
			subGroups.put(subGroup.getId(), subGroup);
			model.getGroups().put(subGroup.getId(), subGroup);
		}
	}

	/**
	 * Write group to output stream
	 * @param input stream
	 * @param model
	 * @throws IOException
	 * @throws ModelFileIOException
	 * @return group
	 */
	private static Group readGroup(InputStream is, Model model) throws IOException, ModelFileIOException {
		Group group = new Group(
			model,
			readString(is),
			readString(is)
			
		);
		group.setJoint(readBoolean(is));
		group.getTransformationsMatrix().set(readFloatArray(is));
		group.setVertices(readVertices(is));
		group.setNormals(readVertices(is));
		group.setTextureCoordinates(readTextureCoordinates(is));
		group.setTangents(readVertices(is));
		group.setBitangents(readVertices(is));
		readAnimation(is, group);
		readSkinning(is, group);
		readFacesEntities(is, group);
		group.determineFeatures();
		readSubGroups(is, model, group.getSubGroups());
		return group;
	}

}
