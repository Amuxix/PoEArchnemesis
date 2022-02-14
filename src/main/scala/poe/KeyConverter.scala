package poe

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*

object KeyConverter:
  private val keyCodes: List[Int] = List(
    VC_ESCAPE,
    VC_F1,
    VC_F2,
    VC_F3,
    VC_F4,
    VC_F5,
    VC_F6,
    VC_F7,
    VC_F8,
    VC_F9,
    VC_F10,
    VC_F11,
    VC_F12,
    VC_F13,
    VC_F14,
    VC_F15,
    VC_F16,
    VC_F17,
    VC_F18,
    VC_F19,
    VC_F20,
    VC_F21,
    VC_F22,
    VC_F23,
    VC_F24,
    VC_BACKQUOTE,
    VC_1,
    VC_2,
    VC_3,
    VC_4,
    VC_5,
    VC_6,
    VC_7,
    VC_8,
    VC_9,
    VC_0,
    VC_MINUS,
    VC_EQUALS,
    VC_BACKSPACE,
    VC_TAB,
    VC_CAPS_LOCK,
    VC_A,
    VC_B,
    VC_C,
    VC_D,
    VC_E,
    VC_F,
    VC_G,
    VC_H,
    VC_I,
    VC_J,
    VC_K,
    VC_L,
    VC_M,
    VC_N,
    VC_O,
    VC_P,
    VC_Q,
    VC_R,
    VC_S,
    VC_T,
    VC_U,
    VC_V,
    VC_W,
    VC_X,
    VC_Y,
    VC_Z,
    VC_OPEN_BRACKET,
    VC_CLOSE_BRACKET,
    VC_BACK_SLASH,
    VC_SEMICOLON,
    VC_QUOTE,
    VC_ENTER,
    VC_COMMA,
    VC_PERIOD,
    VC_SLASH,
    VC_SPACE,
    VC_PRINTSCREEN,
    VC_SCROLL_LOCK,
    VC_PAUSE,
    VC_INSERT,
    VC_DELETE,
    VC_HOME,
    VC_END,
    VC_PAGE_UP,
    VC_PAGE_DOWN,
    VC_UP,
    VC_LEFT,
    VC_CLEAR,
    VC_RIGHT,
    VC_DOWN,
    VC_NUM_LOCK,
    VC_SEPARATOR,
    VC_SHIFT,
    VC_CONTROL,
    VC_ALT,
    VC_META,
    VC_CONTEXT_MENU,
    VC_POWER,
    VC_SLEEP,
    VC_WAKE,
    VC_MEDIA_PLAY,
    VC_MEDIA_STOP,
    VC_MEDIA_PREVIOUS,
    VC_MEDIA_NEXT,
    VC_MEDIA_SELECT,
    VC_MEDIA_EJECT,
    VC_VOLUME_MUTE,
    VC_VOLUME_UP,
    VC_VOLUME_DOWN,
    VC_APP_MAIL,
    VC_APP_CALCULATOR,
    VC_APP_MUSIC,
    VC_APP_PICTURES,
    VC_BROWSER_SEARCH,
    VC_BROWSER_HOME,
    VC_BROWSER_BACK,
    VC_BROWSER_FORWARD,
    VC_BROWSER_STOP,
    VC_BROWSER_REFRESH,
    VC_BROWSER_FAVORITES,
    VC_KATAKANA,
    VC_UNDERSCORE,
    VC_FURIGANA,
    VC_KANJI,
    VC_HIRAGANA,
    VC_YEN,
    VC_SUN_HELP,
    VC_SUN_STOP,
    VC_SUN_PROPS,
    VC_SUN_FRONT,
    VC_SUN_OPEN,
    VC_SUN_FIND,
    VC_SUN_AGAIN,
    VC_SUN_UNDO,
    VC_SUN_COPY,
    VC_SUN_INSERT,
    VC_SUN_CUT,
    VC_UNDEFINED,
    CHAR_UNDEFINED,
  )
  lazy val fromString: Map[String, Int] = keyCodes.map(keyCode => NativeKeyEvent.getKeyText(keyCode).toLowerCase -> keyCode).toMap
  def findKey(text: String): Int =
    val lowered = text.toLowerCase
    fromString.get(lowered)
      .orElse(fromString collectFirst {
        case (text, keyCode) if text.matches(s"^$lowered.+") => keyCode
      })
      .get