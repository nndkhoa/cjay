/*
 * Copyright 2013 Chris Banes
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
 * limitations under the License.
 */
package com.cloudjay.cjay.events;

import java.util.ArrayList;
import java.util.List;

import com.cloudjay.cjay.model.TmpContainerSession;

public class PhotoSelectionRemovedEvent {

	private final List<TmpContainerSession> mUploads;

	public PhotoSelectionRemovedEvent(List<TmpContainerSession> uploads) {
		mUploads = uploads;
	}

	public PhotoSelectionRemovedEvent(TmpContainerSession upload) {
		mUploads = new ArrayList<TmpContainerSession>();
		mUploads.add(upload);
	}

	public List<TmpContainerSession> getTargets() {
		return mUploads;
	}

	public TmpContainerSession getTarget() {
		if (isSingleChange()) {
			return mUploads.get(0);
		} else {
			throw new IllegalStateException(
					"Can only call this when isSingleChange returns true");
		}
	}

	public boolean isSingleChange() {
		return mUploads.size() == 1;
	}

}
