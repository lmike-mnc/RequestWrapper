package org.lmike;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextTest {
    public static final String fileTag="->file";
    public static final String tokenTag="->token";
    public static final String txtTag="->text:";
    public static final String fileRegexp="^-\\>text:.+";
    public static final String tokenRegexp="^-\\>text:.+";
    public static final String txtRegexp="^-\\>text:.+";
    //public static final String txtRegexp="-\\>.+";

    public static String getBody(String txt, String regex){
        String res=null;
        Pattern p= Pattern.compile(regex,Pattern.CASE_INSENSITIVE );
        Matcher matcher = p.matcher(txt);
        if (matcher.find()){
            res=txt.substring(txt.lastIndexOf(txtTag)+txtTag.length());
        }
        return res;
    }
}
