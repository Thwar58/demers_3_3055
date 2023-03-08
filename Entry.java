import csc3055.json.JSONSerializable;
import csc3055.json.types.JSONArray;
import csc3055.json.types.JSONType;
import csc3055.util.Tuple;

import java.io.InvalidObjectException;

public class Entry extends Tuple implements JSONSerializable
{

    public Entry(String o, String o2) {
        super(o, o2);
    }

    @Override
    public String serialize() {
        return toJSONType().getFormattedJSON();
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {

        //need to understand
    }

    @Override
    public JSONType toJSONType() {
        JSONArray obj = new JSONArray();
        obj.add(this.getFirst());
        obj.add(this.getSecond());

        return obj;
    }
}
