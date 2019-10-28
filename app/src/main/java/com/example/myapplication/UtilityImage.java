package com.example.myapplication;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;

import com.example.myapplication.ui.fragment_utente.Utente;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.ByteArrayOutputStream;

public class UtilityImage {

    //da array di byte a String
    public static byte[] getProfilo(String codificaImmagine) {
        if (codificaImmagine == null || codificaImmagine.length() == 0) return null;
        byte[] decodedString = Base64.decode(codificaImmagine, Base64.DEFAULT);
        return decodedString;
    }

    public static String BitmapToString(Bitmap bi, int rotazione, ImageView img) {
        bi = RotateBitmap(bi, rotazione);
        Bitmap bitmap;

        //operazioni per comprimere l'immagine e inserirla nel db

        bitmap = scaleBitmap(bi, img.getWidth(), img.getHeight());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] bytes = stream.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);


    }

    public static Bitmap scaleBitmap(Bitmap bitmapToScale, float newWidth, float newHeight) {
        if (bitmapToScale == null)
            return null;
        //get the original width and height
        int width = bitmapToScale.getWidth();
        int height = bitmapToScale.getHeight();
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bitmap
        matrix.postScale(newWidth / width, newHeight / width);

        // recreate the new Bitmap and set it back

        return Bitmap.createBitmap(bitmapToScale, 0, 0, bitmapToScale.getWidth(), bitmapToScale.getHeight(), matrix, true);
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);


    }

    public static boolean prova(DocumentSnapshot ds){
        Object obj = ds.toObject(Object.class);
        if (obj instanceof Utente){
            return true;
        }
        return false;
    }

    public static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            try {
                Bitmap bmOut = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                return bmOut;
            }catch (Exception e){}
        }
        return bm;
    }

    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    public static String getRealPathFromURI(Uri contentUri, FragmentActivity activity) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = activity.getContentResolver().query(contentUri, proj, null, null,
                    null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}

