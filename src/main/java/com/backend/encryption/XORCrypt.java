package com.backend.encryption;

import com.backend.CustomProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

/**
 * @author Prem Nirmal
 */
@Service
public class XORCrypt {

    private final CustomProperties properties;
    private String keyval;

    public XORCrypt(CustomProperties properties) {
        this.properties = properties;
        this.keyval = properties.getProperty("xorKey");
    }

    public int[] encrypt(String str, String key) {
        int[] output = new int[str.length()];
        for (int i = 0; i < str.length(); i++) {
            int o = (Integer.valueOf(str.charAt(i)) ^ Integer.valueOf(key.charAt(i % (key.length() - 1)))) + '0';
            output[i] = o;
        }
        return output;
    }

    private static int[] string2Arr(String str) {
        String[] sarr = str.split(",");
        int[] out = new int[sarr.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = Integer.valueOf(sarr[i]);
        }
        return out;
    }

    public String decrypt(int[] input, String key) {
        String output = "";
        for (int i = 0; i < input.length; i++) {
            output += (char) ((input[i] - 48) ^ (int) key.charAt(i % (key.length() - 1)));
        }
        return output;
    }

    public String createNewKey() {
        keyval = RandomStringUtils.random(256);
        properties.setProperty("xorKey", keyval);
        return keyval;
    }
}