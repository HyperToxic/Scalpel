# Scalpel
Auto wired framework for Android

### Latest version
[ ![Download](https://api.bintray.com/packages/nickandroid/maven/scalpel/images/download.svg) ](https://bintray.com/nickandroid/maven/scalpel/_latestVersion)

### Features
- Auto find views, int, String, bool, array...
- OnClick listener, action, args.
- Auto bind AIDL service.
- Auto find System services, PowerManager, TelephonyManager, etc
- Auto require permission (for SDK above M).

### Usage

Add dependencies
``` java
dependencies {
    compile 'com.nick.scalpel:scalpel:0.3'
}
```

1. Configurations customize:
``` java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Scalpel.getDefault().config(Configuration.builder().autoFindIfNull(true).debug(true).logTag("Scalpel").build());
    }
}
```

2. Use auto activity or wire things manually
``` java
public class MainActivity extends ScalpelAutoActivity {}

public class MyFragment extends ScalpelAutoFragment {}

public class MyService extends ScalpelAutoService {}
```

``` java
public class ViewHolder {
    @AutoFound(id = R.id.toolbar) // Same as @AutoFound(id = R.id.toolbar, type = Type.Auto)
            Toolbar toolbar;

    @AutoFound(id = R.id.fab)
    @OnClick(listener = "mokeListener")
    FloatingActionButton fab;

    @AutoFound
    TelephonyManager tm;

    @AutoFound
    NotificationManager nm;

    ViewHolder(Context context) {
        View rootV = LayoutInflater.from(context).inflate(R.layout.activity_main, null);
        Scalpel.getDefault().wire(rootV, this);
        Scalpel.getDefault().wire(context, this);

        log(toolbar, fab, hello, size, color, text, bool, strs, ints);
    }
}
```

### Example
``` java
public class MainActivity extends ScalpelAutoActivity implements AutoBind.Callback {

    @AutoFound(id = R.id.toolbar, type = Type.View)
    Toolbar toolbar;

    @AutoFound(id = R.id.fab)
    @OnClick(action = "showSnack", args = {"Hello, I am a fab!", "Nick"})
    FloatingActionButton fab;

    @AutoFound(id = R.id.hello)
    @OnClick(listener = "mokeListener")
    TextView hello;

    @AutoFound(id = R.integer.size, type = Type.Integer)
    int size;

    @AutoFound(id = R.color.colorAccent, type = Type.Color)
    int color;

    @AutoFound(id = R.string.app_name, type = Type.String)
    String text;

    @AutoFound(id = R.bool.boo, type = Type.Bool)
    boolean bool;

    @AutoFound(id = R.array.strs, type = Type.StringArray)
    String[] strs;

    @AutoFound(id = R.array.ints, type = Type.IntArray)
    int[] ints;

    @AutoFound
    AlarmManager alarmManager;

    @AutoBind(action = "com.nick.service", pkg = "com.nick.scalpeldemo", callback = "this")
    IMyAidlInterface mService;

    private View.OnClickListener mokeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);

        hello.setTextSize(size);
        hello.setTextColor(color);
        hello.setText(text + "-" + bool + "-" + Arrays.toString(strs) + "-" + Arrays.toString(ints));

        new ViewHolder(this);
    }

    public void showSnack(String content, String owner) {
        Snackbar.make(getWindow().getDecorView(), owner + ": " + content, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onServiceBound(ComponentName name, ServiceConnection connection) {
        mCallback.onServiceBound(name, connection);
    }
}
```
