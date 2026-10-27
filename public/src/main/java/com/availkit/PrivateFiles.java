package com.availkit;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/** Explicit Linux x64 private storage. Other platforms use an application vault and exported intents. */
public final class PrivateFiles {
  private static final int CLOEXEC = 0x80000;
  private static final int NOFOLLOW = 0x20000;
  private static final int NONBLOCK = 0x800;
  private static final int DIRECTORY = 0x10000;
  private static final int AT_EMPTY_PATH = 0x1000;
  private static final int STATX_BASIC_STATS = 0x7ff;

  private PrivateFiles() {}

  private interface LibC extends Library {
    int open(String path, int flags);
    int openat(int dir, String path, int flags, int mode);
    int statx(int fd, String path, int flags, int mask, Pointer data);
    int geteuid();
    NativeLong read(int fd, Pointer buffer, NativeLong count);
    NativeLong write(int fd, Pointer buffer, NativeLong count);
    int fsync(int fd);
    int close(int fd);
  }

  private static final class Linux {
    static final LibC C = Native.load("c", LibC.class);
  }

  private static LibC libc() {
    String arch = System.getProperty("os.arch", "");
    if (!System.getProperty("os.name", "").equals("Linux")
        || !(arch.equals("amd64") || arch.equals("x86_64"))) {
      throw AvailKitException.input("private_storage_unavailable");
    }
    return Linux.C;
  }

  // statx is a fixed Linux UAPI layout; the mask proves owner/type/mode/size were supplied.
  private static long check(LibC c, int fd, boolean directory) {
    if (fd < 0) throw AvailKitException.input("invalid_private_file");
    try (Memory data = new Memory(256)) {
      if (c.statx(fd, "", AT_EMPTY_PATH, STATX_BASIC_STATS, data) != 0
          || (data.getInt(0) & 0x20b) != 0x20b
          || data.getInt(20) != c.geteuid()) {
        throw AvailKitException.input("invalid_private_file");
      }
      int mode = data.getShort(28) & 0xffff;
      if ((mode & 0xf000) != (directory ? 0x4000 : 0x8000)
          || (mode & 0xfff) != (directory ? 0x1c0 : 0x180)) {
        throw AvailKitException.input("invalid_private_file");
      }
      long size = data.getLong(40);
      if (size < 0) throw AvailKitException.input("invalid_private_file");
      return size;
    }
  }

  private static int parent(LibC c, Path path) {
    Path directory = path.toAbsolutePath().normalize().getParent();
    if (directory == null) throw AvailKitException.input("invalid_private_file");
    int fd = c.open(directory.toString(), CLOEXEC | NOFOLLOW | NONBLOCK | DIRECTORY);
    try {
      check(c, fd, true);
      return fd;
    } catch (RuntimeException | LinkageError failure) {
      if (fd >= 0) c.close(fd);
      throw failure;
    }
  }

  public static byte[] read(Path path, int limit) {
    int directory = -1;
    int file = -1;
    LibC c = null;
    try {
      if (limit < 1 || limit > 131072) throw AvailKitException.input("invalid_private_file");
      c = libc();
      directory = parent(c, path);
      // NONBLOCK makes a substituted FIFO return immediately. Inspect this exact fd before reading.
      file = c.openat(directory, path.getFileName().toString(), CLOEXEC | NOFOLLOW | NONBLOCK, 0);
      long size = check(c, file, false);
      if (size < 1 || size > limit) throw AvailKitException.input("invalid_private_file");
      try (Memory buffer = new Memory(size + 1)) {
        try {
          long used = 0;
          while (true) {
            long count = c.read(file, buffer.share(used), new NativeLong(size + 1 - used)).longValue();
            if (count < 0) throw AvailKitException.input("invalid_private_file");
            if (count == 0) break;
            used += count;
            if (used > size) throw AvailKitException.input("invalid_private_file");
          }
          if (used != size || check(c, file, false) != size) {
            throw AvailKitException.input("invalid_private_file");
          }
          return buffer.getByteArray(0, (int) size);
        } finally {
          buffer.clear();
        }
      }
    } catch (RuntimeException | LinkageError failure) {
      throw AvailKitException.input("invalid_private_file");
    } finally {
      if (c != null && file >= 0) c.close(file);
      if (c != null && directory >= 0) c.close(directory);
    }
  }

  public static void writeNew(Path path, byte[] bytes) {
    int directory = -1;
    int file = -1;
    LibC c = null;
    try {
      if (bytes == null || bytes.length < 1 || bytes.length > 131072) {
        throw AvailKitException.input("intent_persistence_failed");
      }
      c = libc();
      directory = parent(c, path);
      file = c.openat(directory, path.getFileName().toString(), 1 | 0x40 | 0x80 | CLOEXEC | NOFOLLOW | NONBLOCK, 0x180);
      if (check(c, file, false) != 0) throw AvailKitException.input("intent_persistence_failed");
      try (Memory buffer = new Memory(bytes.length)) {
        try {
          buffer.write(0, bytes, 0, bytes.length);
          long written = 0;
          while (written < bytes.length) {
            long count = c.write(file, buffer.share(written), new NativeLong(bytes.length - written)).longValue();
            if (count <= 0) throw AvailKitException.input("intent_persistence_failed");
            written += count;
          }
        } finally {
          buffer.clear();
        }
      }
      if (check(c, file, false) != bytes.length || c.fsync(file) != 0 || c.fsync(directory) != 0) {
        throw AvailKitException.input("intent_persistence_failed");
      }
    } catch (RuntimeException | LinkageError failure) {
      // Never remove a partial file: retain it as the send-blocking latch after persistence failure.
      throw AvailKitException.input("intent_persistence_failed");
    } finally {
      if (c != null && file >= 0) c.close(file);
      if (c != null && directory >= 0) c.close(directory);
    }
  }

  public static String credential(Path path) {
    try {
      String value = StandardCharsets.UTF_8.newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(read(path, 4096))).toString();
      if (value.endsWith("\n")) value = value.substring(0, value.length() - 1);
      if (value.endsWith("\r")) value = value.substring(0, value.length() - 1);
      return Protocol.token(value);
    } catch (Exception failure) {
      throw AvailKitException.input("invalid_private_file");
    }
  }
}
