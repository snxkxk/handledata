package com.batgm.handledata.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.common.geo.GeoPoint;

import java.util.regex.Pattern;

/**
 * @Description: elasticsearch字段类型：
 * 该枚举类用于转换elasticsearch特殊字段的数据值
 */
public enum EsFieldType {
    /**
     * 几何点类型，接收经纬度数据对。
     * geo_point有四种形式：
     * #对象   "location": {"lat": 41.12,"lon": -71.34}
     * #字符串 "location": "41.12,-71.34"
     * #geohash "location": "drm3btev3e86"
     * #数组  "location": [ -71.34, 41.12 ]
     */
    GEO_POINT("geo_point"){
        @Override
        public Object fieldValueTransfer(String fieldValue) {
            String rex1 = "\\{(\\s)*\"lat\"(\\s)*:(\\s)*(\\-|\\+)?\\d+(\\.\\d+)?(\\s)*,"+
                    "(\\s)*\"lon\"(\\s)*:(\\s)*(\\-|\\+)?\\d+(\\.\\d+)?(\\s)*\\}";
            String rex2 = "(\\-|\\+)?\\d+(\\.\\d+)?(\\s)*,(\\s)*(\\-|\\+)?\\d+(\\.\\d+)?";
            String rex3 = "^[a-z0-9]+$";
            String rex4 = "^\\[(\\s)*(\\-|\\+)?\\d+(\\.\\d+)?(\\s)*,(\\s)*(\\-|\\+)?\\d+(\\.\\d+)?(\\s)*\\]$";
            if(match(rex1,fieldValue)){
                //json object
                return  parseJsonToGeopoint(fieldValue);
            }else if(match(rex4,fieldValue)){
                //array
                return JSON.parseArray(fieldValue);
            }else if(match(rex2,fieldValue)){
                //string
                return fieldValue;
            }else if(match(rex3,fieldValue)){
                //geohash,不能有大写的英文字母
                return GeoPoint.fromGeohash(fieldValue);
            }else{
                return null;
            }
        }
        private GeoPoint parseJsonToGeopoint(String jsonStr){
            //{"lat": 41.12,"lon": -71.34}
            JSONObject jo = JSON.parseObject(jsonStr);
            //直接返回JSONObject也可以，但经纬度前后顺序会颠倒。
            return new GeoPoint(jo.getDoubleValue("lat"), jo.getDoubleValue("lon"));
        }
    };

    EsFieldType(String type) {
        this.type = type;
    }

    public Object getTransferedField(String fieldValue){
        return fieldValueTransfer(fieldValue);
    }
    public boolean match(String rex,String input){
        Pattern p = Pattern.compile(rex);
        return p.matcher(input).matches();
    }
    private String type;
    public String getType() {
        return type;
    }
    protected abstract Object fieldValueTransfer(String fieldValue);
}
