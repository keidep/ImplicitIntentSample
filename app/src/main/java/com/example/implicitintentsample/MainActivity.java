package com.example.implicitintentsample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    //緯度フィールド
    private double _latitude = 0;
    //経度フィールド
    private double _longitude = 0;
    //FusedLocationProviderClientオブジェクトフィールド。
    private FusedLocationProviderClient _fusedLocationClient;
    //LocationRequestオブジェクトフィールド。
    private LocationRequest _locationRequest;
    //位置情報が変更されたときの処理を行うコールバックオブジェクトフィールド。
    private OnUpdateLocation _onUpdateLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //FusedLocationProviderClientオブジェクトを取得。
        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        //LocationRequestオブジェクトを生成。
        LocationRequest.Builder builder = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,5000);
        //LocationRequestオブジェクトを生成。
        _locationRequest = builder.build();
        //位置情報が変更されたときの処理を行うコールバックオブジェクトを生成。
        _onUpdateLocation = new OnUpdateLocation();
    }
    public void onMapSearchButtonClick(View view) {
        //入力欄に入力されたキーワード文字列を取得。
        EditText etSearchWord = findViewById(R.id.etSearchWord);
        String searchWord = etSearchWord.getText().toString();

        try {
            //入力されたキーワードをURLエンコード。
            searchWord = URLEncoder.encode(searchWord, "UTF-8");
            //マップアプリと連携するURI文字列を生成。
            String uriStr = "geo:0,0?q=" + searchWord;
            //URI文字列からURIオブジェクトを生成。
            Uri uri = Uri.parse(uriStr);
            //Intentオブジェクトを生成。
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            //アクティビティを起動。
            startActivity(intent);
        } catch (UnsupportedEncodingException ex) {
            Log.e("MainActivity", "検索キーワード変換失敗", ex);
        }
    }
    public void onMapShowCurrentButtonClick(View view) {
        //フィールドの緯度と経度の値をもとにマップアプリと連携するURI文字列を生成。
        String uriStr = "geo:" + _latitude + "," + _longitude + "?z=15";
        //URIの文字列からURIオブジェクトを生成。
        Uri uri = Uri.parse(uriStr);
        //Intentオブジェクトを生成。
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        //アクティビティを起動。
        startActivity(intent);
    }

    private class OnUpdateLocation extends LocationCallback{
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            //直近の位置情報を取得
            Location location = locationResult.getLastLocation();
            if(location != null){
                //locationオブジェクトから緯度を取得。
                _latitude = location.getLatitude();
                //locationオブジェクトから経度を取得。
                _longitude = location.getLongitude();
                //取得した緯度をTextViewに表示。
                TextView tvLatitude = findViewById(R.id.tvLatitude);
                tvLatitude.setText(String.valueOf(_latitude));
                //取得した経度をTextViewに表示。
                TextView tvLongitude = findViewById(R.id.tvLongitude);
                tvLongitude.setText(String.valueOf(_longitude));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //ACCESS_FINE_LOCATIONとACCESS_COARSE_LOCATIONの許可が下りていないなら・・・
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //許可をACCESS_FINE_LOCATIONとACCESS_COARSE_LOCATIONに設定。
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            //許可を求めるダイアログを表示、その際、リクエストコードを1000に設定。
            ActivityCompat.requestPermissions(this, permissions, 1000);
            //onResume()メソッドを終了。
            return;
        }
        _fusedLocationClient.requestLocationUpdates(_locationRequest, _onUpdateLocation, Looper.getMainLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        //位置情報の更新を停止。
        _fusedLocationClient.removeLocationUpdates(_onUpdateLocation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        //位置情報のパーミッションでダイアログでかつ許可を選択したなら・・・
        if(requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            //再度許可が下りていないかどうかのチェックをし、下りていないなら処理を中止。
            if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //位置情報の更新を開始。
            _fusedLocationClient.requestLocationUpdates(_locationRequest,_onUpdateLocation,Looper.getMainLooper());
        }
    }
}