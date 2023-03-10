import csc3055.json.JsonIO;
import csc3055.json.types.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

public class Driver {

    public static void main(String[] args)
    {
        //Starts the program by either making the vault or loading it into entriesCollection
        startup();

        JSONArray entry = new JSONArray();
        entry.add("username");
        entry.add("password");
        entriesCollection.addEntry("website.com", entry);

        ArrayList<String> arr = entriesCollection.listAll();
        System.out.println(generatePassword(9));



        shutdown();
    }

    //collection storing data
    public static Collection entriesCollection;

    public static String salt = "evk+aFczU8DQAyYrDYrX+w==";

    public static void startup()
    {
        File collFile = new File("vault.json");


        // Try to load the collection of images. This file may not exist. If it
        // does not exist, we will create it.
        if (!collFile.exists())
        {
            entriesCollection = new Collection("Password Manager");
            return;
        }

        try
        {
            entriesCollection = new Collection(JsonIO.readObject(collFile));
        }
        catch(FileNotFoundException ex)
        {
            System.out.println("Could not find photo collection: \"photos.json\"");
            System.exit(1);
        }
        catch (InvalidObjectException ex)
        {
            System.out.println(ex);
            System.exit(1);
        }
    }

    public static void shutdown()
    {
        try
        {
            JsonIO.writeSerializedObject(entriesCollection, new File("vault.json"));
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("Could not save collection to disk.");
            System.out.println(ex);
        }
    }

    public static String generatePassword(int sz){
        String rtrn = "";
        String[] arr = {"q","w","e","r","t","y","u","i","o","p",
        "a","s","d","f","g","h","j","k","l","z","x","c","v","b","n","m",
        "Q","W","E","R","T","Y","U","I","O","P","A","S","D","F","G","H",
        "J","K","L","Z","X","C","V","B","N","M","1","2","3","4","5","6",
        "7","8","9","0","!","@","#","$","%","&","*","?"};
        Random rand = new SecureRandom();
        for(int i = 0; i<sz; i++){
            int number = rand.nextInt(70);
            rtrn+=arr[number];
        }
        return rtrn;
    }
}
