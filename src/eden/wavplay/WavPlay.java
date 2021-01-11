package eden.wavplay;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import eden.wavplay.common.GDMAudioEngine;
import eden.wavplay.common.Modal;

/** Plays wav files in sequence

	@author     Brendon
	@version    u1r0, 08/23/2017
*/
public class WavPlay {

	// static constants

	/** {@code Scanner} from which to get user input */
	private static final Scanner scanner = new Scanner(System.in);

	/** Handles audio playback */
	private static final GDMAudioEngine audio = new GDMAudioEngine(2);


	// static variables

	/** {@code Modal} for printouts
		@see    {@link Modal}
	*/
	private static Modal modal;

	/** Number of tracks to be played */
	private static byte tracks;

	/** Whether playback is looped */
	private static boolean looped;


	// constructors

	/** To prevent instatiation of this class */
	private WavPlay(){}


	// static methods

	/** The main method is the entry point to the program

		@param      args
					Command-line arguments to be passed on execution
	*/
	public static void main(String[] args) {
		init(args);

		modal.printCln("\n\n"
			+ "Welcome to WavPlay!\n"
			+ "-------------------\n"
			+ "u1r0 by Brendon, 08/22/2017\n\n");

		modal.println(Modal.Mode.DEBUG, "DEBUG messages visible!");

		// looped
		if (looped) {

			modal.println(Modal.Mode.INFO,
				"Loop mode enabled. [ctrl+c] to terminate");
		}

		// tracks
		tracks = promptTracks();

		while (!checkTracks(tracks)) {
			modal.println(Modal.Mode.ALERT, "Can not proceed!");
			tracks = promptTracks();
		}
		playTracks(tracks);
	}

	/** Prompts for number of tracks
		@return     Entered number of tracks
	*/
	private static byte promptTracks() {

		do {
			modal.print(Modal.Mode.PROMPT, "Input tracks (0 - 99): ");
			String out = scanner.nextLine();

			if (out.matches(".*\\D+.*|.*\\d{3,}.*")) {
				modal.println(Modal.Mode.ERROR, "Invalid input!");
			} else {
				return Byte.parseByte(out);
			}
		} while (true);
	}

	/** Prints the existence of track files and returns whether this check is
		perfect

		@param      tracks
					Number of tracks to check for

		@return     {@code false} if at least one track is missing;
					{@code true} otherwise
	*/
	private static boolean checkTracks(byte tracks) {
		boolean perfect = true;
		modal.println(Modal.Mode.INFO, "Checking tracks...");

		for (byte b = 1; b <= tracks; b++) {

			if (!Files.exists(Paths.get(b + ".wav"))) {

				if (perfect) {
					perfect = false;
					modal.print(Modal.Mode.ERROR, "...track(s) missing:");
				}
				modal.printCln(" " + b);
			}
		}

		if (perfect) {
			modal.println(Modal.Mode.INFO, "...tracks OK");
		} else {
			modal.println();
		}
		return perfect;
	}

	/** Plays audio track files

		@param      track
					Number of tracks to be played

		@throws     IOException
					If an I/O exception occurs

		@throws     UnsupportedAudioFileException
					If the audio resource does not contain valid data of a
					recognized file type and format

		@throws     IllegalStateException
					If there are no free channels available for use

		@throws     LineUnavailableException
					If a line can not be opened because it is unavailable
	*/
	private static void playTracks(byte tracks) {
		byte current = loadTrack((byte) 1);
		byte next = Byte.MIN_VALUE;
		byte number = Byte.MIN_VALUE;
		modal.println(Modal.Mode.ALERT, "BEGIN PLAYBACK SEQUENCE");

		sequence:
		for (byte b = 1; b <= tracks;) {
			audio.play(current);
			number = b;

			modal.println(Modal.Mode.INFO,
				"Playing track " + number + "/" + tracks + "...");

			// loop
			if ((b == tracks) && looped) {
				b = 0;
			}

			// seek and preload next track
			while (b++ < tracks) {
				next  = loadTrack(b);

				if (next != Byte.MIN_VALUE) {
					break;
				}
			}

			audio.await(current);

			try {
				audio.unload(current);
				modal.println(Modal.Mode.DEBUG, "Unload track " + number);
			} catch (IOException e) {

				modal.println(Modal.Mode.ERROR, "An I/O error has occured "
					+ "while unloading track " + number);

				modal.println(Modal.Mode.DEBUG, "Exception caught: "
					+ e.toString());

				break sequence;
			}
			current = next;
		}
		modal.println(Modal.Mode.ALERT, "PLAYBACK SEQUENCE END");
	}

	/** Loads an audio track file

		@param      track
					Audio track file to be loaded

		@return     The channel number mapped the track maps to
	*/
	private static byte loadTrack(byte track) {

		try {

			if (Files.exists(Paths.get(track + ".wav"))) {
				final Byte out = (byte) audio.load(track + ".wav");

				modal.println(Modal.Mode.DEBUG,
					"Load track " + track + " to channel " + out);

				return out;
			}
			modal.println(Modal.Mode.DEBUG, "Missing track " + track);
			return Byte.MIN_VALUE;
		} catch (IOException e) {

			modal.println(Modal.Mode.ERROR,
				"An I/O error has occured while loading track " + track);

			modal.println(Modal.Mode.DEBUG,
				"Exception caught: " + e.toString());

			return -1;

		} catch (UnsupportedAudioFileException e) {

			modal.println(Modal.Mode.ERROR,
				track + ".wav does not contain recognizable valid data");

			modal.println(Modal.Mode.DEBUG,
				"Exception caught: " + e.toString());

			return -1;

		} catch (IllegalStateException e) {

			modal.println(Modal.Mode.ERROR,
				"An audio engine error has occured while loading track " + track);

			modal.println(Modal.Mode.DEBUG,
				"Exception caught: " + e.toString());

			return -1;

		} catch (LineUnavailableException e) {

			modal.println(Modal.Mode.ERROR,
				"A line can not be opened for track " + track);

			modal.println(Modal.Mode.DEBUG,
				"Exception caught: " + e.toString());

			return -1;

		}
	}

	/** Initializes this program

		@param      args
					Command-line arguments to be parsed
	*/
	private static void init(String[] args) {
		boolean debug = false;

		// check arguments
		for (String s : args) {

			if (s.equalsIgnoreCase("loop")) {
				looped = true;
			} else if (s.equalsIgnoreCase("debug")) {
				debug = true;
			}
		}

		// make Modal
		if (debug) {
			modal = new Modal("WavPlay", System.out, 3, true);
		} else {
			modal = new Modal("WavPlay", System.out, 2, true);
		}
	}
}