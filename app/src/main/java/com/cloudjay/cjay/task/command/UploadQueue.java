package com.cloudjay.cjay.task.command;

import android.content.Context;
import android.support.annotation.NonNull;

import com.cloudjay.cjay.DataCenter_;
import com.cloudjay.cjay.model.UploadObject;
import com.cloudjay.cjay.task.command.cjayobject.AddCJayObjectCommand;

import org.androidannotations.annotations.EBean;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

@EBean(scope = EBean.Scope.Singleton)
public class UploadQueue implements Queue<UploadObject> {

	Context context;

	public UploadQueue(Context context) {
		this.context = context;
	}

	/**
	 * Add new command and also start Upload intent service
	 *
	 * @param cJayObject
	 * @return
	 */
	@Override
	public boolean add(UploadObject cJayObject) {
		DataCenter_.getInstance_(context).add(new AddCJayObjectCommand(context, cJayObject));
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends UploadObject> cJayObjects) {
		for (UploadObject object : cJayObjects) {
			DataCenter_.getInstance_(context).add(new AddCJayObjectCommand(context, object));
		}
		return false;
	}

	@Override
	public void clear() {

	}

	@Override
	public boolean contains(Object o) {
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> objects) {
		return false;
	}

	@Override
	public boolean equals(Object o) {
		return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@NonNull
	@Override
	public Iterator<UploadObject> iterator() {
		return null;
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> objects) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> objects) {
		return false;
	}

	@Override
	public int size() {
		return 0;
	}

	@NonNull
	@Override
	public Object[] toArray() {
		return new Object[0];
	}

	@NonNull
	@Override
	public <T> T[] toArray(T[] ts) {
		return null;
	}

	@Override
	public boolean offer(UploadObject cJayObject) {
		return false;
	}

	@Override
	public UploadObject remove() {

		return null;
	}

	@Override
	public UploadObject poll() {
		return null;
	}

	@Override
	public UploadObject element() {
		return null;
	}

	@Override
	public UploadObject peek() {
//		DataCenter_.getInstance_(context).add(new GetNextJobCommand());
		return null;
	}
}
