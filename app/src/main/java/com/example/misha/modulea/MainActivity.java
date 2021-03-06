package com.example.misha.modulea;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import com.example.misha.modulea.Database.LinkRepository;
import com.example.misha.modulea.Link.MyLink;
import com.example.misha.modulea.Local.LinkDataSourceClass;
import com.example.misha.modulea.Local.LinkDatabase;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {
    ImageButton button;
    static LinkAdapter linkAd;
    TabHost tabHost;
    Button btn;
    TextView tv;
    static Context context;
    static List<MyLink> links = new ArrayList<>();
    Map<MyLink, Integer> status_sort = new HashMap<>();
    Map<MyLink, String> status_date = new HashMap<>();
    ListView lv;
    private CompositeDisposable compositeDisposable;
    private LinkRepository linkRepository;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.status:
                for (MyLink loc : links) {
                    status_sort.put(loc, loc.getStatus());
                }
                Map<MyLink, Integer> map = sortByValues((HashMap) status_sort);
                links = new ArrayList<>(map.keySet());
                linkAd = new LinkAdapter(this, android.R.layout.simple_list_item_1, links);
                lv.setAdapter(linkAd);
                Toast toast4 = Toasty.info(getApplicationContext(), "Sort by status", Toast.LENGTH_SHORT);
                toast4.show();
                break;
            case R.id.date:
                for (MyLink loc : links) {
                    status_date.put(loc, loc.getDate());
                }
                Map<MyLink, Integer> map1 = sortByValuesBackward((HashMap) status_date);
                links = new ArrayList<>(map1.keySet());
                linkAd = new LinkAdapter(this, android.R.layout.simple_list_item_1, links);
                lv.setAdapter(linkAd);
                Toast toast1 = Toasty.info(getApplicationContext(), "Sort by date", Toast.LENGTH_SHORT);
                toast1.show();
        }
        return true;
    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
            }
        });
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    private static HashMap sortByValuesBackward(HashMap map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
        });


        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;



    public static boolean isValid(String url) {
        try {
            new URL(url).toURI();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public int checkURL(String u) throws IOException {
        int status;
        String format = "";
        String extension = "";
        int i = u.lastIndexOf('.');
        if (i > 0) {
            extension = u.substring(i + 1);
        }
        switch (extension) {
            case "gif":
                format += "gif";
                break;
            case "png":
                format += "png";
                break;
            case "jpg":
                format += "jpg";
                break;
            case "jpeg":
                format += "jpeg";
                break;
            case "bmp":
                format += "bmp";
                break;
            case "apng":
                format += "apng";
                break;
            case "ico":
                format += "ico";
                break;
            case "wmp":
                format += "wmp";
                break;
        }
        if (format.equals(extension)) {
            status=1;


        } else {
            status = 2;
        }
        return status;
    }

    TabHost Alayout;
    AnimationDrawable animationDrawable;

    int statAfter = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
        }



        Alayout =  findViewById(R.id.tabHost);
        animationDrawable = (AnimationDrawable) Alayout.getBackground();
        animationDrawable.setEnterFadeDuration(4500);
        animationDrawable.setExitFadeDuration(4500);
        animationDrawable.start();



        enableStrictMode();
        context = getApplicationContext();
        compositeDisposable = new CompositeDisposable();
        LinkDatabase linkDatabase = LinkDatabase.getInstance(this);
        linkRepository = LinkRepository.getmInstance(LinkDataSourceClass.getInstance(linkDatabase.linkDAO()));
        loadData();

        TabHost host =  findViewById(R.id.tabHost);
        host.setup();

        TabHost.TabSpec spec = host.newTabSpec("Test");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Test");
        host.addTab(spec);


        spec = host.newTabSpec("History");
        spec.setContent(R.id.tab2);
        spec.setIndicator("History");
        host.addTab(spec);


        lv =  findViewById(R.id.listview);
        linkAd = new LinkAdapter(this, android.R.layout.simple_list_item_1, links);
        lv.setAdapter(linkAd);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                final int id = i;
                if(links.get(id).getStatus()!=4){
                String url = links.get(id).getJust_link();
                if(!isNetworkConnected()){
                    statAfter=3;
                }else{
                    try {
                        statAfter = checkURL(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Disposable disposablen = io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {

                        links.get(id).setStatus(statAfter);
                        linkRepository.updateOneLink(links.get(id).getId(),statAfter);
                        linkAd.notifyDataSetChanged();
                        emitter.onComplete();
                    }
                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(Object o) throws Exception {

                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {

                            }
                        });
                compositeDisposable.add(disposablen);
                if (links.get(id).getStatus() == 1) {
                    statAfter = 4;
                    Disposable disposablen2 = io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
                        @Override
                        public void subscribe(ObservableEmitter<Object> emitter) throws Exception {

                            links.get(id).setStatus(statAfter);
                            linkRepository.updateOneLink(links.get(id).getId(),statAfter);
                            linkAd.notifyDataSetChanged();
                            emitter.onComplete();
                        }
                    })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new Consumer<Object>() {
                                @Override
                                public void accept(Object o) throws Exception {

                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {

                                }
                            });

                    compositeDisposable.add(disposablen2);
                    Toasty.info(getApplicationContext(),"URL will be deleted from DB in 15 seconds",Toast.LENGTH_LONG).show();
                    start_alarm(id);

                }

                Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.moduleb");
                intent.addCategory("com.example.moduleb");
                intent.putExtra("url", url);
                intent.putExtra("stat", statAfter);
                intent.putExtra("from", "history");
                startActivity(intent);
                finish();
                }else{
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.moduleb");
                    intent.addCategory("com.example.moduleb");
                    intent.putExtra("url", links.get(id).getJust_link());
                    intent.putExtra("stat", 1);
                    intent.putExtra("from", "test");
                    startActivity(intent);
                    finish();
                }
            }
        });


        btn =  findViewById(R.id.button);
        tv =  findViewById(R.id.editText);


        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_test));
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_history));


    }

    private void loadData() {

        Disposable disposable = linkRepository.getAllLinks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<MyLink>>() {
                    @Override
                    public void accept(List<MyLink> myLinks) throws Exception {
                        onGetAllLinkSuccess(myLinks);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toasty.error(MainActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
        compositeDisposable.add(disposable);
    }

    private void onGetAllLinkSuccess(List<MyLink> myLinks) {
        links.clear();
        links.addAll(myLinks);
        linkAd.notifyDataSetChanged();
    }


    int statBefore;
    String field;

    public void downloadImageFromUrl(View view) throws IOException {

        field = tv.getText().toString();

            String field = tv.getText().toString();
            if(isValid(field)){

        if(!isNetworkConnected()){statBefore=3;}else{
            enableStrictMode();
            statBefore = checkURL(field);
            if(statBefore==1){
                statBefore = checkUrlExist(field);
            }
        }


            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            final String date_local = dateFormat.format(date);
            Disposable disposable = io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> emitter) throws Exception {

                    MyLink link = new MyLink(tv.getText().toString(), date_local, statBefore);
                    links.add(link);
                    linkRepository.insertLink(link);
                    linkAd.notifyDataSetChanged();
                    emitter.onComplete();
                }
            })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<Object>() {
                        @Override
                        public void accept(Object o) throws Exception {
                            Toasty.success(MainActivity.this, "Link added!", Toast.LENGTH_SHORT).show();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Toasty.error(MainActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    compositeDisposable.add(disposable);

                Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.moduleb");
                intent.addCategory("com.example.moduleb");
                intent.putExtra("url", tv.getText().toString());
                intent.putExtra("stat", statBefore);
                intent.putExtra("from", "test");
                startActivity(intent);
                finish();
            } else {
               Toasty.error(MainActivity.this, "" + "Field must be filled with URL!", Toast.LENGTH_SHORT).show();

            }

        }


    public int checkUrlExist(String str) throws IOException{
        int statusCode=0;
        URL url = new URL(str);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.connect();
            statusCode = connection.getResponseCode();
        }
        catch (UnknownHostException e)
        {
            Toasty.error(this, " Host not found", Toast.LENGTH_SHORT).show();
            return 2;
        }


        if(statusCode == 404){
            Toasty.error(this," 404",Toast.LENGTH_SHORT).show();
            return 2;
        }else{
            Toasty.success(this," Fine "+Integer.toString(statusCode),Toast.LENGTH_SHORT).show();
            return 1;
        }
    }

    public void start_alarm(int id) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent myIntent = new Intent(this, DB_Delete.class);
        myIntent.putExtra("ID_Link", id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);

        manager.set(AlarmManager.RTC_WAKEUP, new Date().getTime() + 15000, pendingIntent);
    }
    public void enableStrictMode()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}





