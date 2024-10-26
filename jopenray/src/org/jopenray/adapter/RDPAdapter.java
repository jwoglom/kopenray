/*
 *  Copyright 2010 jOpenRay, ILM Informatique  
 *  Copyright 2019 Cameron Kaiser
 *  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.jopenray.adapter;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.elusiva.rdp.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jopenray.operation.SetMouseCursorOperation;

import com.elusiva.rdp.rdp5.Rdp5;
import com.elusiva.rdp.rdp5.VChannels;
import com.elusiva.rdp.rdp5.cliprdr.ClipChannel;
import org.jopenray.server.event.Event;
import org.jopenray.server.event.EventManager;
import org.jopenray.server.session.Session;
import org.jopenray.server.thinclient.DisplayMessage;
import org.jopenray.server.thinclient.InputListener;
import org.jopenray.server.thinclient.ThinClient;
import org.jopenray.util.Util;

public class RDPAdapter extends RdesktopCanvas implements InputListener {

	protected static final int MOUSE_FLAG_MOVE = 0x0800;
	protected static final int MOUSE_FLAG_BUTTON1 = 0x1000;
	protected static final int MOUSE_FLAG_BUTTON2 = 0x2000;
	protected static final int MOUSE_FLAG_BUTTON3 = 0x4000;
	protected static final int MOUSE_FLAG_BUTTON4 = 0x0280; // wheel up -
	protected static final int MOUSE_FLAG_BUTTON5 = 0x0380; // wheel down -
	protected static final int MOUSE_FLAG_DOWN = 0x8000;
	protected static final int KBD_FLAG_QUIET = 0x200;
	protected static final int KBD_FLAG_DOWN = 0x4000;
	protected static final int KBD_FLAG_UP = 0x8000;
	protected static final int RDP_INPUT_MOUSE = 0x8001;

	protected static final int RDP_KEYPRESS = 0;
	protected static final int RDP_KEYRELEASE = KBD_FLAG_DOWN | KBD_FLAG_UP;

	static Logger logger = Logger.getLogger("org.jopenray");

	static final String keyMapPath = "Keymaps/";
	private static final boolean DEBUG_REPAINT = false;

	static String mapFile = "en-us";

	private boolean readytosend;

	private ThinClient client;
	private Session session;

	Rdp5 rdp = null;

	Input input;

	private boolean stopped;
	private KeyCode_FileBased_Localised keyMap;

	public RDPAdapter(ThinClient displayClient) {
		super(displayClient.getScreenWidth(), displayClient.getScreenHeight(), new Options());
	}

	public void start(ThinClient displayClient, Session session) {
		this.stopped = false;
		this.client = displayClient;
		this.session = session;

		// Logger configuration
		BasicConfigurator.configure();
		logger.setLevel(Level.INFO);

		String server = session.getServer();
		int logonflags = Rdp.RDP_LOGON_NORMAL;

		VChannels channels = new VChannels();
		ClipChannel clipChannel = new ClipChannel(option);
		// Initialise all RDP5 channels
		if (option.shouldUseRdp5()) {
			// TODO: implement all relevant channels
			if (option.isClipboardMappingEnabled())
				try {
					channels.register(clipChannel, option);
				} catch (RdesktopException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		// Now do the startup...

		String os = System.getProperty("os.name");
		String osvers = System.getProperty("os.version");

		if (os.equals("Windows 2000") || os.equals("Windows XP"))
			option.enableBuiltInLicence();

		logger.info("Operating System is " + os + " version " + osvers);

		if (os.startsWith("Linux"))
			Constants.OS = Constants.LINUX;
		else if (os.startsWith("Windows"))
			Constants.OS = Constants.WINDOWS;
		else if (os.startsWith("Mac"))
			Constants.OS = Constants.MAC;

		if (Constants.OS == Constants.MAC)
			option.disableCapsSendsUpAndDown();
		option.setWidth(displayClient.getScreenWidth());
		option.setHeight(displayClient.getScreenHeight());

		option.setUsername(session.getLogin());

		final String password = session.getPassword();
		if (password != null && password.trim().length() > 0) {
			logonflags |= Rdp.RDP_LOGON_AUTO;
			option.setPassword(password);
		}

		option.disableFullScreen();

		Common.rdp = rdp;

		// Configure a keyboard layout
		keyMap = null;
		try {
			// logger.info("looking for: " + "/" + keyMapPath + mapFile);
			InputStream istr = KeyCode_FileBased_Localised.class.getResourceAsStream("/"
					+ keyMapPath + mapFile);
			// logger.info("istr = " + istr);
			if (istr == null) {
				logger.info("Loading keymap from filename");
				keyMap = new KeyCode_FileBased_Localised(keyMapPath + mapFile, option);
			} else {
				logger.info("Loading keymap from InputStream");
				keyMap = new KeyCode_FileBased_Localised(istr, option);
			}
			if (istr != null)
				istr.close();
			option.setKeylayout(keyMap.getMapCode());
		} catch (Exception kmEx) {
			EventManager.getInstance().add(
					new Event("Keymap not loaded", kmEx.getMessage(),
							Event.TYPE_ERROR));
			kmEx.printStackTrace();

		}

		boolean[] deactivated = new boolean[1];
		int[] ext_disc_reason = new int[1];

		boolean keep_running = true;
		logger.debug("keep_running = " + keep_running);
		{
			logger.debug("Initialising RDP layer...");
			rdp = new Rdp5(channels, option);
			Common.rdp = rdp;
			logger.debug("Registering drawing surface...");
			rdp.registerDrawingSurface(this);
			logger.debug("Registering comms layer...");
			// // window.registerCommLayer(rdp);
			input = new Input_Localised(this, rdp, keyMap, option);

			readytosend = false;
			logger
					.info("Connecting to " + server + ":" + option.getPort()
							+ " ...");

			if (server.equalsIgnoreCase("localhost"))
				server = "127.0.0.1";

			if (rdp != null) {
				// Attempt to connect to server on port Options.port
				try {
					rdp.connect(InetAddress.getByName(server), logonflags);

					if (keep_running) {

						/*
						 * By setting encryption to False here, we have an
						 * encrypted login packet but unencrypted transfer of
						 * other packets
						 */
						if (option.isPacketEncryptionNotEnabled())
							option.disableEncryption();

						logger.info("Connection successful");
						// now show window after licence negotiation
						EventManager.getInstance().add(
								new Event("RDP Connection successfull to "
										+ server, displayClient.getName()
										+ "connected to " + server,
										Event.TYPE_INFO));

						rdp.mainLoop(deactivated, ext_disc_reason);

						if (deactivated[0]) {
							/* clean disconnect */
							rdp.disconnect();
						} else {
							if (ext_disc_reason[0] == DisconnnectCodeMapper.exDiscReasonAPIInitiatedDisconnect
									|| ext_disc_reason[0] == DisconnnectCodeMapper.exDiscReasonAPIInitiatedLogoff) {
								rdp.disconnect();
							}

							if (ext_disc_reason[0] >= 2) {
								String reason = getTextDisconnectReason(ext_disc_reason[0]);
								EventManager.getInstance().add(
										new Event("Connection terminated",
												reason, Event.TYPE_WARNING));
								rdp.disconnect();

							}

						}

						keep_running = false; // exited main loop
						if (!readytosend) {
							// maybe the licence server was having a comms
							// problem, retry?
							String msg1 = "The RDP server ("
									+ server
									+ ") disconnected before licence negotiation completed.";
							String msg2 = "Possible cause: terminal server could not issue a licence.";

							logger.warn(msg1);
							logger.warn(msg2);
							EventManager.getInstance().add(
									new Event("RDP server disconnected", msg1,
											Event.TYPE_WARNING));

						}
					}

				} catch (ConnectionException e) {

					EventManager.getInstance().add(
							new Event("RDP connection failed to " + server, e
									.getMessage(), Event.TYPE_ERROR));
					e.printStackTrace();
					rdp.disconnect();
				} catch (UnknownHostException e) {
					// //error(e, rdp, window, true);
				} catch (SocketException s) {
					if (rdp.isConnected()) {
						logger.fatal(s.getClass().getName() + " "
								+ s.getMessage());
						rdp.disconnect();
					}
				} catch (RdesktopException e) {
					String msg1 = e.getClass().getName();
					String msg2 = e.getMessage();
					logger.fatal(msg1 + ": " + msg2);

					e.printStackTrace();

					if (!readytosend) {
						// maybe the licence server was having a communication
						// issue

						EventManager
								.getInstance()
								.add(
										new Event(
												"RDP connection failed",
												"RDP server reset connection before licence negotiation",
												Event.TYPE_WARNING));

						rdp.disconnect();

					} else {

						EventManager.getInstance().add(
								new Event("RDP connection failed to " + server,
										e.getMessage(), Event.TYPE_WARNING));

						rdp.disconnect();
					}
				} catch (Exception e) {
					logger.warn(e.getClass().getName() + " " + e.getMessage());
					e.printStackTrace();
					rdp.disconnect();
				}
			} else {
				logger
						.fatal("The communications layer could not be initiated!");
			}
		}
		rdp.disconnect();

	}

	public void stop() {
		this.stopped = true;
	}

	// @Override
	public void drawLineVerticalHorizontal(int x1, int y1, int x2, int y2,
			int color, int opcode) {
		// TODO Auto-generated method stub
		super.drawLineVerticalHorizontal(x1, y1, x2, y2, color, opcode);
	}

//	@Override
//	public void backFill(int x, int y, int cx, int cy, int color) {
//		// TODO Auto-generated method stub
//
//		DisplayMessage m = new DisplayMessage(client.getWriter());
//		m.addOperation(new FillOperation(x, y, cx, cy, new Color(color)));
//		this.client.getWriter().addMessage(m);
//		// super.fillRectangle(x, y, cx, cy, color);
//	}

	@Override
	public void update(Graphics g) {
		System.out.println("RDPAdapter.update()");
		// super.update(g);

	}

	@Override
	public void paint(Graphics g) {
		System.out.println("RDPAdapter.paint()");
		super.paint(g);
	}

	int counter = 0;

	private long last_mousemove;

	private Map<Integer, SetMouseCursorOperation> cursorMap = new HashMap<Integer, SetMouseCursorOperation>();

	@Override
	public void repaint(int x, int y, int width, int height) {
		// System.out.println("RDPAdapter.repaint()" + " " + x + "," + y + " "
		// + width + "x" + height);

		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		if (x > this.width || y > this.height) {
			return;
		}
		if (x + width > this.width) {
			width = this.width - x;
		}
		if (y + height > this.height) {
			height = this.height - y;
		}
		if (height <= 0 || width <= 0) {
			return;
		}
		counter++;
		try {
			BufferedImage image = getBackstore().getSubimage(x, y, width, height);

			this.client.getWriter().sendImage(image, x, y);
			if (DEBUG_REPAINT) {
				try {

					ImageIO.write(image, "png", new File("out/" + counter
							+ ".png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			System.out.println("RDPAdapter.repaint():" + x + "," + y + " "
					+ width + "x" + height);
			e.printStackTrace();
		}
		// this.backstore.getRGB()

		// super.repaint(x, y, width, height);

	}

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		System.out.println("RDPAdapter.addMouseListener()" + l);

	}

	@Override
	public void mouseMoved(int mouseX, int mouseY) {
		// processMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_MOVED, System
		// .currentTimeMillis(), 0, mouseX, mouseY, 0, false));
		// limits to 25 events/s
		long mTime = System.currentTimeMillis();
		if ((mTime - last_mousemove) < (1000 / 25))
			return;
		last_mousemove = mTime;

		if (rdp != null) {
			rdp.sendInput((int) System.currentTimeMillis(), RDP_INPUT_MOUSE,
					MOUSE_FLAG_MOVE, mouseX, mouseY);
		}
	}

	@Override
	public void mousePressed(int button, int mouseX, int mouseY) {
		// System.err.println("RDPAdapter.mousePressed()");
		if (rdp != null) {
			if (button == MouseEvent.BUTTON1) {
				logger.debug("Mouse Button 1 Pressed.");
				rdp.sendInput((int) System.currentTimeMillis(),
						RDP_INPUT_MOUSE, MOUSE_FLAG_BUTTON1 | MOUSE_FLAG_DOWN,
						mouseX, mouseY);
			} else if (button == MouseEvent.BUTTON2) {
				logger.debug("Mouse Button 2 Pressed.");
				rdp.sendInput((int) System.currentTimeMillis(),
						RDP_INPUT_MOUSE, MOUSE_FLAG_BUTTON3 | MOUSE_FLAG_DOWN,
						mouseX, mouseY);
			} else if (button == MouseEvent.BUTTON3) {
				logger.debug("Middle Mouse Button Pressed.");
				rdp.sendInput((int) System.currentTimeMillis(),
						RDP_INPUT_MOUSE, MOUSE_FLAG_BUTTON2 | MOUSE_FLAG_DOWN,
						mouseX, mouseY);
			}
		}
	}

	@Override
	public void mouseReleased(int button, int mouseX, int mouseY) {

		if (rdp != null) {
			int time = (int) System.currentTimeMillis();
			if (button == MouseEvent.BUTTON1) {
				logger.debug("Mouse Button 1 released.");
				rdp.sendInput(time, RDP_INPUT_MOUSE, MOUSE_FLAG_BUTTON1,
						mouseX, mouseY);
			} else if (button == MouseEvent.BUTTON2) {
				logger.debug("Mouse Button 2 released.");
				rdp.sendInput(time, RDP_INPUT_MOUSE, MOUSE_FLAG_BUTTON3,
						mouseX, mouseY);
			} else if (button == MouseEvent.BUTTON3) {
				logger.debug("Mouse Button 31 released.");
				rdp.sendInput(time, RDP_INPUT_MOUSE, MOUSE_FLAG_BUTTON2,
						mouseX, mouseY);
			}
		}
	}

	@Override
	public void keyPressed(int key, boolean shift, boolean ctrl, boolean alt,
			boolean meta, boolean altGr) {
		// Deprecated
/*
		KeyEvent e = new KeyEvent(this, KeyEvent.KEY_PRESSED, System
				.currentTimeMillis(), 0, key);
*/
		KeyEvent e = new KeyEvent(this, KeyEvent.KEY_PRESSED, System
				.currentTimeMillis(), 0, key,
				KeyEvent.CHAR_UNDEFINED);

		input.lastKeyEvent = e;
		input.modifiersValid = true;
		long time = input.getTime();

		// Some java versions have keys that don't generate keyPresses -
		// here we add the key so we can later check if it happened
		getPressedKeys().addElement(Integer.valueOf(e.getKeyCode()));

		logger.info("PRESSED keychar='" + e.getKeyChar() + "' keycode=0x"
				+ Integer.toHexString(e.getKeyCode()) + " char='"
				+ ((char) e.getKeyCode()) + "'");

		if (rdp != null) {

			long t = input.getTime();

			input.sendScancode(t, RDP_KEYPRESS, keyMap.getScancode(e));

		}

	}

	@Override
	public void keyReleased(int key) {
		// Deprecated
/*
		KeyEvent e = new KeyEvent(this, KeyEvent.KEY_RELEASED, System
				.currentTimeMillis(), 0, key);
*/
		KeyEvent e = new KeyEvent(this, KeyEvent.KEY_RELEASED, System
				.currentTimeMillis(), 0, key,
				KeyEvent.CHAR_UNDEFINED);
		// Some java versions have keys that don't generate keyPresses -
		// we added the key to the vector in keyPressed so here we check for
		// it
		Integer keycode = new Integer(e.getKeyCode());
		if (!getPressedKeys().contains(keycode)) {
			keyPressed(key, false, false, false, false, false);
		}

		getPressedKeys().removeElement(keycode);

		input.lastKeyEvent = e;
		input.modifiersValid = true;
		long time = input.getTime();

		logger.info("RELEASED keychar='" + e.getKeyChar() + "' keycode=0x"
				+ Integer.toHexString(e.getKeyCode()) + " char='"
				+ ((char) e.getKeyCode()) + "'");
		if (rdp != null) {
			long t = input.getTime();

			input.sendScancode(t, RDP_KEYRELEASE, keyMap.getScancode(e));
		}

	}

//	@Override
//	public void blit(int x, int y, int w, int h, int srcx, int srcy) {
//		// System.out.println("Blit:" + x + "," + y + " " + w + "x" + h +
//		// " from "
//		// + srcx + "," + srcy);
//		DisplayMessage m = new DisplayMessage(client.getWriter());
//		m.addOperation(new CopyOperation(x, y, w, h, srcx, srcy));
//		this.client.getWriter().addMessage(m);
//	}

	@Override
	public void displayImage(int[] data, int w, int h, int x, int y, int cx,
			int cy) throws RdesktopException {
		// TODO Auto-generated method stub
		super.displayImage(data, w, h, x, y, cx, cy);
		// System.err.println(data.length + " ->" + w + "x" + h + " " + x + ","
		// + y + " : " + cx + "," + cy);

		if (w != cx) {
			int[] newData = new int[cx * cy];
			for (int yy = 0; yy < cy; yy++) {
				System.arraycopy(data, yy * w, newData, yy * cx, cx);
			}
			data = newData;
		}
		this.client.getWriter().encoder.encode(x, y, cx, cy, data, false /* flush: ??? */);

		if (cy != h) {
			Thread.dumpStack();
		}
	}

	public boolean isStopped() {

		return this.stopped;
	}

	@Override
	public void mouseWheelDown(int mouseX, int mouseY) {
		// System.err.println("RDPAdapter.mousePressed()");
		if (rdp != null) {
			rdp.sendInput((int) System.currentTimeMillis(), RDP_INPUT_MOUSE,
					MOUSE_FLAG_BUTTON5 | MOUSE_FLAG_DOWN, mouseX, mouseY);
		}

	}

	@Override
	public void mouseWheelUp(int mouseX, int mouseY) {
		if (rdp != null) {
			rdp.sendInput((int) System.currentTimeMillis(), RDP_INPUT_MOUSE,
					MOUSE_FLAG_BUTTON4 | MOUSE_FLAG_DOWN, mouseX, mouseY);
		}

	}

	public int[] getCursor(int w, int h, byte[] andmask, byte[] xormask) {

		int pxormask = 0;
		int pandmask = 0;

		int size = w * h;
		int scanline = w / 8;
		int offset = 0;
		byte[] mask = new byte[size];
		int[] cursor = new int[size];
		int pcursor = 0, pmask = 0;

		offset = size;

		for (int i = 0; i < h; i++) {
			offset -= w;
			pmask = offset;
			for (int j = 0; j < scanline; j++) {
				for (int bit = 0x80; bit > 0; bit >>= 1) {
					if ((andmask[pandmask] & bit) != 0) {
						mask[pmask] = 0;
					} else {
						mask[pmask] = 1;
					}
					pmask++;
				}
				pandmask++;
			}
		}

		offset = size;
		pcursor = 0;

		for (int i = 0; i < h; i++) {
			offset -= w;
			pcursor = offset;
			for (int j = 0; j < w; j++) {
				cursor[pcursor] = ((xormask[pxormask + 2] << 16) & 0x00ff0000)
						| ((xormask[pxormask + 1] << 8) & 0x0000ff00)
						| (xormask[pxormask] & 0x000000ff);
				pxormask += 3;
				pcursor++;
			}

		}

		offset = size;
		pmask = 0;
		pcursor = 0;
		pxormask = 0;

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				if ((mask[pmask] == 0) && (cursor[pcursor] != 0)) {
					cursor[pcursor] = ~(cursor[pcursor]);
					cursor[pcursor] |= 0xff000000;
				} else if ((mask[pmask] == 1) || (cursor[pcursor] != 0)) {
					cursor[pcursor] |= 0xff000000;
				}
				pcursor++;
				pmask++;
			}
		}
		return cursor;
	}

	@Override
	public Cursor createCursor(int x, int y, int w, int h, byte[] andmask,
			byte[] xormask, int cache_idx) {

		Cursor c = super.createCursor(x, y, w, h, andmask, xormask, cache_idx);
		DisplayMessage m = new DisplayMessage(client.getWriter());

		int[] cursor = getCursor(w, h, andmask, xormask);
		int bytesPerLine = w / 8;
		int length = h * bytesPerLine;
		byte[] bitmap2 = new byte[length];
		byte[] bitmap = new byte[length];
		byte[] mask2 = new byte[length];
		byte[] mask = new byte[length];
		andmask = Util.invert(andmask);

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				final int offsetFrom = i * w + j;
				final int offsetTo = i * w + w - j - 1;
				int b = cursor[offsetFrom];

				if (b != 0) {
					Util.setBit(bitmap2, offsetTo, b != -1);
					Util.setBit(mask2, offsetTo, true);
				} else {
					Util.setBit(mask2, offsetTo, false);
				}

			}
		}

		int offset = 0;
		for (int i = h - 1; i >= 0; i--) {
			for (int j = 0; j < bytesPerLine; j++) {
				final int o = i * bytesPerLine + j;
				bitmap[o] = bitmap2[offset];
				mask[o] = mask2[offset];
				offset++;
			}
		}

		final SetMouseCursorOperation op = new SetMouseCursorOperation(x, y, w,
				h, Color.WHITE, Color.BLACK, bitmap, mask);
		cursorMap.put(cache_idx, op);
		m.addOperation(op);
		if (session.isHardwareCursorUsed()) {
			this.client.getWriter().addMessage(m);
		}
		return c;
	}

	public void setCursorFromIdx(int cache_idx) {
		SetMouseCursorOperation op = cursorMap.get(cache_idx);

		if (op == null) {
			op = new SetMouseCursorOperation(
					SetMouseCursorOperation.UNIX_CURSOR);
		}
		DisplayMessage m = new DisplayMessage(client.getWriter());
		m.addOperation(op);
		this.client.getWriter().addMessage(m);

	}

	private WrappedImage getBackstore() {
        Field backstoreField = null;
        try {
            backstoreField = RdesktopCanvas.class.getDeclaredField("backstore");
			backstoreField.setAccessible(true);
			return ((WrappedImage)backstoreField.get(this));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

	private Vector getPressedKeys() {
		Field pressedKeysField = null;
		try {
			pressedKeysField = Input.class.getDeclaredField("pressedKeys");
			pressedKeysField.setAccessible(true);
			return ((Vector)pressedKeysField.get(input));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private String getTextDisconnectReason(int reason) {
		Method textDisconnectReason = null;
		try {
			textDisconnectReason = DisconnnectCodeMapper.class.getDeclaredMethod("textDisconnectReason", int.class);
			textDisconnectReason.setAccessible(true);
			return (String) textDisconnectReason.invoke(null, reason);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
