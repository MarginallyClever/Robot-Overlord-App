package com.marginallyclever.util;

import java.util.Map;
import java.util.prefs.Preferences;

/**
 */
public interface Ancestryable {

  /**
   * @return
   */
  Map<String, Preferences> getChildren();

  /**
   * @return
   */
  Map<String, String> getRoot();

}
