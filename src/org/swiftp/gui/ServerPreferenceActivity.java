package org.swiftp.gui;

import java.io.File;
import java.net.InetAddress;

import org.swiftp.FTPServerService;
import org.swiftp.Globals;
import org.swiftp.R;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;


/**
 * This is the main activity for swiftp, it enables the user to start the server service
 * and allows the users to change the settings.
 *
 * @author ppareit
 */
/**
 * @author ppareit
 *
 */
public class ServerPreferenceActivity extends PreferenceActivity {

    private static String TAG = ServerPreferenceActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Globals.setContext(getApplicationContext());
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        Resources resources = getResources();

        CheckBoxPreference running_state = (CheckBoxPreference) findPreference("running_state");
        running_state.setChecked(FTPServerService.isRunning());
        running_state
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        if ((Boolean) newValue) {
                            startServer();
                        } else {
                            stopServer();
                        }
                        return true;
                    }
                });

        EditTextPreference username_pref = (EditTextPreference) findPreference("username");
        username_pref.setSummary(settings.getString("username",
                resources.getString(R.string.username_default)));
        username_pref
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        String newUsername = (String) newValue;
                        if (preference.getSummary().equals(newUsername))
                            return false;
                        if (!newUsername.matches("[a-zA-Z0-9]+")) {
                            Toast.makeText(ServerPreferenceActivity.this,
                                    R.string.username_validation_error,
                                    Toast.LENGTH_LONG)
                                    .show();
                            return false;
                        }
                        preference.setSummary(newUsername);
                        stopServer();
                        return true;
                    }
                });

        EditTextPreference password_pref = (EditTextPreference) findPreference("password");
        password_pref.setSummary(settings.getString("password",
                resources.getString(R.string.password_default)));
        password_pref
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        String newPassword = (String) newValue;
                        if (preference.getSummary().equals(newPassword))
                            return false;
                        if (!newPassword.matches("[a-zA-Z0-9]+")) {
                            Toast.makeText(ServerPreferenceActivity.this,
                                    R.string.password_validation_error,
                                    Toast.LENGTH_LONG)
                                    .show();
                            return false;
                        }
                        preference.setSummary(newPassword);
                        stopServer();
                        return true;
                    }
                });

        EditTextPreference portnum_pref = (EditTextPreference) findPreference("portNum");
        portnum_pref.setSummary(settings.getString("portNum",
                resources.getString(R.string.portnumber_default)));
        portnum_pref
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        String newPortnumString = (String) newValue;
                        if (preference.getSummary().equals(newPortnumString))
                            return false;
                        int portnum = 0;
                        try {
                            portnum = Integer.parseInt(newPortnumString);
                        } catch (Exception e) {}
                        if (portnum <= 0 || 65535 < portnum) {
                            Toast.makeText(ServerPreferenceActivity.this,
                                    R.string.port_validation_error,
                                    Toast.LENGTH_LONG)
                                    .show();
                            return false;
                        }
                        preference.setSummary(newPortnumString);
                        stopServer();
                        return true;
                    }
                });

        EditTextPreference chroot_pref = (EditTextPreference) findPreference("chrootDir");
        chroot_pref.setSummary(settings.getString("chrootDir",
                resources.getString(R.string.chroot_default)));
        chroot_pref
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        String newChroot = (String) newValue;
                        if (preference.getSummary().equals(newChroot))
                            return false;
                        // now test the new chroot directory
                        File chrootTest = new File(newChroot);
                        if (!chrootTest.isDirectory() || !chrootTest.canRead())
                            return false;
                        preference.setSummary(newChroot);
                        stopServer();
                        return true;
                    }
                });

        final CheckBoxPreference acceptwifi_pref = (CheckBoxPreference) findPreference("allowWifi");
        final CheckBoxPreference acceptproxy_pref = (CheckBoxPreference) findPreference("allowNet");

        acceptwifi_pref
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        if ((Boolean)newValue == false)
                            acceptproxy_pref.setChecked(true);
                        stopServer();
                        return true;
                    }
                });
        acceptproxy_pref
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        if ((Boolean)newValue == false)
                            acceptwifi_pref.setChecked(true);
                        stopServer();
                        return true;
                    }
                });

        final CheckBoxPreference wakelock_pref = (CheckBoxPreference) findPreference("stayAwake");
        wakelock_pref
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        stopServer();
                        return true;
                    }
                });

        Preference help = findPreference("help");
        help.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(ServerPreferenceActivity.this)
                .setTitle(R.string.help_dlg_title)
                .setMessage(R.string.help_dlg_message)
                .setPositiveButton(getText(R.string.ok), null)
                .show();
                return true;
            }
        });

        Preference about = findPreference("about");
        about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(ServerPreferenceActivity.this)
                .setTitle(R.string.about_dlg_title)
                .setMessage(R.string.about_dlg_message)
                .setPositiveButton(getText(R.string.ok), null)
                .show();
                return true;
            }
        });
    }

    private void startServer() {
        Context context = getApplicationContext();
        Intent serverService = new Intent(context, FTPServerService.class);
        if (!FTPServerService.isRunning()) {
            warnIfNoExternalStorage();
            startService(serverService);
        }
    }

    private void stopServer() {
        Context context = getApplicationContext();
        Intent serverService = new Intent(context, FTPServerService.class);
        stopService(serverService);
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();

        Log.v(TAG, "Registering the FTP server actions");
        IntentFilter filter = new IntentFilter();
        filter.addAction(FTPServerService.ACTION_STARTED);
        filter.addAction(FTPServerService.ACTION_STOPPED);
        registerReceiver(ftpServerReceiver, filter);
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();

        Log.v(TAG, "Unregistering the FTPServer actions");
        unregisterReceiver(ftpServerReceiver);
    }

    /**
     * This receiver will check FTPServer.ACTION* messages and will update the button,
     * running_state, if the server is running and will also display at what url the
     * server is running.
     */
    BroadcastReceiver ftpServerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "A FTPServer action received");
            CheckBoxPreference running_state = (CheckBoxPreference) findPreference("running_state");
            if (intent.getAction().equals(FTPServerService.ACTION_STARTED)) {
                Log.v(TAG, "FTPServer is started action received");
                running_state.setChecked(true);
                // Fill in the FTP server address
                InetAddress address = FTPServerService.getWifiIp();
                if (address == null) {
                    Log.v(TAG, "Unable to retreive wifi ip address");
                    running_state.setSummary(R.string.cant_get_url);
                    return;
                }
                String iptext = "ftp://" + address.getHostAddress() + ":"
                        + FTPServerService.getPort() + "/";
                Resources resources = getResources();
                String summary = resources.getString(
                        R.string.running_summary_started, iptext);
                running_state.setSummary(summary);
            } else if (intent.getAction().equals(FTPServerService.ACTION_STOPPED)) {
                Log.v(TAG, "FTPServer is stopped action received");
                running_state.setChecked(false);
                running_state.setSummary(R.string.running_summary_stopped);
            }
        }
    };

    /**
     * Will check if the device contains external storage (sdcard) and display a warning
     * for the user if there is no external storage. Nothing more.
     */
    private void warnIfNoExternalStorage() {
        String storageState = Environment.getExternalStorageState();
        if (!storageState.equals(Environment.MEDIA_MOUNTED)) {
            Log.v(TAG, "Warning due to storage state " + storageState);
            Toast toast = Toast.makeText(this, R.string.storage_warning,
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

}
