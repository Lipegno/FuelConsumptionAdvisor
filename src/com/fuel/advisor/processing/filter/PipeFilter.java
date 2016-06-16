package com.fuel.advisor.processing.filter;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import android.os.Bundle;
import android.os.Parcel;

/**
 * An abstraction class used by the processing pipeline filters. The filters
 * must sub-class this and implement {@link PipeFilter#onDataReceived(Object)} to perform operations according
 * to specific filter needs.
 * 
 * The class implements the {@link Runnable} interface, so that each filter can run on a separate Thread.
 * The class supports both the notion of pure filters and of data sinks. Filter initialization
 * must be coordinated from an outside module, responsible for initializing the filters and
 * respective {@link PipedOutputStream} and {@link ObjectOutputStream} objects.<br><br>
 * 
 * To define a pure filter, we must connect the filter input stream using the
 * {@link PipeFilter#connectInputStream(PipedOutputStream)} method, before calling
 * the {@link PipeFilter#connectOutputStream(PipedInputStream)} method to connect the
 * output stream to another filter or data sink input stream. Do <b>not</b> use the n+1 filter's
 * {@link PipeFilter#connectInputStream(PipedOutputStream)} to connect to the n filter.<br><br>
 * 
 * Finally, a class should be responsible for setting up the {@link Thread} objects and starting
 * them, passing as input the {@link PipeFilter} class. Don't forget to use the
 * {@link PipedOutputStream#write(byte[], int, int)} method to send content to the first filter's
 * input stream.<br><br> 
 * 
 * @author
 * Tiago Camacho
 * 
 * @see
 * {@link PipedInputStream}<br>
 * {@link PipedOutputStream}<br>
 */
@SuppressWarnings("unused")
public abstract class PipeFilter implements Runnable	{

	public static final int PARCELABLE_SIZE = 1024;
	
	private static final boolean DEBUG = true;
	private static final int MAX_BUFFER_SIZE = 1024;
	
	private final byte[] buffer = new byte[MAX_BUFFER_SIZE];
	private final Parcel parcel = Parcel.obtain();
	private final Parcel forwardParcel = Parcel.obtain();
	private final PipeFilter filter;
	
	private String filterName = null;
	private PipedInputStream inPis;
	private PipedOutputStream outPos;
	private Bundle rcvdContent = new Bundle();
	private boolean outputConnected = false;
	private Thread t;
	
	/**
	 * The default constructor. Recevies nothing and as a
	 * consequence as to initialize the input and output streams
	 */
	public PipeFilter() {

		filter = null;
		inPis = new PipedInputStream();
		outPos = new PipedOutputStream();
	}
	
	/**
	 * Constructor that receives solely an outputstream that works as the input
	 * for the current filter
	 * @param output - The output stream to be used as input for this filter
	 */
	public PipeFilter(PipedOutputStream output)	{
		
		this.filter = null;
		try {
			inPis = new PipedInputStream(output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor that receives another filter. This constructor
	 * connects its input to the specified filter, receiving
	 * content after it is filtered by the specified filter. It works
	 * similarly to the Decorator pattern, adding functionality to the
	 * inner filter in run-time
	 * 
	 * @param filter - The inner component that filters the content
	 * before passing it to this object
	 */
	public PipeFilter(PipeFilter filter)	{
		
		this.filter = filter;
		try {
			// We connect the inner filter's output to this filter's input
			// We therefore receive the filter content on the outer filter
			inPis = new PipedInputStream();
			this.filter.connectOutputStream(inPis);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor that receives an output stream that it will use as its input,
	 * forwarding the transformed content to the specified filter
	 * @param output - The output stream to use as the input for this filter
	 * @param filter - The filter to forward the transformed content
	 */
	public PipeFilter(PipedOutputStream output, PipeFilter filter)	{
		
		this.filter = filter;
		try {
			inPis = new PipedInputStream(output);
			outPos = new PipedOutputStream(filter.getInputStream());
			outputConnected = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Call this to initialize the filter
	 */
	public void start()	{
		
		// Start the inner filter if defined
		if (filter != null)	{
			filter.start();
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		t = new Thread(this, filterName);
		t.start();
	}
	
	/**
	 * Call this to interrupt the current Thread flow
	 */
	public void stop()	{
		
		// If inner filter is present, then stop it first
		if (filter != null)
			filter.stop();
		t.interrupt();
	}
	
	@Override
	public void run() {

		try {
			int size = 0;
			int total = 0;

			// Continously read data from the input stream
			while ((size = inPis.read(buffer, total, PARCELABLE_SIZE - total)) != -1)	{
				
				// Determine if we have a full message
				if ((total += size) < PARCELABLE_SIZE)
					continue;
				
				// We can now proceed to extraction of the data
				parcel.unmarshall(buffer, 0, PARCELABLE_SIZE);
				parcel.setDataPosition(0);
				rcvdContent = parcel.readBundle();
				// Call upon the abstract method (implemented by sub-classes)
				onDataReceived(rcvdContent);
				
				// If output is connected then forward transformed content to it
				if (outputConnected)	{
					Parcel p = Parcel.obtain();
					p.writeBundle(rcvdContent);
					p.setDataSize(PARCELABLE_SIZE);
					outPos.write(p.marshall());
				}
				total = 0;
			}
		} catch (InterruptedIOException iioe)	{
			// Ignore this
		} catch (IOException ioe)	{
			// Ignore this
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			// Ensure clean-up
			try {
				parcel.recycle();
				forwardParcel.recycle();
				inPis.close();
				if (outPos != null)
					outPos.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	/**
	 * Get the output stream used to forward content to another filter
	 * @return - The PipedOutputStream object
	 */
	public PipedOutputStream getOutputStream()	{
		
		return this.outPos;
	}
	
	/**
	 * Get the input stream used to read content from a filter or data source
	 * @return - The PipedInputStream object
	 */
	public PipedInputStream getInputStream()	{
		
		return this.inPis;
	}
	
	/**
	 * Attach the input stream to a filter/data source output stream
	 * @param outPos - The output stream used by the filter or data source
	 */
	public void connectInputStream(PipedOutputStream outPos)	{
		
		try {
			inPis.connect(outPos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Attach the output stream to a filter/data sink input stream
	 * @param inPis - The input stream used by the filter or data sink
	 */
	public void connectOutputStream(PipedInputStream inPis)	{
		
		try {
			if (outPos == null)
				outPos = new PipedOutputStream();
			outPos.connect(inPis);
			outputConnected = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Override to implement specific filter behavior when data is made available to
	 * the Thread that listens on the input stream
	 * @param data - The Bundle object that holds the data
	 */
	protected abstract void onDataReceived(Bundle data);
	
	/**
	 * Override to set the filter name. Sub-classes must implement this so that
	 * Thread is differentiated by name
	 * @param filterName - The name of the filter
	 */
	protected void setFilterName(String filterName)	{
		
		this.filterName = filterName;
	}
}