import java.util.HashMap;
import java.util.ArrayList;
import java.security.PublicKey;
import java.security.PrivateKey;
import csc3055.json.JSONSerializable;
import csc3055.json.types.JSONObject;
import csc3055.json.types.JSONArray;
import csc3055.json.types.JSONType;
import csc3055.util.Tuple;

import java.io.InvalidObjectException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;


/**
 * This class represents a collection of Photographs. It stores all 
 * photographs in the entire system.
 * @author Zach Kissel
 */
public class Collection implements JSONSerializable
{
    private HashMap<String, Entry> entries;			// A hash map of username password entries.
    private String name; 			// The name of the collection.
    /**
     * Creates a new collection owned by {@code owner}.
     */
    public Collection(String name)
    {
        this.entries = new HashMap<>();
        this.name = name;
    }

    /**
     * Constructs a collection from a serialized version of the
     * object.
     * @param obj a JSON object that represents a collection of photos.
     * @throws InvalidObjectException if the obj is not of the correct type.
     */
    public Collection(JSONObject obj) throws InvalidObjectException
    {
        this.entries = new HashMap<>();
        deserialize(obj);
    }

    /**
     * Gets the name of the collection.
     * @return the name of the collection.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Modified by JD
     * Adds the entry the collection.
     * @param address the name of the website being added
     * @param entry the entry to add.
     */
    public void addEntry(String address, Entry entry)
    {
        this.entries.put(address, entry);
    }

    /**
     * Modified by JD
     * Gets a entry with name fname from the
     * collection.
     * @return the entry or null if the entry does not exist.
     */
    public Entry getEntry(String fname)
    {
        if (!entries.containsKey(fname))
            return null;
        return entries.get(fname);
    }

    /**
     * List all photo names.
     * @return an array list of photos.
     */
    public ArrayList<String> listAll()
    {
        ArrayList<String> res = new ArrayList();
        for (String key : entries.keySet())
            res.add(key);
        return res;
    }

    /**
     * Serializes the object into a JSON encoded string.
     * @return a string representing the JSON form of the object.
     */
    public String serialize()
    {
        return toJSONType().getFormattedJSON();
    }

    /**
     * Coverts json data to an object of this type.
     * @param obj a JSON type to deserialize.
     * @throws InvalidObjectException the type does not match this object.
     */
    public void deserialize(JSONType obj) throws InvalidObjectException
    {
        JSONObject tmp;
        JSONArray photoArray;
        if (obj instanceof JSONObject)
        {
            tmp = (JSONObject)obj;
            if (tmp.containsKey("name"))
                name = tmp.getString("name");
            else
                throw new InvalidObjectException("Expected a Collection object -- name expected.");
            if (tmp.containsKey("photos"))
                photoArray = tmp.getArray("photos");
            else
                throw new InvalidObjectException("Expected a Collection object -- photos expected.");
        }
        else
            throw new InvalidObjectException("Expected a Collection object -- recieved array");

        for (int i = 0; i < photoArray.size(); i++)
        {
            JSONObject currPhoto = photoArray.getObject(i);
//            try
            {
                //photos.put(currPhoto.getString("fileName"), new Photograph(currPhoto));
                int uselessVariable;
            }
//            catch (NoSuchPaddingException | NoSuchAlgorithmException ex)
//            {
//                System.out.println("Bad cryptographic mechanism!");
//                ex.printStackTrace();
//                System.exit(1);
//            }
        }
    }

    /**
     * Modified by JD
     * Converts the object to a JSON type.
     * @return a JSON type either JSONObject or JSONArray.
     */
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();

        for (String key : entries.keySet()){
            obj.put(key, entries.get(key).toJSONType());
        }

        return obj;
    }
}