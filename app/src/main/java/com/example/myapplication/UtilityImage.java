package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Base64;
import android.widget.ImageView;

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
}

