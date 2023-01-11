package sekelsta.math;

// This Matrix4f class is released under a CC0 licence.
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
import java.util.StringTokenizer;

// Row-major matrix
public class Matrix4f {
    public float m00, m10, m20, m30, m01, m11, m21, m31, m02, m12, m22, m32, m03, m13, m23, m33;

    public Matrix4f() {
        setIdentity();
    }

    public Matrix4f(float m00, float m01, float m02, float m03,
                float m10, float m11, float m12, float m13,
                float m20, float m21, float m22, float m23,
                float m30, float m31, float m32, float m33) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    public Matrix4f(Matrix4f copy) {
        this(copy.m00, copy.m01, copy.m02, copy.m03,
            copy.m10, copy.m11, copy.m12, copy.m13,
            copy.m20, copy.m21, copy.m22, copy.m23,
            copy.m30, copy.m31, copy.m32, copy.m33);
    }

    public Matrix4f(Matrix3f copy) {
        this(copy.m00, copy.m01, copy.m02, 0,
            copy.m10, copy.m11, copy.m12, 0,
            copy.m20, copy.m21, copy.m22, 0,
            0, 0, 0, 1);
    }

    public Matrix4f setIdentity() {
        m00 = 1;
        m01 = 0;
        m02 = 0;
        m03 = 0;
        m10 = 0;
        m11 = 1;
        m12 = 0;
        m13 = 0;
        m20 = 0;
        m21 = 0;
        m22 = 1;
        m23 = 0;
        m30 = 0;
        m31 = 0;
        m32 = 0;
        m33 = 1;
        return this;
    }

    public Matrix4f copy(Matrix4f other) {
        this.m00 = other.m00;
        this.m01 = other.m01;
        this.m02 = other.m02;
        this.m03 = other.m03;
        this.m10 = other.m10;
        this.m11 = other.m11;
        this.m12 = other.m12;
        this.m13 = other.m13;
        this.m20 = other.m20;
        this.m21 = other.m21;
        this.m22 = other.m22;
        this.m23 = other.m23;
        this.m30 = other.m30;
        this.m31 = other.m31;
        this.m32 = other.m32;
        this.m33 = other.m33;
        return this;
    }

    public void parseColumnMajor(String in) {
        StringTokenizer tokens = new StringTokenizer(in);
        m00 = Float.parseFloat(tokens.nextToken());
        m10 = Float.parseFloat(tokens.nextToken());
        m20 = Float.parseFloat(tokens.nextToken());
        m30 = Float.parseFloat(tokens.nextToken());
        m01 = Float.parseFloat(tokens.nextToken());
        m11 = Float.parseFloat(tokens.nextToken());
        m21 = Float.parseFloat(tokens.nextToken());
        m31 = Float.parseFloat(tokens.nextToken());
        m02 = Float.parseFloat(tokens.nextToken());
        m12 = Float.parseFloat(tokens.nextToken());
        m22 = Float.parseFloat(tokens.nextToken());
        m32 = Float.parseFloat(tokens.nextToken());
        m03 = Float.parseFloat(tokens.nextToken());
        m13 = Float.parseFloat(tokens.nextToken());
        m23 = Float.parseFloat(tokens.nextToken());
        m33 = Float.parseFloat(tokens.nextToken());

    }

    public Matrix4f parseRowMajor(String in) {
        return parseRowMajor(new StringTokenizer(in));
    }

    public Matrix4f parseRowMajor(StringTokenizer tokens) {
        m00 = Float.parseFloat(tokens.nextToken());
        m01 = Float.parseFloat(tokens.nextToken());
        m02 = Float.parseFloat(tokens.nextToken());
        m03 = Float.parseFloat(tokens.nextToken());
        m10 = Float.parseFloat(tokens.nextToken());
        m11 = Float.parseFloat(tokens.nextToken());
        m12 = Float.parseFloat(tokens.nextToken());
        m13 = Float.parseFloat(tokens.nextToken());
        m20 = Float.parseFloat(tokens.nextToken());
        m21 = Float.parseFloat(tokens.nextToken());
        m22 = Float.parseFloat(tokens.nextToken());
        m23 = Float.parseFloat(tokens.nextToken());
        m30 = Float.parseFloat(tokens.nextToken());
        m31 = Float.parseFloat(tokens.nextToken());
        m32 = Float.parseFloat(tokens.nextToken());
        m33 = Float.parseFloat(tokens.nextToken());
        return this;
    }

	public Matrix4f storeColumnMajor(FloatBuffer buf) {
		buf.put(m00);
		buf.put(m10);
		buf.put(m20);
		buf.put(m30);
		buf.put(m01);
		buf.put(m11);
		buf.put(m21);
		buf.put(m31);
		buf.put(m02);
		buf.put(m12);
		buf.put(m22);
		buf.put(m32);
		buf.put(m03);
		buf.put(m13);
		buf.put(m23);
		buf.put(m33);
		return this;
	}

    public Matrix4f translate(float x, float y, float z) {
        m03 += m00 * x + m01 * y + m02 * z;
        m13 += m10 * x + m11 * y + m12 * z;
        m23 += m20 * x + m21 * y + m22 * z;
        m33 += m30 * x + m31 * y + m32 * z;
        return this;
    }

    public static Matrix4f rotate(float angle, float x, float y, float z, Matrix4f in, Matrix4f out) {
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
        float c30 = in.m30 * r00 + in.m31 * r10 + in.m32 * r20;
        float c31 = in.m30 * r01 + in.m31 * r11 + in.m32 * r21;
        float c32 = in.m30 * r02 + in.m31 * r12 + in.m32 * r22;

        out.m03 = in.m03;
        out.m13 = in.m13;
        out.m23 = in.m23;
        out.m33 = in.m33;
        out.m00 = c00;
        out.m01 = c01;
        out.m02 = c02;
        out.m10 = c10;
        out.m11 = c11;
        out.m12 = c12;
        out.m20 = c20;
        out.m21 = c21;
        out.m22 = c22;
        out.m30 = c30;
        out.m31 = c31;
        out.m32 = c32;

        return out;
    }

    // angle is in radians
    // x, y, and z are the axis to rotate about. Must be normalized.
    public Matrix4f rotate(float angle, float x, float y, float z) {
        return rotate(angle, x, y, z, this, this);
    }

    public static Matrix4f rotate(float yaw, float pitch, float roll, Matrix4f in, Matrix4f out) {
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
        float c30 = in.m30 * r00 + in.m31 * r10 + in.m32 * r20;
        float c31 = in.m30 * r01 + in.m31 * r11 + in.m32 * r21;
        float c32 = in.m30 * r02 + in.m31 * r12 + in.m32 * r22;

        out.m03 = in.m03;
        out.m13 = in.m13;
        out.m23 = in.m23;
        out.m33 = in.m33;
        out.m00 = c00;
        out.m01 = c01;
        out.m02 = c02;
        out.m10 = c10;
        out.m11 = c11;
        out.m12 = c12;
        out.m20 = c20;
        out.m21 = c21;
        out.m22 = c22;
        out.m30 = c30;
        out.m31 = c31;
        out.m32 = c32;

        return out;
    }

    // Roll, pitch, and yaw are angles in radians
    public Matrix4f rotate(float yaw, float pitch, float roll) {
        return rotate(yaw, pitch, roll, this, this);
    }

    public Matrix4f scale(float x, float y, float z) {
        m00 *= x;
        m01 *= y;
        m02 *= z;
        m10 *= x;
        m11 *= y;
        m12 *= z;
        m20 *= x;
        m21 *= y;
        m22 *= z;
        return this;
    }

    public static Matrix4f mul(Matrix4f left, Matrix4f right, Matrix4f out) {
        float m00 = left.m00 * right.m00 + left.m01 * right.m10 + left.m02 * right.m20 + left.m03 * right.m30;
        float m10 = left.m10 * right.m00 + left.m11 * right.m10 + left.m12 * right.m20 + left.m13 * right.m30;
        float m20 = left.m20 * right.m00 + left.m21 * right.m10 + left.m22 * right.m20 + left.m23 * right.m30;
        float m30 = left.m30 * right.m00 + left.m31 * right.m10 + left.m32 * right.m20 + left.m33 * right.m30;

        float m01 = left.m00 * right.m01 + left.m01 * right.m11 + left.m02 * right.m21 + left.m03 * right.m31;
        float m11 = left.m10 * right.m01 + left.m11 * right.m11 + left.m12 * right.m21 + left.m13 * right.m31;
        float m21 = left.m20 * right.m01 + left.m21 * right.m11 + left.m22 * right.m21 + left.m23 * right.m31;
        float m31 = left.m30 * right.m01 + left.m31 * right.m11 + left.m32 * right.m21 + left.m33 * right.m31;

        float m02 = left.m00 * right.m02 + left.m01 * right.m12 + left.m02 * right.m22 + left.m03 * right.m32;
        float m12 = left.m10 * right.m02 + left.m11 * right.m12 + left.m12 * right.m22 + left.m13 * right.m32;
        float m22 = left.m20 * right.m02 + left.m21 * right.m12 + left.m22 * right.m22 + left.m23 * right.m32;
        float m32 = left.m30 * right.m02 + left.m31 * right.m12 + left.m32 * right.m22 + left.m33 * right.m32;

        float m03 = left.m00 * right.m03 + left.m01 * right.m13 + left.m02 * right.m23 + left.m03 * right.m33;
        float m13 = left.m10 * right.m03 + left.m11 * right.m13 + left.m12 * right.m23 + left.m13 * right.m33;
        float m23 = left.m20 * right.m03 + left.m21 * right.m13 + left.m22 * right.m23 + left.m23 * right.m33;
        float m33 = left.m30 * right.m03 + left.m31 * right.m13 + left.m32 * right.m23 + left.m33 * right.m33;

		out.m00 = m00;
		out.m01 = m01;
		out.m02 = m02;
		out.m03 = m03;
		out.m10 = m10;
		out.m11 = m11;
		out.m12 = m12;
		out.m13 = m13;
		out.m20 = m20;
		out.m21 = m21;
		out.m22 = m22;
		out.m23 = m23;
		out.m30 = m30;
		out.m31 = m31;
		out.m32 = m32;
		out.m33 = m33;

        return out;
    }

    // In-place multiplication
    public Matrix4f multiply(Matrix4f right) {
        return mul(this, right, this);
    }

    public static Vector4f transform(Matrix4f left, Vector4f right, Vector4f out) {
        float tx = left.m00 * right.x + left.m01 * right.y + left.m02 * right.z + left.m03 * right.w;
        float ty = left.m10 * right.x + left.m11 * right.y + left.m12 * right.z + left.m13 * right.w;
        float tz = left.m20 * right.x + left.m21 * right.y + left.m22 * right.z + left.m23 * right.w;
        float tw = left.m30 * right.x + left.m31 * right.y + left.m32 * right.z + left.m33 * right.w;
        out.x = tx;
        out.y = ty;
        out.z = tz;
        out.w = tw;

        return out;
    }

    public Vector4f transform(Vector4f vector) {
        return transform(this, vector, vector);
    }

    public float determinant() {
        float f = m00 * Matrix3f.determinant(m11, m12, m13, m21, m22, m23, m31, m32, m33);
        f -= m01 * Matrix3f.determinant(m10, m12, m13, m20, m22, m23, m30, m32, m33);
        f += m02 * Matrix3f.determinant(m10, m11, m13, m20, m21, m23, m30, m31, m33);
        f -= m03 * Matrix3f.determinant(m10, m11, m12, m20, m21, m22, m30, m31, m32);
        return f;
    }

    public static Matrix4f invert(Matrix4f in, Matrix4f out) {
        float determinant = in.determinant();
        if (determinant == 0) {
            throw new IllegalArgumentException("Cannot invert matrix");
        }

        float inv = 1 / determinant;

    // Based on LWGJL's Matrix4f
        // first row
        float t00 =  Matrix3f.determinant(in.m11, in.m12, in.m13, in.m21, in.m22, in.m23, in.m31, in.m32, in.m33);
        float t01 = -Matrix3f.determinant(in.m10, in.m12, in.m13, in.m20, in.m22, in.m23, in.m30, in.m32, in.m33);
        float t02 =  Matrix3f.determinant(in.m10, in.m11, in.m13, in.m20, in.m21, in.m23, in.m30, in.m31, in.m33);
        float t03 = -Matrix3f.determinant(in.m10, in.m11, in.m12, in.m20, in.m21, in.m22, in.m30, in.m31, in.m32);
        // second row
        float t10 = -Matrix3f.determinant(in.m01, in.m02, in.m03, in.m21, in.m22, in.m23, in.m31, in.m32, in.m33);
        float t11 =  Matrix3f.determinant(in.m00, in.m02, in.m03, in.m20, in.m22, in.m23, in.m30, in.m32, in.m33);
        float t12 = -Matrix3f.determinant(in.m00, in.m01, in.m03, in.m20, in.m21, in.m23, in.m30, in.m31, in.m33);
        float t13 =  Matrix3f.determinant(in.m00, in.m01, in.m02, in.m20, in.m21, in.m22, in.m30, in.m31, in.m32);
        // third row
        float t20 =  Matrix3f.determinant(in.m01, in.m02, in.m03, in.m11, in.m12, in.m13, in.m31, in.m32, in.m33);
        float t21 = -Matrix3f.determinant(in.m00, in.m02, in.m03, in.m10, in.m12, in.m13, in.m30, in.m32, in.m33);
        float t22 =  Matrix3f.determinant(in.m00, in.m01, in.m03, in.m10, in.m11, in.m13, in.m30, in.m31, in.m33);
        float t23 = -Matrix3f.determinant(in.m00, in.m01, in.m02, in.m10, in.m11, in.m12, in.m30, in.m31, in.m32);
        // fourth row
        float t30 = -Matrix3f.determinant(in.m01, in.m02, in.m03, in.m11, in.m12, in.m13, in.m21, in.m22, in.m23);
        float t31 =  Matrix3f.determinant(in.m00, in.m02, in.m03, in.m10, in.m12, in.m13, in.m20, in.m22, in.m23);
        float t32 = -Matrix3f.determinant(in.m00, in.m01, in.m03, in.m10, in.m11, in.m13, in.m20, in.m21, in.m23);
        float t33 =  Matrix3f.determinant(in.m00, in.m01, in.m02, in.m10, in.m11, in.m12, in.m20, in.m21, in.m22);

        // transpose and divide by the determinant
        out.m00 = t00 * inv;
        out.m11 = t11 * inv;
        out.m22 = t22 * inv;
        out.m33 = t33 * inv;
        out.m01 = t10 * inv;
        out.m10 = t01 * inv;
        out.m20 = t02 * inv;
        out.m02 = t20 * inv;
        out.m12 = t21 * inv;
        out.m21 = t12 * inv;
        out.m03 = t30 * inv;
        out.m30 = t03 * inv;
        out.m13 = t31 * inv;
        out.m31 = t13 * inv;
        out.m32 = t23 * inv;
        out.m23 = t32 * inv;

        return out;
    }

    // In-place inverse
    public Matrix4f invert() {
        return invert(this, this);
    }

    // Sets this matrix to a frustum matrix centered at the origin, in a negative-Z-forward
    // right handed coordinate system
    // To factor out a constant of 2, "width" and "height" are actually measured from the center to the edge
    public Matrix4f frustumNegZ(float near, float far, float width, float height) {
        setIdentity();
        
        m00 = near / width;
        m11 = near / height;
        m22 = (near + far) / (near - far);
        m23 = 2 * far * near / (near - far);
        m32 = -1;
        m33 = 0;

        return this;
    }

    // Sets this matrix to a frustum matrix centered at the origin, in a Y-forward
    // right handed coordinate system
    // To factor out a constant of 2, "width" and "height" are actually measured from the center to the edge
    public Matrix4f frustumY(float near, float far, float width, float height) {
        setIdentity();

        m00 = near / width;
        m11 = (near + far) / (near - far);
        m13 = 2 * far * near / (far - near);
        m22 = near / height;
        m31 = 1;
        m33 = 0;

        return this;
    }

    public Matrix3f normalTransform() {
        Matrix3f n = new Matrix3f();
        n.m00 = this.m00;
        n.m01 = this.m01;
        n.m02 = this.m02;
        n.m10 = this.m10;
        n.m11 = this.m11;
        n.m12 = this.m12;
        n.m20 = this.m20;
        n.m21 = this.m21;
        n.m22 = this.m22;
        return (Matrix3f)(n.invert().transpose());
    }

    public Matrix3f getRotation() {
        float scaleX = (float)Math.sqrt(m00 * m00 + m01 * m01 + m02 * m02);
        float scaleY = (float)Math.sqrt(m10 * m10 + m11 * m11 + m12 * m12);
        float scaleZ = (float)Math.sqrt(m20 * m20 + m21 * m21 + m22 * m22);
        return new Matrix3f(m00 / scaleX, m01 / scaleX, m02 / scaleX,
                            m10 / scaleY, m11 / scaleY, m12 / scaleY,
                            m20 / scaleZ, m21 / scaleZ, m22 / scaleZ);
    }

    public Vector3f getTranslation() {
        return new Vector3f(m03, m13, m23);
    }

    public String toString() {
        return m00 + " " + m01 + " " + m02 + " " + m03 + "\n"
            + m10 + " " + m11 + " " + m12 + " " + m13 + "\n"
            + m20 + " " + m21 + " " + m22 + " " + m23 + "\n"
            + m30 + " " + m31 + " " + m32 + " " + m33 + "\n";
    }
}
