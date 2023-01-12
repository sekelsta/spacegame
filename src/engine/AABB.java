package sekelsta.engine;

import shadowfox.math.Vector3f;

// Axis aligned bounding box
public class AABB {
    // To think about: for other uses it might be better to have x, y, z, width, height, depth
    float minX, maxX, minY, maxY, minZ, maxZ;

    public AABB() {}
/*
    public AABB(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }*/

    public AABB bound(Vector3f... vectors) {
        minX = maxX = vectors[0].x;
        minY = maxY = vectors[0].y;
        minZ = maxZ = vectors[0].z;
        for (Vector3f vector : vectors) {
            minX = Math.min(minX, vector.x);
            maxX = Math.max(maxX, vector.x);
            minY = Math.min(minY, vector.y);
            maxY = Math.max(maxY, vector.y);
            minZ = Math.min(minZ, vector.z);
            maxZ = Math.max(maxZ, vector.z);
        }
        return this;
    }

    public float getMinX() {
        return minX;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxY() {
        return maxY;
    }

    public float getMinZ() {
        return minZ;
    }

    public float getMaxZ() {
        return maxZ;
    }
}
