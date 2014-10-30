package assign4;


import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.*;

public class WebWorker implements Runnable {
	
	private String urlString;
	private int row;
	private WebFrame frame;
	private long t_start;
	private long t_end;
	private int bytes;
	
	
	public WebWorker(String urlString, int row, WebFrame frame) {
		this.urlString = urlString;
		this.row = row;
		this.frame = frame;
	}
	
	public void run() {
		t_start = System.currentTimeMillis();
		frame.threadsRunning++;
		updateRunning();
		download();
		frame.latch.countDown();
		frame.threadsRunning--;
		updateRunning();
		updateComplete();
		checkForFinish();
	}
	
	public void updateRunning() {
		frame.running.setText("Running: " + Integer.toString(frame.threadsRunning));
		int value = frame.tblData.size() - (int) frame.latch.getCount();
		frame.progBar.setValue(value);
	}
	
	public void updateComplete() {
		int value = frame.tblData.size() - (int) frame.latch.getCount();
		frame.completed.setText("Completed: " + value);
	}
	
	public void checkForFinish(){
		if (frame.latch.getCount() == 0){
			frame.single.setEnabled(true);
			frame.concurrent.setEnabled(true);
			frame.stop.setEnabled(false);
			frame.progBar.setValue(0);
			frame.running.setText("Running: " + 0);
			frame.endTime = System.currentTimeMillis();
			long elapsedTot = frame.endTime - frame.startTime;
			frame.elapsed.setText("Elapsed: " + (int) elapsedTot);
		}
	}

	public void download() {
	//  This is the core web/download i/o code...
 		InputStream input = null;
		StringBuilder contents = null;
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
		
			// Set connect() to throw an IOException
			// if connection does not succeed in this many msecs.
			connection.setConnectTimeout(5000);
			
			connection.connect();
			input = connection.getInputStream();

			BufferedReader reader  = new BufferedReader(new InputStreamReader(input));
		
			char[] array = new char[1000];
			int len;
			contents = new StringBuilder(1000);
			while ((len = reader.read(array, 0, array.length)) > 0) {
				contents.append(array, 0, len);
				Thread.sleep(100);
			}
			
			bytes = contents.length();
			
//			System.out.println("Nice!");
			t_end = System.currentTimeMillis();
			String dwnStr = "";
			dwnStr += getTime() + "   ";
			dwnStr += getElapsed() + " ms    ";
			dwnStr += bytes + " bytes";
			
			updateModel(dwnStr);
			// Successful download if we get here
			
		}
		// Otherwise control jumps to a catch...
		catch(MalformedURLException ignored) {
//			System.out.println("Malformed Address");
			updateModel("err: malformed address");
		}
		catch(InterruptedException exception) {
			// YOUR CODE HERE
			// deal with interruption
			
			updateModel("interrupted");
			
		}
		catch(IOException ignored) {
//			System.out.println("Website does not exist");
			updateModel("err: website does not exist");
		}
		// "finally" clause, to close the input stream
		// in any case
		finally {
			try{
				if (input != null) input.close();
			}
			catch(IOException ignored) {}
		}
		
	}
	
	public long getElapsed() {
		return (t_end - t_start);
	}
	
	public String getTime(){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
		
	}
	
	public void updateModel(String msg) {
		frame.tblData.get(row)[1] = msg;
		frame.model.fireTableDataChanged();
	}


	
}
