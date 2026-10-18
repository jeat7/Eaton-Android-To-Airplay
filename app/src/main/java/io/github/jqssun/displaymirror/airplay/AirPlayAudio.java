package io.github.jqssun.displaymirror.airplay;

import android.content.Context;
import android.media.AudioManager;
import android.media.projection.MediaProjection;
import android.os.SystemClock;
import android.os.Process;
import android.util.Log;
import io.github.jqssun.displaymirror.Pref;
import io.github.jqssun.displaymirror.State;
import io.github.jqssun.displaymirror.job.CaptureAudio;
import java.util.Arrays;

// RAOP mirroring audio fixed at 44.1kHz stereo S16LE; Go side frames it into ALAC
public class AirPlayAudio {
  private static final String TAG = "AirPlayAudio";
  private static final int SAMPLE_RATE = 44100;
  // exactly one ALAC frame (352 samples) per read
  // batching bursts the send side and Apple drops frames late against the sync anchor
  private static final int READ_SIZE = 352 * 2 * 2;

  private volatile CaptureAudio.PcmStream stream;
  private volatile boolean running;
  private Thread readThread;
  private AudioManager audioManager;
  private long lastVolumeRead = -1;
  private float phoneVolumeGain = 1f;

  // open can wait for Shizuku bind, so it runs on the read thread
  public void start(MediaProjection projection) {
    if (running) {
      return;
    }
    running = true;
    readThread = new Thread(() -> _run(projection), "AirPlayAudio");
    readThread.start();
  }

  public void stop() {
    running = false;
    _release();
    if (readThread != null) {
      readThread.interrupt();
      readThread = null;
    }
  }

  // idempotent: stop() and the read thread both release
  private synchronized void _release() {
    if (stream != null) {
      stream.close();
      stream = null;
    }
  }

  private void _run(MediaProjection projection) {
    // drain has to keep up with capture or samples are lost
    Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
    CaptureAudio.PcmStream s = CaptureAudio.openPcm16(projection, SAMPLE_RATE);
    if (s == null) {
      running = false;
      return;
    }
    synchronized (this) {
      if (!running) {
        // stopped while open was waiting
        s.close();
        return;
      }
      stream = s;
    }
    State.log("AirPlay audio: streaming 44.1kHz stereo");
    try {
      _readLoop(s);
    } finally {
      running = false;
      _release();
    }
  }

  private void _readLoop(CaptureAudio.PcmStream s) {
    Context context = State.getContext();
    if (context != null) {
      audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
    byte[] buf = new byte[READ_SIZE];
    while (running) {
      try {
        int n = s.read(buf);
        if (n > 0) {
          if (Pref.getAirPlayStreamMuted()) {
            java.util.Arrays.fill(buf, 0, n, (byte) 0);
          }
          if (Pref.getAirPlayFollowPhoneVolume() && audioManager != null) {
            long now = SystemClock.uptimeMillis();
            if (lastVolumeRead < 0 || now - lastVolumeRead >= 200) {
              int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
              int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
              phoneVolumeGain = max > 0
                  ? Math.max(0f, Math.min(1f, (float) current / (float) max))
                  : 1f;
              lastVolumeRead = now;
            }
            applyPcm16Volume(buf, n, phoneVolumeGain);
          }
          AirPlayService.onAudioFrame(n == buf.length ? buf : Arrays.copyOf(buf, n));
        } else {
          // 0 means recorder stopped; < 0 means error
          if (running) State.log("AirPlay audio stopped: read failed (" + n + ")");
          return;
        }
      } catch (Exception e) {
        if (running) {
          Log.e(TAG, "read error", e);
          State.log("AirPlay audio stopped: " + e);
        }
        return;
      }
    }
  }
  // Interleaved stereo PCM, signed 16-bit little-endian: attenuate only.
  // This affects the sent audio samples, not the receiver's volume.
  static void applyPcm16Volume(byte[] data, int count, float gain) {
    if (gain >= 1f) return;
    if (gain < 0f) gain = 0f;
    for (int i = 0; i + 1 < count; i += 2) {
      int packed = (data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8);
      int signedSample = (short) packed;
      int scaled = Math.round(signedSample * gain);
      data[i] = (byte) (scaled & 0xFF);
      data[i + 1] = (byte) ((scaled >>> 8) & 0xFF);
    }
  }

}
