package me.pjq.leapmotion.hello;
import java.io.IOException;

import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Gesture.State;
import com.leapmotion.leap.Gesture.Type;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.HandList;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.ScreenTapGesture;
import com.leapmotion.leap.SwipeGesture;
import com.leapmotion.leap.Vector;

public class HelloLeap {

	public static void main(String[] args) {
		LeapMotionListener listener = new LeapMotionListener();

		Controller controller = new Controller();
		controller.addListener(listener);

		System.out.println("Press Enter to quit...");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Remove the sample listener when done
		controller.removeListener(listener);

	}

}

class LeapMotionListener extends Listener {
	private static final String TAG = LeapMotionListener.class.getSimpleName();

	@Override
	public void onInit(Controller controller) {
		System.out.println("Initialized");
	}

	@Override
	public void onConnect(Controller controller) {
		LogUtils.log(TAG, "Connected");

		controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
		controller.enableGesture(Gesture.Type.TYPE_SWIPE);
		controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
		controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
		controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
	}

	@Override
	public void onDisconnect(Controller controller) {
		LogUtils.log(TAG, "Disconnected");
	}

	public void onExit(Controller controller) {
		LogUtils.log(TAG, "Exited");
	}

	public void onFrame2(Controller controller) {
		// Get the most recent frame and report some basic information
		Frame frame = controller.frame();

		if (!frame.hands().empty()) {
			// Get the first hand
			Hand hand = frame.hands().get(0);

			// Check if the hand has any fingers
			FingerList fingers = hand.fingers();
			if (!fingers.empty()) {
				// Calculate the hand's average finger tip position
				Vector avgPos = Vector.zero();
				for (Finger finger : fingers) {
					avgPos = avgPos.plus(finger.tipPosition());
				}

				avgPos = avgPos.divide(fingers.count());

				float x = ((float) avgPos.getX() + 100) * 6;
				float y = 1000 - (3f * (float) avgPos.getY());
				float z = (float) avgPos.getZ();
				LogUtils.log(TAG, "onFrame,x=" + x + ",y=" + y + ",z=" + z);
			}
		}

	}

	private String type2String(Type type) {
		String string = "";

		if (Type.TYPE_CIRCLE == type) {
			string = "TYPE_CIRCLE";
		} else if (Type.TYPE_KEY_TAP == type) {
			string = "TYPE_KEY_TAP";
		} else if (Type.TYPE_SCREEN_TAP == type) {
			string = "TYPE_SCREEN_TAP";
		} else if (Type.TYPE_SWIPE == type) {
			string = "TYPE_SWIPE";
		} else if (Type.TYPE_INVALID == type) {
			string = "TYPE_INVALID";
		}

		return string;
	}

	private void showGestures(Controller controller, Frame frame) {
		GestureList gestures = frame.gestures();
		int count = gestures.count();

		// LogUtils.log(TAG, "showGestures,count=" + count);
		for (int i = 0; i < count; i++) {
			Gesture gesture = gestures.get(i);
			Type type = gesture.type();
			LogUtils.log(TAG, i + " showGestures,gesture=" + gesture + ",type="
					+ type2String(type));

			switch (gesture.type()) {
			case TYPE_CIRCLE:
				CircleGesture circle = new CircleGesture(gesture);
				// Calculate clock direction using the angle between circle
				// normal and pointable
				String clockwiseness;
				if (circle.pointable().direction().angleTo(circle.normal()) <= Math.PI / 4) {
					// Clockwise if angle is less than 90 degrees
					clockwiseness = "clockwise";
				} else {
					clockwiseness = "counterclockwise";
				}

				// Calculate angle swept since last frame
				double sweptAngle = 0;
				if (circle.state() != State.STATE_START) {
					CircleGesture previousUpdate = new CircleGesture(controller
							.frame(1).gesture(circle.id()));
					sweptAngle = (circle.progress() - previousUpdate.progress())
							* 2 * Math.PI;
				}

				LogUtils.log(TAG,
						"Circle id: " + circle.id() + ", " + circle.state()
								+ ", progress: " + circle.progress()
								+ ", radius: " + circle.radius() + ", angle: "
								+ Math.toDegrees(sweptAngle) + ", "
								+ clockwiseness);
				break;
			case TYPE_SWIPE:
				SwipeGesture swipe = new SwipeGesture(gesture);
				LogUtils.log(TAG,
						"Swipe id: " + swipe.id() + ", " + swipe.state()
								+ ", position: " + swipe.position()
								+ ", direction: " + swipe.direction()
								+ ", speed: " + swipe.speed());
				break;
			case TYPE_SCREEN_TAP:
				ScreenTapGesture screenTap = new ScreenTapGesture(gesture);
				LogUtils.log(
						TAG,
						"Screen Tap id: " + screenTap.id() + ", "
								+ screenTap.state() + ", position: "
								+ screenTap.position() + ", direction: "
								+ screenTap.direction());
				break;
			case TYPE_KEY_TAP:
				KeyTapGesture keyTap = new KeyTapGesture(gesture);
				LogUtils.log(TAG,
						"Key Tap id: " + keyTap.id() + ", " + keyTap.state()
								+ ", position: " + keyTap.position()
								+ ", direction: " + keyTap.direction());
				break;
			default:
				LogUtils.log(TAG, "Unknown gesture type.");
				break;
			}
		}
	}

	private void showLeftRight(Frame frame) {
		HandList hands = frame.hands();

		if (hands.count() > 0) {
			// get the first hand
			Hand hand = hands.get(0);
			Vector vector = frame.translation(frame);
			printVector(vector);
		}

	}

	private void printVector(Vector v) {
		if (null == v) {
			return;
		}

		LogUtils.log(TAG,
				"vector:x=" + v.getX() + ",y=" + v.getY() + ",z=" + v.getZ());
	}

	private void showHands(Frame frame) {
		HandList hands = frame.hands();
		long numHands = hands.count();
		System.out.println("Frame id: " + frame.id() + ", timestamp: "
				+ frame.timestamp() + ", hands: " + numHands);

		if (numHands >= 1) {
			// Get the first hand
			Hand hand = hands.get(0);

			// Check if the hand has any fingers
			FingerList fingers = hand.fingers();
			long numFingers = fingers.count();
			if (numFingers >= 1) {
				// Calculate the hand's average finger tip position
				Vector pos = new Vector(0, 0, 0);
				for (int i = 0; i < numFingers; ++i) {
					Finger finger = fingers.get(i);
					Vector tip = finger.tipPosition();
					pos.setX(pos.getX() + tip.getX());
					pos.setY(pos.getY() + tip.getY());
					pos.setZ(pos.getZ() + tip.getZ());
				}
				pos = new Vector(pos.getX() / numFingers, pos.getY()
						/ numFingers, pos.getZ() / numFingers);
				LogUtils.log(
						TAG,
						"Hand has " + numFingers
								+ " fingers with average tip position" + " ("
								+ pos.getX() + ", " + pos.getY() + ", "
								+ pos.getZ() + ")");
			}

			// Check if the hand has a palm
			Vector palmRay = hand.palmNormal();
			if (palmRay != null) {
				// Get the palm position and wrist direction
				Vector wrist = hand.direction();
				String direction = "";
				if (wrist.getX() > 0)
					direction = "left";
				else
					direction = "right";
				LogUtils.log(TAG, "Hand is pointing to the " + direction
						+ " with palm position" + " (" + palmRay.getX() + ", "
						+ palmRay.getY() + ", " + palmRay.getZ() + ")");
			}
		}
	}

	@Override
	public void onFrame(Controller controller) {
		// Get the most recent frame and report some basic information
		Frame frame = controller.frame();

		// showHands(frame);
		showGestures(controller, frame);
		// showLeftRight(frame);

	}

}
