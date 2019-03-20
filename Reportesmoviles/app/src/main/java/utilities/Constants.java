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
    public static final String URL_CREATE_REPORT = URL+"/createReport.php";
    public static final String URL_CREATE_IMAGES_REPORT = URL+"/createReportImages.php";
    public static final String URL_GET_REPORTS = URL+"/getReports.php";
    public static final String URL_GET_REPORT_IMAGES = URL+"/getReportImages.php";
    public static final String URL_GET_MY_REPORTS = URL+"/getMyReports.php";
    public static final String URL_SUPPORT = URL+"/support.php";
    public static final String URL_COMMENT = URL+"/comment.php";
    public static final String URL_GET_COMMENTS = URL+"/getComments.php";
    public static final String URL_GET_USER_STATS = URL+"/getUserStats.php";
    public static final String URL_UPDATE_USER = URL+"/updateUser.php";

    /**
     * EXTRAS
     */
    public static final int IMAGE_COMPLETE = 0;
    public static final int IMAGE_THUMBNAIL = 1;

    public static final String IMG_CONS = "REPMJ_";
    public static final String IMG_CONS_USER = "USR_";
    public static final String IMG_CONS_REP = "REP_";
    public static final String IMG_EXTENSION = ".jpg";

    public static final int SUCCESS_CODE = 1;
    public static final int FAIL_CODE = 2;

    public static final String ID_EXTRA = "id";
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

    public static final String REPORT_EXTRA = "reporte";
    public static final String IMAGES_REPORT_EXTRA = "imagenes";
    public static final String TITLE_EXTRA = "titulo";
    public static final String DESCRIPTION_EXTRA = "descripcion";
    public static final String ADDRESS_EXTRA = "direccion";
    public static final String LONGITUDE_EXTRA = "longitud";
    public static final String LATITUDE_EXTRA = "latitud";
    public static final String CATEGORY_EXTRA = "categoria";
    public static final String SUPPORTS_EXTRA = "apoyo";
    public static final String COMMENTS_EXTRA = "comentario";
    public static final String SUPPORTING_EXTRA = "apoyando";
    public static final String ALL_REPORTS_EXTRA = "reportes";
    public static final String COMMENT_EXTRA = "comentario";


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

    /**
     * Get the status give its status id
     * @return
     */
    public static String getStatus(String statusId){
        String ret = Report.PUBLISHED;
        switch (statusId){
            case "P":
                ret = Report.PUBLISHED;
                break;
            case "T":
                ret = Report.IN_PROCESS;
                break;
            case "S":
                ret = Report.SOLVED;
                break;
        }
        return ret;
    }
}
