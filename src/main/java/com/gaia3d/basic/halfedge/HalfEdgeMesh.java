package com.gaia3d.basic.halfedge;

import com.gaia3d.basic.geometry.GaiaBoundingBox;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class HalfEdgeMesh {
    @Setter
    @Getter
    private List<HalfEdgePrimitive> primitives = new ArrayList<>();
    private GaiaBoundingBox boundingBox = null;

    public void doTrianglesReduction() {
        for (HalfEdgePrimitive primitive : primitives) {
            primitive.doTrianglesReduction();
        }
    }

    public void deleteObjects() {
        for (HalfEdgePrimitive primitive : primitives) {
            primitive.deleteObjects();
        }
        primitives.clear();
    }

    public void checkSandClockFaces() {
        for (HalfEdgePrimitive primitive : primitives) {
            primitive.checkSandClockFaces();
        }
    }

    public void transformPoints(Matrix4d finalMatrix) {
        for (HalfEdgePrimitive primitive : primitives) {
            primitive.transformPoints(finalMatrix);
        }
    }

    public void cutByPlane(PlaneType planeType, Vector3d planePosition, double error) {
        for (HalfEdgePrimitive primitive : primitives) {
            primitive.cutByPlane(planeType, planePosition, error);
        }
    }

    public GaiaBoundingBox calculateBoundingBox(GaiaBoundingBox resultBBox) {
        if(resultBBox == null) {
            resultBBox = new GaiaBoundingBox();
        }
        for (HalfEdgePrimitive primitive : primitives) {
            resultBBox = primitive.calculateBoundingBox(resultBBox);
        }
        return resultBBox;
    }

    public GaiaBoundingBox getBoundingBox() {
        if (boundingBox == null) {
            boundingBox = calculateBoundingBox(null);
        }
        return boundingBox;
    }

    public void classifyFacesIdByPlane(PlaneType planeType, Vector3d planePosition)
    {
        for (HalfEdgePrimitive primitive : primitives) {
            primitive.classifyFacesIdByPlane(planeType, planePosition);
        }
    }
}
