package rosita.linkage.filtering;
import java.util.List;

import rosita.linkage.deprecated.Record;

/**
 * This is a utility class to serve as a wrapper/interface to working with 
 * threads.  That is, instead of having someone deal with all the individual threads
 * and synchronization issues, just have a "threadedProcessRecords()" visible. This
 * class should be quite scalable.
 * @author Brandon Abbott
 *
 */
public class ThreadHelper 
{

	// ******************************************************************************
	// ** RecordProcesor Sub-Class Begin
	// ******************************************************************************
	
	/**
	 * RecordProcessors are threads that can process records
	 * @author Brandon Abbott
	 */
	private static class RecordProcessor extends Thread
	{
		// A reference to the records
		private static List<E_Record> records = null;
		
		// Flags to determine what this thread should do
		private boolean performClean = false;
		private boolean performEncryption = false;
		
		// let this thread know what number it is.
		private int threadNum;
		
		// Where the thread should start processing records
		private int startIndex;
		
		// How many records this thread needs to process.
		private int quantity;

		/**
		 * Constructor for RecordProcssor thread
		 * @param inRecords - The set of records in need of processing
		 * @param clean - This thread will clean the records
		 * @param encrypt - This thread will encrypt the records
		 */
		public RecordProcessor(boolean clean, boolean encrypt, int startIndex, int quantity)
		{
			this.performClean = clean;
			this.performEncryption = encrypt;
			this.startIndex = startIndex;
			this.quantity = quantity;
		}

		/**
		 *  This method is called when a thread's start() method has been called.
		 */
		@Override
		public void run()
		{
			
			// TODO: It may be better to throw an exception instead of returning here.
			// Don't do anything if the record array hasn't been set.
			if (records == null)
				return;

			// Iterate through the proper amount of records.
			for (int i = 0; i < quantity; i++)
			{
				if (performClean)
					RecordProcessor.records.get(i+startIndex).normalizeValues();
				if (performEncryption)
					RecordProcessor.records.get(i+startIndex).threadedEncryptValues(threadNum);
			}

		}	

		/**
		 * Setter for member threadNum
		 * Let this thread know what number it is.
		 * @param n - The number to be set
		 */
		public void setThreadNum(int n)
		{
			this.threadNum = n;
		}

		/**
		 * Set the Records ArrayList
		 * @param records - A reference to a list of records
		 */
		public static synchronized void setRecords (List<E_Record> records)
		{
			RecordProcessor.records = records;
		}

	}
	// ******************************************************************************
	// ** RecordProcesor Sub-Class End
	// ******************************************************************************

	
	// ******************************************************************************
	// ** ThreadHelper Class Definitions Begin
	// ******************************************************************************

	/** 
	 * Suppress default constructor for non-instantiability.
	 * This is a utility class!
	 */
	private ThreadHelper() 
	{
		throw new AssertionError();
	}

	/**
	 * Cleans each record, and then returns the cleaned version of the original list
	 * @param records - The list of records needing to be cleaned, and processed in parallel.
	 */
	public static void cleanRecords(List<E_Record> records)
	{
		processRecords(records, true, false);
	}

	/**
	 * Encrypts each record, and then returns the encrypted version of the original list
	 * @param records - The list of records needing to be cleaned, and processed in parallel.
	 */
	public static void encryptRecords(List<E_Record> records)
	{
		processRecords(records, false, true);
	}

	/**
	 * Process the records given to this function.  Options are clean and/or encrypt.
	 * @param records - The records to clean or encrypt.
	 * @param clean - True to clean, False to not clean.
	 * @param encrypt - True to encrypt, False to not encrypt.
	 */
	public static void processRecords(List<E_Record> records, boolean clean, boolean encrypt)
	{
		// Number of threads
		final int numThreads = 4;

		// TODO: just process the records iteratively if there's less than numThreads

		// Determine where to split the records
		int n = records.size();
		int subSize = n / numThreads;

		// Give the RecordProcessors a hey've all been joined.reference to the records.
		RecordProcessor.setRecords(records);
		
		// If we need to encrypt things, initialize threaded filters in Record
		if (Record.getThreadedEncryptionFilters() != true)
			Record.setThreadedEncryptionFilters(numThreads);

		// Create RecordProcessor Objects & Kick 'em off!
		RecordProcessor rp[] = new RecordProcessor[numThreads];
		for (int i = 0; i < numThreads; i++)
		{
			// Determine where each thread starts, and how many records it needs to process.
			int start = i * subSize;
			int quantity = subSize;
			// The last thread may have to handle more. 
			if (i == numThreads - 1)
				quantity += n % numThreads;
			
			rp[i] = new RecordProcessor(clean, encrypt, start, quantity);
			rp[i].setThreadNum(i);
			rp[i].start();
		}
		
		// Wait for threads to finish
		waitForThreads(rp);
		
	}

	/**
	 * Given an array of RecordProcessors, wait for them all to finish running.
	 * That is, keep trying to join them until they're dead.
	 * @param rp - An array of RecordProcessors.
	 */
	private static void waitForThreads(RecordProcessor rp[])
	{
		int numThreads = rp.length;
		boolean done = false;
		do
		{
			// Try to join the threads.
			try
			{
				for (int i = 0; i < numThreads; i++)
					rp[i].join();

			} catch (InterruptedException e) {
				System.err.println("Error: Could not join threads.");
				e.printStackTrace();
			} 

			// Determine if we're done.
			done = true;
			for (int i = 0; i < numThreads; i++)
				if (rp[i].isAlive())
					done = false;

		} while (! done);
	}
	
	/**
	 * Print a message to System.out 
	 * but also identify which thread is saying the message
	 * @param message - The message to post.
	 */
	private static void threadMessage(String message)
	{
		String threadName = Thread.currentThread().getName();
		System.out.format("%s: %s%n", threadName, message);
	}

}
