package org.sqlman;

import java.util.concurrent.atomic.AtomicLong;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class LongStatistic extends AtomicLong {

  LongStatistic(long initialValue) {
    super(initialValue);
  }

  LongStatistic() {
  }

  void setIfLesser(long update) {
    while (true) {
      long a = get();
      if (update < a) {
        if (compareAndSet(a, update)) {
          break;
        }
      }
    }
  }

  void setIfGreater(long update) {
    while (true) {
      long a = get();
      if (update > a) {
        if (compareAndSet(a, update)) {
          break;
        }
      }
    }
  }
}
