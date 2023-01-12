package sekelsta.engine.render;

import shadowfox.math.Matrix4f;

public class Bone {
    public final String name;
    public final int id;
    public final Bone[] children;
    public final Matrix4f bind;
    public final Matrix4f transform;
    public final Matrix4f inverseBind;

    public Bone(String name, int id, Bone[] children, Matrix4f bind, Matrix4f inverseBind) {
        this.name = name;
        this.id = id;
        this.children = children;
        this.bind = bind;
        this.transform = new Matrix4f();
        this.inverseBind = inverseBind;
    }

    // Note parentBind and parentInverse may be modified
    public void updateTransforms(Matrix4f[] joints, Matrix4f parentBind, Matrix4f parentInverse) {
        Matrix4f totalInverse = new Matrix4f(inverseBind).multiply(parentInverse);
        Matrix4f totalBind = new Matrix4f(parentBind).multiply(bind);
        joints[id].copy(totalBind).multiply(transform).multiply(totalInverse);

        if (children == null) {
            return;
        }

        for (int i = 0; i < children.length; ++i) {
            // TO_OPTIMIZE: potential optimization, new calls for the last child can be avoided
            Matrix4f childBind = new Matrix4f(totalBind).multiply(transform);
            Matrix4f childInverse = new Matrix4f(totalInverse);
            children[i].updateTransforms(joints, childBind, childInverse);
        }
    }

    public String toString() {
        return "Name: " + name + ", id: " + id + ", children: " + children + "\n"
            + "Bind pose: \n" + bind + "Inverse bind pose: \n" + inverseBind;
    }
}
