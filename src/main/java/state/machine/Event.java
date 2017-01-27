package state.machine;

import java.util.Properties;

/**
 * Created by yael on 27/01/17.
 */
public abstract class Event {
    private Properties properties;

    public Event() {
        this.properties = new Properties();
    }

    public Event(String identifier, Properties properties) {
        this.properties = properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getIdentifier() {
        return this.getClass().getName();
    }
}
