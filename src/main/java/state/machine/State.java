package state.machine;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yael
 */
public abstract class State {

    private Map<String, String> transitions;

    public State() {
        this.transitions = new HashMap<>();
    }

    public State(Map<String, String> transitions) {
        this.transitions = transitions;
    }

    public void addTransition(String event, String state){
        this.transitions.put(event, state);
    }

    public abstract void doAction();

    /**
     * getNextStateId
     * @return next state or null if no transition.
     */
    public String getNextStateId(String eventIdentifier){
        return transitions.get(eventIdentifier);
    }

    public Map<String, String> getTransitions() {
        return transitions;
    }

    public void setTransitions(Map<String, String> transitions) {
        this.transitions = transitions;
    }

    public static class StateAdapter implements JsonSerializer<State>, JsonDeserializer<State> {
        @Override
        public JsonElement serialize(State src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.add("type", new JsonPrimitive(src.getClass().getName()));
            result.add("properties", context.serialize(src, src.getClass()));

            return result;
        }

        @Override
        public State deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();
            JsonElement element = jsonObject.get("properties");
            try {
                return context.deserialize(element, Class.forName(type));
            } catch (ClassNotFoundException cnfe) {
                throw new JsonParseException("Unknown element type: " + type, cnfe);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        return transitions != null ? transitions.equals(state.transitions) : state.transitions == null;
    }

    @Override
    public int hashCode() {
        return transitions != null ? transitions.hashCode() : 0;
    }

    public String getIdentifier() {
        return this.getClass().getName();
    }
}
