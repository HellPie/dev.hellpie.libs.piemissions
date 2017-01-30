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

package dev.hellpie.libs.piemissions;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class PiemissionsUtils {

	private static ArrayList<PiemissionRequest> queue = new ArrayList<>(0);
	private static WeakReference<Activity> bindReference;

	private PiemissionsUtils() {
		throw new UnsupportedOperationException("Must not instantiate PiemissionsUtils.");
	}

	public static void init(@NonNull Activity activity) {
		bindReference = new WeakReference<>(activity);
	}

	public static void requestPermission(@NonNull PiemissionRequest request) {
		Activity bind = bindReference.get();
		if(bind == null) throw new NullPointerException("init() has not been called yet.");

		ArrayList<String> denied = getDenied(request.getPermissions());

		if(denied.isEmpty()) {
			request.getCallback().onGranted();
		} else {
			queue.add(request);

			String[] deniedPerms = denied.toArray(new String[denied.size()]);
			ActivityCompat.requestPermissions(bind, deniedPerms, request.getCode());
		}
	}

	public static void onRequestResult(int requestCode, String[] permissions, int[] grantResults) {
		PiemissionRequest request = new PiemissionRequest(requestCode, "");
		if(queue.contains(request)) {
			request = queue.get(queue.indexOf(request));
			queue.remove(request);

			HashMap<String, Boolean> ratPerms = new HashMap<>(0);
			boolean allWentWell = true;

			for(int i = 0; i < permissions.length; i++) {
				if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
					String perm = permissions[i];
					ratPerms.put(perm, shouldShowRationale(Collections.singletonList(perm)));
					allWentWell = false;
				}
			}

			if(allWentWell) {
				request.getCallback().onGranted();
			} else {
				if(request.getCallback().onDenied(ratPerms)) {
					PiemissionRequest newRequest = new PiemissionRequest(request.getCode());

					ArrayList<String> ratPermsAble = new ArrayList<>(0);
					for(String key : ratPerms.keySet()) {
						if(ratPerms.get(key)) ratPermsAble.add(key);
					}

					newRequest.addPermissions(ratPermsAble);
					newRequest.setCallback(request.getCallback());

					if(ratPerms.isEmpty()) {
						final PiemissionRequest finalRequest = request;
						newRequest.setCallback(new BasePiemissionsCallback() {
							@Override
							public boolean onDenied(HashMap<String, Boolean> ratPerms) {
								finalRequest.getCallback().onDenied(ratPerms);
								return false;
							}
						});
					} else {
						newRequest.setCallback(request.getCallback());
					}

					requestPermission(newRequest);
				}
			}
		}
	}

	public static boolean shouldShowRationale(@NonNull List<String> permissions) {
		Activity bind = bindReference.get();
		if(bind == null) throw new NullPointerException("init() has not been called yet.");

		for(String perm : permissions) {
			if(!ActivityCompat.shouldShowRequestPermissionRationale(bind, perm)) return false;
		}

		return true;
	}

	public static ArrayList<String> getDenied(@NonNull List<String> permissions) {
		Activity bind = bindReference.get();
		if(bind == null) throw new NullPointerException("init() has not been called yet.");

		ArrayList<String> denied = new ArrayList<>(0);
		for(String perm : permissions) {
			if(ContextCompat.checkSelfPermission(bind, perm) == PackageManager.PERMISSION_DENIED) {
				if(!denied.contains(perm)) denied.add(perm);
			}
		}

		return denied;
	}
}
