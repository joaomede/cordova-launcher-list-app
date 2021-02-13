package com.joaomede.launcherList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.util.Base64;
import android.util.Log;

public class LauncherList extends CordovaPlugin {
	private CallbackContext callback;

    private abstract class LauncherRunnable implements Runnable {
		public CallbackContext callbackContext;
		LauncherRunnable(CallbackContext cb) {
			this.callbackContext = cb;
		}
	}
    private class AppDetail {
        CharSequence label;
        CharSequence name;
        Drawable icon;

        public String toString() {
            Bitmap icon;
            if (this.icon.getIntrinsicWidth() <= 0 || this.icon.getIntrinsicHeight() <= 0) {
                icon = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            } else {
                icon = Bitmap.createBitmap(this.icon.getIntrinsicWidth(), this.icon.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
            }
            final Canvas canvas = new Canvas(icon);
            this.icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            this.icon.draw(canvas);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP);

            return "{\"label\":\"" + this.label + "\",\"name\":\"" + this.name + "\",\"icon\":\"" + encoded + "\"}";
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.callback = callbackContext;
        if (action.equals("getApps")) {
            this.getApps();
        } else if (action.equals("launch")) {
            this.launchApp(args);
        }
        return true;
    }

    private void getApps() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                List<AppDetail> apps = new ArrayList<>();

                PackageManager pm = cordova.getActivity().getPackageManager();
                String appPackageName = cordova.getActivity().getPackageName();
                List<PackageInfo> packages = pm.getInstalledPackages(0);

                for (PackageInfo p : packages) {
                    if (cordova.getActivity().getPackageManager()
                            .getLaunchIntentForPackage(p.packageName) != null) {
                        AppDetail app = new AppDetail();
                        app.label = p.applicationInfo.loadLabel(pm);
                        app.name = p.packageName;
                        app.icon = p.applicationInfo.loadIcon(pm);
                        apps.add(app);
                    }
                }

                try {
                    callback.success(new JSONObject().put("result", apps));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

	private void launchApp(JSONArray args) throws JSONException {
        final JSONObject options = args.getJSONObject(0);
        final String packageName = options.getString("packageName");
		final CordovaInterface mycordova = cordova;
		final CordovaPlugin plugin = this;

        cordova.getThreadPool().execute(new LauncherRunnable(this.callback) {
			public void run() {
				final PackageManager pm = plugin.webView.getContext().getPackageManager();
				final Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
				boolean appNotFound = launchIntent == null;

				if (!appNotFound) {
					try {
						mycordova.startActivityForResult(plugin, launchIntent, 0);
                        callback.success();
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
						callback.error("Not found package name.");
					}
				} else {
					callbackContext.error("Not found package name.");
				}
			}
		});
	}
}
