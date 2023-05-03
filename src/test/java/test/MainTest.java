package test;


import org.apache.commons.lang3.RandomStringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author yqq
 * @createdate 2020/4/29
 */
public class MainTest {
    public static void main(String[] args) {

     /*   File file = new File("C:\\Users\\abcd\\Desktop\\师学通bug汇总-20210416.txt");
        String fileStr = txt2String(file).replaceAll(System.lineSeparator(),"、");
        fileStr = fileStr.substring(1,fileStr.length());
        System.out.println(fileStr);*/
        System.out.println(AddNum(2));


    }

    public static int AddNum(int n){
        int result = 0;
        result = (n+1)*n/2;
        return result;
    }


    public static int ShellSort(int[] array) {
        int max = 0;
        int len = array.length;
        int temp, gap = len / 2;
        while (gap > 0) {
            for (int i = gap; i < len; i++) {
                temp = array[i];
                int preIndex = i - gap;
                while (preIndex >= 0 && array[preIndex] > temp) {
                    array[preIndex + gap] = array[preIndex];
                    preIndex -= gap;
                }
                array[preIndex + gap] = temp;
            }
            gap /= 2;
        }
        return max;
    }
    public static int getMax(int[] array) {
        int result = 0 ;
        if (array.length == 0){
            return result;
        }
        for (int i = 0; i < array.length; i++) {
            for (int j = i; j < array.length; j++) {
                int temp;
                if (array[j] < array[i]) {
                    temp =array[j]*(j-i);
                }else {
                    temp =array[i]*(j-i);
                }
                if(result<temp){
                    result = temp;
                }
            }

        }
        return result;
    }
    /**
     *
     * @param file
     * @return
     */
    public static String txt2String(File file){
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            int i = 0;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                result.append(System.lineSeparator()+s);
                i++;
                if(i%15==0){
                    result.append("\n");
                }
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }



}
