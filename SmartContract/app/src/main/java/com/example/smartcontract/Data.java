package com.example.smartcontract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data {
    public static String privateKey;
    public static String publicKey;
    public static Map<Integer, String> State = new HashMap<Integer, String>() {{
        put(0, "Assembling");
        put(1, "Made");
        put(2, "Packed");
        put(3, "ForSale");
        put(4, "Sold");
        put(5, "Shipped");
        put(6, "Received");
    }};
    public static List<String> userRoles = new ArrayList<>();
}
