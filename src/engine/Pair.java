package sekelsta.engine;

import java.util.Objects;

public class Pair<K,V> {
    K key;
    V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public int hashCode() {
        return Objects.hash(key, value);
    }

    public String toString() {
        return "<%s, %s>".format(key.toString(), value.toString());
    }
}
