package com.gaia3d.process.tileprocess.tile;

import com.gaia3d.basic.geometry.GaiaBoundingBox;
import com.gaia3d.converter.kml.KmlInfo;
import com.gaia3d.process.tileprocess.tile.tileset.asset.*;
import com.gaia3d.process.tileprocess.tile.tileset.node.Node;
import com.gaia3d.util.GlobeUtils;
import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class DefaultTiler {

    protected double calcGeometricError(List<TileInfo> tileInfos) {
        return tileInfos.stream().mapToDouble(tileInfo -> {
            GaiaBoundingBox boundingBox = tileInfo.getBoundingBox();
            double result = boundingBox.getLongestDistance();
            if (result > 1000.0d) {
                log.warn("[Warn]{} is too long distance. check it please. (GeometricError)", result);
            }
            return result;
        }).max().orElse(0.0d);
    }

    /*protected GaiaBoundingBox calcRootBoundingBox(List<TileInfo> tileInfos) {
        GaiaBoundingBox boundingBox = new GaiaBoundingBox();
        tileInfos.forEach(tileInfo -> {
            KmlInfo kmlInfo = tileInfo.getKmlInfo();
            Vector3d position = kmlInfo.getPosition();
            GaiaBoundingBox localBoundingBox = tileInfo.getBoundingBox();
            // rotate
            localBoundingBox = localBoundingBox.convertLocalToLonlatBoundingBox(position);
            boundingBox.addBoundingBox(localBoundingBox);
        });

        // make square bounding box
        double xLength = boundingBox.getMaxX() - boundingBox.getMinX();
        double yLength = boundingBox.getMaxY() - boundingBox.getMinY();
        double offset = Math.abs(xLength - yLength);

        if (xLength > yLength) {
            boundingBox.setMaxY(boundingBox.getMaxY() + offset);
        } else {
            boundingBox.setMaxX(boundingBox.getMaxX() + offset);
        }
        return boundingBox;
    }*/

    protected GaiaBoundingBox calcBoundingBox(List<TileInfo> tileInfos) {
        GaiaBoundingBox boundingBox = new GaiaBoundingBox();
        tileInfos.forEach(tileInfo -> {
            KmlInfo kmlInfo = tileInfo.getKmlInfo();
            Vector3d position = kmlInfo.getPosition();
            GaiaBoundingBox localBoundingBox = tileInfo.getBoundingBox();
            // rotate
            localBoundingBox = localBoundingBox.convertLocalToLonlatBoundingBox(position);
            boundingBox.addBoundingBox(localBoundingBox);
        });

        // make square bounding box
//        double xLength = boundingBox.getMaxX() - boundingBox.getMinX();
//        double yLength = boundingBox.getMaxY() - boundingBox.getMinY();
//        double offset = Math.abs(xLength - yLength);
//        if (xLength > yLength) {
//            boundingBox.setMaxY(boundingBox.getMaxY() + offset);
//        } else {
//            boundingBox.setMaxX(boundingBox.getMaxX() + offset);
//        }
        return boundingBox;
    }

    protected void rotateX90(Matrix4d matrix) {
        Matrix4d rotationMatrix = new Matrix4d();
        rotationMatrix.identity();
        rotationMatrix.rotateX(Math.toRadians(-90));
        matrix.mul(rotationMatrix, matrix);
    }

    protected Matrix4d getTransformMatrix(GaiaBoundingBox boundingBox) {
        Vector3d center = boundingBox.getCenter();
        double[] cartesian = GlobeUtils.geographicToCartesianWgs84(center.x, center.y, center.z);
        return GlobeUtils.transformMatrixAtCartesianPointWgs84(cartesian[0], cartesian[1], cartesian[2]);
    }

    protected Asset createAsset() {
        Asset asset = new Asset();
        Extras extras = new Extras();
        Cesium cesium = new Cesium();
        Ion ion = new Ion();
        List<Credit> credits = new ArrayList<>();
        Credit credit = new Credit();
        credit.setHtml("<html>Gaia3D</html>");
        credits.add(credit);
        cesium.setCredits(credits);
        extras.setIon(ion);
        extras.setCesium(cesium);
        asset.setExtras(extras);
        return asset;
    }

    protected Node createRoot() {
        Node root = new Node();
        root.setParent(root);
        root.setNodeCode("R");
        root.setRefine(Node.RefineType.REPLACE);
        root.setChildren(new ArrayList<>());
        return root;
    }
}