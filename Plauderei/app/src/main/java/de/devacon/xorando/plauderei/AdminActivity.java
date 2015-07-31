package de.devacon.xorando.plauderei;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AdminActivity extends Activity  implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener,
        View.OnClickListener ,MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener,ListView.MultiChoiceModeListener,
        MediaPlayer.OnCompletionListener{

    private MediaPlayer player = null;
    SharedPreferences mPrefs = null;
    ArrayList<MyFile> queue = new ArrayList<>();

    enum State {
        GRID,
        PERSON,
        PLAY
    }
    State state = State.GRID;
    class MyFile extends File {

        /**
         * Constructs a new file using the specified directory and name.
         *
         * @param dir  the directory where the file is stored.
         * @param name the file's name.
         * @throws NullPointerException if {@code name} is {@code null}.
         */
        public MyFile(File dir, String name) {
            super(dir, name);
        }
        public MyFile(File file) {
            super(file.getPath());
        }
        public String toString() {
            String ret = getName().toUpperCase();
            ret = ret.substring(0,ret.indexOf(".3GP"));
            try {
                Date date = new Date(Long.parseLong(ret));
                ret = DateFormat.getDateTimeInstance().format(date);

            }
            catch(Throwable any){
                //any.printStackTrace();
            }
            return ret;
        }
    }
    static Person activePerson = new Person("rot",Person.Gender.MALE,"","",R.drawable.personxrot,null);
    //static int personSrc = R.drawable.personxrot;
    //static String personText = "";
    static File rootdir = null;
    File dir = null;
    MediaRecorder recorder = null;
    boolean isRecording = false;
    ImageButton play = null;
    ImageButton stop = null;
    ImageButton list = null;
    ImageButton person = null;
    ImageButton delete = null;
    ListView listView = null;
    ArrayList<MyFile> array = new ArrayList<>();
    //HashMap<String,Integer> personsImages = new HashMap<>();
    //HashMap<String,String> personsNames = new HashMap<>();
    static Person[] persons = new Person[]{
      
            new Person("rot",Person.Gender.MALE,"","Rot",R.drawable.personxrot,null),
            new Person("gruen",Person.Gender.MALE,"","Grün",R.drawable.personxgruen,null),
            new Person("gelb",Person.Gender.FEMALE,"","Gelb",R.drawable.personxgelb,null),
            new Person("orange",Person.Gender.FEMALE,"","Rot",R.drawable.personxorange,null),
            new Person("grau",Person.Gender.MALE,"","Grau",R.drawable.personxgrau,null),
            new Person("blau",Person.Gender.MALE,"","Blau",R.drawable.personxblau,null),
            new Person("braun",Person.Gender.MALE,"","Braun",R.drawable.personxbraun,null),
            new Person("hellblau",Person.Gender.FEMALE,"","Blau",R.drawable.personxhellblau,null),
            new Person("hellgruen",Person.Gender.FEMALE,"","Grün",R.drawable.personxhellgruen,null)
    };
    private GridLayout grid = null;
    private LinearLayout back = null;
    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     * <p/>
     * Impelmenters can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        ArrayAdapter adapter = (ArrayAdapter)listView.getAdapter();
        if(adapter instanceof PersonsAdapter) {
            PersonsAdapter my = (PersonsAdapter)adapter;
            activePerson = persons[ position ];
            dir = new File(rootdir,activePerson.name);
            fillArray();
            listView.setVisibility(View.INVISIBLE);
            grid.setVisibility(View.VISIBLE);
        }
        else if(adapter instanceof TextAdapter) {
            listView.setVisibility(View.INVISIBLE);
            grid.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Perform any final cleanup before an activity is destroyed.  This can
     * happen either because the activity is finishing (someone called
     * {@link #finish} on it, or because the system is temporarily destroying
     * this instance of the activity to save space.  You can distinguish
     * between these two scenarios with the {@link #isFinishing} method.
     * <p/>
     * <p><em>Note: do not count on this method being called as a place for
     * saving data! For example, if an activity is editing data in a content
     * provider, those edits should be committed in either {@link #onPause} or
     * {@link #onSaveInstanceState}, not here.</em> This method is usually implemented to
     * free resources like threads that are associated with an activity, so
     * that a destroyed activity does not leave such things around while the
     * rest of its application is still running.  There are situations where
     * the system will simply kill the activity's hosting process without
     * calling this method (or any others) in it, so it should not be used to
     * do things that are intended to remain around after the process goes
     * away.
     * <p/>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onPause
     * @see #onStop
     * @see #finish
     * @see #isFinishing
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key.  The default implementation simply finishes the current activity,
     * but you can override this to do whatever you want.
     */
    @Override
    public void onBackPressed() {
        setResult(1);

        if(grid.getVisibility() == View.VISIBLE)
            super.onBackPressed();
        else {
            hideListPersons();
            hideListPlay();
            showGrid();
        }

    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if(state == State.PLAY){
            if(parent instanceof ListView) {
                ListView listView = (ListView)parent;
                if(listView.isItemChecked(position)) {
                    playFile(array.get(position));
                    if(view instanceof TextView){
                        ((TextView)view).setTextColor(Color.BLUE);
                    }
                    //listView.setItemChecked(position,true);
                } else {
                    if(view instanceof TextView){
                        ((TextView)view).setTextColor(Color.BLACK);
                    }
                }
            }
        }
        else if(state == State.PERSON){
            activePerson = persons[ position ];
            SharedPreferences.Editor edit = mPrefs.edit();

            edit.putString("personName",activePerson.name);
            edit.apply();
            ((ImageButton)findViewById(R.id.personSelected)).setImageResource(activePerson.resIcon);
            if(parent instanceof ListView) {
                ListView listView = (ListView) parent;
                if (listView.isItemChecked(position)) {

                        view.setBackgroundColor(Color.rgb(0x7f, 0x7f, 0xff));

                    //listView.setItemChecked(position,true);
                } else {
                    view.setBackgroundColor(Color.rgb(0xee,0xee,0xee));

                }
            }
            //personSrc = personsImages.get(persons[ position ]);
            //personText = personsNames.get(persons[ position ]);
            dir = new File(rootdir,activePerson.name);
            fillArray();
        }
    }

    private void playFile(MyFile myFile) {
        player = new MediaPlayer();
        player.setOnCompletionListener(this);

        try {
            player.setDataSource(myFile.getAbsolutePath());
            player.prepare();
            player.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class TextAdapter extends ArrayAdapter {

        /**
         * Constructor
         *
         * @param context  The current context.
         * @param resource The resource ID for a layout file containing a TextView to use when
         *                 instantiating views.
         * @param objects  The objects to represent in the ListView.
         */
        public TextAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);
        }

        /**
         * {@inheritDoc}
         *
         * @param position
         * @param convertView
         * @param parent
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView != null){
                if(convertView instanceof TextView){
                    if(parent instanceof ListView) {
                        if(((ListView)parent).isItemChecked(position))
                            ((TextView) convertView).setTextColor(Color.BLUE);
                    }
                }
            }
            return super.getView(position, convertView, parent);
        }
    }

    class PersonsAdapter extends ArrayAdapter {

        /**
         * Constructor
         *
         * @param context  The current context.
         * @param resource The resource ID for a layout file containing a TextView to use when
         *                 instantiating views.
         * @param objects  The objects to represent in the ListView.
         */
        public PersonsAdapter(Context context, int resource, Person[] objects) {
            super(context, resource, objects);
        }

        /**
         * {@inheritDoc}
         *
         * @param position
         * @param convertView
         * @param parent
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null) {
                RelativeLayout view = (RelativeLayout)getLayoutInflater().inflate(R.layout.list_element,parent,false);
                convertView = view;
            }
            ((ImageView) convertView.findViewById(R.id.imageView)).setImageResource(
                        persons[position].resIcon);
            ((TextView) convertView.findViewById(R.id.textView)).setText(
                        persons[position].toString());

            return convertView;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true ; //super.areAllItemsEnabled();
        }
    }
    private void fillPersonen() {
        /*
        personsImages.clear();
        personsNames.clear();
        personsImages.put("blau", R.drawable.personxblau);
        personsImages.put("gruen", R.drawable.personxgruen);
        personsImages.put("gelb", R.drawable.personxgelb);
        personsImages.put("orange",R.drawable.personxorange);
        personsImages.put("rot",R.drawable.personxrot);
        personsImages.put("grau",R.drawable.personxgrau);
        personsImages.put("hellblau",R.drawable.personxhellblau);
        personsImages.put("hellgruen",R.drawable.personxhellgruen);
        personsImages.put("braun", R.drawable.personxbraun);
        personsNames.put("blau","Herr Blau");
        personsNames.put("hellblau","Frau Blau");
        personsNames.put("gruen","Herr Grün") ;
        personsNames.put("hellgruen","Frau Grün") ;
        personsNames.put("braun","Herr Braun") ;
        personsNames.put("gelb","Frau Gelb") ;
        personsNames.put("rot","Herr Rot") ;
        personsNames.put("orange","Frau Rot") ;
        personsNames.put("grau","Herr Grau") ;*/
    }
    private void fillArray() {
        dir = new File(Environment.getExternalStorageDirectory() +"/.Plauderer",activePerson.name);
        //dir.mkdirs();
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
        array.clear();
        if(files == null)
            return;
        for(File file : files) {

            array.add(new MyFile(file));

        }
    }

    @Override
    public boolean navigateUpTo(Intent upIntent) {
        setResult(1);
        return super.navigateUpTo(upIntent);
    }
    void showPersonDialog() {
        ((EditText)findViewById(R.id.firstName)).setText( activePerson.firstName );
        ((EditText)findViewById(R.id.lastName)).setText(activePerson.lastName );
        ((RadioButton)findViewById(R.id.male)).setChecked(activePerson.gender == Person.Gender.MALE);
        ((RadioButton)findViewById(R.id.female)).setChecked(activePerson.gender == Person.Gender.FEMALE);

        (findViewById(R.id.personDialog)).setVisibility(View.VISIBLE);
    }
    void hidePersonDialog() {
        (findViewById(R.id.personDialog)).setVisibility(View.INVISIBLE);
    }
    void showListPersons() {
        back.setVisibility(View.VISIBLE);
        ((ImageButton)findViewById(R.id.personSelected)).setVisibility(View.VISIBLE);
        state = State.PERSON;
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        //((LinearLayout)back).layout(back.getLeft(), back.getTop(), back.getWidth(), back.getTop() + 600);
    }
    void hideListPersons() {
        back.setVisibility(View.INVISIBLE);
        ((ImageButton)findViewById(R.id.personSelected)).setVisibility(View.INVISIBLE);
    }
    void hideListPlay() {
        back.setVisibility(View.INVISIBLE);
        ((ImageButton)findViewById(R.id.playSelected)).setVisibility(View.INVISIBLE);
        ((ImageButton)findViewById(R.id.deleteSelected)).setVisibility(View.INVISIBLE);
    }
    void showListPlay() {
        back.setVisibility(View.VISIBLE);
        ((ImageButton)findViewById(R.id.playSelected)).setVisibility(View.VISIBLE);
        ((ImageButton)findViewById(R.id.deleteSelected)).setVisibility(View.VISIBLE);
        state = State.PLAY;
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    }
    void showGrid() {
        grid.setVisibility(View.VISIBLE);
        state = State.GRID;
    }
    void hideGrid() {
        grid.setVisibility(View.INVISIBLE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        back = (LinearLayout)findViewById(R.id.backList);
        grid = (GridLayout)findViewById(R.id.grid);
        play = (ImageButton)findViewById(R.id.play);
        stop = (ImageButton)findViewById(R.id.stop);
        list = (ImageButton)findViewById(R.id.list);
        person = (ImageButton)findViewById(R.id.person);
        listView  = (ListView)findViewById(R.id.listview);
        delete =(ImageButton)findViewById(R.id.delete);
        rootdir = new File(Environment.getExternalStorageDirectory() ,".Plauderer");
        mPrefs = getSharedPreferences("Plauderei",MODE_MULTI_PROCESS);
        initPerson(mPrefs);
        fillArray();

        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        list.setOnClickListener(this);
        delete.setOnClickListener(this);
        person.setOnClickListener(this);
        ((ImageButton)findViewById(R.id.deleteSelected)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.playSelected)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.personSelected)).setOnClickListener(this);
        list.setVisibility(View.VISIBLE);
        person.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        stop.setVisibility(View.INVISIBLE);
        hideListPersons();
        hideListPlay();
        hidePersonDialog();
        showGrid();
        //listView.setOnItemSelectedListener(this);
        listView.setOnItemClickListener(this);
        listView.setClickable(true);
        listView.setActivated(true);
        (findViewById(R.id.OK)).setOnClickListener(this);
        //listView.setAdapter(new PersonsAdapter(getApplicationContext(),R.id.imageView,array));
    }

    public static void initPerson(SharedPreferences prefs ) {

        String name = prefs.getString("personName","rot");
        for(Person p : persons) {
            if(p.name.equals(name)){
                activePerson = p ;
                break;
            }
        }
        File dir = new File(rootdir,activePerson.name);
        dir.mkdirs();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin, menu);
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
    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.play) {
            if(!isRecording)
                record();
        }
        else if(v.getId() == R.id.stop) {
            if(isRecording)
                stop();
        }
        else if(v.getId() == R.id.OK) {
            activePerson.firstName = ((EditText)findViewById(R.id.firstName)).getText().toString();
            activePerson.lastName = ((EditText)findViewById(R.id.lastName)).getText().toString();
            if(((RadioButton)findViewById(R.id.male)).isChecked())
                activePerson.gender = Person.Gender.MALE;
            else if(((RadioButton)findViewById(R.id.female)).isChecked()) {
                activePerson.gender = Person.Gender.FEMALE;
            }
            else
                activePerson.gender = Person.Gender.UNKNOWN;
            hidePersonDialog();
            showListPersons();
        }
        else if(v.getId() == R.id.person) {
            //fillPersonen();
            PersonsAdapter my = new PersonsAdapter(this, R.layout.list_element, persons);
            int count = my.getCount();
            listView.setAdapter(my);
            listView.setOnItemClickListener(this);
            hideGrid();
            showListPersons();
            listView.setEnabled(true);
        }
        else if(v.getId() == R.id.list) {
            fillArray();
            listView.setAdapter(new TextAdapter(this, R.layout.play_list, array));
            listView.setMultiChoiceModeListener(this);
            hideGrid();
            showListPlay();
        }
        else if (v.getId() == R.id.personSelected) {
            hideListPersons();
            showPersonDialog();
        }
        else if (v.getId() == R.id.playSelected) {
            SparseBooleanArray positions = listView.getCheckedItemPositions();
            if(positions == null) {
                return;
            }
            for(int i = 0 ; i < array.size();i++) {

                if(positions.get(i)) {
                    queue.add(array.get(i));
                }
            }
            if(queue.size() > 0 ){
                MyFile next = queue.get(0);
                queue.remove(0);
                playFile(next);
            }
        }
        else if (v.getId() == R.id.deleteSelected) {
            SparseBooleanArray positions = listView.getCheckedItemPositions();
            if(positions == null) {
                return;
            }
            for(int i = 0 ; i < array.size();i++) {
                if(positions.get(i)) {
                    array.get(i).delete();
                }
            }
            fillArray();
            listView.setAdapter(new TextAdapter(getApplicationContext(), R.layout.play_list, array));
            listView.setEnabled(true);

        }
        else if (v.getId() == R.id.delete) {
            AlertDialog ad = new AlertDialog.Builder(this).create();
            ad.setCancelable(true); // This blocks the 'BACK' button
            ad.setMessage("Sollen allen Aufnahmen für die aktuelle Person gelöscht werden?");
            ad.setButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(dir.delete()) {
                        dir.mkdirs();
                    }
                    else {
                        File[] files = dir.listFiles();
                        int i = 0;
                        for(File file : files) {
                            file.delete();
                            i++;
                        }
                        if(!dir.delete()) {
                            Toast.makeText(getApplicationContext(), "Fehler beim Löschen", Toast.LENGTH_LONG);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), Integer.toString(i) + " Dateien erfolgreich gelöscht", Toast.LENGTH_LONG);
                        }
                    }

                    dialog.dismiss();
                }
            });
            ad.show();
        }
        else {

            }

    }

    private void stop() {
        recorder.stop();
        recorder.release();
        recorder = null;
        isRecording = false;
        stop.setVisibility(View.INVISIBLE);

        play.setVisibility(View.VISIBLE);
    }

    private void record() {
        recorder = new MediaRecorder();
        recorder.setOnErrorListener(this);
        recorder.setOnInfoListener(this);
        String str = "";
        //recorder.reset();
        int max = recorder.getAudioSourceMax();
        boolean success = false;
        Toast.makeText(getApplicationContext(),"Max:" + Integer.toString(max),Toast.LENGTH_LONG).show();
        for(int i = 0 ; i < max ; ++i) {
            try {
                recorder.setAudioSource(i);
                success = true;
                break;
            } catch (RuntimeException e) {
                e.printStackTrace();
                str = e.getLocalizedMessage();
                Toast.makeText(getApplicationContext(), str + " " + Integer.toString(i), Toast.LENGTH_LONG).show();
                recorder.reset();
            }
        }
        if(!success) {
            recorder.release();
            recorder = null;
            return;
        }
        isRecording = true;
        try {
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        }
        catch(IllegalStateException e) {
            e.printStackTrace();
            str = e.getLocalizedMessage();
            Toast.makeText(getApplicationContext(),str,Toast.LENGTH_LONG).show();
        }

        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        dir.mkdirs();
        File file = new File(dir,Long.toString(System.currentTimeMillis())+ ".3gp");
        recorder.setOutputFile(file.getAbsolutePath());
        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stop.setVisibility(View.VISIBLE);
        play.setVisibility(View.INVISIBLE);
    }

    /**
     * Called when an error occurs while recording.
     *
     * @param mr    the MediaRecorder that encountered the error
     * @param what  the type of error that has occurred:
     *              <ul>
     *              <li>{@link #MEDIA_RECORDER_ERROR_UNKNOWN}
     *              <li>{@link #MEDIA_ERROR_SERVER_DIED}
     *              </ul>
     * @param extra an extra code, specific to the error type
     */
    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        String txt ;
        switch(what) {
            case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
                txt = "Unknown error";
                break;
            case MediaRecorder.MEDIA_ERROR_SERVER_DIED:
                txt = "Server died";
                break;
            default:
                txt = "Unknown Unknown";
                break;
        }
        Toast.makeText(getApplicationContext(), "Media recorder error " + txt, Toast.LENGTH_LONG).show();
    }

    /**
     * Called when an error occurs while recording.
     *
     * @param mr    the MediaRecorder that encountered the error
     * @param what  the type of error that has occurred:
     *              <ul>
     *              <li>{@link #MEDIA_RECORDER_INFO_UNKNOWN}
     *              <li>{@link #MEDIA_RECORDER_INFO_MAX_DURATION_REACHED}
     *              <li>{@link #MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED}
     *              </ul>
     * @param extra an extra code, specific to the error type
     */
    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        String str;

        switch(what) {
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                str = "duration reached";
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                str = "File size reached";
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
                str = "Unknown";
                break;
            default:
                str = "Unknown unknown";
                break;
        }
        Toast.makeText(getApplicationContext(),"info:" + str,Toast.LENGTH_LONG).show();

    }
    /**
     * Called when the end of a media source is reached during playback.
     *
     * @param mp the MediaPlayer that reached the end of the file
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player != null) {
            player.release();
            player = null;
        }
        if(queue.size()>0) {
            MyFile next = queue.get(0);
            queue.remove(0);
            playFile(next);
        }

    }

    /**
     * Called when an item is checked or unchecked during selection mode.
     *
     * @param mode     The {@link ActionMode} providing the selection mode
     * @param position Adapter position of the item that was checked or unchecked
     * @param id       Adapter ID of the item that was checked or unchecked
     * @param checked  <code>true</code> if the item is now checked, <code>false</code>
     */
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

    }

    /**
     * Called when action mode is first created. The menu supplied will be used to
     * generate action buttons for the action mode.
     *
     * @param mode ActionMode being created
     * @param menu Menu used to populate action buttons
     * @return true if the action mode should be created, false if entering this
     * mode should be aborted.
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    /**
     * Called to refresh an action mode's action menu whenever it is invalidated.
     *
     * @param mode ActionMode being prepared
     * @param menu Menu used to populate action buttons
     * @return true if the menu or action mode was updated, false otherwise.
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    /**
     * Called to report a user click on an action button.
     *
     * @param mode The current ActionMode
     * @param item The item that was clicked
     * @return true if this callback handled the event, false if the standard MenuItem
     * invocation should continue.
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    /**
     * Called when an action mode is about to be exited and destroyed.
     *
     * @param mode The current ActionMode being destroyed
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

}
