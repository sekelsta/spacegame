package sekelsta.engine.render;

public class MaterialShader extends ShaderProgram {
    public MaterialShader(String vertexSource, String fragmentSource) {
        super(vertexSource, fragmentSource);
    }

    public static MaterialShader load(String vertexResource, String fragmentResource) {
        return new MaterialShader(loadResource(vertexResource), loadResource(fragmentResource));
    }

    public void setReflectance(float reflectance) {
        setFloat("reflectance", reflectance);
    }

    public void setShininess(float shininess) {
        setFloat("shininess", shininess);
    }

    public void setScattering(float scattering) {
        setFloat("scattering", scattering);
    }

    public void setDefaultMaterial() {
        setReflectance(0.5f);
        setShininess(16);
        setScattering(0.05f);
    }
}
