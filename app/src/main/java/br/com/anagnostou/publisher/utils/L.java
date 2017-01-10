package br.com.anagnostou.publisher.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class L {
    public static void m(String message)
    {
        Log.d("HIELSEN",message);
    }
    public static void t(Context context, String message)
    {
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show();
    }
}