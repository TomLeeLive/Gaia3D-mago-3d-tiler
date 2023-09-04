package com.gaia3d.converter.geometry.shape;

import com.gaia3d.basic.geometry.GaiaBoundingBox;
import com.gaia3d.basic.structure.GaiaMaterial;
import com.gaia3d.basic.structure.GaiaNode;
import com.gaia3d.basic.structure.GaiaScene;
import com.gaia3d.converter.Converter;
import com.gaia3d.converter.geometry.*;
import com.gaia3d.process.ProcessOptions;
import com.gaia3d.util.GlobeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.geotools.data.DataStore;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.util.factory.Hints;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ShapeConverter extends AbstractGeometryConverter implements Converter {
    private final CommandLine command;
    private final CoordinateReferenceSystem crs;

    public ShapeConverter(CommandLine command, CoordinateReferenceSystem crs) {
        this.command = command;
        //this.crs = crs;

        //CRSFactory factory = new CRSFactory();
        //crs = factory.createFromParameters("EPSG:5174", "+proj=tmerc +lat_0=38 +lon_0=127.0028902777778 +k=1 +x_0=200000 +y_0=500000 +ellps=bessel +units=m +no_defs +towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43");
        this.crs = crs;
    }

    @Override
    public List<GaiaScene> load(String path) {
        return convert(new File(path));
    }

    @Override
    public List<GaiaScene> load(File file) {
        return convert(file);
    }

    @Override
    public List<GaiaScene> load(Path path) {
        return convert(path.toFile());
    }

    protected List<GaiaScene> convert(File file) {
        List<GaiaScene> scenes = new ArrayList<>();
        Tessellator tessellator = new Tessellator();
        Extruder extruder = new Extruder(tessellator);

        boolean flipCoordnate = this.command.hasOption(ProcessOptions.Flip_Coordinate.getArgName());

        ShpFiles shpFiles = null;
        try {
            shpFiles = new ShpFiles(file);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        try (ShapefileReader reader = new ShapefileReader(shpFiles, true, true, new GeometryFactory())) {
            //ShapefileHeader header = reader.getHeader();
            DataStore dataStore = new ShapefileDataStore(file.toURI().toURL());
            String typeName = dataStore.getTypeNames()[0];
            SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
            //SimpleFeatureType schema = source.getSchema();
            //ShapefileDataStore shapeFileDatastore = (ShapefileDataStore) dataStore;

            source = dataStore.getFeatureSource(typeName);
            //schema = source.getSchema();

            var query = new Query(typeName, Filter.INCLUDE);
            query.getHints().add(new Hints(Hints.FEATURE_2D, true)); // for 3d feature
            SimpleFeatureCollection features = source.getFeatures(query);

            //SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(schema);

            FeatureIterator<SimpleFeature> iterator = features.features();

            List<GaiaBuilding> buildings = new ArrayList<>();
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                //geom = geom.getEnvelope();

                GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
                Coordinate[] coordinates = geom.getCoordinates();

                GaiaBoundingBox boundingBox = new GaiaBoundingBox();
                List<Vector3d> positions = new ArrayList<>();

                Vector3d firstPosition = null;
                for (Coordinate coordinate : coordinates) {
                    Point point = geometryFactory.createPoint(coordinate);

                    double x, y;
                    if (flipCoordnate) {
                        x = point.getY();
                        y = point.getX();
                    } else {
                        x = point.getX();
                        y = point.getY();
                    }

                    Vector3d position;
                    if (crs != null) {
                        ProjCoordinate projCoordinate = new ProjCoordinate(x, y, boundingBox.getMinZ());
                        ProjCoordinate centerWgs84 = GlobeUtils.transform(crs, projCoordinate);
                        position = new Vector3d(centerWgs84.x, centerWgs84.y, 0.0d);
                    } else {
                        position = new Vector3d(x, y, 0.0d);
                    }

                    if (firstPosition == null) {
                        firstPosition = position;
                    } else if (firstPosition.equals(position)) {
                        break;
                    }

                    positions.add(position);
                    boundingBox.addPoint(position);
                }

                double height = 0;
                int floor = 0;
                Object floorAttributeObject = feature.getAttribute("GRND_FLR");
                if (floorAttributeObject != null) {
                    floor = (int) floorAttributeObject;
                    //String floorAttribute = (String) floorAttributeObject;
                    //floor = Double.parseDouble(floorAttribute);
                }
                Object heightAttributeObject = feature.getAttribute("HEIGHT");
                if (heightAttributeObject != null) {
                    height = (int) heightAttributeObject;
                    //String heightAttribute = (String) heightAttributeObject;
                    //height = Double.parseDouble(heightAttribute);
                }

                if (height == 0 && floor > 1) {
                    height = floor * 3.0d;
                }

                if (height == 0) {
                    height = 3.0d;
                }


                /*String heightAttribute = (String) feature.getAttribute("height");
                int heightUppercaseAttribute = 0;

                Object heightUppercaseAttributeObject = feature.getAttribute("HEIGHT");
                if (heightUppercaseAttributeObject != null) {
                    heightUppercaseAttribute = (int) feature.getAttribute("HEIGHT");
                }



                double height = 0;
                if (heightAttribute != null && !heightAttribute.isEmpty()) {
                    height = Double.parseDouble(heightAttribute);
                } else if (heightUppercaseAttribute != 0) {
                    height = heightUppercaseAttribute;
                }

                if (height == 0) {
                    height = 1.0d;
                }*/

                GaiaBuilding building = GaiaBuilding.builder().id(feature.getID()).name("test").boundingBox(boundingBox).floorHeight(0).roofHeight(height).positions(positions).build();
                buildings.add(building);
            }

            for (GaiaBuilding building : buildings) {
                GaiaScene scene = initScene();
                scene.setOriginalPath(file.toPath());

                GaiaMaterial material = scene.getMaterials().get(0);
                GaiaNode rootNode = scene.getNodes().get(0);
                rootNode.setName(building.getName());

                Vector3d center = building.getBoundingBox().getCenter();

                Vector3d centerWorldCoordinate = GlobeUtils.geographicToCartesianWgs84(center);
                Matrix4d transformMatrix = GlobeUtils.normalAtCartesianPointWgs84(centerWorldCoordinate);
                Matrix4d transfromMatrixInv = new Matrix4d(transformMatrix).invert();

                List<Vector3d> localPositions = new ArrayList<>();
                for (Vector3d position : building.getPositions()) {
                    Vector3d positionWorldCoordinate = GlobeUtils.geographicToCartesianWgs84(position);
                    Vector3d localPosition = positionWorldCoordinate.mulPosition(transfromMatrixInv);
                    localPosition.z = 0.0d;
                    localPositions.add(localPosition);
                }

                Extrusion extrusion = extruder.extrude(localPositions, building.getRoofHeight(), building.getFloorHeight());
                GaiaNode node = createNode(material, extrusion.getPositions(), extrusion.getTriangles());
                rootNode.getChildren().add(node);

                Matrix4d rootTransformMatrix = new Matrix4d().identity();
                rootTransformMatrix.translate(center, rootTransformMatrix);
                rootNode.setTransformMatrix(rootTransformMatrix);
                scenes.add(scene);
            }

            dataStore.dispose();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        shpFiles.dispose();
        return scenes;
    }
}
