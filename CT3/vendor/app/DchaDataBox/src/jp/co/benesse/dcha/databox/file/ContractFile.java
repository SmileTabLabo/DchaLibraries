package jp.co.benesse.dcha.databox.file;

import android.net.Uri;
import java.util.Locale;
/* loaded from: classes.dex */
public enum ContractFile {
    TOP_DIR;
    
    final String pathName = name().toLowerCase(Locale.JAPAN);
    final int codeForMany = ordinal() * 10;
    public final Uri contentUri = Uri.parse("content://" + FileProvider.AUTHORITY + "/" + this.pathName);

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static ContractFile[] valuesCustom() {
        ContractFile[] valuesCustom = values();
        int length = valuesCustom.length;
        ContractFile[] contractFileArr = new ContractFile[length];
        System.arraycopy(valuesCustom, 0, contractFileArr, 0, length);
        return contractFileArr;
    }

    ContractFile() {
    }
}
