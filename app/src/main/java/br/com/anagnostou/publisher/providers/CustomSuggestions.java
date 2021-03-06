package br.com.anagnostou.publisher.providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import br.com.anagnostou.publisher.DBAdapter;

/**
 * Created by George on 21/08/2016.
 * JUST TOO COMPLICATED
 */
public class CustomSuggestions extends ContentProvider {
    DBAdapter dbAdapter;
    SQLiteDatabase sqLiteDatabase;

    private static final String AUTHORITY = "anagnostou.publisher.providers";
    //public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/publicador");
    private static final int SUGGESTIONS_PUBLICADOR = 1;
    private static final int SEARCH_PUBLICADOR = 2;
    private static final int GET_PUBLICADOR = 3;

    UriMatcher mUriMatcher = buildUriMatcher();

    private UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Suggestion items of Search Dialog is provided by this uri
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,SUGGESTIONS_PUBLICADOR);
        // This URI is invoked, when user presses "Go" in the Keyboard of Search Dialog
        // Listview items of SearchableActivity is provided by this uri
        // See android:searchSuggestIntentData="content://in.wptrafficanalyzer.searchdialogdemo.provider/countries" of searchable.xml
        uriMatcher.addURI(AUTHORITY, DBAdapter.DBHelper.TN_PUBLICADOR, SEARCH_PUBLICADOR);

        // This URI is invoked, when user selects a suggestion from search dialog or an item from the listview
        uriMatcher.addURI(AUTHORITY, DBAdapter.DBHelper.TN_PUBLICADOR +"/#", GET_PUBLICADOR);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbAdapter = new DBAdapter(getContext());
        sqLiteDatabase = dbAdapter.mydbHelper.getReadableDatabase();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor c = null;
        switch(mUriMatcher.match(uri)){

            case SUGGESTIONS_PUBLICADOR :
                c = dbAdapter.getPublicadores(selectionArgs);
                break;
            case SEARCH_PUBLICADOR :
                c = dbAdapter.getPublicadores(selectionArgs);
                break;
            case GET_PUBLICADOR :
                String id = uri.getLastPathSegment();
                c = dbAdapter.getPublicador(id);
        }
        //L.m("mUriMatcher.match(uri)" + mUriMatcher.match(uri));
        //mUriMatcher.match(uri)1
        return c;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

}