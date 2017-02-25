package com.example.administrator.sqlitedemo;

/**
 * Created by Administrator on 2017/1/2.
 */


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class otherActivity extends AppCompatActivity{
    HttpURLConnection httpConn = null;
    InputStream din =null;
    Button find = null;
    TextView tv_show = null;
    Spinner sp_province,sp_city,sp_area;
    TextView tv_sl;
    String db_name = "weather";//数据库名称
    String db_path = "data/data/com.example.administrator.sqlitedemo/database/";//数据库路径，本程序的专属目录 data/data/本程序的包名/database
    //数据库字段名
    String t1_name = "weathers";
    String find1_name = "province_name";
    String find2_name = "city_name";
    String find3_name = "area_name";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp_province=(Spinner)findViewById(R.id.sp_province);//省份选择下拉列表
        sp_city =(Spinner)findViewById(R.id.sp_city);
        sp_area =(Spinner)findViewById(R.id.sp_area);
        tv_sl=(TextView)findViewById(R.id.tv_sl);
        copydb();
        createPro();
        setTitle("天气查询SQLite精简版");
        find = (Button)findViewById(R.id.find);
        tv_show = (TextView)findViewById(R.id.tv_show);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_show.setText("");//清空数据
                Toast.makeText(otherActivity.this, "正在查询天气信息", Toast.LENGTH_SHORT).show();
                String  arr= (String)tv_sl.getText();
                GetJson gd = new GetJson(arr);//调用线程类创建的对象
                gd.start();//运行线程对象
            }
        });
    }
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 123:
                    showData((String)msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private  void showData(String jData){
        String jd =jData;
        String wed="";
        try {
            JSONObject jsonObject = new JSONObject(jd);
            JSONObject cityweather =jsonObject.getJSONObject("data");
            JSONArray forecast =cityweather.getJSONArray("forecast");
            for(int i=0;i<forecast.length();i++){
                JSONObject object =forecast.getJSONObject(i);
                wed+="天气："+object.getString("type")+"\n";
                wed+="日期："+object.getString("date")+"\n";
                wed+="风向："+object.getString("fengxiang")+"\n";
                wed+="风力："+object.getString("fengli")+"\n";
                wed+="高温："+object.getString("high")+"\n";
                wed+="低温："+object.getString("low")+"\n";
                wed+="\n";
            }

            tv_show.setText(wed);

        }catch(Exception ee){
            ee.printStackTrace();
        }

    }
    class GetJson extends Thread{

        private String urlstr = "http://wthrcdn.etouch.cn/weather_mini?city=";
        public GetJson(String cityname){
            try{
                urlstr = urlstr+URLEncoder.encode(cityname,"UTF-8");

            }catch (Exception ee){

            }
        }
        @Override
        public void run() {
            try {
                URL url = new URL(urlstr);
                httpConn = (HttpURLConnection)url.openConnection();
                httpConn.setRequestMethod("GET");
                din = httpConn.getInputStream();
                InputStreamReader in = new InputStreamReader(din);
                BufferedReader buffer = new BufferedReader(in);
                StringBuffer sbf = new StringBuffer();
                String line = null;
                while( (line=buffer.readLine())!=null) {
                    sbf.append(line);
                }
                Message msg = new Message();
                msg.obj = sbf.toString();
                msg.what = 123;
                handler.sendMessage(msg);
                Looper.prepare(); //在线程中调用Toast，要使用此方法，这里纯粹演示用:)
                Toast.makeText(otherActivity.this, "获取数据成功", Toast.LENGTH_LONG).show();
                Looper.loop(); //在线程中调用Toast，要使用此方法
            } catch (Exception ee) {
                Looper.prepare(); //在线程中调用Toast，要使用此方法
                Toast.makeText(otherActivity.this,"获取数据失败，网络连接失败或输入有误",Toast.LENGTH_LONG).show();
                Looper.loop(); //在线程中调用Toast，要使用此方法
                ee.printStackTrace();
            }finally {
                try{
                    httpConn.disconnect();
                    din.close();

                }catch (Exception ee){
                    ee.printStackTrace();
                }
            }
        }
    }
    private void createPro(){ //生成省份选择下拉列表
        SQLiteDatabase db= SQLiteDatabase.openOrCreateDatabase(db_path+db_name,null); //生成数据库对象，并打开数据库
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select distinct "+find1_name+" from "+t1_name, null); //查省份，去掉重复行
        }catch (Exception ee){
            ee.printStackTrace();
        }
        List<String>list = new ArrayList<String>();
        String pro="";
        while(cursor.moveToNext()){
            pro = cursor.getString(cursor.getColumnIndex(find1_name));
            list.add(pro);
        }
        cursor.close();

        //为下拉列表定义一个适配器，这里就用到里前面定义的list
        final ArrayAdapter<String>adapter = new ArrayAdapter<String>(otherActivity.this,android.R.layout.simple_spinner_item,list);
        //为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将适配器添加到下拉列表上
        sp_province.setAdapter(adapter);
        //为下拉列表设置各种事件的响应，这个事响应菜单被选中
        sp_province.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String province = adapter.getItem(position);//选择的省名
                //tv_sl.setText("你选了"+province+"省");
                createCity(province);
                parent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private  void createCity(String province){ //生成市列表
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(db_path+db_name,null);
        Cursor cursor = db.rawQuery("select distinct "+find2_name+" from "+t1_name+" where "+find1_name+"='"+province+"'",null);
        String city="";

        List<String>list = new ArrayList<String>();
        while(cursor.moveToNext()){
            city = cursor.getString(cursor.getColumnIndex(find2_name));
            list.add(city);
        }
        cursor.close();
        final ArrayAdapter<String>adapter = new ArrayAdapter<String>(otherActivity.this,android.R.layout.simple_spinner_item,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_city.setAdapter(adapter);
        sp_city.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String city = adapter.getItem(position);
                String province = sp_province.getSelectedItem().toString();
                createArea(province,city);
                //tv_sl.setText("你选了" + province + "省" + city + "市");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }



    private  void createArea(String province,String city){ //生成区列表
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(db_path+db_name,null);
        Cursor cursor = db.rawQuery("select distinct "+find3_name+" from "+t1_name+" where  "+find1_name+"='" + province + "'and "+find2_name+"='" + city + "'", null);
        String are="";
        List<String>list = new ArrayList<String>();
        while (cursor.moveToNext()) {
            are = cursor.getString(cursor.getColumnIndex(find3_name));
            list.add(are);
        }
        cursor.close();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(otherActivity.this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_area.setAdapter(adapter);
        sp_area.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String area1 = adapter.getItem(position);
                String province1 = sp_province.getSelectedItem().toString();
                String city1 = sp_city.getSelectedItem().toString();
                tv_sl.setText(area1);
                //tv_sl.setText("你选了" + province1 + "省" + city1 + "市" + area1 + "区");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /*
      函数copydb()的作用是复制数据库weather到当前程序可访问的私有目录下
     */
    private void copydb(){//

        File db_file = new File(db_path+db_name);
        if(!db_file.exists()){   //如果第一次运行，文件不存在，那么就建立database目录，并从raw目录下复制weateher.db
            File db_dir= new File(db_path);
            if(!db_dir.exists()){  //如果database目录不存在，新建此目录
                db_dir.mkdir();
            }
            InputStream is = getResources().openRawResource(R.raw.weather);//获取输入流，就是随程序打包，放到raw目录下的weather.db文件
            try {
                OutputStream os = new FileOutputStream(db_path+db_name);//建立一个输出流
                byte[]buff = new byte[1024];//缓冲区大小
                int length = 0;
                while((length=is.read(buff))>0){
                    os.write(buff,0,length); //将buff写入os。写入长度为实践的buff的长度
                }
                os.flush(); //强制把缓冲区内容写入。确保缓存区所有的内容全部写入os
                os.close();
                is.close();
            }catch (Exception ee){
                ee.printStackTrace();
            }

        }

    }

}

