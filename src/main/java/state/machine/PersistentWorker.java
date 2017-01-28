package state.machine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

/**
 * Created by yael
 */
public class PersistentWorker<T> {

    private Gson gson;
    private String fileName;

    public PersistentWorker(String fileIdentifier) {
        this.fileName = fileIdentifier + ".json";
        this.gson =  new GsonBuilder()
                .registerTypeAdapter(State.class, new State.StateAdapter())
                .create();
    }

    public void serialize(T object){
        String json = gson.toJson(object);
        FileWriter writer = null;
        try {
            writer = new FileWriter(fileName);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public T deserialize(Class<T> classOfT){
        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new FileReader(fileName));
            return gson.fromJson(br, classOfT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(br != null){
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
