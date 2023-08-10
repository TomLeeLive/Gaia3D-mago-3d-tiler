package basic.exchangable;

import de.javagl.jgltf.model.GltfConstants;
import basic.types.AccessorType;
import basic.types.AttributeType;
import util.io.LittleEndianDataInputStream;
import util.io.LittleEndianDataOutputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;

/**
 * GaiaBuffer represents a buffer by attribute, which is a convenient form to convert to gltf.
 * @auther znkim
 * @since 1.0.0
 * @see AttributeType, AccessorType, GaiaBufferDataSet
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GaiaBuffer {
    AttributeType attributeType;
    AccessorType accessorType;

    int elementsCount = -1;

    byte glDimension;
    int glType;
    int glTarget;

    float[] floats;
    int[] ints;
    short[] shorts;
    byte[] bytes;

    public void writeBuffer(LittleEndianDataOutputStream stream) throws IOException {
        stream.writeByte(glDimension);
        stream.writeInt(glType);
        stream.writeInt(glTarget);
        stream.writeInt(elementsCount);
        if (glType == GltfConstants.GL_FLOAT) {
            stream.writeInt(floats.length);
            stream.writeFloats(floats);
        } else if (glType == GltfConstants.GL_INT) {
            stream.writeInt(ints.length);
            stream.writeInts(ints);
        } else if (glType == GltfConstants.GL_SHORT || glType == GltfConstants.GL_UNSIGNED_SHORT) {
            stream.writeInt(shorts.length);
            stream.writeShorts(shorts);
        } else if (glType == GltfConstants.GL_BYTE || glType == GltfConstants.GL_UNSIGNED_BYTE) {
            stream.writeInt(bytes.length);
            stream.write(bytes);
        }
    }

    public void readBuffer(LittleEndianDataInputStream stream) throws IOException {
        glDimension = stream.readByte();
        glType = stream.readInt();
        glTarget = stream.readInt();
        elementsCount = stream.readInt();
        if (glType == GltfConstants.GL_FLOAT) {
            int length = stream.readInt();
            floats = stream.readFloats(length);
        } else if (glType == GltfConstants.GL_INT) {
            int length = stream.readInt();
            ints = stream.readInts(length);
        } else if (glType == GltfConstants.GL_SHORT || glType == GltfConstants.GL_UNSIGNED_SHORT) {
            int length = stream.readInt();
            shorts = stream.readShorts(length);
        } else if (glType == GltfConstants.GL_BYTE || glType == GltfConstants.GL_UNSIGNED_BYTE) {
            int length = stream.readInt();
            bytes = stream.readBytes(length);
        }
    }
}
