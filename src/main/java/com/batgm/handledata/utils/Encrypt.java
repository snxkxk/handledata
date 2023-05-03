package com.batgm.handledata.utils;

import com.batgm.handledata.utils.encrypt.RSAUtil;
import java.security.PrivateKey;
import java.security.PublicKey;


public class Encrypt {


    public static void main(String[] args) throws Exception {
        //公钥加密
        PublicKey publicKey = RSAUtil.getPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDyl/kUzYcFdItEjGeZvqRBYxi/\n" +
                "lyD10XX4j1biE8q3Pi3db2BaBPfPbMX1JP9FTDcxbiqlih2fP+AG+ytAQLvSrMpJ\n" +
                "NFi5HPh0D6PDdby45KdZSoV4QquNTHS/dEXTCw2zqwUSjGq1m663MCnve9Jnh1bu\n" +
                "izfEOkLG3PQlkLhCPwIDAQAB");

        System.out.println("publicKey:"+publicKey);
        String encript = RSAUtil.encryptString(publicKey, "123456");
        System.out.println("加密后数据：" + encript);

        //私钥解密
        PrivateKey privateKey = RSAUtil.getPrivateKey("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAPKX+RTNhwV0i0SM\n" +
                "Z5m+pEFjGL+XIPXRdfiPVuITyrc+Ld1vYFoE989sxfUk/0VMNzFuKqWKHZ8/4Ab7\n" +
                "K0BAu9Ksykk0WLkc+HQPo8N1vLjkp1lKhXhCq41MdL90RdMLDbOrBRKMarWbrrcw\n" +
                "Ke970meHVu6LN8Q6Qsbc9CWQuEI/AgMBAAECgYBiiJ2v+GbeLV8fwZOW29slf9We\n" +
                "hAjsL16mTboxa26MDSJd7Y4KIjhxNLIXByb0A6frWN7TjAzKEQyyaTRBDcn7Vqgz\n" +
                "76I+B3agFzyOHp08fZagOR/Al2Oa9R7/pGs1G9m2R7EwXMtd1UYU7AJAIpMdJiQF\n" +
                "cP8vCQjyMwE17feCoQJBAPlUC8sOJkhLeVdSaRVVDSoB5+CE49xiNAl6U8aNy8wn\n" +
                "F/mRiBQf6V1gCybNC0XyBTErAraR6WdWXB3P053UP5ECQQD5FcsBu087/xrXvZTb\n" +
                "aj9Jjn0VBmacWjuuiYvB7Q/hopC3UDTxwokSHfzoGt44evk34DMQpL0nHNFrlXKg\n" +
                "VRzPAkEA76tA4uIpHpmefLg1V1I4o0bNtN9JghHMX8f9PAIWA5sYysiAfIfodd/b\n" +
                "GGNGEOiC4S6tHv7H1JQJXIborvDWYQJBAMMR4yuXAsWM0vvUeAiiG7BCjAj0O1YF\n" +
                "gKn/BFm6i258vvMhOGWBoZFztMYdjJ0VCapNxhApxA0mj4e+wcd5AJcCQChxMet9\n" +
                "ewi29qbfAY097euodGkd1Ipj1ihSVzM1d9J4M0Mq+nRvVLMbeMmiQm6wGbSn2rkP\n" +
                "o4+lKgRQKTxNEdo=");
        String oldSource = RSAUtil.decryptString(privateKey, encript);
        System.out.println("解密后数据:" + oldSource);
    }





}
