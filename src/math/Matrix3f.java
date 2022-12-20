package sekelsta.math;

// This Matrix3f class is released under a CC0 licence.
// Feel free to use it in your own projects with or without attribution. There
// is only so much creativity that can go into a matrix class, anyway.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

import java.nio.FloatBuffer;

public class Matrix3f {
    public float m00, m01, m02, m10, m11, m12, m20, m21, m22;

    public Matrix3f(float m00, float m01, float m02,
                    float m10, float m11, float m12,
                    float m20, float m21, float m22) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
    }

    public Matrix3f() {
        setIdentity();
    }

    public Matrix3f setIdentity() {
        m00 = 1;
        m01 = 0;
        m02 = 0;
        m10 = 0;
        m11 = 1;
        m12 = 0;
        m20 = 0;
        m21 = 0;
        m22 = 1;
        return this;
    }

	public Matrix3f storeColumnMajor(FloatBuffer buf) {
		buf.put(m00);
		buf.put(m10);
		buf.put(m20);
		buf.put(m01);
		buf.put(m11);
		buf.put(m21);
		buf.put(m02);
		buf.put(m12);
		buf.put(m22);
		return this;
	}

    public Matrix3f transpose() {
        float f01 = m01;
        m01 = m10;
        m10 = f01;
        float f02 = m02;
        m02 = m20;
        m20 = f02;
        float f12 = m12;
        m12 = m21;
        m21 = f12;
        return this;
    }

    public float determinant() {
        return m00 * m11 * m22 
                + m01 * m12 * m20 
                + m02 * m10 * m21 
                - m02 * m11 * m20 
                - m01 * m10 * m22 
                - m00 * m12 * m21;
    }

    public static float determinant(
            float m00, float m01, float m02, 
            float m10, float m11, float m12,
            float m20, float m21, float m22) {
        return m00 * m11 * m22 
                + m01 * m12 * m20 
                + m02 * m10 * m21 
                - m02 * m11 * m20 
                - m01 * m10 * m22 
                - m00 * m12 * m21;
    }

    public Matrix3f invert() {
        return Matrix3f.invert(this, this);
    }

    public static Matrix3f invert(Matrix3f in, Matrix3f out) {
        float determinant = in.determinant();

        if (determinant == 0) {
            throw new IllegalArgumentException("Cannot invert matrix");
        }

        float inv = 1 / determinant;

        float t00 = in.m11 * in.m22 - in.m12 * in.m21;
        float t01 = in.m02 * in.m21 - in.m01 * in.m22;
        float t02 = in.m01 * in.m12 - in.m02 * in.m11;
        float t10 = in.m12 * in.m20 - in.m10 * in.m22;
        float t11 = in.m00 * in.m22 - in.m02 * in.m20; // ai - cg
        float t12 = in.m02 * in.m10 - in.m00 * in.m12; // cd - af
        float t20 = in.m10 * in.m21 - in.m11 * in.m20; // dh - eg
        float t21 = in.m01 * in.m20 - in.m00 * in.m21; // bg - ah
        float t22 = in.m00 * in.m11 - in.m01 * in.m10; // ae - bd

        out.m00 = inv * t00;
        out.m01 = inv * t01;
        out.m02 = inv * t02;
        out.m10 = inv * t10;
        out.m11 = inv * t11;
        out.m12 = inv * t12;
        out.m20 = inv * t20;
        out.m21 = inv * t21;
        out.m22 = inv * t22;

        return out;
    }

    public static Matrix3f rotate(float angle, float x, float y, float z, Matrix3f in, Matrix3f out) {
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);

        // 3x3 rotation matrix
        float r00 = x * x * (1f - c) + c;
        float r10 = y * x * (1f - c) + z * s;
        float r20 = z * x * (1f - c) - y * s;
        float r01 = x * y * (1f - c) - z * s;
        float r11 = y * y * (1f - c) + c;
        float r21 = z * y * (1f - c) + x * s;
        float r02 = x * z * (1f - c) + y * s;
        float r12 = y * z * (1f - c) - x * s;
        float r22 = z * z * (1f - c) + c;

        // Multiply
        float c00 = in.m00 * r00 + in.m01 * r10 + in.m02 * r20;
        float c01 = in.m00 * r01 + in.m01 * r11 + in.m02 * r21;
        float c02 = in.m00 * r02 + in.m01 * r12 + in.m02 * r22;
        float c10 = in.m10 * r00 + in.m11 * r10 + in.m12 * r20;
        float c11 = in.m10 * r01 + in.m11 * r11 + in.m12 * r21;
        float c12 = in.m10 * r02 + in.m11 * r12 + in.m12 * r22;
        float c20 = in.m20 * r00 + in.m21 * r10 + in.m22 * r20;
        float c21 = in.m20 * r01 + in.m21 * r11 + in.m22 * r21;
        float c22 = in.m20 * r02 + in.m21 * r12 + in.m22 * r22;

        out.m00 = c00;
        out.m01 = c01;
        out.m02 = c02;
        out.m10 = c10;
        out.m11 = c11;
        out.m12 = c12;
        out.m20 = c20;
        out.m21 = c21;
        out.m22 = c22;

        return out;
    }

    // angle is in radians
    // x, y, and z are the axis to rotate about. Must be normalized.
    public Matrix3f rotate(float angle, float x, float y, float z) {
        return rotate(angle, x, y, z, this, this);
    }

    public static Matrix3f rotate(float yaw, float pitch, float roll, Matrix3f in, Matrix3f out) {
        float cy = (float) Math.cos(yaw);
        float sy = (float) Math.sin(yaw);
        float cp = (float) Math.cos(pitch);
        float sp = (float) Math.sin(pitch);
        float cr = (float) Math.cos(roll);
        float sr = (float) Math.sin(roll);

        // 3x3 rotation matrix
        // This is the result of multiplying the yaw, pitch, and roll rotation matrices
        float r00 = cr * cy - sr * sp * sy;
        float r10 = cr * sy + sr * sp * cy;
        float r20 = -1 * sr * cp;
        float r01 = -1 * cp * sy;
        float r11 = cp * cy;
        float r21 = sp;
        float r02 = sr * cy + cr * sp * sy;
        float r12 = sr * sy - cr * sp * cy;
        float r22 = cr * cp;

        // Multiply (same as func above)
        float c00 = in.m00 * r00 + in.m01 * r10 + in.m02 * r20;
        float c01 = in.m00 * r01 + in.m01 * r11 + in.m02 * r21;
        float c02 = in.m00 * r02 + in.m01 * r12 + in.m02 * r22;
        float c10 = in.m10 * r00 + in.m11 * r10 + in.m12 * r20;
        float c11 = in.m10 * r01 + in.m11 * r11 + in.m12 * r21;
        float c12 = in.m10 * r02 + in.m11 * r12 + in.m12 * r22;
        float c20 = in.m20 * r00 + in.m21 * r10 + in.m22 * r20;
        float c21 = in.m20 * r01 + in.m21 * r11 + in.m22 * r21;
        float c22 = in.m20 * r02 + in.m21 * r12 + in.m22 * r22;

        out.m00 = c00;
        out.m01 = c01;
        out.m02 = c02;
        out.m10 = c10;
        out.m11 = c11;
        out.m12 = c12;
        out.m20 = c20;
        out.m21 = c21;
        out.m22 = c22;

        return out;
    }

    // Roll, pitch, and yaw are angles in radians
    public Matrix3f rotate(float yaw, float pitch, float roll) {
        return rotate(yaw, pitch, roll, this, this);
    }

    public static Matrix3f mul(Matrix3f left, Matrix3f right, Matrix3f out) {
        float m00 = left.m00 * right.m00 + left.m01 * right.m10 + left.m02 * right.m20;
        float m10 = left.m10 * right.m00 + left.m11 * right.m10 + left.m12 * right.m20;
        float m20 = left.m20 * right.m00 + left.m21 * right.m10 + left.m22 * right.m20;

        float m01 = left.m00 * right.m01 + left.m01 * right.m11 + left.m02 * right.m21;
        float m11 = left.m10 * right.m01 + left.m11 * right.m11 + left.m12 * right.m21;
        float m21 = left.m20 * right.m01 + left.m21 * right.m11 + left.m22 * right.m21;

        float m02 = left.m00 * right.m02 + left.m01 * right.m12 + left.m02 * right.m22;
        float m12 = left.m10 * right.m02 + left.m11 * right.m12 + left.m12 * right.m22;
        float m22 = left.m20 * right.m02 + left.m21 * right.m12 + left.m22 * right.m22;

		out.m00 = m00;
		out.m01 = m01;
		out.m02 = m02;
		out.m10 = m10;
		out.m11 = m11;
		out.m12 = m12;
		out.m20 = m20;
		out.m21 = m21;
		out.m22 = m22;

        return out;
    }

    // In-place multiplication
    public Matrix3f multiply(Matrix3f right) {
        return mul(this, right, this);
    }

    public float getYaw() {
        return (float) Math.atan2(-1 * m01, m11);
    }

    public float getPitch() {
        return (float) Math.asin(m21);
    }

    public float getRoll() {
        // When pitch = pi/2 or -pi/2, m20 and m22 will both be zero. Java's
        // atan2 implementation returns 0 in that case, exactly as we want.
        return (float) Math.atan2(-1 * m20, m22);
    }

    public String toString() {
        return m00 + " " + m01 + " " + m02 + "\n"
            + m10 + " " + m11 + " " + m12 + "\n"
            + m20 + " " + m21 + " " + m22 + "\n";
    }
}
