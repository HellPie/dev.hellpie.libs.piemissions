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

import java.util.HashMap;

public interface PiemissionsCallback {

	/**
	 * Called if all the requested permissions were granted
	 */
	void onGranted();

	/**
	 * Called if at least one of the requested permissions is not granted.
	 *
	 * @param rationalizablePermissions The list of permissions that were denied, with an
	 *                                  associated boolean value per each permissions,
	 *                                  determining whether or not the permissions has
	 *                                  been permanently denied. If the boolean value
	 *                                  is true, it also means that the shouldShowRationale
	 *                                  function will also return true and the user must
	 *                                  be warned about the usage and needing of the
	 *                                  corresponding permissions.
	 *
	 * @return Whether or not to ask again fo all the permissions that were not permanently denied.
	 */
	boolean onDenied(HashMap<String, Boolean> rationalizablePermissions);
}
