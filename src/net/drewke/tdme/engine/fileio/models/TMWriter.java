package net.drewke.tdme.engine.fileio.models;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.drewke.tdme.engine.fileio.textures.Texture;
import net.drewke.tdme.engine.model.Animation;
import net.drewke.tdme.engine.model.Face;
import net.drewke.tdme.engine.model.FacesEntity;
import net.drewke.tdme.engine.model.Group;
import net.drewke.tdme.engine.model.Joint;
import net.drewke.tdme.engine.model.JointWeight;
import net.drewke.tdme.engine.model.Material;
import net.drewke.tdme.engine.model.Model;
import net.drewke.tdme.engine.model.Skinning;
import net.drewke.tdme.engine.model.TextureCoordinate;
import net.drewke.tdme.math.Vector3;
import net.drewke.tdme.os.FileSystem;

/**
 * TDME model writer
 * @author Andreas Drewke
 * @version $Id$
 */
public class TMWriter {

	/**
	 * TDME model format writer
	 * @param model
	 * @param path name
	 * @param file name
	 * @throws IOException
	 * @throws ModelIOException
	 */
	public static void write(Model model, String pathName, String fileName) throws IOException {
		OutputStream os = null;
		try {
			os = FileSystem.getInstance().getOutputStream(pathName, fileName);
			// version major.minor = 1.0
			writeString(os, "TDME Model");
			writeByte(os, (byte)1);
			writeByte(os, (byte)0);
			writeByte(os, (byte)0);

			// meta data
			writeString(os, model.getName());
			writeFloat(os, model.getFPS());
			writeFloatArray(os, model.getImportTransformationsMatrix().getArray());

			// materials
			writeInt(os, model.getMaterials().size());
			for (Material material: model.getMaterials().getValuesIterator()) {
				writeMaterial(os, material);
			}

			// sub groups
			for (Group group: model.getSubGroups().getValuesIterator()) {
				writeGroup(os, group);
			}
		} catch (IOException ioe) {
			throw ioe;
		} finally {
			if (os != null) {
				os.flush();
				os.close();
			}
		}
	}

	/**
	 * Writes a boolean to output stream
	 * @param output stream
	 * @param boolean
	 * @throws IOException
	 */
	private static void writeBoolean(OutputStream os, boolean b) throws IOException {
		os.write((byte)(b == true?1:0));
	}

	/**
	 * Writes a byte to output stream
	 * @param output stream
	 * @param byte
	 * @throws IOException
	 */
	private static void writeByte(OutputStream os, byte b) throws IOException {
		os.write(b);
	}

	/**
	 * Writes a short to output stream
	 * @param output stream
	 * @param short
	 * @throws IOException
	 */
	private static void writeShort(OutputStream os, short s) throws IOException {
		os.write((s >> 0) & 0xff);
		os.write((s >> 8) & 0xff);
	}

	/**
	 * Writes a integer to output stream
	 * @param output stream
	 * @param int
	 * @throws IOException
	 */
	private static void writeInt(OutputStream os, int i) throws IOException {
		os.write((i >> 24) & 0xff);
		os.write((i >> 16) & 0xff);
		os.write((i >> 8) & 0xff);
		os.write((i >> 0) & 0xff);
	}

	/**
	 * Writes a integer to output stream
	 * @param output stream
	 * @param float
	 * @throws IOException
	 */
	private static void writeFloat(OutputStream os, float f) throws IOException {
		writeInt(os, Float.floatToIntBits(f));
	}

	/**
	 * Writes a integer to output stream
	 * @param output stream
	 * @param string
	 * @throws IOException
	 */
	private static void writeString(OutputStream os, String s) throws IOException {
		writeInt(os, s.length());
		for (int i = 0; i < s.length(); i++) {
			writeByte(os, (byte)s.charAt(i));
		}
	}

	/**
	 * Writes a integer to output stream
	 * @param output stream
	 * @param float array
	 * @throws IOException
	 */
	private static void writeFloatArray(OutputStream os, float[] f) throws IOException {
		writeInt(os, f.length);
		for (int i = 0; i < f.length; i++) {
			writeFloat(os, f[i]);
		}
	}

	/**
	 * Write material 
	 * @param output stream
	 * @param material
	 * @throws IOException
	 */
	private static void writeMaterial(OutputStream os, Material m) throws IOException {
		writeString(os, m.getId());
		writeFloatArray(os, m.getAmbientColor().getArray());
		writeFloatArray(os, m.getDiffuseColor().getArray());
		writeFloatArray(os, m.getSpecularColor().getArray());
		writeFloatArray(os, m.getEmissionColor().getArray());
		writeFloat(os, m.getShininess());
		writeBoolean(os, m.hasDiffuseTextureTransparency() == true);
		writeTexture(os, m.getDiffuseTexture()); 
		writeTexture(os, m.getSpecularTexture());
		writeTexture(os, m.getNormalTexture());
		writeTexture(os, m.getDisplacementTexture());
	}

	/**
	 * Write vertices to output stream
	 * @param output stream
	 * @param vertices
	 * @throws IOException
	 */
	private static void writeVertices(OutputStream os, Vector3[] v) throws IOException {
		if (v == null) {
			writeBoolean(os, false);
		} else {
			writeBoolean(os, true);
			writeInt(os, v.length);
			for (int i = 0; i < v.length; i++) {
				writeFloatArray(os, v[i].getArray());
			}			
		}
	}

	/**
	 * Write texture coordinates to output stream
	 * @param output stream
	 * @param texture coordinates
	 * @throws IOException
	 */
	private static void writeTextureCoordinates(OutputStream os, TextureCoordinate[] tc) throws IOException {
		if (tc == null) {
			writeBoolean(os, false);
		} else {
			writeBoolean(os, true);
			writeInt(os, tc.length);
			for (int i = 0; i < tc.length; i++) {
				writeFloatArray(os, tc[i].getArray());
			}
		}
	}

	/**
	 * Write indices to output stream
	 * @param output stream
	 * @param indices
	 * @throws IOException
	 */
	private static void writeIndices(OutputStream os, int[] indices) throws IOException {
		if (indices == null) {
			writeBoolean(os, false);
		} else {
			writeBoolean(os, true);
			writeInt(os, indices.length);
			for (int i = 0; i < indices.length; i++) {
				writeInt(os, indices[i]);
			}
		}
	}

	/**
	 * Write byte buffer to output stream
	 * @param output stream
	 * @param byte buffer
	 * @throws IOException
	 */
	private static void writeByteBuffer(OutputStream os, ByteBuffer bb) throws IOException {
		writeInt(os, bb.remaining());
		while (bb.remaining() > 0) {
			writeByte(os, bb.get());
		}
	}

	/**
	 * Write texture to output stream
	 * @param output stream
	 * @param texture
	 * @throws IOException
	 */
	private static void writeTexture(OutputStream os, Texture t) throws IOException {
		if (t == null) {
			writeBoolean(os, false);
		} else {
			writeBoolean(os, true);
			writeString(os, t.getId());
			writeByte(os, (byte)t.getDepth());
			writeShort(os, (short)t.getTextureWidth());
			writeShort(os, (short)t.getTextureHeight());
			writeByteBuffer(os, t.getTextureData());
		}
	}

	/**
	 * Write animation to output stream
	 * @param output stream
	 * @param animation
	 * @throws IOException 
	 */
	private static void writeAnimation(OutputStream os, Animation a) throws IOException {
		if (a == null) {
			writeBoolean(os, false);
		} else {
			writeBoolean(os, true);
			for (int i = 0; i < a.getTransformationsMatrices().length; i++) {
				writeFloatArray(os, a.getTransformationsMatrices()[i].getArray());
			}
		}
	}

	/**
	 * Write faces entities to output stream
	 * @param output stream
	 * @param faces entities
	 * @throws IOException
	 */
	private static void writeFacesEntities(OutputStream os, FacesEntity[] facesEntities) throws IOException {
		writeInt(os, facesEntities.length);
		for (int i = 0; i < facesEntities.length; i++) {
			FacesEntity fe = facesEntities[i];
			writeString(os, fe.getId());
			if (fe.getMaterial() == null) {
				writeBoolean(os, false);
			} else {
				writeBoolean(os, true);
				writeString(os, fe.getMaterial().getId());
			}
			for (int j = 0; j < fe.getFaces().length; j++) {
				Face f = fe.getFaces()[j];
				writeIndices(os, f.getVertexIndices());
				writeIndices(os, f.getNormalIndices());
				writeIndices(os, f.getTextureCoordinateIndices());
				writeIndices(os, f.getTangentIndices());
				writeIndices(os, f.getBitangentIndices());
			}
		}
	}

	/**
	 * Write skinning joint
	 * @param output stream
	 * @param joint
	 * @throws IOException
	 */
	private static void writeSkinningJoint(OutputStream os, Joint joint) throws IOException {
		writeString(os, joint.getGroupId());
		writeFloatArray(os, joint.getBindMatrix().getArray());
	}

	/**
	 * Write skinning joint weight
	 * @param output stream
	 * @param joint
	 * @throws IOException
	 */
	private static void writeSkinningJointWeight(OutputStream os, JointWeight jointWeight) throws IOException {
		writeInt(os, jointWeight.getJointIndex());
		writeInt(os, jointWeight.getWeightIndex());
	}

	/**
	 * Write skinning to output stream
	 * @param output stream
	 * @param skinning
	 * @throws IOException
	 */
	private static void writeSkinning(OutputStream os, Skinning skinning) throws IOException {
		if (skinning == null) {
			writeBoolean(os, false);
		} else {
			writeBoolean(os, true);
			writeFloatArray(os, skinning.getWeights());
			writeInt(os, skinning.getJoints().length);
			for (int i = 0; i < skinning.getJoints().length; i++) {
				writeSkinningJoint(os, skinning.getJoints()[i]);
			}
			writeInt(os, skinning.getVerticesJointsWeights().length);
			for (int i = 0; i < skinning.getVerticesJointsWeights().length; i++) {
				writeInt(os, skinning.getVerticesJointsWeights()[i].length);
				for (int j = 0; j < skinning.getVerticesJointsWeights()[i].length; j++) {
					writeSkinningJointWeight(os, skinning.getVerticesJointsWeights()[i][j]);
				}
			}
		}
	}

	/**
	 * Write group to output stream
	 * @param output stream
	 * @param group
	 * @throws IOException
	 */
	private static void writeGroup(OutputStream os, Group g) throws IOException {
		writeString(os, g.getId());
		writeString(os, g.getName());
		writeBoolean(os, g.isJoint());
		writeFloatArray(os, g.getTransformationsMatrix().getArray());
		writeVertices(os, g.getVertices());
		writeVertices(os, g.getNormals());
		writeTextureCoordinates(os, g.getTextureCoordinates());
		writeVertices(os, g.getTangents());
		writeVertices(os, g.getBitangents());
		writeAnimation(os, g.getAnimation());
		writeSkinning(os, g.getSkinning());
		writeFacesEntities(os, g.getFacesEntities());
		writeInt(os, g.getSubGroups().size());
		for (Group subGroup: g.getSubGroups().getValuesIterator()) {
			writeGroup(os, subGroup);
		}
	}

}
