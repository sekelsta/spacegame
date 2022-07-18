package sekelsta.engine.render.mesh;

import java.util.*;

import sekelsta.engine.render.Bone;
import sekelsta.math.Vector2f;
import sekelsta.math.Vector3f;

public class ModelData {
    // Maximum number of bones that can influence the same vertex
    public static final int MAX_BONE_INFLUENCE = 4;

    private HashMap<VertexData, Integer> vertexMap = new HashMap<>();
    public List<VertexData> vertices = new ArrayList<>();
    public List<int[]> faces = new ArrayList<>();

    public Bone[] bones;

    public static class VertexData {
        public Vector3f vertex;
        public Vector3f normal;
        public Vector2f texture;
        public int boneIDs[];
        public float boneWeights[];

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o == this) {
                return true;
            }
            if (!(o instanceof VertexData)) {
                return false;
            }
            VertexData other = (VertexData)o;
            return ((this.vertex == null && other.vertex == null)
                        || (this.vertex != null && this.vertex.equals(other.vertex)))
                    && ((this.normal == null && other.normal == null)
                        || (this.normal != null && this.normal.equals(other.normal)))
                    && ((this.texture == null && other.texture == null)
                        || (this.texture != null && this.texture.equals(other.texture)));
        }

        @Override
        public int hashCode() {
            return Objects.hash(vertex, normal, texture);
        }

        @Override
        public String toString() {
            return "Vertex " + vertex + " normal " + normal + " texture " + texture;
        }
    }

    public void calcNormals() {
        // Initialize all normals
        for (VertexData data : vertices) {
            data.normal = new Vector3f(0, 0, 0);
        }
        // For each face, calculate surface normal and add to each vertex
        for (int[] face : faces) {
            Vector3f a = Vector3f.subtract(vertices.get(face[1]).vertex, vertices.get(face[0]).vertex, new Vector3f());
            Vector3f b = Vector3f.subtract(vertices.get(face[2]).vertex, vertices.get(face[1]).vertex, new Vector3f());
            a.normalize();
            b.normalize();
            Vector3f normal = Vector3f.cross(a, b, new Vector3f());
            // Weight by angle
            Vector3f c = Vector3f.subtract(vertices.get(face[2]).vertex, vertices.get(face[0]).vertex, new Vector3f());
            c.normalize();
            float w0 = (float)Math.acos(a.dot(c));
            a.negate();
            float w1 = (float)Math.acos(b.dot(a));
            float w2 = (float)Math.acos(c.dot(b));
            vertices.get(face[0]).normal.addWeighted(normal, w0);
            vertices.get(face[1]).normal.addWeighted(normal, w1);
            vertices.get(face[2]).normal.addWeighted(normal, w2);
        }
        // Normalize all normals
        for (VertexData data : vertices) {
            data.normal.normalize();
        }
    }

    private int addVertex(VertexData v) {
        Integer i = vertexMap.get(v);
        if (i != null) {
            return i;
        }
        int l = vertices.size();
        vertexMap.put(v, l);
        vertices.add(v);
        return l;
    }

    // TODO: fix API, shouldn't allow people to modify vertices/faces lists directly AND use this,
    // since it may not work as expected.
    // Should only do one or the other
    public void addTriangle(VertexData v0, VertexData v1, VertexData v2) {
        int[] face = new int[3];
        face[0] = addVertex(v0);
        face[1] = addVertex(v1);
        face[2] = addVertex(v2);
        faces.add(face);
    }

    private void print() {
        System.out.println("Obj " + this.toString());
        System.out.println("Vertices: " + vertices.size());
        for (VertexData vertex : vertices) {
            System.out.println(vertex);
        }

        System.out.println("Faces: " + faces.size());
        for (int[] face : faces) {
            String s = "";
            for (int i = 0; i < face.length; ++i) {
                s += face[i] + " ";
            }
            System.out.println(s);
        }
    }
}
