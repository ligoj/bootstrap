package org.ligoj.bootstrap.core.crypto;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.experimental.UtilityClass;

/**
 * Minimal CBOR (RFC 8949) codec for the WebAuthn structures: unsigned/negative integers, byte strings, text strings,
 * arrays, maps, booleans, null. Tags are skipped, floats and indefinite lengths are rejected. Integer map keys are
 * decoded as {@link Long}, byte strings as <code>byte[]</code>.
 */
@UtilityClass
public class Cbor {

	/**
	 * Decode a single CBOR item.
	 *
	 * @param data The CBOR bytes.
	 * @return The decoded item.
	 * @throws IllegalArgumentException When the data is not supported or truncated.
	 */
	public static Object decode(final byte[] data) {
		return new Reader(data).read();
	}

	/**
	 * Decode the first CBOR item and return the number of bytes consumed.
	 *
	 * @param data   The CBOR bytes.
	 * @param offset The start offset.
	 * @return The decoded item and the position after it.
	 */
	public static Decoded decodeAt(final byte[] data, final int offset) {
		final var reader = new Reader(data);
		reader.position = offset;
		final var value = reader.read();
		return new Decoded(value, reader.position);
	}

	/**
	 * A decoded item with the position following it.
	 */
	public record Decoded(Object value, int end) {
	}

	/**
	 * Encode an item: {@link Map}, {@link List}, {@link String}, <code>byte[]</code>, {@link Number} (integers),
	 * {@link Boolean} or <code>null</code>.
	 *
	 * @param value The item.
	 * @return The CBOR bytes.
	 */
	public static byte[] encode(final Object value) {
		final var out = new ByteArrayOutputStream();
		write(out, value);
		return out.toByteArray();
	}

	private static void write(final ByteArrayOutputStream out, final Object value) {
		switch (value) {
		case null -> out.write(0xF6);
		case Boolean b -> out.write(b ? 0xF5 : 0xF4);
		case byte[] bytes -> {
			writeHead(out, 2, bytes.length);
			out.writeBytes(bytes);
		}
		case String text -> {
			final var bytes = text.getBytes(StandardCharsets.UTF_8);
			writeHead(out, 3, bytes.length);
			out.writeBytes(bytes);
		}
		case Number number -> {
			final var l = number.longValue();
			if (l >= 0) {
				writeHead(out, 0, l);
			} else {
				writeHead(out, 1, -1 - l);
			}
		}
		case List<?> list -> {
			writeHead(out, 4, list.size());
			list.forEach(item -> write(out, item));
		}
		case Map<?, ?> map -> {
			writeHead(out, 5, map.size());
			map.forEach((k, v) -> {
				write(out, k);
				write(out, v);
			});
		}
		default -> throw new IllegalArgumentException("Unsupported CBOR value " + value.getClass());
		}
	}

	private static void writeHead(final ByteArrayOutputStream out, final int major, final long length) {
		final var type = major << 5;
		if (length < 24) {
			out.write(type | (int) length);
		} else if (length < 0x100) {
			out.write(type | 24);
			out.write((int) length);
		} else if (length < 0x10000) {
			out.write(type | 25);
			out.write((int) (length >> 8));
			out.write((int) length);
		} else if (length < 0x100000000L) {
			out.write(type | 26);
			for (var i = 3; i >= 0; i--) {
				out.write((int) (length >> (8 * i)));
			}
		} else {
			out.write(type | 27);
			for (var i = 7; i >= 0; i--) {
				out.write((int) (length >> (8 * i)));
			}
		}
	}

	private static final class Reader {
		private final byte[] data;
		private int position;

		private Reader(final byte[] data) {
			this.data = data;
		}

		private Object read() {
			final var initial = next();
			final var major = initial >> 5;
			final var info = initial & 0x1F;
			return switch (major) {
			case 0 -> length(info);
			case 1 -> -1 - length(info);
			case 2 -> bytes((int) length(info));
			case 3 -> new String(bytes((int) length(info)), StandardCharsets.UTF_8);
			case 4 -> {
				final var size = (int) length(info);
				final var list = new ArrayList<>(size);
				for (var i = 0; i < size; i++) {
					list.add(read());
				}
				yield list;
			}
			case 5 -> {
				final var size = (int) length(info);
				final var map = new LinkedHashMap<>(size * 2);
				for (var i = 0; i < size; i++) {
					map.put(read(), read());
				}
				yield map;
			}
			case 6 -> {
				// Tag: skipped
				length(info);
				yield read();
			}
			default -> switch (info) {
			case 20 -> Boolean.FALSE;
			case 21 -> Boolean.TRUE;
			case 22 -> null;
			default -> throw new IllegalArgumentException("Unsupported CBOR simple value " + info);
			};
			};
		}

		private long length(final int info) {
			if (info < 24) {
				return info;
			}
			final var size = switch (info) {
			case 24 -> 1;
			case 25 -> 2;
			case 26 -> 4;
			case 27 -> 8;
			default -> throw new IllegalArgumentException("Unsupported CBOR length " + info);
			};
			var value = 0L;
			for (var i = 0; i < size; i++) {
				value = (value << 8) | next();
			}
			if (value < 0 || value > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Unsupported CBOR length " + value);
			}
			return value;
		}

		private int next() {
			if (position >= data.length) {
				throw new IllegalArgumentException("Truncated CBOR data");
			}
			return data[position++] & 0xFF;
		}

		private byte[] bytes(final int size) {
			if (position + size > data.length) {
				throw new IllegalArgumentException("Truncated CBOR data");
			}
			final var out = new byte[size];
			System.arraycopy(data, position, out, 0, size);
			position += size;
			return out;
		}
	}
}
