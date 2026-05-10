package UniSpace.storage;

import java.io.*;

/**
 * Static utility for serializing and deserializing objects to/from disk.
 * All methods are static — no state, no instantiation.
 */
public class FileStorage {

    private FileStorage() {}

    public static void save(Serializable data, String path) {
        File file = new File(path);
        if (file.getParentFile() != null) file.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("[Storage] Save failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T load(String path) {
        File file = new File(path);
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(path)))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Storage] Load failed: " + e.getMessage());
            return null;
        }
    }
}
