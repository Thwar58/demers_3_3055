import csc3055.json.JsonIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;

public class Driver {

    public static void main(String[] args)
    {
        System.out.println("ee");
        startup();
        entriesCollection.addEntry(address, new Entry(username, password));
        System.out.println(entriesCollection.getEntry(address));

    }

    //collection storing data
    public static Collection entriesCollection;
    public static String address = "google.com";
    public static String username = "user123";
    public static String password = "password123";

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
}
