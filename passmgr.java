import csc3055.cli.LongOption;
import csc3055.cli.OptionParser;
import csc3055.json.JsonIO;
import csc3055.util.Tuple;
import org.bouncycastle.jcajce.spec.ScryptKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

public class passmgr {

    public static void main(String[] args)
    {
        //trial 1
        // -a -s NSA.gov -u BigBro

        //trial 2
        // -l NSA.gov

        //the actual main for when everything is done
        // Make sure we have a suitable number of arguments.
        if (args.length < 1)
            usage();

        startup();

        if (!processArgs(args))
            usage();

        shutdown();
    }

    //collection storing data
    private static Collection entriesCollection;

    private static boolean addFile = false;

    private static boolean genPassword = false;
    private static int passwordSize;//used for -g
    private static boolean lookupURL = false;//used for -l
    private static String URLtoLookUp = null;//used for -l

    private static String site = null;//used for new entry
    private static String password = null;//used for new entry
    private static String user = null;//used for new entry
    private static String masterPassword = "MappingReductionsAreHard123";
    private static Key masterKey = null;

    /**
     * loads in the vault and if it doesnt exist makes a new one
     */
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

    /**
     * writes the entries to the vault
     */
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

    /**
     * generates a password of random charaters of the given length
     * @param sz the size of the password
     * @return the password in plaintext
     */
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

    /**
     * Uses the salt and the master password to create a master key used to encrypt all entries
     */
    public static void ScryptPassword() {

        final int COST = 2048;          // A.K.A Iterations
        final int BLK_SIZE = 8;
        final int PARALLELIZATION = 1;  // Number of parallel threads to use.
        final int KEY_SIZE=128;
        ScryptKeySpec scryptSpec;



        Security.addProvider(new BouncyCastleProvider());


        if(entriesCollection.getSalt()==null){
            byte[] byteSalt = new byte[16];
            SecureRandom rand = new SecureRandom();
            rand.nextBytes(byteSalt);
            String strSalt = Base64.getEncoder().encodeToString(byteSalt);
            entriesCollection.setSalt(strSalt);

        }

        scryptSpec = new ScryptKeySpec(masterPassword.toCharArray(),
                Base64.getDecoder().decode(entriesCollection.getSalt()), COST, BLK_SIZE,
                PARALLELIZATION, KEY_SIZE);

        // Generate the secrete key.
        SecretKey key;
        try {
            key = SecretKeyFactory.getInstance("SCRYPT").generateSecret(scryptSpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        masterKey = key;
    }

    /**
     * Encrypts the passed msg with the master key and a generated iv
     * @param msg the msg passed in
     * @return an arraylist where the first entry is cipher text and the second is the iv
     */
    public static ArrayList<String> AESencrypt(String msg){

        //in case the masterKey isn't made yet, its made here
        if(masterKey == null){
            ScryptPassword();
        }

        int tagSize = 128;
        // Set up an AES cipher object.
        Cipher aesCipher = null;
        try {
            aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }

        // Generate the IV.
        SecureRandom rand = new SecureRandom();
        byte[] rawIv = new byte[16];		// Block size of AES.
        rand.nextBytes(rawIv);					// Fill the array with random bytes.
        GCMParameterSpec gcmParams = new GCMParameterSpec(tagSize, rawIv);

        // Put the cipher in encrypt mode with the specified key.
        try {
            aesCipher.init(Cipher.ENCRYPT_MODE, masterKey, gcmParams);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

        //updateAAD here before doFinal is called
        //got help from william for the updateAAD method
        aesCipher.updateAAD(user.getBytes(StandardCharsets.UTF_8));
        aesCipher.updateAAD(site.getBytes(StandardCharsets.UTF_8));

        // Finalize the message. Note: we have specified our type of character
        // encoding for our call to getBytes() this is, on occasion important.
        // Especially if an interface we use mixes UNICODE and ASCII.
        byte[] ciphertext = new byte[0];
        try {
            ciphertext = aesCipher.doFinal(msg.getBytes(StandardCharsets.US_ASCII));
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }

        ArrayList<String> rtrn = new ArrayList<>();
        rtrn.add(Base64.getEncoder().encodeToString(ciphertext));//C text
        rtrn.add(Base64.getEncoder().encodeToString(rawIv));//iv


        return rtrn;
    }

    /**
     * decrypts the given ctext wit the master key and the given iv
     * @param ciphertext the encrypteed password
     * @param iv the iv used to encrypt and decrypt the password
     * @return the password in plaintext
     */
    public static String AESdecrypt(String ciphertext, String iv){

        //in case the masterKey isn't made yet, its made here
        if(masterKey == null){
            ScryptPassword();
        }

        int tagSize = 128;		// 128-bit authentication tag.

        // Set up an AES cipher object.
        Cipher aesCipher = null;
        try {
            aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }

        SecretKeySpec aesKey = new SecretKeySpec(masterKey.getEncoded(),
                "AES");

        // Put the cipher in encrypt mode with the specified key.
        try {
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey,
                    new GCMParameterSpec(tagSize, Base64.getDecoder().decode(iv)));
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

        //got help with the update AAD from william
        aesCipher.updateAAD(entriesCollection.getEntry(URLtoLookUp).getString("user").getBytes(StandardCharsets.UTF_8));
        aesCipher.updateAAD(entriesCollection.getEntry(URLtoLookUp).getString("url").getBytes(StandardCharsets.UTF_8));

        // Finalize the message.
        byte[] plaintext = new byte[0];
        try {
            plaintext = aesCipher.doFinal(Base64.getDecoder().decode(ciphertext));
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }

        return(new String(plaintext));
    }


    /**
     * prints the usage so you know what you are doing
     */
    public static void usage()
    {
        System.out.println("usage:");
        System.out.println("  passmgr --add --site <url> --user <uname>");
        System.out.println("  passmgr --add --site <url> --user <uname> --gen <len>");
        System.out.println("  passmgr --lookup <url>");
        System.out.println("options:");
        System.out.println("  -a, --add Adds an account.");
        System.out.println("  -s, --site Specifies the URL of the site.");
        System.out.println("  -u, --user Specifies the user.");
        System.out.println("  -g, --gen Generate a password of given length.");
        System.out.println("  -l, --lookup Displays the account information for the URL.");
        System.exit(1);
    }

    /**
     * reads in the args and does stuff based on them
     * @param args the args passed in to the command line
     * @return true if the args are valid else false
     */
    public static boolean processArgs(String[] args)
    {
        OptionParser optParser = new OptionParser(args);
        Tuple<Character, String> currOpt;
        if(!passwordAccepted()){
            System.out.println("Invalid Password, you are not the Manager");
            return true;//we don't want usage to print
        }

        // Set up the option parser.
        optParser.setOptString("as:u:g:l:");
        LongOption[] lopts = new LongOption[5];
        lopts[0] = new LongOption("add", false, 'a');
        lopts[1] = new LongOption("site", true, 's');
        lopts[2] = new LongOption("user", true, 'u');
        lopts[3] = new LongOption("gen", true, 'g');
        lopts[4] = new LongOption("lookup", true, 'l');
        optParser.setLongOpts(lopts);

        while(optParser.getOptIdx() != args.length)
        {
            currOpt = optParser.getLongOpt(false);
            if (currOpt == null)
                return false;

            switch(currOpt.getFirst())
            {
                case 'a':		// Add a file.
                    if (addFile != false && lookupURL != false)
                    {
                        System.out.println("Only one operation can be chosen at a time.");
                        return false;
                    }
                    addFile = true;
                    break;
                case 's':
                    site = currOpt.getSecond();
                    break;
                case 'u':
                    user = currOpt.getSecond();
                    break;
                case 'g':
                    genPassword = true;
                    passwordSize = Integer.valueOf(currOpt.getSecond());
                    break;
                case 'l':
                    if (addFile && lookupURL)
                    {
                        System.out.println("Only one operation can be chosen at a time.");
                        return false;
                    }
                    lookupURL = true;
                    URLtoLookUp = currOpt.getSecond();
                    break;
                default: // we have an unknown option or an argument here
                    return false;
            } // end switch
        } // end while

        if (lookupURL){
            if(entriesCollection.getEntry(URLtoLookUp) == null){
                System.out.println("No Account Exists for this URL");
                return true;
            }
            System.out.println("Username: "+entriesCollection.getEntry(URLtoLookUp).get("user"));
            String encryptedPass = entriesCollection.getEntry(URLtoLookUp).getString("pass");
            String unlockedPass = AESdecrypt(encryptedPass,
                    entriesCollection.getEntry(URLtoLookUp).getString("iv"));

            System.out.println("Password: "+unlockedPass);
        }
        if(addFile){
            if(genPassword){
                if(passwordSize<7) {
                    if (passwordSize < 1) {
                        System.out.println("Password size must be positive, no account added");
                        return true;
                    }
                    System.out.println("Password is very weak, fewer than 7 characters");
                }
                password = generatePassword(passwordSize);
            }else{
                password = getPassword();
            }

            ArrayList<String> arr = AESencrypt(password);

            entriesCollection.addEntry(site,user,arr.get(1),arr.get(0));
        }

        return (addFile != lookupURL);
    }

    /**
     * asks for password and returns  if it is the same and false if different
     * @return true if it is the same and false if different
     */
    public static boolean passwordAccepted(){
        Scanner in = new Scanner(System.in);
        System.out.print("Manager Password: ");
        String allegedPassword = in.nextLine();
        return allegedPassword.equals((masterPassword));
    }

    /**
     * gets the password for the entry from the console after prompting the user
     * @return the password in plaintext from the user
     */
    public static String getPassword(){
        Scanner in = new Scanner(System.in);
        System.out.print("Website Password: ");
        return in.nextLine();
    }
}
