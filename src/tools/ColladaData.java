package sekelsta.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import sekelsta.engine.render.Bone;
import shadowfox.math.Matrix4f;
import shadowfox.math.Vector2f;
import shadowfox.math.Vector3f;

public class ColladaData {

    public static class BoneExtra {
        boolean connect = false;
        float roll;
        float tip_x;
        float tip_y;
        float tip_z;
    }

    public static class ColladaJoint {
        String name;
        ArrayList<ColladaJoint> children;
        Matrix4f bindPose;
        BoneExtra extra;
    }


    public Vector3f[] positions;
    public Vector3f[] normals;
    public Vector2f[] textureCoords;
    // Triangulated
    public int[] faces;
    public int[] normalIndices;
    public int[] textureIndices;

    float[] weights;
    int[][] bones;
    int[][] weightIndices;

    String[] boneNames;
    Matrix4f[] bindPoses;

    ArrayList<ColladaJoint> armature;

    public void setPositions(float[] rawPositions) {
        this.positions = toVec3Array(rawPositions);
    }

    public void setNormals(float[] rawNormals) {
        this.normals = toVec3Array(rawNormals);
    }

    public void setTextureCoords(float[] rawTextureCoords) {
        this.textureCoords = toVec2Array(rawTextureCoords);
    }

    public ModelData getModelData() {
        ModelData obj = new ModelData();
        HashMap<Vertex, Integer> map = new HashMap<>();
        HashMap<Vertex, Integer> ref = new HashMap<>();
        for (int i = 0; i < faces.length / 3; ++i) {
            obj.faces.add(new int[3]);
        }
        for (int i = 0; i < faces.length; ++i) {
            Vertex data = new Vertex();
            data.position = positions[faces[i]];
            if (textureIndices.length > 0) {
                data.texture = textureCoords[textureIndices[i]];
            }
            if (normalIndices.length > 0) {
                data.normal = normals[normalIndices[i]];
            }

            int num = obj.getVertices().size();
            if (map.containsKey(data)) {
                num = map.get(data);
            }
            else {
                obj.getVertices().add(data);
                map.put(data, num);
                ref.put(data, faces[i]);
            }

            obj.faces.get(i / 3)[i % 3] = num;
        }

        if (weights != null) {
            setMeshWeights(obj, ref);
            setArmature(obj);
        }
        return obj;
    }

    private void setMeshWeights(ModelData modelData, HashMap<Vertex, Integer> ref) {
        assert(weights != null);
        float[][] merged = mergeWeights(weights, weightIndices);
        trimInfluence(merged, bones, ModelData.MAX_BONE_INFLUENCE);
        // Iterate over vertices, not weights, because weights are sorted by the old mapping
        for (Vertex vertex : modelData.getVertices()) {
            vertex.boneIDs = new int[ModelData.MAX_BONE_INFLUENCE];
            vertex.boneWeights = new float[ModelData.MAX_BONE_INFLUENCE];
            int index = ref.get(vertex);
            for (int i = 0; i < merged[index].length; ++i) {
                vertex.boneIDs[i] = bones[index][i];
                vertex.boneWeights[i] = merged[index][i];
            }
            for (int i = merged[index].length; i < ModelData.MAX_BONE_INFLUENCE; ++i) {
                vertex.boneIDs[i] = -1;
            }
        }
    }

    private void setArmature(ModelData modelData) {
        modelData.bones = parseChildBones(armature);
    }

    private Bone[] parseChildBones(ArrayList<ColladaJoint> bones) {
        Bone[] parsed = new Bone[bones.size()];
        for (int child = 0; child < bones.size(); ++child) {
            String name = bones.get(child).name.replace('.', '_');
            for (int id = 0; id < boneNames.length; ++id) {
                if (!boneNames[id].equals(name)) {
                    continue;
                }
                Bone[] children = null;
                if (bones.get(child).children != null && bones.get(child).children.size() > 0) {
                    children = parseChildBones(bones.get(child).children);
                }
                Matrix4f bind = bones.get(child).bindPose;
                Matrix4f inverseBind = Matrix4f.invert(bind, new Matrix4f());
                parsed[child] = new Bone(name, id, children, bind, inverseBind);
            }
            assert(parsed[child] != null);
        }
        return parsed;
    }

    private static Vector3f[] toVec3Array(float[] array) {
        Vector3f[] vectors = new Vector3f[array.length / 3];
        for (int i = 0; i < vectors.length; ++i) {
            vectors[i] = new Vector3f(array[3 * i], array[3 * i + 1], array[3 * i + 2]);
        }
        return vectors;
    }

    private static Vector2f[] toVec2Array(float[] array) {
        Vector2f[] vectors = new Vector2f[array.length / 2];
        for (int i = 0; i < vectors.length; ++i) {
            vectors[i] = new Vector2f(array[2 * i], array[2 * i + 1]);
        }
        return vectors;
    }

    private static float[][] mergeWeights(float[] weights, int[][] weightIndices) {
        assert(weights != null);
        float[][] merged = new float[weightIndices.length][];
        for (int i = 0; i < weightIndices.length; ++i) {
            merged[i] = new float[weightIndices[i].length];
            for (int j = 0; j < weightIndices[i].length; ++j) {
                merged[i][j] = weights[weightIndices[i][j]];
            }
        }
        return merged;
    }

    private static void trimInfluence(float[][] weights, int[][] bones, int maxLength) {
        for (int i = 0; i < weights.length; ++i) {
            if (weights[i].length <= maxLength) {
                continue;
            }

            float[] shortened = new float[maxLength];
            int[] shortBones = new int[maxLength];
            for (int j = 0; j < maxLength; ++j) {
                shortBones[j] = -1;
            }
            float prevSum = 0;
            for (int w = 0; w < weights[i].length; ++w) {
                prevSum += weights[i][w];
                for (int j = 0; j < maxLength; ++j) {
                    if (weights[i][w] > shortened[j]) {
                        shortened[j] = weights[i][w];
                        shortBones[j] = bones[i][w];
                    }
                }
            }
            if (prevSum < 0.99f || prevSum > 1.01f) {
                throw new RuntimeException("Expected bone weights to sum to 1");
            }
            weights[i] = shortened;
            bones[i] = shortBones;

            float sum = 0;
            for (int j = 0; j < maxLength; ++j) {
                sum += weights[i][j];
            }
            for (int j = 0; j < maxLength; ++j) {
                weights[i][j] /= sum;
            }
        }
    }
}
