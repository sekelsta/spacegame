package sekelsta.math;

import java.util.Random;

// This Vector3f class is released under a CC0 licence.
// Feel free to use it in your own projects with or without attribution. There
// is only so much creativity that can go into a vector class, anyway.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.


public class Vector3f {
    public float x, y, z;

    public Vector3f() {}

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f add(Vector3f other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        return this;
    }

    public Vector3f translate(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector3f addWeighted(Vector3f other, float w) {
        this.x += other.x * w;
        this.y += other.y * w;
        this.z += other.z * w;
        return this;
    }

    public Vector3f scale(float s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
        return this;
    }

    public Vector3f negate() {
        return this.scale(-1);
    }

    public float dot(Vector3f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public float length() {
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3f normalize() {
        return this.scale(1f / this.length());
    }

    public static Vector3f average(Vector3f... vectors) {
        Vector3f avg = new Vector3f(0, 0, 0);
        for (Vector3f vector : vectors) {
            avg.add(vector);
        }
        avg.scale(1f / vectors.length);

        return avg;
    }

    public static Vector3f negate(Vector3f vec, Vector3f out) {
        out.x = -1 * vec.x;
        out.y = -1 * vec.y;
        out.z = -1 * vec.z;
        return out;
    }

    public static Vector3f add(Vector3f left, Vector3f right, Vector3f out) {
        out.x = left.x + right.x;
        out.y = left.y + right.y;
        out.z = left.z + right.z;
        return out;
    }

    public static Vector3f subtract(Vector3f left, Vector3f right, Vector3f out) {
        return add(left, negate(right, out), out);
    }

    public static Vector3f cross(Vector3f left, Vector3f right, Vector3f out) {
        float cx = left.y * right.z - left.z * right.y;
        float cy = left.z * right.x - left.x * right.z;
        float cz = left.x * right.y - left.y * right.x;
        out.x = cx;
        out.y = cy;
        out.z = cz;
        return out;
    }

    public static Vector3f mul(Matrix3f left, Vector3f right, Vector3f out) {
        float x = right.x * left.m00 + right.y * left.m01 + right.z * left.m02;
        float y = right.x * left.m10 + right.y * left.m11 + right.z * left.m12;
        float z = right.x * left.m20 + right.y * left.m21 + right.z * left.m22;

        out.x = x;
        out.y = y;
        out.z = z;

        return out;
    }

    public static Vector3f rotate(float yaw, float pitch, float roll, Vector3f in, Vector3f out) {
        float cy = (float) Math.cos(yaw);
        float sy = (float) Math.sin(yaw);
        float cp = (float) Math.cos(pitch);
        float sp = (float) Math.sin(pitch);
        float cr = (float) Math.cos(roll);
        float sr = (float) Math.sin(roll);

        // 3x3 rotation matrix
        float r00 = cr * cy - sr * sp * sy;
        float r10 = cr * sy + sr * sp * cy;
        float r20 = -1 * sr * cp;
        float r01 = -1 * cp * sy;
        float r11 = cp * cy;
        float r21 = sp;
        float r02 = sr * cy + cr * sp * sy;
        float r12 = sr * sy - cr * sp * cy;
        float r22 = cr * cp;

        float x = in.x * r00 + in.y * r01 + in.z * r02;
        float y = in.x * r10 + in.y * r11 + in.z * r12;
        float z = in.x * r20 + in.y * r21 + in.z * r22;

        out.x = x;
        out.y = y;
        out.z = z;
        return out;
    }

    public Vector3f rotate(float yaw, float pitch, float roll) {
        return rotate(yaw, pitch, roll, this, this);
    }

    // Return a random vector within the unit sphere, excluding {0, 0, 0}
    public static Vector3f randomNonzero(Vector3f v, Random random) {
        do {
            v.x = 2f * random.nextFloat() - 1f;
            v.y = 2f * random.nextFloat() - 1f;
            v.z = 2f * random.nextFloat() - 1f;
        }
        while (v.length() > 1f || (v.x == 0f && v.y == 0f && v.z == 0f));
        return v;
    }

    @Override
    public String toString() {
        return x + " " + y + " " + z;
    }
}
