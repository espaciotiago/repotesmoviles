package utilities;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tiago on 20/09/16.
 */
public class Constants {

    /**
     * ---------------------------------------------------------------------------------------------
     * CONSTANTS
     * ---------------------------------------------------------------------------------------------
     */

    //Timeout for the connection
    public static final int TIMEOUTS = 5000;

    /*
    *For permission
    */
    public static final int PERMISO_GPS = 1;
    public static final int PERMISO_CAMARA = 2;

    /**
     * URLs
     */
    public static final String URL = "http://ufomobile.co/mijamundi/app_backend";
    public static final String URL_SIGNUP = URL+"/signup.php";
    public static final String URL_USER_IMAGE = URL+"/userImage.php";
    public static final String URL_LOGIN = URL+"/login.php";
    /**
     * EXTRAS
     */
    public static final String IMG_CONS = "REPMJ_";
    public static final String IMG_CONS_USER = "USR_";
    public static final String IMG_CONS_REP = "REP_";
    public static final String IMG_EXTENSION = ".jpg";

    public static final int SUCCESS_CODE = 1;
    public static final int FAIL_CODE = 2;

    public static final String USER_EXTRA = "usuario";
    public static final String NAME_EXTRA = "nombre";
    public static final String MAIL_EXTRA = "correo";
    public static final String ID_NUMBER_EXTRA = "cedula";
    public static final String PHONE_EXTRA = "telefono";
    public static final String GENDER_EXTRA = "genero";
    public static final String PASSWORD_EXTRA = "contrasena";
    public static final String IMAGE_EXTRA = "imagen";
    public static final String DATE_EXTRA = "fecha";
    public static final String RESPONSE_EXTRA = "respuesta";
    public static final String STATUS_EXTRA = "estado";
    public static final String MESSAGE_EXTRA = "mensaje";

    /**
     * ---------------------------------------------------------------------------------------------
     * STATIC METHODS
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * Convert an input stream object into a String object
     * @param ists
     * @return
     * @throws IOException
     */
    public static String convertinputStreamToString(InputStream ists)
            throws IOException {
        if (ists != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader r1 = new BufferedReader(new InputStreamReader(ists, "UTF-8"));
                while ((line = r1.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                ists.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * Get the String with the actual date
     * @return today string
     */
    public static String getDateString(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    /**
     * Get the String with the actual date
     * @return today string
     */
    public static Date getActualDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = new Date();
        return today;
    }

    /**
     * Convert a image into String
     * @param bmp
     * @return
     */
    public static String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    /**
     * Creates a name for the image to send
     * @return
     */
    public static String giverImageName(String img){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String date = dateFormat.format(new Date());
        String imageName = IMG_CONS;
        if(img.equals(IMG_CONS_REP)){
            imageName+=IMG_CONS_REP;
        }else if(img.equals(IMG_CONS_USER)){
            imageName+=IMG_CONS_USER;
        }
        imageName+=date+IMG_EXTENSION;
        return imageName;
    }
}
