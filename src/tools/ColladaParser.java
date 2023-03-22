package sekelsta.tools;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sekelsta.engine.Pair;
import shadowfox.math.Matrix4f;
import shadowfox.math.Vector2f;
import shadowfox.math.Vector3f;
import sekelsta.tools.ColladaData.ColladaJoint;

public class ColladaParser {
    private static final String IDENTITY = "1 0 0 0 0 1 0 0 0 0 1 0 0 0 0 1";

    public static ModelData parse(InputStream input) {
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        Node collada = document.getFirstChild();
        if (collada == null || !collada.getNodeName().equals("COLLADA")) {
            throw new RuntimeException("Expected collada node");
        }

        NodeList children = collada.getChildNodes();
        Node asset = null;
        Node geometry = null;
        Node controller = null;
        Node scene = null;
        for (int i = 0; i < children.getLength(); ++i) {
            Node node = children.item(i);
            String name = node.getNodeName();
            if (name.equals("asset")) {
                asset = node;
            }
            else if (name.equals("library_geometries")) {
                geometry = node;
            }
            else if (name.equals("library_controllers")) {
                controller = node;
            }
            else if (name.equals("library_visual_scenes")) {
                scene = node;
            }
        }

        parseAsset(asset);
        ColladaData colladaData = new ColladaData();
        parseGeometry(geometry, colladaData);
        parseController(controller, colladaData);
        parseScene(scene, colladaData);

        return colladaData.getModelData();
    }

    private static void parseAsset(Node asset) {
        String axis = findChild(asset, "up_axis").getTextContent();
        if (!axis.equals("Z_UP")) {
            throw new RuntimeException("Expected Z to be up");
        }
    }

    private static void parseGeometry(Node geometryLibrary, ColladaData collada) {
        if (geometryLibrary == null || !geometryLibrary.getNodeName().equals("library_geometries")) {
            throw new RuntimeException("Invalid argument, expected \"library_geometries\", got: \"" 
                + geometryLibrary.getNodeName() + "\"");
        }
        // Assume there is exactly one geometry for now
        Node geometry = getSingleNonTextChild(geometryLibrary, "geometry");
        Node mesh = getSingleNonTextChild(geometry, "mesh");

        NodeList meshNodes = mesh.getChildNodes();
        for (int i = 0; i < meshNodes.getLength(); ++i) {
            Node node = meshNodes.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                continue;
            }

            if (node.getNodeName().equals("source")) {
                Node array = findChild(node, "float_array");
                Pair<String, float[]> pair = floatArray(array);
                String id = pair.getKey();
                if (id.endsWith("-positions-array")) {
                    collada.setPositions(pair.getValue());
                }
                else if (id.endsWith("-normals-array")) {
                    collada.setNormals(pair.getValue());
                }
                else if (id.endsWith("-map-0-array")) {
                    collada.setTextureCoords(pair.getValue());
                }
            }
            else if (node.getNodeName().equals("triangles")) {
                parseTriangles(node, collada);
            }
        }
    }

    private static void parseController(Node controllerLibrary, ColladaData collada) {
        Node controller = getSingleNonTextChild(controllerLibrary, "controller");
        Node skin = getSingleNonTextChild(controller, "skin");

        NodeList children = skin.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                continue;
            }

            if (node.getNodeName().equals("bind_shape_matrix")) {
                if (!node.getTextContent().equals(IDENTITY)) {
                    throw new RuntimeException("Object controller: Bind matrix not supported. Use Ctrl+A to apply transforms in blender before exporting.");
                }
            }
            else if (node.getNodeName().equals("source")) {
                Node array = firstNonTextChild(node);
                if (array.getNodeName().equals("float_array")) {
                    Pair<String, float[]> pair = floatArray(array);
                    if (pair.getKey().endsWith("-skin-weights-array")) {
                        collada.weights = pair.getValue();
                    }
                    else if (pair.getKey().endsWith("-skin-bind_poses-array")) {
                        int numMatrices = getCount(array) / 16;
                        collada.bindPoses = new Matrix4f[numMatrices];
                        StringTokenizer tokens = new StringTokenizer(array.getTextContent());
                        for (int m = 0; m < numMatrices; ++m) {
                            collada.bindPoses[m] = new Matrix4f().parseRowMajor(tokens);
                        }
                    }
                }
                else if (array.getNodeName().equals("Name_array")) {
                    int count = getCount(array);
                    collada.boneNames = new String[count];
                    StringTokenizer tokens = new StringTokenizer(array.getTextContent());
                    for (int j = 0; j < count; ++j) {
                        collada.boneNames[j] = tokens.nextToken();
                        // Parser expects no duplicate names
                        for (int k = 0; k < j; ++k) {
                            assert(j == k || !collada.boneNames[j].equals(collada.boneNames[k]));
                        }
                    }
                }
            }
            else if (node.getNodeName().equals("vertex_weights")) {
                int count = getCount(node);
                collada.bones = new int[count][];
                collada.weightIndices = new int[count][];
                StringTokenizer countTokens = new StringTokenizer(findChild(node, "vcount").getTextContent());
                StringTokenizer vTokens = new StringTokenizer(findChild(node, "v").getTextContent());
                for (int j = 0; j < count; ++j) {
                    int influences = Integer.parseInt(countTokens.nextToken());
                    collada.bones[j] = new int[influences];
                    collada.weightIndices[j] = new int[influences];
                    for (int k = 0; k < influences; ++k) {
                        collada.bones[j][k] = Integer.parseInt(vTokens.nextToken());
                        collada.weightIndices[j][k] = Integer.parseInt(vTokens.nextToken());
                    }
                }
            }
        }
    }

    private static void parseScene(Node sceneLibrary, ColladaData collada) {
        Node scene = getSingleNonTextChild(sceneLibrary, "visual_scene");
        Node armature = findChildByID(scene, "Armature");
        Node armatureMatrix = findChild(armature, "matrix");
        if (!armatureMatrix.getTextContent().equals(IDENTITY)) {
            throw new RuntimeException("Armature matrix: Bind matrix not supported. Use Ctrl+A to apply transforms in blender before exporting.");
        }
        collada.armature = getChildJoints(armature);
        // Don't bother parsing camera, lights, or mesh
    }

    private static ArrayList<ColladaJoint> getChildJoints(Node node) {
        assert(node != null);
        if (!node.hasChildNodes()) {
            return null;
        }
        ArrayList<ColladaJoint> joints = new ArrayList<>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (!child.hasAttributes()) {
                continue;
            }
            NamedNodeMap attributes = child.getAttributes();
            Node typeNode = attributes.getNamedItem("type");
            if (typeNode == null) {
                continue;
            }
            String type = typeNode.getNodeValue();
            if (!type.equals("JOINT")) {
                continue;
            }
            Node matrix = findChild(child, "matrix");

            ColladaJoint joint = new ColladaJoint();
            joint.name = attributes.getNamedItem("name").getNodeValue();
            joint.children = getChildJoints(child);
            joint.bindPose = new Matrix4f();
            joint.bindPose.parseRowMajor(matrix.getTextContent());
            joints.add(joint);
        }
        return joints;
    }

    private static Node getSingleNonTextChild(Node node, String expectedName) {
        NodeList children = node.getChildNodes();
        Node result = null;
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                continue;
            }
            if (result != null) {
                throw new RuntimeException("Expected only one child");
            }
            if (!child.getNodeName().equals(expectedName)) {
                throw new RuntimeException("Expected " + expectedName + ", got: " + child.getNodeName());
            }
            result = child;
        }
        return result;
    }

    private static Node findChild(Node node, String name) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (child.getNodeName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    private static Node firstNonTextChild(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.TEXT_NODE) {
                return child;
            }
        }
        return null;
    }

    private static Node findChildByID(Node node, String id) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (!child.hasAttributes()) {
                continue;
            }
            NamedNodeMap attributes = child.getAttributes();
            Node idNode = attributes.getNamedItem("id");
            if (idNode == null) {
                continue;
            }
            if (id.equals(idNode.getNodeValue())) {
                return child;
            }
        }
        return null;
    }

    private static Pair<String, float[]> floatArray(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        String id = attributes.getNamedItem("id").getNodeValue();
        int count = getCount(node);

        float[] array = new float[count];
        StringTokenizer tokenizer = new StringTokenizer(node.getTextContent());

        for (int i = 0; i < count; ++i) {
            array[i] = Float.parseFloat(tokenizer.nextToken());
        }

        return new Pair<String, float[]>(id, array);
    }

    private static int getCount(Node node) {
        return Integer.parseInt(node.getAttributes().getNamedItem("count").getNodeValue());
    }

    private static void parseTriangles(Node node, ColladaData collada) {
        int count = getCount(node) * 3;

        int[][] array = new int[3][];
        // Positions
        array[0] = new int[count];
        // Normals
        array[1] = new int[count];
        // Texture coordinates
        array[2] = new int[count];
        StringTokenizer tokenizer = new StringTokenizer(node.getTextContent());

        for (int i = 0; i < count; ++i) {
            for (int j = 0; j < 3; ++j) {
                array[j][i] = Integer.parseInt(tokenizer.nextToken());
            }
        }

        collada.faces = array[0];
        collada.normalIndices = array[1];
        collada.textureIndices = array[2];
    }
}
