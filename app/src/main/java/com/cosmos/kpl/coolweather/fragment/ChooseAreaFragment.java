package com.cosmos.kpl.coolweather.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cosmos.kpl.coolweather.R;
import com.cosmos.kpl.coolweather.db.City;
import com.cosmos.kpl.coolweather.db.County;
import com.cosmos.kpl.coolweather.db.Province;
import com.cosmos.kpl.coolweather.util.HttpUtil;
import com.cosmos.kpl.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backBTN;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*
            create view from inflater,aim the layout.
            get titleText,backBTN,listView
            put dalaList in adapater,aim the style
            set adapter in listview
         */
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backBTN = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(
                getContext(),android.R.layout.simple_list_item_1,dataList
        );
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            /*
                when leven on province,query cities
                when on city,query counties
                always check below level on based level
             */
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(i);
                    queryCities();
                }
                if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(i);
                    queryCounties();
                }
            }
        });
        /*
            backBTN directs to upper level,
            always check up level on based level
         */
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY)
                    queryCities();
                else if (currentLevel == LEVEL_CITY)
                    queryProvinces();
            }
        });
        queryProvinces();
    }
    /*
        always query from database.
        if not exist,query from server
     */

    private void queryProvinces(){
        titleText.setText("中国");
        backBTN.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (null != provinceList && provinceList.size()>0){
            dataList.clear();
            for (Province p : provinceList){
                dataList.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();//告诉adapter发生了改变，强制刷新listview
            listView.setSelection(0);//刷新之后定位到listview的首位
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backBTN.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (null != cityList && cityList.size()>0){
            dataList.clear();
            for (City c : cityList){
                dataList.add(c.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,"city");
        }
    }

    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backBTN.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if (null != cityList && cityList.size()>0){
            dataList.clear();
            for (County c : countyList){
                dataList.add(c.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode +"/" + cityCode;
            queryFromServer(address,"county");
        }

    }

    private void queryFromServer(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"加载失败!",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                /*
                    this method 'onResponse' is the callback of mothod 'HttpUtil.sendOkHttpRequest'
                    analyze the response text in Utility.return boolean
                    if success,refresh the uiThread
                    if failure,go mothod 'onFailure'
                 */
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type))
                    result = Utility.handleProvinceResponse(responseText);
                else if ("city".equals(type))
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                else if ("county".equals(type))
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        /*
                           refresh the uiThread(main thread)
                         */
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type))
                                queryProvinces();
                            else if ("city".equals(type))
                                queryCities();
                            else if ("county".equals(type))
                                queryCounties();
                        }
                    });

                }
            }
        });
    }

    private void showProgressDialog(){
        /*
        这里可以getContext,也可以getActivity,区别：
        getContext()获得的是程序的application实例，一个app只有一个application，只有程序进程杀掉才结束，如果dialog绑定在它上面
        会获得超长的生命周期，当它显示后的页面（也就是activity）finish掉，它扔存在且别的页面无法杀死它，这样就造成程序的内存泄漏。
        所以应该绑定在显示的activity上。
         */
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("正在加载");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void closeProgressDialog(){
        if (progressDialog != null)
            progressDialog.dismiss();
    }
}
