package com.android.settingslib.drawer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.UserInfo;
import android.database.DataSetObserver;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.android.internal.util.UserIcons;
import com.android.settingslib.R$layout;
import com.android.settingslib.R$string;
import com.android.settingslib.drawable.UserIconDrawable;
import java.util.ArrayList;
import java.util.List;
/* loaded from: a.zip:com/android/settingslib/drawer/UserAdapter.class */
public class UserAdapter implements SpinnerAdapter, ListAdapter {
    private ArrayList<UserDetails> data;
    private final LayoutInflater mInflater;

    /* loaded from: a.zip:com/android/settingslib/drawer/UserAdapter$UserDetails.class */
    public static class UserDetails {
        private final Drawable mIcon;
        private final String mName;
        private final UserHandle mUserHandle;

        public UserDetails(UserHandle userHandle, UserManager userManager, Context context) {
            BitmapDrawable bitmapDrawable;
            this.mUserHandle = userHandle;
            UserInfo userInfo = userManager.getUserInfo(this.mUserHandle.getIdentifier());
            if (userInfo.isManagedProfile()) {
                this.mName = context.getString(R$string.managed_user_title);
                bitmapDrawable = context.getDrawable(17302312);
            } else {
                this.mName = userInfo.name;
                int i = userInfo.id;
                bitmapDrawable = userManager.getUserIcon(i) != null ? new BitmapDrawable(context.getResources(), userManager.getUserIcon(i)) : UserIcons.getDefaultUserIcon(i, false);
            }
            this.mIcon = encircle(context, bitmapDrawable);
        }

        private static Drawable encircle(Context context, Drawable drawable) {
            return new UserIconDrawable(UserIconDrawable.getSizeForList(context)).setIconDrawable(drawable).bake();
        }
    }

    public UserAdapter(Context context, ArrayList<UserDetails> arrayList) {
        if (arrayList == null) {
            throw new IllegalArgumentException("A list of user details must be provided");
        }
        this.data = arrayList;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    private View createUser(ViewGroup viewGroup) {
        return this.mInflater.inflate(R$layout.user_preference, viewGroup, false);
    }

    public static UserAdapter createUserAdapter(UserManager userManager, Context context, List<UserHandle> list) {
        ArrayList arrayList = new ArrayList(list.size());
        int size = list.size();
        for (int i = 0; i < size; i++) {
            arrayList.add(new UserDetails(list.get(i), userManager, context));
        }
        return new UserAdapter(context, arrayList);
    }

    private int getTitle(UserDetails userDetails) {
        int identifier = userDetails.mUserHandle.getIdentifier();
        return (identifier == -2 || identifier == ActivityManager.getCurrentUser()) ? R$string.category_personal : R$string.category_work;
    }

    @Override // android.widget.ListAdapter
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.data.size();
    }

    @Override // android.widget.SpinnerAdapter
    public View getDropDownView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = createUser(viewGroup);
        }
        UserDetails userDetails = this.data.get(i);
        ((ImageView) view.findViewById(16908294)).setImageDrawable(userDetails.mIcon);
        ((TextView) view.findViewById(16908310)).setText(getTitle(userDetails));
        return view;
    }

    @Override // android.widget.Adapter
    public UserDetails getItem(int i) {
        return this.data.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return this.data.get(i).mUserHandle.getIdentifier();
    }

    @Override // android.widget.Adapter
    public int getItemViewType(int i) {
        return 0;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        return getDropDownView(i, view, viewGroup);
    }

    @Override // android.widget.Adapter
    public int getViewTypeCount() {
        return 1;
    }

    @Override // android.widget.Adapter
    public boolean hasStableIds() {
        return false;
    }

    @Override // android.widget.Adapter
    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    @Override // android.widget.ListAdapter
    public boolean isEnabled(int i) {
        return true;
    }

    @Override // android.widget.Adapter
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
    }

    @Override // android.widget.Adapter
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
    }
}
