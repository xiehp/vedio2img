package xie.v2i.core;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

/**
 *
 */
public class MyMediaPlayerFactory extends MediaPlayerFactory {
	public MyMediaPlayerFactory(String[] args) {
		super(args);
	}

	public LibVlc getLibvlc() {
		return null;
	}
}
