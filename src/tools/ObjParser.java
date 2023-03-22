package sekelsta.tools;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import shadowfox.math.Vector2f;
import shadowfox.math.Vector3f;

public class ObjParser {
    final static String COMMENT = "#";
    final static String VERTEX_NORMAL = "vn";
    final static String VERTEX_TEXTURE = "vt";
    final static String VERTEX = "v";
    final static String FACE = "f";

    public static ModelData parse(Scanner scanner) {
        ArrayList<Vector3f> vertices = new ArrayList<>();
        ArrayList<Vector3f> normals = new ArrayList<>();
        ArrayList<Vector2f> textures = new ArrayList<>();
        ArrayList<Integer> faces = new ArrayList<>();
        ArrayList<Integer> textureIndices = new ArrayList<>();
        ArrayList<Integer> normalIndices = new ArrayList<>();
        // Make sure obj parsing is independent of the user's language
        scanner.useLocale(Locale.ENGLISH);
        while (scanner.hasNext()) {
            String start = scanner.next();
            if (start.startsWith(COMMENT)) {
                // Skip comment
                scanner.nextLine();
            }
            else if (start.startsWith(VERTEX_NORMAL)) {
                float x = scanner.nextFloat();
                float y = scanner.nextFloat();
                float z = scanner.nextFloat();
                normals.add(new Vector3f(x, y, z));
                scanner.nextLine();
            }
            else if (start.startsWith(VERTEX_TEXTURE)) {
                float u = scanner.nextFloat();
                float v = scanner.nextFloat();
                textures.add(new Vector2f(u, v));
                scanner.nextLine();
            }
            else if (start.startsWith(VERTEX)) {
                float x = scanner.nextFloat();
                float y = scanner.nextFloat();
                float z = scanner.nextFloat();
                vertices.add(new Vector3f(x, y, z));
                scanner.nextLine();
            }
            else if (start.startsWith(FACE)) {
                // TODO #19: throw error if face has more than three vertices
                for (int i = 0; i < 3; ++i) {
                    if (scanner.hasNextInt()) {
                        // Read 1-indexed, write 0-indexed
                        faces.add(scanner.nextInt() - 1);
                    }
                    else {
                        // Note: obj files may be missing one of these values,
                        // but this parser will not support that unless it is needed
                        String[] data = scanner.next().split("/");
                        faces.add(Integer.valueOf(data[0]) - 1);
                        textureIndices.add(Integer.valueOf(data[1]) - 1);
                        normalIndices.add(Integer.valueOf(data[2]) - 1);
                    }
                }
            }
        }

        assert(textureIndices.size() == 0 || textureIndices.size() == faces.size());
        assert(normalIndices.size() == 0 || normalIndices.size() == faces.size());

        ColladaData data = new ColladaData();

        data.positions = vertices.toArray(new Vector3f[0]);
        data.normals = normals.toArray(new Vector3f[0]);
        data.textureCoords = textures.toArray(new Vector2f[0]);

        data.faces = new int[faces.size()];
        for (int i = 0; i < faces.size(); ++i) {
            data.faces[i] = faces.get(i);
        }
        data.normalIndices = new int[normalIndices.size()];
        for (int i = 0; i < normalIndices.size(); ++i) {
            data.normalIndices[i] = normalIndices.get(i);
        }
        data.textureIndices = new int[textureIndices.size()];
        for (int i = 0; i < textureIndices.size(); ++i) {
            data.textureIndices[i] = textureIndices.get(i);
        }

        return data.getModelData();
    }
}
