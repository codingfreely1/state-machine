package state.machine;

import java.util.Properties;

/**
 * Created by yael
 */
public abstract class Event {
    protected Properties properties;

    public Event() {
        this.properties = new Properties();
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getIdentifier() {
        return this.getClass().getName();
    }

    public Properties getProperties() {
        return properties;
    }
}
