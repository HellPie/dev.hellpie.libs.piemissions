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

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PiemissionsRequest {

	private final int code;
	private ArrayList<String> permissions;
	private PiemissionsCallback callback = new BasePiemissionsCallback();

	public PiemissionsRequest(int requestCode, @NonNull String... permissions) {
		this.code = requestCode;
		this.permissions = new ArrayList<>(Arrays.asList(permissions));
	}

	public void addPermissions(@NonNull List<String> permissions) {
		this.permissions.addAll(permissions);
	}

	public void removePermissions(@NonNull List<String> permissions) {
		this.permissions.removeAll(permissions);
	}

	@NonNull
	public List<String> getPermissions() {
		return new ArrayList<>(permissions);
	}

	/*package*/ int getCode() {
		return code;
	}

	/*package*/ PiemissionsCallback getCallback() {
		return callback;
	}

	public void setCallback(PiemissionsCallback callback) {
		this.callback = (callback == null ? new BasePiemissionsCallback() : callback);
	}

	@Override
	public boolean equals(Object o) {
		return o != null && o instanceof PiemissionsRequest && ((PiemissionsRequest) o).getCode() == code;
	}
}
