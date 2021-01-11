package eden.wavplay.common;

import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/** The {@code GDMAudio} class eliminates the boilerplate required to play an
	{@code AudioInputStream} and a handful of relevant features.
	<br><br>
	Use the static methods found in {@code AudioSystem} to aid the construction
	of new instances. It is recommended for the {@code AudioInputStream} to
	support its {@code mark} and {@code reset} methods to make full use of this
	class. This can be achieved by wrapping its underlying {@code InputStream}
	to one that supports these methods, like a {@code BufferedInputStream}.

	@author     Brendon
	@version    u0r0, 08/19/2017
*/
public class GDMAudio implements Runnable {

	/** Amount of audio data to be buffered in bytes */
	public static final int BUFFER_SIZE = 4096;


	/** Audio resource to be read from upon playback */
	private final AudioInputStream stream;

	/** Defines audio parameters */
	private final AudioFormat format;

	/** Temporary medium for data interchange */
	private final byte[] buffer;

	/** Data line to write audio data to */
	private final SourceDataLine line;

	/** Reference to thread on which playback is being performed */
	private Thread thread;

	/** Denotes whether playback is not paused */
	private boolean running;


	/** Constructs a new instance of this class with a given format and stream.
		If done right, playback at abnormal speeds can be achieved here

		@param      format
					{@code AudioFormat} defining audio parameters

		@param      stream
					Audio resource to be read from upon playback

		@throws     LineUnavailableException
					If a line can not be opened because it is unavailable

		@throws     IOException
					If an input or output error occurs
	*/
	public GDMAudio(AudioInputStream stream, AudioFormat format)
		throws LineUnavailableException, IOException {

		// stream
		if (stream.markSupported()) {
			stream.mark(stream.available());
		}
		this.stream = stream;

		// format
		this.format = format;

		// buffer
		this.buffer = new byte[BUFFER_SIZE];

		// line
		this.line = AudioSystem.getSourceDataLine(format);
		line.open();
	}


	/** Plays audio from its underlying {@code AudioInputStream} */
	@Override
	public void run() {

		try {
			thread = Thread.currentThread();
			running = true;
			int bytes;

			line.start();

			while (running && (stream.available() > 0) && !isClosed()) {
				bytes = stream.read(buffer, 0, buffer.length);
				line.write(buffer, 0, bytes);
			}

			if (running) {
				line.drain();
				stop();
				reset();
			}
		} catch (Exception e) {

			System.err.println("[GDMAudioEngine]\n  Thread "
				+ Thread.currentThread().toString() + " caught exception: "
				+ e.toString());
		}
	}

	/** Pauses playback. Effective only when playback is ongoing */
	public void stop() {
		line.stop();
		thread = null;
		this.running = false;
	}

	/** Resets playback marker to its starting point

		@return     false if the operation was unsuccessful, in which case
					the {@code AudioInputStream} does not support {@code mark}
					and {@code reset};
					true otherwise
	*/
	public boolean reset() {

		try {
			stream.reset();
			stream.mark(stream.available());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/** Skips playback marker by an amount of bytes

		@return     false if the operation was unsuccessful;
					true otherwise

		@throw      IllegalArgumentException
					If {@code bytes < 0}
	*/
	public boolean skip(long bytes) {

		if (bytes < 0) {
			throw new IllegalArgumentException("Bad bytes: " + bytes);
		}

		try {
			stream.skip(bytes);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/** Awaits for playback to end, then returns

		@return     false if the operation was unsuccessful, in which case
					playback has ended; true otherwise
	*/
	public boolean await() {

		try {
			if (thread != null) {
				thread.join();
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/** Returns whether this {@code GDMAudio} is not playing

		@return     true if the condition is true;
					false otherwise
	*/
	public boolean isFree() {
		return !running;
	}

	/** Returns whether this {@code GDMAudio} is closed

		@return     true if the condition is true;
					false otherwise
	*/
	public boolean isClosed() {
		return !line.isOpen();
	}

	/** Releases any system resource associated to
		this {@code GDMAudio}

		@return     false if the operation was unsuccessful;
					true otherwise
	*/
	public boolean close() {

		try {
			line.close();
			stream.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}