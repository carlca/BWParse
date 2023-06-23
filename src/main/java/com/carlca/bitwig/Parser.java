package com.carlca.bitwig;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.javatuples.Triplet;

public class Parser {

	public static void main(String[] args) {
		if (args.length == 0) {
			generateDummyOutput();
		} else {
			boolean debug = args.length == 2 && args[1].equals("debug");
			processPreset(args[0], debug);
		}
	}

	private static void generateDummyOutput() {
		System.out.println();
	}

	private static void processPreset(String filename, boolean debug) {
		try (RandomAccessFile file = new RandomAccessFile(new File(filename), "r")) {
			int pos = 0x36;
			int size;
			do {
				Triplet<Integer, Integer, String> result = readKeyAndValue(file, pos, debug);
				pos = result.getValue0();
				size = result.getValue1();
			} while (size != 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Triplet<Integer, Integer, String> readKeyAndValue(RandomAccessFile file, int pos, boolean debug) throws IOException {
		int size;
		String text;
		Triplet<Integer, Integer, String> result = new Triplet<>(0, 0, null);
		int skips = getSkipSize(file, pos);
		if (debug) {
			getSkipSizeDebug(file, pos);
			System.out.printf("%d skips\n", skips);
		}
		pos += skips;
		result = readNextSizeAndChunk(file, pos);
		pos = result.getValue0(); size = result.getValue1(); text = result.getValue2();
		if (size == 0) {
			return new Triplet<Integer,Integer,String>(0, 0, null);
		}
		printOutput(size, pos, text);

		skips = getSkipSize(file, pos);
		if (debug) {
			getSkipSizeDebug(file, pos);
			System.out.printf("%d skips\n", skips);
		}
		pos += skips;
		result = readNextSizeAndChunk(file, pos);
		pos = result.getValue0(); size = result.getValue1(); text = result.getValue2();
		printOutput(size, pos, text);
		System.out.println();
		return new Triplet<>(pos, size, null);
	}

	private static int getSkipSize(RandomAccessFile file, int pos) throws IOException {
		Triplet<Integer, Integer, byte[]> newRead = readFromFile(file, pos, 32, false);
		byte[] bytes = newRead.getValue2();
		int[] check = new int[]{5, 8, 13};
		for (int i = 0; i < bytes.length; i++) {
			if ((bytes[i] >= 0x20) && (inArray(check, i & 255))) {
				return i - 4;
			}
		}
		return 1;
	}

	private static boolean inArray(int[] array, int element) {
		for (int i : array) {
			if (i == element) {
				return true;
			}
		}
		return false;
	}

	private static void getSkipSizeDebug(RandomAccessFile file, int pos) throws IOException {
		Triplet<Integer, Integer, byte[]> newRead = readFromFile(file, pos, 32, false);
		byte[] bytes = newRead.getValue2();
		for (byte b : bytes) {
			System.out.printf("%02x ", b);
		}
		System.out.println();
		for (byte b : bytes) {
			if (b >= 0x41) {
				System.out.printf(".%c.", b);
			} else {
				System.out.print("   ");
			}
		}
		System.out.println();
	}

	private static void printOutput(int size, int pos, String text) {
		System.out.printf("size: %x\n", size);
		System.out.printf("stringPos: %x\n", pos);
		System.out.println("text: " + text);
	}

	private static Triplet<Integer, Integer, String> readNextSizeAndChunk(RandomAccessFile file, int pos) throws IOException {
		int size;
		Triplet<Integer, Integer, String> intChunk;
		intChunk = readIntChunk(file, pos);
		pos = intChunk.getValue0(); size = intChunk.getValue1();
		if (size == 0) {
			return new Triplet<>(pos, 0, null);
		}
		return readTextChunk(file, pos, size);
	}

	private static Triplet<Integer, Integer, String> readIntChunk(RandomAccessFile file, int pos) throws IOException {
		Triplet<Integer, Integer, byte[]> newRead = readFromFile(file, pos, 4, true);
		pos = newRead.getValue0();
		byte[] chunk = newRead.getValue2();
		ByteBuffer buffer = ByteBuffer.wrap(chunk);
		buffer.order(ByteOrder.BIG_ENDIAN);
		int size = buffer.getInt();
		return new Triplet<>(pos, size, null);
	}

	private static Triplet<Integer, Integer, String> readTextChunk(RandomAccessFile file, int pos, int size) throws IOException {
		Triplet<Integer, Integer, byte[]> newRead = readFromFile(file, pos, size, true);
		pos = newRead.getValue0();
		byte[] chunk = newRead.getValue2();
		String text = new String(chunk);
		return new Triplet<>(pos, size, text);
	}

	private static Triplet<Integer, Integer, byte[]> readFromFile(RandomAccessFile file, int pos, int size, boolean advance) throws IOException {
		byte[] res = new byte[size];
		file.seek(pos);
		file.readFully(res);
		if (advance) {
			pos += size;
		}
		return new Triplet<>(pos, size, res);
	}
}