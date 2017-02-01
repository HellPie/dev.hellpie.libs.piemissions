/*
 * Copyright 2017 Diego Rossi (@_HellPie)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package dev.hellpie.libs.piemissions.demo;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import dev.hellpie.libs.piemissions.PiemissionsRequest;
import dev.hellpie.libs.piemissions.PiemissionsCallback;
import dev.hellpie.libs.piemissions.PiemissionsUtils;

// Please see https://developer.android.com/guide/topics/permissions/index.html
// before continuing for a great deep explanations on how runtime permissions work.

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	private static final int PERMISSIONS_CODE = 1337;
	private static final String[] PERMISSIONS = new String[] {
			Manifest.permission.INTERNET,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_CONTACTS
	};

	private Switch internetSwitch;
	private Switch storageSwitch;
	private Switch contactsSwitch;

	private Button askAgainButton;

	// We store the PiemissionsRequest here because we need to access it from another method
	// within this class (onClick() to ask again).
	// A PiemissionsRequest manages asking the system to get the requested permissions and uses
	// a unique number to this application to identify which request this is.
	private PiemissionsRequest request = new PiemissionsRequest(PERMISSIONS_CODE, PERMISSIONS);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Register all the UI needed BY the permission first
		internetSwitch = (Switch) findViewById(R.id.internetSwitch);
		storageSwitch = (Switch) findViewById(R.id.storageSwitch);
		contactsSwitch = (Switch) findViewById(R.id.contactsSwitch);

		askAgainButton = (Button) findViewById(R.id.askAgainButton);
		askAgainButton.setOnClickListener(this);

		// Permissions in Android 6+ are at runtime, init my helper library
		PiemissionsUtils.init(this);

		// Create a new Callback class that will be called by Piemissions Utils whenever a PiemissionsRequest
		// is executed and the user has replied to all the permission requested
		request.setCallback(new PiemissionsCallback() {
			@Override
			public void onGranted() {

				// Here you will be able to do anything you want to happen whenever a new permission
				// is granted. Be it starting a new Activity via startActivity(), opening a file through
				// java.nio Channels or java.io Buffers, calling a method that does other stuff and
				// even nothing at all if the permission was useless to you! (Don't add uselessness tho)

				// We simply check what we got granted and toggle the Switches accordingly...
				updateSwitches();
			}

			@Override
			public boolean onDenied(HashMap<String, Boolean> rationalizablePermissions) {

				// We simply check what we got granted and toggle the Switches accordingly...
				updateSwitches();

				for(String permission : PERMISSIONS) {
					if(rationalizablePermissions.containsKey(permission)) {
						// If we got denied a permission (HOPEFULLY NOT INTERNET, CAUSE THAT'D BE WEIRD)
						// We might be able to ask for it again, if the user didn't check the checkbox
						// on the system dialog...

						if(rationalizablePermissions.get(permission)) {
							// Ok, we can ask again for AT LEAST one permission. Piemissions Utils
							// will simply manage all the denied permissions on its own and won't need
							// to know which ones to ask again for.

							// Please notice that before simply asking again the Android Developers
							// Guidelines **highly** suggest you CLEARLY STATE why you need the permission
							// to the user, so that (s)he will be able to truly decide whether or not
							// to give or deny the permission again.

							// Enable the button in case the user wants to ask again
							toggleButton(true);

							// Returning "true" would mean the library will automatically request the
							// permissions again, but since this is NOT expected behavior for an app
							// and is therefore a BAD DESIGN decision, we will let the user click the
							// button in this case.
							// Please note that showing an AlertDialog explaining  why the need of
							// certain permissions and how they will be used, and then acting based
							// on the reply (dialog canceled or positive button clicked) eventually
							// asking again via "return true".

							return false;
						}
					}
				}


				// If we got denied some permissions and we reached this point this means no permission
				// in the list of the denied ones can be asked for again, which means the user ticked
				// the checkbox in the system dialog.
				// The only way out of this is politely asking the user whenever the permission is truly
				// needed if it can go to the Settings app, in the Apps section, find you app and manually
				// toggle the required permission for you.
				// Please note you can help the user get there with an Intent with action
				// Intent.ACTION_APPLICATION_DETAILS_SETTINGS
				// Which will redirect the user directly to your app's page in the Settings app.

				// Disable the button since we cannot ask again for permissions...
				toggleButton(false);

				// Tell the library to not try asking again for permissions
				return false;
			}
		});


		// Execute the request for the permissions listed in it, result will be reported to the
		// PiemissionCallback set on the request itself.
		PiemissionsUtils.requestPermission(request);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		// This method is called by Android  whenever a permission is denied or granted, be it forever
		// or not.
		// Piemissions Utils features a good integrated system to manage this changes and only warn
		// when needed in an asynchronous thread-safe fashion.

		// Let the library handle everything in this method.
		PiemissionsUtils.onRequestResult(requestCode, permissions, grantResults);
	}


	// This method is overriding the View.OnClickListener.onClick() method, we use this not for the
	// permission, but rather to let the user manually ask again for permissions through the UI button
	@Override
	public void onClick(View v) {

		// Execute the request again, the already granted permissions will simply be managed automatically
		PiemissionsUtils.requestPermission(request);
	}

	public void updateSwitches() {

		// Since this method might be called from other Threads the app needs to make sure all UI
		// modifications are executed on the UI Thread, otherwise the application will crash and
		// laugh at the developer for how incompetent (s)he is on the most basic knowledge.
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Get all the denied permissions asked so far to the utils class.
				// The getDenied() method is static and only returns the permissions that were denied
				// since the last time Piemissions Utils was initialized with .init().
				List<String> deniedPermissions = PiemissionsUtils.getDenied(Arrays.asList(PERMISSIONS));

				// Since INTERNET is a pre-granted permissions it should never be denied
				internetSwitch.setChecked(!deniedPermissions.contains(PERMISSIONS[0]));

				// Check the other permissions too
				storageSwitch.setChecked(!deniedPermissions.contains(PERMISSIONS[1]));
				contactsSwitch.setChecked(!deniedPermissions.contains(PERMISSIONS[2]));
			}
		});
	}

	private void toggleButton(final boolean value) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				askAgainButton.setEnabled(value);
			}
		});
	}
}
