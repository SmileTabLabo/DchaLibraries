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
/* loaded from: classes.dex */
public class UserAdapter implements SpinnerAdapter, ListAdapter {
    private ArrayList<UserDetails> data;
    private final LayoutInflater mInflater;

    /* loaded from: classes.dex */
    public static class UserDetails {
        private final Drawable mIcon;
        private final String mName;
        private final UserHandle mUserHandle;

        public UserDetails(UserHandle userHandle, UserManager um, Context context) {
            Drawable icon;
            this.mUserHandle = userHandle;
            UserInfo userInfo = um.getUserInfo(this.mUserHandle.getIdentifier());
            if (userInfo.isManagedProfile()) {
                this.mName = context.getString(R$string.managed_user_title);
                icon = context.getDrawable(17302312);
            } else {
                this.mName = userInfo.name;
                int userId = userInfo.id;
                if (um.getUserIcon(userId) != null) {
                    icon = new BitmapDrawable(context.getResources(), um.getUserIcon(userId));
                } else {
                    icon = UserIcons.getDefaultUserIcon(userId, false);
                }
            }
            this.mIcon = encircle(context, icon);
        }

        private static Drawable encircle(Context context, Drawable icon) {
            return new UserIconDrawable(UserIconDrawable.getSizeForList(context)).setIconDrawable(icon).bake();
        }
    }

    public UserAdapter(Context context, ArrayList<UserDetails> users) {
        if (users == null) {
            throw new IllegalArgumentException("A list of user details must be provided");
        }
        this.data = users;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public UserHandle getUserHandle(int position) {
        if (position < 0 || position >= this.data.size()) {
            return null;
        }
        return this.data.get(position).mUserHandle;
    }

    @Override // android.widget.SpinnerAdapter
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View row = convertView != null ? convertView : createUser(parent);
        UserDetails user = this.data.get(position);
        ((ImageView) row.findViewById(16908294)).setImageDrawable(user.mIcon);
        ((TextView) row.findViewById(16908310)).setText(getTitle(user));
        return row;
    }

    private int getTitle(UserDetails user) {
        int userHandle = user.mUserHandle.getIdentifier();
        if (userHandle == -2 || userHandle == ActivityManager.getCurrentUser()) {
            return R$string.category_personal;
        }
        return R$string.category_work;
    }

    private View createUser(ViewGroup parent) {
        return this.mInflater.inflate(R$layout.user_preference, parent, false);
    }

    @Override // android.widget.Adapter
    public void registerDataSetObserver(DataSetObserver observer) {
    }

    @Override // android.widget.Adapter
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.data.size();
    }

    @Override // android.widget.Adapter
    public UserDetails getItem(int position) {
        return this.data.get(position);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return this.data.get(position).mUserHandle.getIdentifier();
    }

    @Override // android.widget.Adapter
    public boolean hasStableIds() {
        return false;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        return getDropDownView(position, convertView, parent);
    }

    @Override // android.widget.Adapter
    public int getItemViewType(int position) {
        return 0;
    }

    @Override // android.widget.Adapter
    public int getViewTypeCount() {
        return 1;
    }

    @Override // android.widget.Adapter
    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    @Override // android.widget.ListAdapter
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override // android.widget.ListAdapter
    public boolean isEnabled(int position) {
        return true;
    }

    public static UserAdapter createUserSpinnerAdapter(UserManager userManager, Context context) {
        List<UserHandle> userProfiles = userManager.getUserProfiles();
        if (userProfiles.size() < 2) {
            return null;
        }
        UserHandle myUserHandle = new UserHandle(UserHandle.myUserId());
        userProfiles.remove(myUserHandle);
        userProfiles.add(0, myUserHandle);
        return createUserAdapter(userManager, context, userProfiles);
    }

    public static UserAdapter createUserAdapter(UserManager userManager, Context context, List<UserHandle> userProfiles) {
        ArrayList<UserDetails> userDetails = new ArrayList<>(userProfiles.size());
        int count = userProfiles.size();
        for (int i = 0; i < count; i++) {
            userDetails.add(new UserDetails(userProfiles.get(i), userManager, context));
        }
        return new UserAdapter(context, userDetails);
    }
}
