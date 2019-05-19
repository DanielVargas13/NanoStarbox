package box.star.chron;

public interface ITimerMethod<T> {
  void onTimer(T[] data);
}
