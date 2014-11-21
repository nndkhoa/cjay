package com.cloudjay.cjay.task.command;

import android.content.Context;
import android.content.Intent;

import com.cloudjay.cjay.task.service.QueryService;
import com.google.gson.Gson;
import com.squareup.tape.InMemoryObjectQueue;
import com.squareup.tape.TaskQueue;

import org.androidannotations.annotations.EBean;

@EBean(scope = EBean.Scope.Singleton)
public class CommandQueue extends TaskQueue<Command> {

	private static final String FILENAME = "command_queue";
	Context context;

	public CommandQueue(Context context) {
		super(new InMemoryObjectQueue<Command>());
		this.context = context;
	}

	private void startService() {
		context.startService(new Intent(context, QueryService.class));
	}

	@Override
	public void add(Command entry) {
		super.add(entry);
		startService();
	}

	@Override
	public void remove() {
		super.remove();
	}

	public static CommandQueue create(Context context, Gson gson) {

//		FileObjectQueue.Converter<Command> converter = new GsonConverter<Command>(gson, Command.class);
//		File queueFile = new File(context.getFilesDir(), FILENAME);
//		FileObjectQueue<Command> delegate;
//
//		try {
//			delegate = new FileObjectQueue<Command>(queueFile, converter);
//		} catch (IOException e) {
//			throw new RuntimeException("Unable to create file queue.", e);
//		}

		return new CommandQueue(context);
	}
}
