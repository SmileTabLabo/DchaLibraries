package com.android.browser;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.android.browser.provider.BrowserContract;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.CRC32;
/* loaded from: classes.dex */
public class BrowserBackupAgent extends BackupAgent {
    @Override // android.app.backup.BackupAgent
    public void onBackup(ParcelFileDescriptor parcelFileDescriptor, BackupDataOutput backupDataOutput, ParcelFileDescriptor parcelFileDescriptor2) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(parcelFileDescriptor.getFileDescriptor()));
        try {
            long readLong = dataInputStream.readLong();
            long readLong2 = dataInputStream.readLong();
            dataInputStream.readInt();
            dataInputStream.close();
            writeBackupState(readLong, readLong2, parcelFileDescriptor2);
        } catch (EOFException e) {
            dataInputStream.close();
        } catch (Throwable th) {
            dataInputStream.close();
            throw th;
        }
    }

    @Override // android.app.backup.BackupAgent
    public void onRestore(BackupDataInput backupDataInput, int i, ParcelFileDescriptor parcelFileDescriptor) throws IOException {
        long j;
        File createTempFile = File.createTempFile("rst", null, getFilesDir());
        long j2 = -1;
        while (backupDataInput.readNextHeader()) {
            try {
                if ("_bookmarks_".equals(backupDataInput.getKey())) {
                    j2 = copyBackupToFile(backupDataInput, createTempFile, backupDataInput.getDataSize());
                    DataInputStream dataInputStream = new DataInputStream(new FileInputStream(createTempFile));
                    try {
                        int readInt = dataInputStream.readInt();
                        ArrayList arrayList = new ArrayList(readInt);
                        char c = 0;
                        for (int i2 = 0; i2 < readInt; i2++) {
                            Bookmark bookmark = new Bookmark();
                            bookmark.url = dataInputStream.readUTF();
                            bookmark.visits = dataInputStream.readInt();
                            bookmark.date = dataInputStream.readLong();
                            bookmark.created = dataInputStream.readLong();
                            bookmark.title = dataInputStream.readUTF();
                            arrayList.add(bookmark);
                        }
                        int size = arrayList.size();
                        String[] strArr = {"url"};
                        int i3 = 0;
                        int i4 = 0;
                        while (i4 < size) {
                            Bookmark bookmark2 = (Bookmark) arrayList.get(i4);
                            ContentResolver contentResolver = getContentResolver();
                            Uri uri = BrowserContract.Bookmarks.CONTENT_URI;
                            String[] strArr2 = new String[1];
                            strArr2[c] = bookmark2.url;
                            int i5 = i3;
                            int i6 = i4;
                            Cursor query = contentResolver.query(uri, strArr, "url == ?", strArr2, null);
                            if (query.getCount() <= 0) {
                                addBookmark(bookmark2);
                                i3 = i5 + 1;
                            } else {
                                i3 = i5;
                            }
                            query.close();
                            i4 = i6 + 1;
                            c = 0;
                        }
                        Log.i("BrowserBackupAgent", "Restored " + i3 + " of " + size + " bookmarks");
                        dataInputStream.close();
                    } catch (IOException e) {
                        Log.w("BrowserBackupAgent", "Bad backup data; not restoring");
                        dataInputStream.close();
                        j = -1;
                    }
                }
                j = j2;
                writeBackupState(createTempFile.length(), j, parcelFileDescriptor);
                j2 = j;
            } finally {
                createTempFile.delete();
            }
        }
    }

    void addBookmark(Bookmark bookmark) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", bookmark.title);
        contentValues.put("url", bookmark.url);
        contentValues.put("folder", (Integer) 0);
        contentValues.put("created", Long.valueOf(bookmark.created));
        contentValues.put("modified", Long.valueOf(bookmark.date));
        getContentResolver().insert(BrowserContract.Bookmarks.CONTENT_URI, contentValues);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class Bookmark {
        public long created;
        public long date;
        public String title;
        public String url;
        public int visits;

        Bookmark() {
        }
    }

    private long copyBackupToFile(BackupDataInput backupDataInput, File file, int i) throws IOException {
        byte[] bArr = new byte[8192];
        CRC32 crc32 = new CRC32();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        while (i > 0) {
            try {
                int readEntityData = backupDataInput.readEntityData(bArr, 0, 8192);
                crc32.update(bArr, 0, readEntityData);
                fileOutputStream.write(bArr, 0, readEntityData);
                i -= readEntityData;
            } catch (Throwable th) {
                fileOutputStream.close();
                throw th;
            }
        }
        fileOutputStream.close();
        return crc32.getValue();
    }

    private void writeBackupState(long j, long j2, ParcelFileDescriptor parcelFileDescriptor) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(parcelFileDescriptor.getFileDescriptor()));
        try {
            dataOutputStream.writeLong(j);
            dataOutputStream.writeLong(j2);
            dataOutputStream.writeInt(0);
        } finally {
            dataOutputStream.close();
        }
    }
}
