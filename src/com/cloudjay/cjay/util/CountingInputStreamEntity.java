package com.cloudjay.cjay.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.InputStreamEntity;

public class CountingInputStreamEntity extends InputStreamEntity {

	class CountingOutputStream extends OutputStream {
		private long counter = 0l;
		private OutputStream outputStream;

		public CountingOutputStream(OutputStream outputStream) {
			this.outputStream = outputStream;
		}

		@Override
		public void write(int oneByte) throws IOException {
			outputStream.write(oneByte);
			counter++;
			if (listener != null) {
				int percent = (int) (counter * 100 / length);
				listener.onChange(percent);
			}
		}
	}

	public interface UploadListener {
		public void onChange(int percent);
	}

	private UploadListener listener;

	private long length;

	public CountingInputStreamEntity(InputStream instream, long length) {
		super(instream, length);
		this.length = length;
	}

	public void setUploadListener(UploadListener listener) {
		this.listener = listener;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream));
	}

}
