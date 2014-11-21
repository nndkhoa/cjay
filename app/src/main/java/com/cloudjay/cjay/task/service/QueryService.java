package com.cloudjay.cjay.task.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.cloudjay.cjay.task.command.Command;

import org.androidannotations.annotations.EService;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

@EService
public class QueryService extends Service {

    Queue<Command> listCommands;

    public void add(Command command) {
        if (listCommands == null) {
            // Init
        }
        listCommands.add(command);
    }

    @Override
	public void onCreate() {
		super.onCreate();
        listCommands = new Queue<Command>() {
            @Override
            public boolean add(Command command) {
                return false;
            }

            @Override
            public boolean offer(Command command) {
                return false;
            }

            @Override
            public Command remove() {
                return null;
            }

            @Override
            public Command poll() {
                return null;
            }

            @Override
            public Command element() {
                return null;
            }

            @Override
            public Command peek() {
                return null;
            }

            @Override
            public boolean addAll(Collection<? extends Command> commands) {
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
            public boolean isEmpty() {
                return false;
            }

            @NonNull
            @Override
            public Iterator<Command> iterator() {
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
        };
	}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
