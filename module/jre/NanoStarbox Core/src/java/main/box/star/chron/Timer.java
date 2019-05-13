package box.star.chron;

import java.util.Date;
import java.util.TimerTask;

public class Timer {

  private java.util.Timer timer = new java.util.Timer();

  public TimerTask createTimeout(int time, ITimerMethod<Object> callback, Object... parameter) {
    TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        callback.onTimer(parameter);
      }
    };
    timer.schedule(timerTask, time);
    return timerTask;
  }

  /**
   * Schedules the specified task for repeated <i>fixed-delay execution</i>,
   * beginning after the specified delay.  Subsequent executions take place
   * at approximately regular intervals separated by the specified period.
   *
   * <p>Each execution is scheduled relative to
   * the actual execution time of the previous execution.  If an execution
   * is delayed for any reason (such as garbage collection or other
   * background activity), subsequent executions will be delayed as well.
   *
   * <p>Fixed-delay execution is appropriate for recurring activities
   * that require "smoothness."  In other words, it is appropriate for
   * activities where it is more important to keep the frequency accurate
   * in the short run than in the long run.  This includes most animation
   * tasks, such as blinking a cursor at regular intervals.  It also includes
   * tasks wherein regular activity is performed in response to human
   * input, such as automatically repeating a character as long as a key
   * is held down.
   *
   * @param time time in milliseconds between successive task executions.
   */
  public TimerTask createPulse(int time, ITimerMethod<Object> callback, Object... parameter) {
    TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        callback.onTimer(parameter);
      }
    };
    timer.schedule(timerTask, time, time);
    return timerTask;
  }

  public TimerTask createAlarm(Date time, ITimerMethod<Object> callback, Object... parameter) {
    TimerTask timerTask = new TimerTask() {
      @Override
      public void run() {
        callback.onTimer(parameter);
      }
    };
    timer.schedule(timerTask, time);
    return timerTask;
  }

  public void cancelTimers() {
    timer.cancel();
  }

}
