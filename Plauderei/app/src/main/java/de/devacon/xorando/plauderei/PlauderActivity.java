package de.devacon.xorando.plauderei;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;


public class PlauderActivity extends Activity  implements View.OnClickListener , MediaPlayer.OnCompletionListener{
    MediaPlayer player = null;

    ArrayList<String> array = new ArrayList<>();
    int nextFile = 0 ;
    ImageButton button = null;
    private ImageView person = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plauder);
        button = (ImageButton)findViewById(R.id.button);
        button.setOnClickListener(this);
        person = (ImageView)findViewById(R.id.person);
        SharedPreferences prefs = getSharedPreferences("Plauderei",MODE_MULTI_PROCESS);
        AdminActivity.initPerson(prefs);
        person.setImageResource(AdminActivity.activePerson.resIcon);
        fillArray();
    }

    private void fillArray() {
        File dir = new File(Environment.getExternalStorageDirectory()+ "/.Plauderer",AdminActivity.activePerson.name);
        dir.mkdirs();
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if(filename.toUpperCase().endsWith(".3GP")) {
                    File file = new File(dir, filename);

                    return file.canRead() && !file.isDirectory();
                }
                return false;
            }
        });
        if(files == null)
            return;
        array.clear();
        nextFile = 0;
        for(File file : files) {

                array.add(file.getAbsolutePath());

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_plauder, menu);
        return true;
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     * <p>This method is never invoked if your activity sets
     * {@link android.R.styleable#AndroidManifestActivity_noHistory noHistory} to
     * <code>true</code>.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fillArray();
        person.setImageResource(AdminActivity.activePerson.resIcon);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,AdminActivity.class);
            startActivityForResult(intent, 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        playNext();
    }

    private void playNext() {
        if(array.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Keine Aufnahme vorhanden!",Toast.LENGTH_LONG).show();
            return;
        }
        if(nextFile >= array.size())
            nextFile = 0 ;

        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        String uri = array.get((nextFile));
        try {
            player.setDataSource(uri);
            player.prepare();
            player.start();
            button.setVisibility(View.INVISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        nextFile++;
    }

    /**
     * Called when the end of a media source is reached during playback.
     *
     * @param mp the MediaPlayer that reached the end of the file
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        player.release();
        player = null;
        button.setVisibility(View.VISIBLE);
    }
}
