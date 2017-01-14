package net.drewke.tdme.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.drewke.tdme.audio.Audio;
import net.drewke.tdme.audio.AudioEntity;


public final class AudioTest {

	public static void main(String[] args) throws Exception {
		AudioEntity test = Audio.getInstance().addStream("test", "resources/tests/music", "memento-sanchez_monate_spaeter.ogg");
		test.setLooping(true);
		test.setFixed(true);
		test.play();
		Thread audioUpdateThread = new Thread() {
			public void run() {
				while(this.isInterrupted() == false) {
					Audio.getInstance().update();
					try { Thread.sleep(17L); } catch (InterruptedException ie) { this.interrupt(); }
				}
			}
		};
		audioUpdateThread.start();
		System.out.println("Press 'enter' to quit");
		BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
		buf.readLine();
		audioUpdateThread.interrupt();
		Audio.getInstance().shutdown();
	}

}
