package com.example.handwriting.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.example.handwriting.R;
import com.example.handwriting.bean.User;
import com.example.handwriting.bean.UserLab;
import com.example.handwriting.db.Replay;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AllJiluActivity extends Activity{
    private ListView listView;
    ShowRecorderAdpter showRecord;
    private TextView title;
    List<String> listjl=new ArrayList<>();
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_jilu);
        user= UserLab.get(AllJiluActivity.this,"","").getUser();
        List<Replay> list2= DataSupport.where("userPhone = ?",user.getPhoneNum()).find(Replay.class);
        for(int i=0;i<list2.size();i++){
            listjl.add(list2.get(i).getName());
        }
        //初始化控件
        InitView();
    }
    private void InitView() {
        listView = (ListView) findViewById(R.id.list_view);
        title=(TextView)findViewById(R.id.tv_title);
        title.setText("学习记录");
        showRecord = new ShowRecorderAdpter();
        listView.setAdapter(showRecord);
    }
    //由于在item中涉及到了控件的点击效果，所以采用BaseAdapter
    class ShowRecorderAdpter extends BaseAdapter {
        @Override
        public int getCount() {
            return listjl.size();
        }
        @Override
        public Object getItem(int arg0) {
            return arg0;
        }
        @Override
        public long getItemId(int arg0) {
            return arg0;
        }
        @Override
        public View getView(final int postion, View arg1, ViewGroup arg2) {
            View views = LayoutInflater.from(AllJiluActivity.this).inflate(
                    R.layout.item_jilu, null);
            LinearLayout parent = (LinearLayout) views.findViewById(R.id.list_parent);
            TextView filename = (TextView) views.findViewById(R.id.j_name);
            TextView fileid=(TextView)views.findViewById(R.id.j_id);
            filename.setText(listjl.get(postion).toString());
            fileid.setText(""+(postion+1)+".");
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String data=listjl.get(postion).toString();
                    Intent intent=new Intent(AllJiluActivity.this, ReplayActivity.class);
                    intent.putExtra("d",data);
                    startActivity(intent);
                }
            });
            parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog aler = new AlertDialog.Builder(AllJiluActivity.this)
                            .setTitle("确定删除该记录？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog
                                        , int which) {
                                    deleteReplay(listjl.get(postion));
                                    de(listjl.get(postion));
                                    listjl.remove(postion);
                                    showRecord.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .create();
                    //设置不允许点击提示框之外的区域
                    aler.setCanceledOnTouchOutside(false);
                    aler.show();
                    return false;
                }
            });
            return views;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private void deleteReplay(String name){
        DataSupport.deleteAll(Replay.class,"name=?",name);
    }
    private void de(String name){
        String url = "http://192.168.137.1:8080/HSKHandWrting/DeleteServlet";
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("msg1",name)
                .add("msg2",user.getPhoneNum())
                .build();
        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().string();
                Log.d("dsa", "ps="+s);
            }
        });
    }
}