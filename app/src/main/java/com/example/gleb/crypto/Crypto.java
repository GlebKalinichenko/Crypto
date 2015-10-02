package com.example.gleb.crypto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.gleb.crypto.filedialogs.FileChooserActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Crypto extends Activity {
    public static final String TAG = "Tag";
    public Button button;
    public Button decryptButton;
    public Button encryptRsaButton;
    public Button decryptRsaButton;
    public String filePath;
    public String name;
    public byte[] key;
    public byte[] rsaKey;
    public byte[] encryptMd5Hash;
    public byte[] decryptMd5Hash;
    public byte[] md5Hash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto);

        button = (Button) findViewById(R.id.button);
        decryptButton = (Button) findViewById(R.id.decryptButton);
        encryptRsaButton = (Button) findViewById(R.id.encryptRsaButton);
        decryptRsaButton = (Button) findViewById(R.id.decryptRsaButton);

//        securityKey = securityKeyEditText.getText().toString();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Crypto.this, FileChooserActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        encryptRsaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Crypto.this, FileChooserActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        decryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] a = readContentIntoByteArray(new File(filePath));
                byte[] decryptedData = new byte[0];
                try {
                    decryptedData = decrypt(key, a);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String decoded = new String(decryptedData);
                Log.d(TAG, "Decoded " + decoded);
                writeFileString(decoded);
                Toast.makeText(Crypto.this, "Decoded" + new String(decryptedData), Toast.LENGTH_LONG).show();
            }
        });

        decryptRsaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] decryptedData = new byte[0];
                try {
//                    decryptMd5Hash = RSA.decryptMd5(encryptMd5Hash);
                    byte[] b = readHash();
                    decryptMd5Hash = RSA.decryptMd5(b);
                    deleteHash();
                    byte[] a = readContentIntoByteArray(new File(filePath));
                    if (compare(md5Hash, decryptMd5Hash)){
                        Log.d(TAG, String.valueOf(md5Hash));
                        byte[] bytes = RSA.Decrypt(rsaKey);
                        decryptedData = decrypt(bytes, a);
                    }
                    //if (compare(md5Hash, decryptMd5Hash)) {
//                        byte[] bytes = RSA.Decrypt(rsaKey);
//                        decryptedData = decrypt(bytes, a);
                    //}
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String decoded = new String(decryptedData);
                Log.d(TAG, "Decoded " + decoded);
                writeFileString(decoded);
                Toast.makeText(Crypto.this, "Decoded" + new String(decryptedData), Toast.LENGTH_LONG).show();

            }
        });

    }

    public static boolean compare(byte[] b1, byte[] b2){
        if (b1 == null && b2 == null){
            return true;
        }
        if (b1 == null || b2 == null){
            return false;
        }
        if (b1.length != b2.length){
            return false;
        }
        for (int i=0; i<b1.length; i++){
            if (b1[i] != b2[i]){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_crypto, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                boolean fileCreated = false;
                filePath = "";
                name = "";

                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    if (bundle.containsKey(FileChooserActivity.OUTPUT_NEW_FILE_NAME)) {
                        fileCreated = true;
                        File folder = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                        name = bundle.getString(FileChooserActivity.OUTPUT_NEW_FILE_NAME);
                        filePath = folder.getAbsolutePath() + "/" + name;
                    } else {
                        fileCreated = false;
                        File file = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                        filePath = file.getAbsolutePath();
                    }
                }

                String message = fileCreated ? "File created" : "File opened";
                message += ": " + filePath;
                Toast toast = Toast.makeText(Crypto.this, message, Toast.LENGTH_LONG);
                toast.show();

                byte[] b = readContentIntoByteArray(new File(filePath));
                byte[] keyStart = "thiskey".getBytes();
                SecureRandom sr = null;
                KeyGenerator kgen = null;
                try {
                    kgen = KeyGenerator.getInstance("AES");
                    sr = SecureRandom.getInstance("SHA1PRNG");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                sr.setSeed(keyStart);//генерация числа байт
                kgen.init(128, sr); // 192 and 256 bits may not be available
                SecretKey skey = kgen.generateKey();
                key = skey.getEncoded();

                // encrypt
                try {
                    byte[] encryptedData = encrypt(key, b);
                    Toast.makeText(Crypto.this, new String(encryptedData), Toast.LENGTH_LONG).show();
                    writeFileByte(encryptedData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                boolean fileCreated = false;
                filePath = "";
                name = "";

                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    if (bundle.containsKey(FileChooserActivity.OUTPUT_NEW_FILE_NAME)) {
                        fileCreated = true;
                        File folder = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                        name = bundle.getString(FileChooserActivity.OUTPUT_NEW_FILE_NAME);
                        filePath = folder.getAbsolutePath() + "/" + name;
                    } else {
                        fileCreated = false;
                        File file = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                        filePath = file.getAbsolutePath();
                    }
                }

                String message = fileCreated ? "File created" : "File opened";
                message += ": " + filePath;
                Toast toast = Toast.makeText(Crypto.this, message, Toast.LENGTH_LONG);
                toast.show();

//                byte[] b = readContentIntoByteArray(new File(filePath));
//                byte[] keyStart = new byte[0];
//                try {
//                    keyStart = RSA.Encrypt(securityKey);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                SecureRandom sr = null;
//                KeyGenerator kgen = null;
//                try {
//                    kgen = KeyGenerator.getInstance("AES");
//                    sr = SecureRandom.getInstance("SHA1PRNG");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                sr.setSeed(keyStart);
//                kgen.init(128, sr); // 192 and 256 bits may not be available
//                SecretKey skey = kgen.generateKey();
//                key = skey.getEncoded();
//
//                // encrypt
//                try {
//                    byte[] encryptedData = encrypt(key, b);
//                    Toast.makeText(Crypto.this, new String(encryptedData), Toast.LENGTH_LONG).show();
//                    writeFileByte(encryptedData);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                byte[] b = readContentIntoByteArray(new File(filePath));
                byte[] keyStart = "thiskey".getBytes();
                SecureRandom sr = null;
                KeyGenerator kgen = null;
                try {
                    kgen = KeyGenerator.getInstance("AES");
                    sr = SecureRandom.getInstance("SHA1PRNG");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                sr.setSeed(keyStart);
                kgen.init(128, sr); // 192 and 256 bits may not be available
                SecretKey skey = kgen.generateKey();
                key = skey.getEncoded();

                try {
                    File gpxfile = new File(Environment.getExternalStorageDirectory(), "AESKey");
                    FileWriter writer = new FileWriter(gpxfile);
                    writer.append(new String(key));
                    writer.flush();
                    writer.close();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }

                // encrypt
                try {
                    String contentMd5 = readContentIntoString(new File(filePath));
                    md5Hash = MD5.getMD5(contentMd5);
                    byte[] encryptedData = encrypt(key, b);
                    Toast.makeText(Crypto.this, new String(encryptedData), Toast.LENGTH_LONG).show();
                    writeFileByte(encryptedData);
                    rsaKey = RSA.Encrypt(key);
                    encryptMd5Hash = RSA.encryptMd5(md5Hash);
                    writeFileByteContentAndHash(encryptedData, encryptMd5Hash);
                    Toast.makeText(Crypto.this, new String(rsaKey), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private static byte[] readContentIntoByteArray(File file)
    {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try
        {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
            for (int i = 0; i < bFile.length; i++)
            {
                System.out.print((char) bFile[i]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return bFile;
    }

    private static String readContentIntoString(File file)
    {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try
        {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
            for (int i = 0; i < bFile.length; i++)
            {
                System.out.print((char) bFile[i]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return new String(bFile);
    }

//    public void writeFileByte(byte[] bFile) throws IOException {
//        FileOutputStream fos = new FileOutputStream(filePath);
//        fos.write(bFile);
//        fos.close();
//
//    }

    public void writeFileByte(byte[] bFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(bFile);
        fos.close();

    }

    /**
     * Write content and hash
     */
    public void writeFileByteContentAndHash(byte[] bFile, byte[] md5) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        int a = md5.length;
        fos.write(bFile);
        fos.write(md5);
        fos.write((byte) a);
        fos.close();

        byte[] b = readHash();

//        deleteHash();

//        /**
//        * Read hash from content
//        */
//        File target = new File(filePath);
//        RandomAccessFile raf = new RandomAccessFile(target, "rw");
//        raf.seek(target.length() - 129);
//        byte[] b = new byte[128];
//        raf.read(b);
//        raf.close();
//
//        /**
//        * Delete hash
//        * */
//        target = new File(filePath);
//        raf = new RandomAccessFile(target, "rw");
//        raf.seek(target.length() - 129);
//        raf.setLength(raf.length() - 129);
//        raf.close();
    }

    /**
     * Read hash from content
     */
    public byte[] readHash() throws IOException {
        File target = new File(filePath);
        RandomAccessFile raf = new RandomAccessFile(target, "rw");
        raf.seek(target.length() - 129);
        byte[] b = new byte[128];
        raf.read(b);
        raf.close();

        return b;
    }

    /**
     * Delete hash
     * */
    public void deleteHash() throws IOException {
        File target = new File(filePath);
        RandomAccessFile raf = new RandomAccessFile(target, "rw");
        raf.seek(target.length() - 129);
        raf.setLength(raf.length() - 129);
        raf.close();
    }

    public void writeFileString(String value){
        BufferedWriter writer = null;
        try{
            writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(value);
        }
        catch ( IOException e) {
        }
        finally {
            try {
                if ( writer != null)
                    writer.close( );
            }
            catch ( IOException e) {
            }
        }

    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }
}
