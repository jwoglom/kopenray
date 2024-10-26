package org.jopenray.adapter;

import java.awt.event.KeyEvent;

public class RDPKeymap {
    /**
     * Return the scancode associated to the keyCode (from KeyEvent)
     * https://github.com/PKRoma/xrdp/blob/master/xrdp/rdp-scan-codes.txt
     * */
    public static int getScancode(int keyCode) {
        int scancode;
        switch (keyCode) {

            case KeyEvent.VK_ESCAPE:
                scancode = 1;
                break;
            case KeyEvent.VK_1:
                scancode = 2;
                break;
            case KeyEvent.VK_2:
                scancode = 3;
                break;
            case KeyEvent.VK_3:
                scancode = 4;
                break;
            case KeyEvent.VK_4:
                scancode = 5;
                break;
            case KeyEvent.VK_5:
                scancode = 6;
                break;
            case KeyEvent.VK_6:
                scancode = 7;
                break;
            case KeyEvent.VK_7:
                scancode = 8;
                break;
            case KeyEvent.VK_8:
                scancode = 9;
                break;
            case KeyEvent.VK_9:
                scancode = 10;
                break;
            case KeyEvent.VK_0:
                scancode = 11;
                break;
            case KeyEvent.VK_MINUS:
                scancode = 12;
                break;
            case KeyEvent.VK_EQUALS:
                scancode = 13;
                break;
            case KeyEvent.VK_BACK_SPACE:
                scancode = 14;
                break;
            case KeyEvent.VK_TAB:
                scancode = 15;
                break;
            case KeyEvent.VK_Q:
                scancode = 16;
                break;
            case KeyEvent.VK_W:
                scancode = 17;
                break;
            case KeyEvent.VK_E:
                scancode = 18;
                break;
            case KeyEvent.VK_R:
                scancode = 19;
                break;
            case KeyEvent.VK_T:
                scancode = 20;
                break;
            case KeyEvent.VK_Y:
                scancode = 21;
                break;
            case KeyEvent.VK_U:
                scancode = 22;
                break;
            case KeyEvent.VK_I:
                scancode = 23;
                break;
            case KeyEvent.VK_O:
                scancode = 24;
                break;
            case KeyEvent.VK_P:
                scancode = 25;
                break;
            case KeyEvent.VK_BRACELEFT:
                scancode = 26;
                break;
            case KeyEvent.VK_BRACERIGHT:
                scancode = 27;
                break;
            case KeyEvent.VK_ENTER:
                scancode = 28;
                break;
            case KeyEvent.VK_CONTROL:
                scancode = 29;
                break;
            case KeyEvent.VK_A:
                scancode = 30;
                break;
            case KeyEvent.VK_S:
                scancode = 31;
                break;
            case KeyEvent.VK_D:
                scancode = 32;
                break;
            case KeyEvent.VK_F:
                scancode = 33;
                break;
            case KeyEvent.VK_G:
                scancode = 34;
                break;
            case KeyEvent.VK_H:
                scancode = 35;
                break;
            case KeyEvent.VK_J:
                scancode = 36;
                break;
            case KeyEvent.VK_K:
                scancode = 37;
                break;
            case KeyEvent.VK_L:
                scancode = 38;
                break;
            case KeyEvent.VK_SEMICOLON:
                scancode = 39;
                break;
            case KeyEvent.VK_QUOTE:
                scancode = 40;
                break;
            case KeyEvent.VK_BACK_QUOTE:
                scancode = 41;
                break;
            case KeyEvent.VK_SHIFT:
                scancode = 42;
                break;
            case KeyEvent.VK_BACK_SLASH:
                scancode = 43;
                break;
            case KeyEvent.VK_Z:
                scancode = 44;
                break;
            case KeyEvent.VK_X:
                scancode = 45;
                break;
            case KeyEvent.VK_C:
                scancode = 46;
                break;
            case KeyEvent.VK_V:
                scancode = 47;
                break;
            case KeyEvent.VK_B:
                scancode = 48;
                break;
            case KeyEvent.VK_N:
                scancode = 49;
                break;
            case KeyEvent.VK_M:
                scancode = 50;
                break;
            case KeyEvent.VK_COMMA:
                scancode = 51;
                break;
            case KeyEvent.VK_PERIOD:
                scancode = 52;
                break;
            case KeyEvent.VK_SLASH:
                scancode = 53;
                break;
            // 54 = right shift
            case KeyEvent.VK_MULTIPLY:
                scancode = 55;
                break;
            case KeyEvent.VK_ALT:
                scancode = 56;
                break;
            case KeyEvent.VK_ALT_GRAPH:
                scancode = 128 + 56;
                break;
            case KeyEvent.VK_SPACE:
                scancode = 57;
                break;
            case KeyEvent.VK_CAPS_LOCK:
                scancode = 58;
                break;
            case KeyEvent.VK_F1:
                scancode = 59;
                break;
            case KeyEvent.VK_F2:
                scancode = 60;
                break;
            case KeyEvent.VK_F3:
                scancode = 61;
                break;
            case KeyEvent.VK_F4:
                scancode = 62;
                break;
            case KeyEvent.VK_F5:
                scancode = 63;
                break;
            case KeyEvent.VK_F6:
                scancode = 64;
                break;
            case KeyEvent.VK_F7:
                scancode = 65;
                break;
            case KeyEvent.VK_F8:
                scancode = 66;
                break;
            case KeyEvent.VK_F9:
                scancode = 67;
                break;
            case KeyEvent.VK_F10:
                scancode = 68;
                break;
            case KeyEvent.VK_NUM_LOCK:
                scancode = 69;
                break;
            case KeyEvent.VK_SCROLL_LOCK:
                scancode = 70;
                break;
            case KeyEvent.VK_NUMPAD7:
                scancode = 71;
                break;
            case KeyEvent.VK_NUMPAD8:
                scancode = 72;
                break;
            case KeyEvent.VK_NUMPAD9:
                scancode = 73;
                break;
            case KeyEvent.VK_SUBTRACT:
                scancode = 74;
                break;
            case KeyEvent.VK_NUMPAD4:
                scancode = 75;
                break;
            case KeyEvent.VK_NUMPAD5:
                scancode = 76;
                break;
            case KeyEvent.VK_NUMPAD6:
                scancode = 77;
                break;
            case KeyEvent.VK_ADD:
                scancode = 78;
                break;
            case KeyEvent.VK_NUMPAD1:
                scancode = 79;
                break;
            case KeyEvent.VK_NUMPAD2:
                scancode = 80;
                break;
            case KeyEvent.VK_NUMPAD3:
                scancode = 81;
                break;
            case KeyEvent.VK_NUMPAD0:
                scancode = 82;
                break;
            case KeyEvent.VK_DECIMAL:
                scancode = 83;
                break;
            // 84 = SysRq
            // 85 = ?
            // 86 = VK_OEM_102
            case KeyEvent.VK_F11:
                scancode = 87;
                break;
            case KeyEvent.VK_F12:
                scancode = 88;
                break;
            // ..gap..
            case KeyEvent.VK_HELP:
                scancode = 99;
                break;
            case KeyEvent.VK_F13:
                scancode = 100;
                break;
            case KeyEvent.VK_F14:
                scancode = 101;
                break;
            case KeyEvent.VK_F15:
                scancode = 102;
                break;
            case KeyEvent.VK_F16:
                scancode = 103;
                break;
            case KeyEvent.VK_F17:
                scancode = 104;
                break;
            case KeyEvent.VK_F18:
                scancode = 105;
                break;
            case KeyEvent.VK_F19:
                scancode = 106;
                break;
            case KeyEvent.VK_F20:
                scancode = 107;
                break;
            case KeyEvent.VK_F21:
                scancode = 108;
                break;
            case KeyEvent.VK_F22:
                scancode = 109;
                break;
            case KeyEvent.VK_F23:
                scancode = 110;
                break;
            case KeyEvent.VK_F24:
                scancode = 111;
                break;
            // EXTENDED
            case KeyEvent.VK_DIVIDE:
                scancode = 128 + 53;
                break;
            case KeyEvent.VK_PRINTSCREEN:
                scancode = 128 + 55;
                break;
            case KeyEvent.VK_PAUSE:
                scancode = 128 + 70;
                break;
            case KeyEvent.VK_HOME:
                scancode = 128 + 71;
                break;
            case KeyEvent.VK_UP:
                scancode = 128 + 72;
                break;
            case KeyEvent.VK_PAGE_UP:
                scancode = 128 + 73;
                break;
            case KeyEvent.VK_LEFT:
                scancode = 128 + 75;
                break;
            case KeyEvent.VK_RIGHT:
                scancode = 128 + 77;
                break;
            case KeyEvent.VK_END:
                scancode = 128 + 79;
                break;
            case KeyEvent.VK_DOWN:
                scancode = 128 + 80;
                break;
            case KeyEvent.VK_PAGE_DOWN:
                scancode = 128 + 81;
                break;
            case KeyEvent.VK_INSERT:
                scancode = 128 + 82;
                break;
            case KeyEvent.VK_DELETE:
                scancode = 128 + 83;
                break;

            default:
                scancode = 0;
                break;
        }
        return scancode;
    }
}
