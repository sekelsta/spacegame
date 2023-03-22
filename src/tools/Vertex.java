package sekelsta.tools;

import java.util.*;
import shadowfox.math.Vector2f;
import shadowfox.math.Vector3f;

public class Vertex {
    public Vector3f position;
    public Vector3f normal;
    public Vector2f texture;
    public int boneIDs[];
    public float boneWeights[];
    public short type;

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof Vertex)) {
            return false;
        }
        Vertex other = (Vertex)o;
        return ((this.position == null && other.position == null)
                    || (this.position != null && this.position.equals(other.position)))
                && ((this.normal == null && other.normal == null)
                    || (this.normal != null && this.normal.equals(other.normal)))
                && ((this.texture == null && other.texture == null)
                    || (this.texture != null && this.texture.equals(other.texture)));
    }

    public boolean facesUpwardsOrDownwards() {
        double edge = Math.sin(Math.PI / 8);
        return normal.z < -1 * edge || normal.z > edge;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, normal, texture);
    }

    @Override
    public String toString() {
        return "Position " + position + " normal " + normal + " texture " + texture;
    }
}
