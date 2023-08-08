package process.tileprocess.tile;

import lombok.Getter;

@Getter
public enum LevelOfDetail {
    NONE(-1,0, 1.0f, new float[]{1.0f, 1.0f, 1.0f}),
    LOD0(0,0, 1.0f, new float[]{1.0f, 0.0f, 0.0f}),
    LOD1(1, 2, 0.5f, new float[]{1.0f, 1.0f, 0.0f}),
    LOD2(2, 4, 0.25f, new float[]{0.25f, 1.0f, 0.5f}),
    LOD3(3, 8, 0.125f, new float[]{0.0f, 1.0f, 1.0f}),
    LOD4(4, 16, 0.0625f, new float[]{0.25f, 0.25f, 0.0f}),
    LOD5(5, 32, 0.03125f, new float[]{0.0f, 0.25f, 0.25f}),
    LOD6(6, 64, 0.01625f, new float[]{0.25f, 0.25f, 0.25f});

    final int level;
    final int geometricError;
    final float textureScale;
    final float[] debugColor;

    LevelOfDetail(int level, int geometricError, float textureScale, float[] debugColor) {
        this.level = level;
        this.geometricError = geometricError;
        this.textureScale = textureScale;
        this.debugColor = debugColor;
    }

    public static LevelOfDetail getByLevel(int level) {
        for (LevelOfDetail lod : LevelOfDetail.values()) {
            if (lod.getLevel() == level) {
                return lod;
            }
        }
        return NONE;
    }
}
