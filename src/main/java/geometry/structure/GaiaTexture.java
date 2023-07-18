package geometry.structure;

import geometry.types.TextureType;
import io.LittleEndianDataInputStream;
import io.LittleEndianDataOutputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import tiler.LevelOfDetail;
import util.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.Arrays;

import static org.lwjgl.system.MemoryStack.stackPush;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GaiaTexture {
    Path parentPath;
    private String name;
    private String path;
    private TextureType type;

    private int width;
    private int height;
    private int format;

    private int byteLength;
    private BufferedImage bufferedImage;
    private ByteBuffer byteBuffer;

    private int textureId = -1;

    public void loadImage() {
        Path diffusePath = new File(path).toPath();
        String imagePath = parentPath + File.separator + diffusePath;
        BufferedImage bufferedImage = ImageUtils.readImage(imagePath);
        this.bufferedImage = bufferedImage;
        this.width = bufferedImage.getWidth();
        this.height = bufferedImage.getHeight();
        assert bufferedImage != null;
    }

    public void loadImage(float scaleFactor) {
        loadImage();
        int resizeWidth = (int) (this.bufferedImage.getWidth() * scaleFactor);
        int resizeHeight = (int) (this.bufferedImage.getHeight() * scaleFactor);
        resizeWidth = ImageUtils.getNearestPowerOfTwo(resizeWidth);
        resizeHeight = ImageUtils.getNearestPowerOfTwo(resizeHeight);
        this.width = resizeWidth;
        this.height = resizeHeight;
        this.bufferedImage = ImageUtils.resizeImageGraphic2D(this.bufferedImage, resizeWidth, resizeHeight);
    }

    // getBufferedImage
    public BufferedImage getBufferedImage() {
        if (this.bufferedImage == null) {
            loadImage();
        }
        return this.bufferedImage;
    }

    // getBufferedImage
    public BufferedImage getBufferedImage(float scaleFactor) {
        if (this.bufferedImage == null) {
            loadImage(scaleFactor);
        }
        return this.bufferedImage;
    }

    public void deleteObjects()
    {
        //if (textureId != -1) {
        //    GL20.glDeleteTextures(textureId);
        //}

        if (byteBuffer != null) {
            byteBuffer.clear();
        }

        bufferedImage = null;

    }

    public boolean isEqualTexture(GaiaTexture compareTexture) {
        BufferedImage bufferedImage = this.getBufferedImage();
        BufferedImage comparebufferedImage = compareTexture.getBufferedImage();

        if (this.getWidth() != compareTexture.getWidth()) {
            return false;
        }
        if (this.getHeight() != compareTexture.getHeight()) {
            return false;
        }
        if (this.getFormat() != compareTexture.getFormat()) {
            return false;
        }

        // now, compare the pixels
        int width = this.getWidth();
        int height = this.getHeight();

        byte[] rgbaByteArray = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        byte[] rgbaByteArray2 = ((DataBufferByte) comparebufferedImage.getRaster().getDataBuffer()).getData();

        boolean areEqual = Arrays.equals(rgbaByteArray, rgbaByteArray2);

        return areEqual;
    }

    public boolean isEqualTexture(GaiaTexture compareTexture, LevelOfDetail levelOfDetail) {
        float scaleFactor = levelOfDetail.getTextureScale();
        BufferedImage bufferedImage = this.getBufferedImage(scaleFactor);
        BufferedImage comparebufferedImage = compareTexture.getBufferedImage(scaleFactor);
        return isEqualTexture(compareTexture);
    }

    /*public static boolean areEqualTextures(GaiaTexture textureA, GaiaTexture textureB) throws IOException {
        if (textureA == null || textureB == null) {
            return false;
        }

        if(textureA == textureB) {
            return true;
        }

        BufferedImage bufferedImageA = textureA.getBufferedImage();
        BufferedImage bufferedImageB = textureB.getBufferedImage();

        if (textureA.getWidth() != textureB.getWidth()) {
            return false;
        }
        if (textureA.getHeight() != textureB.getHeight()) {
            return false;
        }
        if (textureA.getFormat() != textureB.getFormat()) {
            return false;
        }
        // now, compare the pixels
        int width = textureA.getWidth();
        int height = textureA.getHeight();

        byte[] rgbaByteArray = ((DataBufferByte) bufferedImageA.getRaster().getDataBuffer()).getData();
        byte[] rgbaByteArray2 = ((DataBufferByte) bufferedImageB.getRaster().getDataBuffer()).getData();

        boolean areEqual = Arrays.equals(rgbaByteArray, rgbaByteArray2);

        return areEqual;
    } */

    public void write(LittleEndianDataOutputStream stream) throws IOException {
        stream.writeText(path);
    }

    public void read(LittleEndianDataInputStream stream) throws IOException {
        this.setPath(stream.readText());
    }
}
