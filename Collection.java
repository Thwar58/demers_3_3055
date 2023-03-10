import java.lang.reflect.Array;
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
 * modified by jason demers for the password manager project
 */
public class Collection implements JSONSerializable
{
    private ArrayList<JSONObject> entries;			// An arraylist of hashmap entries per url
    private String name; 			// The name of the collection.

    public static String salt; //salt of the collection
    /**
     * Creates a new collection owned by {@code owner}.
     */
    public Collection(String name)
    {
        this.entries = new ArrayList<JSONObject>();
        this.name = name;
        this.salt = "evk+aFczU8DQAyYrDYrX+w==";
    }

    /**
     * Constructs a collection from a serialized version of the
     * object.
     * @param obj a JSON object that represents a collection of photos.
     * @throws InvalidObjectException if the obj is not of the correct type.
     */
    public Collection(JSONObject obj) throws InvalidObjectException
    {
        this.entries = new ArrayList<JSONObject>();
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
     */
    public void addEntry(String address, String user, String iv, String pass)
    {
        JSONObject hash = new JSONObject();
        hash.put("url", address);
        hash.put("pass", pass);
        hash.put("user", user);
        hash.put("iv",iv);
        this.entries.add(hash);
    }

    /**
     * Modified by JD
     * Gets a entry with url name fname from the
     * collection.
     * @return the entry or null if the entry does not exist.
     */
    public JSONObject getEntry(String fname)
    {
        for(int i = 0; i < entries.size(); i++){
            if(entries.get(i).get("url").equals(fname)){
                return entries.get(i);
            }
        }
        return null;
    }

    /**
     * List all urls.
     * @return an array list of url names.
     */
    public ArrayList<String> listAll()
    {
        ArrayList<String> urls = new ArrayList<>();
        for(int i = 0; i < entries.size(); i++){
            urls.add(entries.get(i).getString("url"));
        }
        return urls;
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
        tmp = (JSONObject) obj;
        this.salt = tmp.getString("salt");
        JSONArray accounts = tmp.getArray("accounts");
        for(int i = 0; i < accounts.size(); i++){
            entries.add(accounts.getObject(i));
        }

//
    }

    /**
     * Modified by JD
     * Converts the object to a JSON type.
     * @return a JSON type either JSONObject or JSONArray.
     */
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("salt",this.salt);
        JSONArray jarr = new JSONArray();
        for(int i = 0; i < entries.size(); i++){
            jarr.add(entries.get(i));
        }
        obj.put("accounts", jarr);

        return obj;
    }

}