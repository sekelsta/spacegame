package sekelsta.math;

// This Matrix3f class is released under a CC0 licence.
// Feel free to use it in your own projects with or without attribution. There
// is only so much creativity that can go into a matrix class, anyway.

//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

import java.nio.FloatBuffer;

public class Matrix3f {
    public float m00, m01, m02, m10, m11, m12, m20, m21, m22;

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
}
