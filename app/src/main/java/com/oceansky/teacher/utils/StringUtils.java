package com.oceansky.teacher.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tangqifa on 16/3/24.
 */
public class StringUtils {

    public static List<String> dateJsonConvertToList(String jsonDate) {
        List<String> stringList = new ArrayList<>();
        if (TextUtils.isEmpty(jsonDate)) return stringList;

        try {
            JSONObject jsonObject = new JSONObject(jsonDate);
            JSONArray namesArray = jsonObject.names();
            int lenth = namesArray.length();
            for (int i = 0; i < lenth; i++) {
                String monthStr = namesArray.getString(i);
                JSONArray dayArray = jsonObject.getJSONArray(monthStr);
                int size = dayArray.length();
                for (int k = 0; k < size; k++) {
                    int day = dayArray.getInt(k);
                    String dayStr;
                    if (day < 10) {
                        dayStr = "0" + day;
                    } else {
                        dayStr = "" + day;
                    }
                    stringList.add(monthStr + dayStr);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stringList;
    }

    public static int getVersionCode(Context ctx) {
        int versionCode = 0;
        try {
            versionCode = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), versionCode).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;

    }

    public static String getVersionName(Context ctx) {
        String versionName = "";
        try {
            versionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;

    }

    public static boolean checkPassWord(String pwd) {
        if (TextUtils.isEmpty(pwd.trim())) return false;
        Pattern p = Pattern.compile("^(?![a-zA-Z]+$)(?![^a-zA-Z0-9]+$)(?!\\d+$).{6,16}$");
        //Pattern p = Pattern.compile("^(((?=.[0-9])(?=.[a-zA-Z])|(?=.[0-9])(?=.[^0-9a-zA-Z])|(?=.[a-zA-Z])(?=.[^0-9a-zA-Z]))+)$");
        Matcher m = p.matcher(pwd);
        return m.matches();
    }

    public static String replaceAssetFileString(Context context, String fileName, String replaceContent) {
        StringBuilder result = new StringBuilder();
        String line = null;
        FileReader fr = null;
        BufferedReader br = null;
        boolean first = true;
        try {
            br = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName), "UTF-8"));
            while ((line = br.readLine()) != null) {
                if (line.contains("{question}"))
                    line = line.replace("{question}", replaceContent);
                if (!first) {
                    result.append('\n');
                } else {
                    first = false;
                }
                result.append(line);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (fr != null) fr.close();
                if (br != null) br.close();

            } catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
        return result.toString();
    }

    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i< c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }if (c[i]> 65280&& c[i]< 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }
}
