package nemosofts.streambox.utils.encrypter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import nemosofts.streambox.BuildConfig;
import nemosofts.streambox.item.ItemVideoDownload;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.AsyncTaskExecutor;
import nemosofts.streambox.utils.helper.DBHelper;
import okio.BufferedSource;

public class Encrypt {

    private DBHelper dbHelper;
    private Context context;

    @SuppressLint("StaticFieldLeak")
    private static Encrypt instance = null;

    private Encrypt() {}

    public static Encrypt getInstance() {
        if (instance == null) {
            instance = new Encrypt();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
    }

    public String getEditedFileName(File file, String token) {
        String path = file.getAbsolutePath();
        int i = path.lastIndexOf('.');
        return (i > 0 ? path.substring(0, i) : path) + token;
    }

    public void encrypt(String fileName, BufferedSource bufferedSource, final ItemVideoDownload itemDownload) {
        try {

            File fileEncrypt = new File(getEditedFileName(
                    new File(fileName.concat(ApplicationUtil.containerExtension(itemDownload.getContainerExtension()))), "")
            );
            final String fileSavedName = fileEncrypt.getName().replace(
                    ApplicationUtil.containerExtension(itemDownload.getContainerExtension()), ""
            );
            itemDownload.setTempName(fileSavedName);

            Cipher encryptionCipher = Cipher.getInstance("AES/CTR/NoPadding");

            // Securely generate random IV
            String inputString = BuildConfig.ENC_KEY;
            byte[] secretKey   = inputString.getBytes();

            byte[] initialIv = BuildConfig.IV.getBytes();

            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
            encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(initialIv));

            try (InputStream fis = bufferedSource.inputStream();
                 OutputStream fileStream = new BufferedOutputStream(new FileOutputStream(fileEncrypt));
                 OutputStream outputStream = new CipherOutputStream(fileStream, encryptionCipher)) {

                byte[] buffer = new byte[2048];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
            }

            new AsyncTaskExecutor<String, String, String>() {
                String imageName;

                @Override
                protected String doInBackground(String strings) {
                    imageName = getBitmapFromURL(itemDownload.getStreamIcon(), fileSavedName);
                    if (!imageName.equals("0")) {
                        return "1";
                    } else {
                        return "0";
                    }
                }

                @Override
                protected void onPostExecute(String s) {
                    if (!s.equals("1")) {
                        imageName = "null";
                    }
                    itemDownload.setStreamIcon(imageName);
                    itemDownload.setTempName(fileSavedName);
                    dbHelper.addToDownloads(DBHelper.TABLE_DOWNLOAD_MOVIES, itemDownload);
                }
            }.execute();
        } catch (Exception e) {
            ApplicationUtil.log("Encrypt", "Error encrypting file",e);
        }
    }

    public String getBitmapFromURL(String src, String name) {
        if (src != null && src.isEmpty()) {
            return "0";
        }

        try (InputStream input = getInputStream(src)) {
            Bitmap myBitmap = BitmapFactory.decodeStream(input);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            myBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);

            // Define the file path and create necessary directories
            File root = new File(context.getExternalFilesDir("").getAbsolutePath() + File.separator + "/tempim/");
            if (!root.exists()) {
                root.mkdirs();
            }

            // Create the file
            File f = new File(root, name + ".jpg");
            if (!f.exists()) {
                boolean created = f.createNewFile();
                if (!created) {
                    ApplicationUtil.log("File Creation", "File could not be created.");
                }
            }

            // Write the byte array to the file
            try (FileOutputStream fo = new FileOutputStream(f)) {
                fo.write(bytes.toByteArray());
            }

            return f.getAbsolutePath();
        } catch (IOException e) {
            return "0";
        }
    }

    private static InputStream getInputStream(String src) throws IOException {
        if (src == null || src.isEmpty()) {
            throw new IOException("Invalid URL: " + src);
        }
        URL url = new URL(src);
        InputStream input;
        if(src.contains("https://")) {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
        } else {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
        }
        return input;
    }
}