# WavPlay-Java

—Digital audio playback written in Java.

*This project is on hold.*

## Downloads

* [[**Latest Release**](https://github.com/ed7n/wavplay-java/raw/master/release/wavplay.jar)] — Update 1 Revision 0, 08/22/2017.

## Building

    $ javac -d release --release 8 --source-path src src/eden/wavplay/WavPlay.java && jar -c -f release/wavplay.jar -e eden.wavplay.WavPlay -C release eden

## Usage

`java -jar wavplay.jar [debug] [loop]`

## About

See [[**wavplay-ppt**](https://github.com/ed7n/wavplay-ppt)].
