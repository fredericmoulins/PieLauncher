package de.markusfisch.android.pielauncher.app;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LauncherApps;
import android.os.Build;
import android.os.UserHandle;

import de.markusfisch.android.pielauncher.content.AppMenu;
import de.markusfisch.android.pielauncher.preference.Preferences;
import de.markusfisch.android.pielauncher.receiver.PackageEventReceiver;

public class PieLauncherApp extends Application {
	private static final PackageEventReceiver packageEventReceiver =
			new PackageEventReceiver();

	public static final Preferences prefs = new Preferences();
	public static final AppMenu appMenu = new AppMenu();

	@Override
	public void onCreate() {
		super.onCreate();
		prefs.init(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
				Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			// Because package broadcasts stop mysteriously working after
			// a while on Android Nougat only.
			registerCallback();
		} else {
			registerPackageEventReceiver();
		}
	}

	private void registerPackageEventReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_LOCALE_CHANGED);
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		filter.addDataScheme("package");
		filter.addDataScheme("file");
		registerReceiver(packageEventReceiver, filter);
		// Note it's not required to unregister the receiver because it
		// needs to be there as long as this application is running.
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void registerCallback() {
		LauncherApps launcherApps = (LauncherApps) getSystemService(
				LAUNCHER_APPS_SERVICE);
		launcherApps.registerCallback(new LauncherApps.Callback() {
			@Override
			public void onPackageAdded(String packageName,
					UserHandle user) {
				appMenu.indexAppsAsync(PieLauncherApp.this, packageName, user);
			}

			@Override
			public void onPackageChanged(String packageName,
					UserHandle user) {
				appMenu.indexAppsAsync(PieLauncherApp.this, packageName, user);
			}

			@Override
			public void onPackageRemoved(String packageName,
					UserHandle user) {
				appMenu.removePackageAsync(packageName, user);
			}

			@Override
			public void onPackagesAvailable(String[] packageNames,
					UserHandle user, boolean replacing) {
			}

			@Override
			public void onPackagesUnavailable(String[] packageNames,
					UserHandle user, boolean replacing) {
			}
		});
	}
}
