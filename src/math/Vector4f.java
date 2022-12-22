package sekelsta.math;

// This Vector4f class is released under a CC0 licence.
// Feel free to use it in your own projects with or without attribution. There
// is only so much creativity that can go into a vector class, anyway.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.


public class Vector4f {
    public float x, y, z, w;

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4f(float x, float y, float z) {
        this(x, y, z, 1f);
    }

    public Vector4f(Vector3f v) {
        this(v.x, v.y, v.z);
    }

    public Vector3f toVec3() {
        return new Vector3f(x/w, y/w, z/w);
    }

    public static Vector4f mul(Matrix4f left, Vector4f right, Vector4f out) {
        float x = right.x * left.m00 + right.y * left.m01 + right.z * left.m02 + right.w * left.m03;
        float y = right.x * left.m10 + right.y * left.m11 + right.z * left.m12 + right.w * left.m13;
        float z = right.x * left.m20 + right.y * left.m21 + right.z * left.m22 + right.w * left.m23;
        float w = right.x * left.m30 + right.y * left.m31 + right.z * left.m32 + right.w * left.m33;

        out.x = x;
        out.y = y;
        out.z = z;
        out.w = w;

        return out;
    }
}
