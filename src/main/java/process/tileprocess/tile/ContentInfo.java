package process.tileprocess.tile;

import basic.exchangable.GaiaSet;
import basic.geometry.GaiaBoundingBox;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ContentInfo {
    private String name;
    private String nodeCode;
    private LevelOfDetail lod;
    private List<TileInfo> tileInfos;
    //private GaiaUniverse universe;
    private GaiaBoundingBox boundingBox;
    private GaiaSet batchedSet;

    public void deleteTexture() {
        for (TileInfo tileInfo : tileInfos) {
            GaiaSet set = tileInfo.getSet();
            set.deleteTextures();
        }
    }

    public void clear() {
        this.name = null;
        this.boundingBox = null;
        this.batchedSet = null;
        this.tileInfos.forEach(TileInfo::clear);
        this.tileInfos = null;
    }
}
