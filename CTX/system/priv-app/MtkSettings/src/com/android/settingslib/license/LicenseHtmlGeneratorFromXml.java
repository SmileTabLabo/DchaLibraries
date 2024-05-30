package com.android.settingslib.license;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
/* loaded from: classes.dex */
class LicenseHtmlGeneratorFromXml {
    private final List<File> mXmlFiles;
    private final Map<String, String> mFileNameToContentIdMap = new HashMap();
    private final Map<String, String> mContentIdToFileContentMap = new HashMap();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class ContentIdAndFileNames {
        final String mContentId;
        final List<String> mFileNameList = new ArrayList();

        ContentIdAndFileNames(String str) {
            this.mContentId = str;
        }
    }

    private LicenseHtmlGeneratorFromXml(List<File> list) {
        this.mXmlFiles = list;
    }

    public static boolean generateHtml(List<File> list, File file) {
        return new LicenseHtmlGeneratorFromXml(list).generateHtml(file);
    }

    private boolean generateHtml(File file) {
        PrintWriter printWriter;
        Throwable e;
        for (File file2 : this.mXmlFiles) {
            parse(file2);
        }
        if (this.mFileNameToContentIdMap.isEmpty() || this.mContentIdToFileContentMap.isEmpty()) {
            return false;
        }
        try {
            printWriter = new PrintWriter(file);
        } catch (FileNotFoundException | SecurityException e2) {
            printWriter = null;
            e = e2;
        }
        try {
            generateHtml(this.mFileNameToContentIdMap, this.mContentIdToFileContentMap, printWriter);
            printWriter.flush();
            printWriter.close();
            return true;
        } catch (FileNotFoundException | SecurityException e3) {
            e = e3;
            Log.e("LicenseHtmlGeneratorFromXml", "Failed to generate " + file, e);
            if (printWriter != null) {
                printWriter.close();
            }
            return false;
        }
    }

    private void parse(File file) {
        InputStreamReader fileReader;
        if (file == null || !file.exists() || file.length() == 0) {
            return;
        }
        InputStreamReader inputStreamReader = null;
        try {
            if (file.getName().endsWith(".gz")) {
                fileReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(file)));
            } else {
                fileReader = new FileReader(file);
            }
            inputStreamReader = fileReader;
            parse(inputStreamReader, this.mFileNameToContentIdMap, this.mContentIdToFileContentMap);
            inputStreamReader.close();
        } catch (IOException | XmlPullParserException e) {
            Log.e("LicenseHtmlGeneratorFromXml", "Failed to parse " + file, e);
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e2) {
                    Log.w("LicenseHtmlGeneratorFromXml", "Failed to close " + file);
                }
            }
        }
    }

    static void parse(InputStreamReader inputStreamReader, Map<String, String> map, Map<String, String> map2) throws XmlPullParserException, IOException {
        HashMap hashMap = new HashMap();
        Map<? extends String, ? extends String> hashMap2 = new HashMap<>();
        XmlPullParser newPullParser = Xml.newPullParser();
        newPullParser.setInput(inputStreamReader);
        newPullParser.nextTag();
        newPullParser.require(2, "", "licenses");
        for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
            if (eventType == 2) {
                if ("file-name".equals(newPullParser.getName())) {
                    String attributeValue = newPullParser.getAttributeValue("", "contentId");
                    if (!TextUtils.isEmpty(attributeValue)) {
                        String trim = readText(newPullParser).trim();
                        if (!TextUtils.isEmpty(trim)) {
                            hashMap.put(trim, attributeValue);
                        }
                    }
                } else if ("file-content".equals(newPullParser.getName())) {
                    String attributeValue2 = newPullParser.getAttributeValue("", "contentId");
                    if (!TextUtils.isEmpty(attributeValue2) && !map2.containsKey(attributeValue2) && !hashMap2.containsKey(attributeValue2)) {
                        String readText = readText(newPullParser);
                        if (!TextUtils.isEmpty(readText)) {
                            hashMap2.put(attributeValue2, readText);
                        }
                    }
                }
            }
        }
        map.putAll(hashMap);
        map2.putAll(hashMap2);
    }

    private static String readText(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        StringBuffer stringBuffer = new StringBuffer();
        int next = xmlPullParser.next();
        while (next == 4) {
            stringBuffer.append(xmlPullParser.getText());
            next = xmlPullParser.next();
        }
        return stringBuffer.toString();
    }

    static void generateHtml(Map<String, String> map, Map<String, String> map2, PrintWriter printWriter) {
        ArrayList<String> arrayList = new ArrayList();
        arrayList.addAll(map.keySet());
        Collections.sort(arrayList);
        printWriter.println("<html><head>\n<style type=\"text/css\">\nbody { padding: 0; font-family: sans-serif; }\n.same-license { background-color: #eeeeee;\n                border-top: 20px solid white;\n                padding: 10px; }\n.label { font-weight: bold; }\n.file-list { margin-left: 1em; color: blue; }\n</style>\n</head><body topmargin=\"0\" leftmargin=\"0\" rightmargin=\"0\" bottommargin=\"0\">\n<div class=\"toc\">\n<ul>");
        HashMap hashMap = new HashMap();
        ArrayList<ContentIdAndFileNames> arrayList2 = new ArrayList();
        int i = 0;
        for (String str : arrayList) {
            String str2 = map.get(str);
            if (!hashMap.containsKey(str2)) {
                hashMap.put(str2, Integer.valueOf(i));
                arrayList2.add(new ContentIdAndFileNames(str2));
                i++;
            }
            int intValue = ((Integer) hashMap.get(str2)).intValue();
            ((ContentIdAndFileNames) arrayList2.get(intValue)).mFileNameList.add(str);
            printWriter.format("<li><a href=\"#id%d\">%s</a></li>\n", Integer.valueOf(intValue), str);
        }
        printWriter.println("</ul>\n</div><!-- table of contents -->\n<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
        int i2 = 0;
        for (ContentIdAndFileNames contentIdAndFileNames : arrayList2) {
            printWriter.format("<tr id=\"id%d\"><td class=\"same-license\">\n", Integer.valueOf(i2));
            printWriter.println("<div class=\"label\">Notices for file(s):</div>");
            printWriter.println("<div class=\"file-list\">");
            Iterator<String> it = contentIdAndFileNames.mFileNameList.iterator();
            while (it.hasNext()) {
                printWriter.format("%s <br/>\n", it.next());
            }
            printWriter.println("</div><!-- file-list -->");
            printWriter.println("<pre class=\"license-text\">");
            printWriter.println(map2.get(contentIdAndFileNames.mContentId));
            printWriter.println("</pre><!-- license-text -->");
            printWriter.println("</td></tr><!-- same-license -->");
            i2++;
        }
        printWriter.println("</table></body></html>");
    }
}
