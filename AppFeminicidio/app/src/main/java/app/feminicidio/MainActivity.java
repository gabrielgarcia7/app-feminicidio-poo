package app.feminicidio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 123;

    //protected Location sLatitudeLabel;

    private Button bDenuncia, bTelefones, bInfo, bEmergenciaL;
    private TextView nomeApp;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    // private FusedLocationProviderClient mFusedLocationClient;

    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializando e referenciando os objetos;
        bDenuncia = (Button) findViewById(R.id.bDenuncia);
        bTelefones = (Button) findViewById(R.id.bTelefones);
        bInfo = (Button) findViewById(R.id.bInfos);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        bEmergenciaL = (Button) findViewById(R.id.bEmergenciaL);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Adicionando a sidebar
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView nvDrawer = (NavigationView) findViewById(R.id.sidebar);
        setupDrawerContent(nvDrawer);

        FirebaseApp.initializeApp(getApplicationContext());



        // Listener do botao "Fazer denuncia", que abre a activity de denuncia;
        bDenuncia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDenunciaActivity();
            }
        });

        // Listener do botao "Telefones uteis", que abre a activity de telefones;
        bTelefones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTelefonesActivity();
            }
        });

        bEmergenciaL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pegando a localizacao do usuario

                getPermissaoLocalizacao();
                getLocalizacao();
                Toast.makeText(MainActivity.this, "Localização enviada!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**
     * Metodo que inicializa e abre a activity de denuncia.
     */
    public void openDenunciaActivity() {
        Intent intent = new Intent(this, DenunciaActivity.class);
        startActivity(intent);
    }

    /**
     * Metodo que inicializa e abre a activity de telefones.
     */
    public void openTelefonesActivity() {
        Intent intent = new Intent(this, TelefonesActivity.class);
        startActivity(intent);
    }

    /**
     * Metodo que inicializa e abre a activity de informacoes.
     */
    public void openInfoActivity() {
        Intent intent = new Intent(this, InformacoesActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.bInfos:
                openInfoActivity();
                return true;
            case R.id.nav_info:
                openInfoActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_button, menu);
        return true;
    }

    public void selectItemDrawer(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_main:
                //something
                break;

            case R.id.nav_denuncia:
                openDenunciaActivity();
                break;
            case R.id.nav_emergencia:
                //something
                break;

            case R.id.nav_telefones:
                openTelefonesActivity();
                break;

            case R.id.nav_info:
                openInfoActivity();
                break;
            default:
                break;
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                selectItemDrawer(menuItem);
                return true;
            }
        });
    }

    private synchronized void callConnection() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("LOG", "onConnected(" + bundle + ")");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(l != null) {
            Log.i("LOG", "latitude: "+l.getLatitude());
            Log.i("LOG", "longitude: "+l.getLongitude());

        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("LOG", "onConnectionSuspended("+i+")");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("LOG", "onConnectionFailed("+connectionResult+")");
    }

    @SuppressLint("MissingPermission")
    public void getLocalizacao(){
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Date currentTime = Calendar.getInstance().getTime();
                            String data = currentTime.toString();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("emergencia/"+data+"/latitude");
                            myRef.setValue(String.valueOf(location.getLatitude()));

                            FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                            DatabaseReference myRef2 = database.getReference("emergencia/"+data+"/longitude");
                            myRef2.setValue(String.valueOf(location.getLongitude()));
                        }
                    }
                });
    }

    public void getPermissaoLocalizacao(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }



}
