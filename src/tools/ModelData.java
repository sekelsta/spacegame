package sekelsta.tools;

import java.util.*;

import sekelsta.engine.render.Bone;
import shadowfox.math.Vector3f;

public class ModelData {
    // Maximum number of bones that can influence the same vertex
    public static final int MAX_BONE_INFLUENCE = 4;

    protected HashMap<Vertex, Integer> vertexMap = new HashMap<>();
    protected List<Vertex> vertices = new ArrayList<>();
    public List<int[]> faces = new ArrayList<>();
    public List<int[]> quads = new ArrayList<>();
    protected Map<Vertex, Set<Vertex>> connectivity = null;

    public Bone[] bones;

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void calcNormals() {
        // Initialize all normals
        for (Vertex vertex : vertices) {
            vertex.normal = new Vector3f(0, 0, 0);
        }
        // For each face, calculate surface normal and add to each vertex
        addPartialNormals(faces);
        addPartialNormals(quads);
        // Normalize all normals
        for (Vertex vertex : vertices) {
            vertex.normal.normalize();
        }
    }

    private void addPartialNormals(List<int[]> faces) {
        for (int[] face : faces) {
            Vector3f prev = vertices.get(face[face.length - 1]).position;
            Vector3f pos = vertices.get(face[0]).position;
            Vector3f a = Vector3f.subtract(pos, prev, new Vector3f());
            a.normalize();
            
            for (int i = 0; i < face.length; ++i) {
                Vector3f next = vertices.get(face[(i + 1) % face.length]).position;
                Vector3f b = Vector3f.subtract(next, pos, new Vector3f());
                b.normalize();
                Vector3f normal = Vector3f.cross(a, b, new Vector3f());
                // Weight by angle
                float w = (float)Math.acos(b.dot(a));
                vertices.get(face[i]).normal.addWeighted(normal, w);

                prev = pos;
                pos = next;
                a = b;
            }
        }
    }

    public void triangulate() {
        ArrayList<int[]> toAdd = new ArrayList<>();
        for (int[] face : quads) {
            // Only handles quads
            assert(face.length == 4);
            Vector3f pos0 = vertices.get(face[0]).position;
            Vector3f pos1 = vertices.get(face[1]).position;
            Vector3f pos2 = vertices.get(face[2]).position;
            Vector3f pos3 = vertices.get(face[3]).position;
            float distance02 = Vector3f.subtract(pos0, pos2, new Vector3f()).length();
            float distance13 = Vector3f.subtract(pos1, pos3, new Vector3f()).length();
            int[] tri1 = new int[3];
            int[] tri2 = new int[3];
            if (distance02 < distance13) {
                tri1[0] = face[0];
                tri1[1] = face[1];
                tri1[2] = face[2];
                tri2[0] = face[2];
                tri2[1] = face[3];
                tri2[2] = face[0];
            }
            else {
                tri1[0] = face[3];
                tri1[1] = face[0];
                tri1[2] = face[1];
                tri2[0] = face[1];
                tri2[1] = face[2];
                tri2[2] = face[3];
            }
            toAdd.add(tri1);
            toAdd.add(tri2);
        }
        faces.addAll(toAdd);
    }

    private void calculateConnectivity() {
        connectivity = new HashMap<>();
        addPartialConnectivity(faces);
        addPartialConnectivity(quads);
    }

    private void addPartialConnectivity(List<int[]> faces) {
        for (int[] face : faces) {
            int prev = face.length - 1;
            for (int f = 0; f < face.length; ++f) {
                Vertex prevVertex = vertices.get(face[prev]);
                Vertex currentVertex = vertices.get(face[f]);
                connectivity.computeIfAbsent(prevVertex, (k) -> new HashSet<Vertex>());
                connectivity.computeIfAbsent(currentVertex, (k) -> new HashSet<Vertex>());
                connectivity.get(prevVertex).add(currentVertex);
                connectivity.get(currentVertex).add(prevVertex);
                prev = f;
            }
        }
    }

    public void contract(List<Vector3f> originalLocations, float maxDistance, float weight) {
        if (connectivity == null) {
            calculateConnectivity();
        }

        List<Vector3f> newPositions = new ArrayList<>();
        for (int i = 0; i < vertices.size(); ++i) {
            Set<Vertex> connected = connectivity.get(vertices.get(i));
            Vector3f average = new Vector3f(vertices.get(i).position);
            average.scale(weight);
            for (Vertex v : connected) {
                average.add(v.position);
            }
            average.scale(1f/(connected.size() + weight));

            Vector3f diff = Vector3f.subtract(average, originalLocations.get(i), new Vector3f());
            if (diff.length() > maxDistance) {
                diff.normalize().scale(maxDistance);
                average = Vector3f.add(originalLocations.get(i), diff, average);
            }

            newPositions.add(average);
        }
        for (int i = 0; i < vertices.size(); ++i) {
            vertices.get(i).position.x = newPositions.get(i).x;
            vertices.get(i).position.y = newPositions.get(i).y;
            vertices.get(i).position.z = newPositions.get(i).z;
        }
    }

    public Vertex getNearest(Vector3f position) {
        if (vertices.size() == 0) {
            return null;
        }
        Vertex nearest = vertices.get(0);
        float best_dsq = nearest.position.distanceSquared(position);
        for (Vertex v : vertices) {
            float dsq = v.position.distanceSquared(position);
            if (dsq < best_dsq) {
                nearest = v;
                best_dsq = dsq;
            }
        }
        return nearest;
    }

    protected int addVertex(Vertex v) {
        Integer i = vertexMap.get(v);
        if (i != null) {
            return i;
        }
        int l = vertices.size();
        vertexMap.put(v, l);
        vertices.add(v);
        return l;
    }

    public void addTriangle(Vertex v0, Vertex v1, Vertex v2) {
        int[] face = new int[3];
        face[0] = addVertex(v0);
        face[1] = addVertex(v1);
        face[2] = addVertex(v2);
        faces.add(face);
        connectivity = null;
    }

    public void addTriangle(Vertex v0, Vertex v1, Vertex v2, boolean flip) {
        if (flip) {
            addTriangle(v2, v1, v0);
        }
        else {
            addTriangle(v0, v1, v2);
        }
    }

    public void addQuad(Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        int[] face = new int[4];
        face[0] = addVertex(v0);
        face[1] = addVertex(v1);
        face[2] = addVertex(v2);
        face[3] = addVertex(v3);
        quads.add(face);
        connectivity = null;
    }

    public void addQuad(Vertex v0, Vertex v1, Vertex v2, Vertex v3, boolean flip) {
        if (flip) {
            addQuad(v3, v2, v1, v0);
        }
        else {
            addQuad(v0, v1, v2, v3);
        }
    }

    public void removeVertices(Collection<Vertex> vertices) {
        vertices.removeAll(vertices);
        for (Vertex v : vertices) {
            vertexMap.remove(v);
        }
    }

    private void print() {
        System.out.println("Obj " + this.toString());
        System.out.println("Vertices: " + vertices.size());
        for (Vertex vertex : vertices) {
            System.out.println(vertex);
        }

        System.out.println("Tris: " + faces.size());
        for (int[] face : faces) {
            String s = "";
            for (int i = 0; i < face.length; ++i) {
                s += face[i] + " ";
            }
            System.out.println(s);
        }
        System.out.println("Quads: " + quads.size());
        for (int[] face : quads) {
            String s = "";
            for (int i = 0; i < face.length; ++i) {
                s += face[i] + " ";
            }
            System.out.println(s);
        }
    }
}
