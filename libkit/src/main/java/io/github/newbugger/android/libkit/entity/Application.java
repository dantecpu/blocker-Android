package io.github.newbugger.android.libkit.entity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import java.util.Date;


/**
 * Created by Mercury on 2017/12/30.
 * An entity class that describe simplified application information
 */

public class Application implements Parcelable {

    public static final Creator<Application> CREATOR = new Creator<Application>() {
        @Override
        public Application createFromParcel(Parcel source) {
            return new Application(source);
        }

        @Override
        public Application[] newArray(int size) {
            return new Application[size];
        }
    };

    private String packageName;
    private final String versionName;
    private final int versionCode;
    private boolean enabled;
    private boolean blocked;
    private int targetSdkVersion;
    private int minSdkVersion;
    private String nonLocalizedLabel;
    private String sourceDir;
    private String publicSourceDir;
    private String dataDir;
    private String label;
    private Date firstInstallTime;
    private Date lastUpdateTime;

    private Application(@NonNull PackageInfo info) {
        this.packageName = info.packageName;
        this.versionName = info.versionName;
        this.versionCode = 1;
        ApplicationInfo appDetails = info.applicationInfo;
        if (appDetails != null) {
            this.targetSdkVersion = appDetails.targetSdkVersion;
            this.enabled = appDetails.enabled;
        }
    }

    public Application(@NonNull PackageManager pm, @NonNull PackageInfo info) {
        this(info);
        ApplicationInfo appDetail = info.applicationInfo;
        this.targetSdkVersion = appDetail.targetSdkVersion;
        this.nonLocalizedLabel = String.valueOf(appDetail.nonLocalizedLabel);
        this.sourceDir = appDetail.sourceDir;
        this.publicSourceDir = appDetail.sourceDir;
        this.dataDir = appDetail.dataDir;
        this.label = appDetail.loadLabel(pm).toString();
        minSdkVersion = appDetail.minSdkVersion;
        this.firstInstallTime = new Date(info.firstInstallTime);
        this.lastUpdateTime = new Date(info.lastUpdateTime);
    }

    public Application(@NonNull PackageManager pm, @NonNull PackageInfo info, boolean blocked) {
        this(pm, info);
        this.blocked = blocked;
    }

    private Application(Parcel in) {
        this.label = in.readString();
        this.packageName = in.readString();
        this.versionName = in.readString();
        this.versionCode = in.readInt();
        this.enabled = in.readByte() != 0;
        this.blocked = in.readByte() != 0;
        this.minSdkVersion = in.readInt();
        this.targetSdkVersion = in.readInt();
        this.nonLocalizedLabel = in.readString();
        this.sourceDir = in.readString();
        this.publicSourceDir = in.readString();
        this.dataDir = in.readString();
        this.firstInstallTime = new Date(in.readLong());
        this.lastUpdateTime = new Date(in.readLong());
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEnabled() {
        return enabled;
    }

    public int getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public String getLabel() {
        return label;
    }

    public int getMinSdkVersion() {
        return minSdkVersion;
    }

    public Date getFirstInstallTime() {
        return firstInstallTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean setBlocked) {
        this.blocked = setBlocked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Nullable
    public Drawable getApplicationIcon(PackageManager pm) {
        try {
            return pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NotNull
    @Override
    public String toString() {
        return "Application{" +
                "label='" + label + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                ", enabled=" + enabled +
                ", blocked=" + blocked +
                ", minSdkVersion=" + minSdkVersion +
                ", targetSdkVersion=" + targetSdkVersion +
                ", nonLocalizedLabel='" + nonLocalizedLabel + '\'' +
                ", sourceDir='" + sourceDir + '\'' +
                ", publicSourceDir='" + publicSourceDir + '\'' +
                ", dataDir='" + dataDir + '\'' +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.label);
        dest.writeString(this.packageName);
        dest.writeString(this.versionName);
        dest.writeInt(this.versionCode);
        dest.writeByte(this.enabled ? (byte) 1 : (byte) 0);
        dest.writeByte(this.blocked ? (byte) 1 : (byte) 0);
        dest.writeInt(this.minSdkVersion);
        dest.writeInt(this.targetSdkVersion);
        dest.writeString(this.nonLocalizedLabel);
        dest.writeString(this.sourceDir);
        dest.writeString(this.publicSourceDir);
        dest.writeString(this.dataDir);
        dest.writeLong(this.firstInstallTime.getTime());
        dest.writeLong(this.lastUpdateTime.getTime());
    }
}
