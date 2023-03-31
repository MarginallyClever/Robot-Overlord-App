package com.marginallyclever.robotoverlord.robots.spidee;

import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.SessionLayer;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotoverlord.mesh.Mesh;
import com.marginallyclever.robotoverlord.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.parameters.MaterialEntity;
import com.marginallyclever.robotoverlord.robots.RobotEntity;

import javax.vecmath.Vector3d;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.charset.StandardCharsets;
import java.util.prefs.Preferences;

@Deprecated
public class Spidee extends RobotEntity {
    public static final String hello = "HELLO WORLD!  I AM SPIDEE #";
    protected long robotUID = 0;

    protected boolean isPortConfirmed = false;

    public static final float MAX_VEL = 60.0f / (1000.0f * 0.2f);   // 60 degrees/0.2s for TowerPro SG-5010.
    public static final float MAX_VEL_TRANSLATED = 512.0f * MAX_VEL / 360.0f;

    // must match defines in arduino code =see v7.pde;
    public static final int BUTTONS_X_POS = 0;
    public static final int BUTTONS_X_NEG = 1;
    public static final int BUTTONS_Y_POS = 2;
    public static final int BUTTONS_Y_NEG = 3;
    public static final int BUTTONS_Z_POS = 4;
    public static final int BUTTONS_Z_NEG = 5;
    public static final int BUTTONS_X_ROT_POS = 6;
    public static final int BUTTONS_X_ROT_NEG = 7;
    public static final int BUTTONS_Y_ROT_POS = 8;
    public static final int BUTTONS_Y_ROT_NEG = 9;
    public static final int BUTTONS_Z_ROT_POS = 10;
    public static final int BUTTONS_Z_ROT_NEG = 11;

    public static final int BUTTONS_0 = 12;  // reset
    public static final int BUTTONS_1 = 13;  // d-pad up?
    public static final int BUTTONS_2 = 14;  // d-pad down?
    public static final int BUTTONS_3 = 15;  // d-pad left?
    public static final int BUTTONS_4 = 16;  // d-pad right?
    public static final int BUTTONS_5 = 17;  // square
    public static final int BUTTONS_6 = 18;  // circle
    public static final int BUTTONS_7 = 19;  // x
    public static final int BUTTONS_8 = 20;  // triangle

    public static final int BUTTONS_MAX = 21;


    private final MaterialEntity matBody = new MaterialEntity();
    private final MaterialEntity matHead = new MaterialEntity();
    private final MaterialEntity matLeg1 = new MaterialEntity();
    private final MaterialEntity matThigh = new MaterialEntity();
    private final MaterialEntity matShin = new MaterialEntity();

    public enum MoveModes {
        MOVE_MODE_CALIBRATE,

        MOVE_MODE_SITDOWN,
        MOVE_MODE_STANDUP,
        MOVE_MODE_BODY,
        MOVE_MODE_RIPPLE,
        MOVE_MODE_WAVE,
        MOVE_MODE_TRIPOD,

        MOVE_MODE_MAX
    }

    SpideeLocation body = new SpideeLocation();
    SpideeLocation target = new SpideeLocation();
    SpideeLeg[] legs = new SpideeLeg[6];

    public int[] buttons = new int[BUTTONS_MAX];

    double bodyRadius;
    double standingRadius;  // Distance from center of body to foot on ground in body-relative XY plane.  Used for motion planning
    double standingHeight;  // How high the body should "ride" when walking
    double turnStrideLength;  // how far to move a foot when turning
    double strideLength;  // how far to move a step when walking
    double strideHeight;  // how far to lift a foot
    double maxLegLength;

    MoveModes moveMode;  // What is the bot's current agenda?
    float gaitCycleTime;

    float speedScale;
    boolean paused;

	// models
    protected transient Mesh modelThigh = null;
    protected transient Mesh modelBody = null;
    protected transient Mesh modelShoulderLeft = null;
    protected transient Mesh modelShoulderRight = null;
    protected transient Mesh modelShinLeft = null;
    protected transient Mesh modelShinRight = null;

    protected transient SpideeControlPanel spideePanel;


    public Spidee() {
        super();
        setName("Spidee");

		buildLegsAtCalibrationPose();
		loadLegRangeLimits();
		setServoAddresses();
		loadPostureSettings();

        maxLegLength = legs[0].knee.relative.length()
                		+ legs[0].ankle.relative.length();

        matHead.setDiffuseColor(1.0f, 0.8f, 0.0f, 1.0f);
        matLeg1.setDiffuseColor(0, 1, 0, 1);
        matThigh.setDiffuseColor(1, 0, 0, 1);
        matShin.setDiffuseColor(0, 0, 1, 1);

		loadModels();
		paused = false;
    }

	private void buildLegsAtCalibrationPose() {
		int i;
		for (i = 0; i < 6; ++i) {
			legs[i] = new SpideeLeg();
		}

		body.pos.set(0, 0, 0);
		body.up.set(0, 0, 1);
		body.forward.set(0, 1, 0);
		body.left.set(-1, 0, 0);

		bodyRadius = 10;
		legs[0].facingAngle = 45;
		legs[1].facingAngle = 90;
		legs[2].facingAngle = 135;
		legs[3].facingAngle = -135;
		legs[4].facingAngle = -90;
		legs[5].facingAngle = -45;

		legs[0].name = "RF";
		legs[1].name = "RM";
		legs[2].name = "RB";
		legs[3].name = "LB";
		legs[4].name = "LM";
		legs[5].name = "LF";

		target.forward.set(body.pos);
		target.forward.add(new Vector3d(0, 1, 0));

		target.up.set(body.pos);
		target.up.add(new Vector3d(0, 0, 1));

		moveMode = MoveModes.MOVE_MODE_CALIBRATE;
		speedScale = 1.0f;

		int j = 0;
		for (i = 0; i < 7; ++i) {
			if (i == 3) continue;
			SpideeLeg leg = legs[j++];

			leg.active = false;

			double x = (i + 1) * Math.PI * 2.0f / 8.0f;
			//float y = leg.facingAngle*DEG2RAD;
			leg.shoulderPan.forward.set(Math.sin(x), Math.cos(x), 0);
			leg.shoulderPan.forward.normalize();
			leg.shoulderPan.up.set(body.up);

			leg.shoulderPan.left.cross(leg.shoulderPan.forward, leg.shoulderPan.up);
			leg.shoulderPan.angle = 127.0f;
			leg.shoulderPan.lastAngle = (int) leg.shoulderPan.angle;

			leg.shoulderTilt.set(leg.shoulderPan);
			leg.knee.set(leg.shoulderPan);
			leg.ankle.set(leg.shoulderPan);

			leg.shoulderPan.relative.set(leg.shoulderPan.forward);
			leg.shoulderPan.relative.scale(bodyRadius);
			leg.shoulderPan.relative.z += 2.0f;
			leg.shoulderTilt.relative.set(leg.shoulderPan.forward);
			leg.shoulderTilt.relative.scale(2.232f);
			Vector3d a = new Vector3d(leg.shoulderPan.forward);
			Vector3d b = new Vector3d(leg.shoulderPan.up);
			a.scale(5.5f);
			b.scale(5.5f);
			leg.knee.relative.set(a);
			leg.knee.relative.add(b);
			leg.ankle.relative.set(leg.shoulderPan.forward);
			leg.ankle.relative.scale(10.0f);

			leg.shoulderPan.pos.set(body.pos);
			leg.shoulderPan.pos.add(leg.shoulderPan.relative);

			leg.shoulderTilt.pos.set(leg.shoulderPan.pos);
			leg.shoulderTilt.pos.add(leg.shoulderTilt.relative);
			leg.knee.pos.set(leg.shoulderTilt.pos);
			leg.knee.pos.add(leg.knee.relative);
			leg.ankle.pos.set(leg.knee.pos);
			leg.ankle.pos.add(leg.ankle.relative);
		}
	}

	private void loadLegRangeLimits() {
		Preferences prefs = Preferences.userRoot().node("Spidee");
		int j=0;
		for (SpideeLeg leg : legs) {
			Preferences pn = prefs.node("Leg " + j);
			j++;
			leg.shoulderPan.angleMax = pn.getInt("panMax", 127 + 60);
			leg.shoulderPan.angleMin = pn.getInt("panMin", 127 - 60);
            leg.shoulderPan.zero = pn.getFloat("panZero", 127);
            leg.shoulderPan.scale = pn.getFloat("panScale", 1);

			leg.shoulderTilt.angleMax = pn.getInt("tiltMax", 240);
			leg.shoulderTilt.angleMin = pn.getInt("tiltMin", 15);
            leg.shoulderTilt.zero = pn.getFloat("tiltZero", 127);
            leg.shoulderTilt.scale = pn.getFloat("tiltScale", 1);

			leg.knee.angleMax = pn.getInt("kneeMax", 240);
			leg.knee.angleMin = pn.getInt("kneeMin", 15);
			leg.knee.zero = pn.getFloat("kneeZero", 127);
            leg.knee.scale = pn.getFloat("kneeScale", 1);
        }
	}

	private void loadPostureSettings() {
		Preferences prefs = Preferences.userRoot().node("Spidee");
		strideLength = prefs.getFloat("strideLength", 15.0f);
		strideHeight = prefs.getFloat("strideHeight", 5.0f);
		turnStrideLength = prefs.getFloat("turnStrideLength", 150.0f);
		standingRadius = prefs.getFloat("standingRadius", 21.0f);
		standingHeight = prefs.getFloat("standingHeight", 5.5f);
	}

	private void loadModels() {
		// models
		try {
			modelThigh = MeshFactory.load("/Spidee.zip:thigh.stl");
			modelBody = MeshFactory.load("/Spidee.zip:body.stl");
			modelShoulderLeft = MeshFactory.load("/Spidee.zip:shoulderLeft.stl");
			modelShoulderRight = MeshFactory.load("/Spidee.zip:shoulderRight.stl");
			modelShinLeft = MeshFactory.load("/Spidee.zip:shinLeft.stl");
			modelShinRight = MeshFactory.load("/Spidee.zip:shinRight.stl");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// set the servo addresses in case a pin doesn't work on the servo board.
	private void setServoAddresses() {
		Preferences prefs = Preferences.userRoot().node("Spidee");
		Preferences pn = prefs.node("Leg 0");
		legs[0].shoulderPan.servoAddress = pn.getInt("panAddress", 0);
		legs[0].shoulderTilt.servoAddress = pn.getInt("tiltAddress", 1);
		legs[0].knee.servoAddress = pn.getInt("kneeAddress", 2);
		pn = pn.node("Leg 1");
		legs[1].shoulderPan.servoAddress = pn.getInt("panAddress", 3);
		legs[1].shoulderTilt.servoAddress = pn.getInt("tiltAddress", 4);
		legs[1].knee.servoAddress = pn.getInt("kneeAddress", 5);
		pn = pn.node("Leg 2");
		legs[2].shoulderPan.servoAddress = pn.getInt("panAddress", 6);
		legs[2].shoulderTilt.servoAddress = pn.getInt("tiltAddress", 7);
		legs[2].knee.servoAddress = pn.getInt("kneeAddress", 8);
		pn = pn.node("Leg 3");
		legs[3].shoulderPan.servoAddress = pn.getInt("panAddress", 22);
		legs[3].shoulderTilt.servoAddress = pn.getInt("tiltAddress", 23);
		legs[3].knee.servoAddress = pn.getInt("kneeAddress", 24);
		pn = pn.node("Leg 4");
		legs[4].shoulderPan.servoAddress = pn.getInt("panAddress", 19);
		legs[4].shoulderTilt.servoAddress = pn.getInt("tiltAddress", 20);
		legs[4].knee.servoAddress = pn.getInt("kneeAddress", 21);
		pn = pn.node("Leg 5");
		legs[5].shoulderPan.servoAddress = pn.getInt("panAddress", 16);
		legs[5].shoulderTilt.servoAddress = pn.getInt("tiltAddress", 17);
		legs[5].knee.servoAddress = pn.getInt("kneeAddress", 18);
	}


    void centerBodyAroundFeet(double dt) {
        // center the body between the feet even if they are not on the ground.
        Vector3d p = new Vector3d();
		for(SpideeLeg leg : legs) {
			p.add(leg.ankle.pos);
		}
		p.scale(1.0f / 6.0f);
		Vector3d dp = new Vector3d(p.x - body.pos.x, p.y - body.pos.y, 0);
		dp.scale(0.5f);
		body.pos.add(dp);
		// zero body height
		body.pos.z += (standingHeight - body.pos.z) * dt;

		// average the body orientation to the feet.
        Vector3d r = new Vector3d();
        for (int i = 0; i < 6; ++i) {
            if (i < 3) r.sub(legs[i].ankle.pos);
            else r.add(legs[i].ankle.pos);
        }
        r.scale(1.0f / 6.0f);
        target.left = r;
        target.left.normalize();
        target.up.set(0, 0, 1);
        target.forward.cross(target.left, target.up);
    }

    void translateBody(double dt) {
        // IK test - moving body

        float x = (float) buttons[BUTTONS_X_NEG] - (float) buttons[BUTTONS_X_POS];
        float y = (float) buttons[BUTTONS_Y_NEG] - (float) buttons[BUTTONS_Y_POS];
        float z = (float) buttons[BUTTONS_Z_NEG] - (float) buttons[BUTTONS_Z_POS];
        x = Math.max(Math.min(x, MAX_VEL), -MAX_VEL);  // sideways
        y = Math.max(Math.min(y, MAX_VEL), -MAX_VEL);  // forward/back
        z = Math.max(Math.min(z, MAX_VEL), -MAX_VEL);  // raise/lower body

        Vector3d forward = new Vector3d(body.forward);
        forward.scale(y);
        Vector3d left = new Vector3d(body.left);
        left.scale(x);
        forward.sub(left);

        body.pos.z -= z;
        if (body.pos.z > 0) {
            body.pos.add(forward);
			for(SpideeLeg leg : legs) {
                leg.shoulderPan.pos.add(forward);
                leg.shoulderPan.pos.z += z;
            }
        }
    }

    void translateBodyTowards(Vector3d point, float dt) {
        Vector3d dp = new Vector3d(point);
        dp.sub(body.pos);

        double dpl = dp.length();
        if (dpl > dt) {
            dp.normalize();
            dp.scale(dt);
            if (body.pos.z != 0 || dp.z >= 0) {
                body.pos.add(dp);
				for(SpideeLeg leg : legs) {
                    leg.shoulderPan.pos.add(dp);
                }
            }
        } else {
            body.pos = point;
        }
    }

    void angleBody(double dt) {
        // IK test - moving body

        float roll = buttons[BUTTONS_X_ROT_NEG] - buttons[BUTTONS_X_ROT_POS];
        float tilt = buttons[BUTTONS_Y_ROT_NEG] - buttons[BUTTONS_Y_ROT_POS];
        float pan = buttons[BUTTONS_Z_ROT_NEG] - buttons[BUTTONS_Z_ROT_POS];

        roll = Math.max(Math.min(roll, MAX_VEL), -MAX_VEL);
        tilt = Math.max(Math.min(tilt, MAX_VEL), -MAX_VEL);
        pan = Math.max(Math.min(pan, MAX_VEL), -MAX_VEL);
        //Log.message(""+a1+"\t"+b1+"\t"+c1);

        if (roll != 0 || tilt != 0) {
			// tilt and roll
            Vector3d forward = new Vector3d(body.forward);
            forward.scale(roll);
            Vector3d sideways = new Vector3d(body.left);
            sideways.scale(-tilt);
            forward.add(sideways);
            forward.normalize();
            forward.scale(Math.toRadians(turnStrideLength) * dt / 3.0f);
            target.up.add(forward);
            target.up.normalize();
        }

        if (pan != 0) {
			// pan
            Vector3d left = new Vector3d(target.left);
            left.scale(pan * Math.toRadians(turnStrideLength) * dt / 6.0f);
            target.forward.add(left);
            target.forward.normalize();
            target.left.cross(target.up, target.forward);
        }
    }

    void moveBody(double dt) {
        translateBody(dt);
        angleBody(dt);

        if (buttons[BUTTONS_0] > 0) {
            centerBodyAroundFeet(dt);
        }
    }

    void standUp(double dt) {
        int onFloor = 0;
        float scale = 2.0f;

		int bringFeetIn = 0;

		// put feet closer to shoulders
		for(SpideeLeg leg : legs) {
			Vector3d df = new Vector3d(leg.ankle.pos);
			df.sub(body.pos);
			df.z = 0;
			if (df.length() > standingRadius) {
				df.normalize();
				df.scale(6 * scale * dt);
				leg.ankle.pos.sub(df);
			} else bringFeetIn++;
		}

		if(bringFeetIn == 6) {
			// touch the feet to the floor
			for (SpideeLeg leg : legs) {
				if (leg.ankle.pos.z > 0) leg.ankle.pos.z -= 4 * scale * dt;
				else ++onFloor;

			}

			if (onFloor == 6) {
				// we've planted all feet, raise the body a bit
				if (body.pos.z < standingHeight) body.pos.z += 2 * scale * dt;

				for (SpideeLeg leg : legs) {
					Vector3d ds = new Vector3d(leg.shoulderPan.pos);
					ds.sub(body.pos);
					ds.normalize();
					ds.scale(standingRadius);
					leg.nextPointOfContact.set(body.pos.x + ds.x,
							body.pos.y + ds.y,
							0);
				}
			}
		}
    }

    boolean sitDown(double dt) {
        int i;
        int countLegsUp = 0;

        // we've planted all feet, lower the body to the ground
        if (body.pos.z > 0) {
			body.pos.z -= 2 * dt;
		} else {
			// body on ground, raise feet.
			for( SpideeLeg leg : legs ) {
                // raise feet
                Vector3d ls = new Vector3d(leg.ankle.pos);
                ls.sub(leg.shoulderPan.pos);
                if (ls.length() < 16) {
                    ls.z = 0;
                    ls.normalize();
                    ls.scale(4 * dt);
                    leg.ankle.pos.add(ls);
                } else ++countLegsUp;

                if (leg.ankle.pos.z - leg.shoulderPan.pos.z < 5.5)
                    leg.ankle.pos.z += 4 * dt;
                else ++countLegsUp;

                if (leg.knee.pos.z - leg.shoulderPan.pos.z < 5.5)
                    leg.knee.pos.z += 4 * dt;
                else ++countLegsUp;
            }
        }

        return countLegsUp == 6 * 3;
    }

	/**
	 * Check all joint angles are in range and send to robot if anything has changed.
	 */
    void moveSendSerial() {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        int used = 0;

		for(SpideeLeg leg : legs) {
			used = updateJoint(leg.shoulderPan, buffer, used);
			used = updateJoint(leg.shoulderTilt, buffer, used);
			used = updateJoint(leg.knee, buffer, used);
        }

        if (used > 0) {
            sendToRobot('U', buffer);
        }
    }

	private int updateJoint(SpideeJoint joint, ByteBuffer buffer, int used) {
		joint.angle = Math.max( Math.min(joint.angle, joint.angleMax), joint.angleMin);
		if (joint.lastAngle != (int) joint.angle) {
			joint.lastAngle = (int) joint.angle;
			buffer.put(used++, (byte) joint.servoAddress);
			buffer.put(used++, (byte) joint.angle);
			//Log3("%d=%d ",buffer[used-2],buffer[used-1]);
		}
		return used;
	}

	void teleport(Vector3d newpos) {
        // move simulated robot to a new position, update all positions.
        newpos.sub(body.pos);
        body.pos.add(newpos);

		for(SpideeLeg leg : legs) {
            leg.shoulderPan.pos.add(newpos);
            leg.shoulderTilt.pos.add(newpos);
            leg.knee.pos.add(newpos);
            leg.ankle.pos.add(newpos);
        }
    }

    @Override
    public void update(double dt) {
		sendStateToRobot();

        dt *= speedScale;

        if (dt != 0) {
			switch (moveMode) {
				case MOVE_MODE_CALIBRATE -> moveCalibrate(dt);
				case MOVE_MODE_SITDOWN -> sitDown(dt);
				case MOVE_MODE_STANDUP -> standUp(dt);
				case MOVE_MODE_BODY -> moveBody(dt);
				case MOVE_MODE_RIPPLE -> rippleGait(dt);
				case MOVE_MODE_WAVE -> waveGait(dt);
				case MOVE_MODE_TRIPOD -> tripodGait(dt);
				default -> {
				}
			}
		}

        if (moveMode != Spidee.MoveModes.MOVE_MODE_CALIBRATE) {
			//Center_Body_Around_Feet(dt);
			if (paused) {
				plantFeet();
			}

            applyPhysics(dt);
            applyConstraints(dt);
            calculateJointAngles();
        } else {
            // since we now do all the math on the arduino, this is only used to move individual joints for calibration.
            moveSendSerial();
        }
    }

	private void sendStateToRobot() {
		byte modeNow = switch (moveMode) {
			case MOVE_MODE_CALIBRATE -> 0;
			case MOVE_MODE_SITDOWN -> 1;
			case MOVE_MODE_STANDUP -> 2;
			case MOVE_MODE_BODY -> 3;
			case MOVE_MODE_RIPPLE -> 4;
			case MOVE_MODE_WAVE -> 5;
			case MOVE_MODE_TRIPOD -> 6;
			case MOVE_MODE_MAX -> (byte) 0;
		};
		ByteBuffer buffer = ByteBuffer.allocate(BUTTONS_MAX + 1);
		buffer.put(0, modeNow);
		for (int i = 0; i < BUTTONS_MAX; ++i) {
			buffer.put(i, (byte) buttons[i]);
		}
		buffer.rewind();
		sendToRobot('I', buffer);
	}

	public void setMoveMode(MoveModes mode) {
        moveMode = mode;

    }

    public void setSpeed(float speed) {
        speedScale = speed;
    }


    @Override
    public void render(GL2 gl2) {
        super.render(gl2);
        gl2.glPushMatrix();

        Vector3d p = getPosition();
        gl2.glTranslated(p.x, p.y, p.z);
        drawHead(gl2);
        drawLegs(gl2);
        drawBody(gl2);
        gl2.glPopMatrix();
    }


    void drawBody(GL2 gl2) {
        gl2.glPushMatrix();

        DoubleBuffer m = DoubleBuffer.allocate(16);

        m.put(0, -body.left.x);
        m.put(1, -body.left.y);
        m.put(2, -body.left.z);
        m.put(4, body.forward.x);
        m.put(5, body.forward.y);
        m.put(6, body.forward.z);
        m.put(8, body.up.x);
        m.put(9, body.up.y);
        m.put(10, body.up.z);
        m.put(15, 1);

        matBody.render(gl2);
        gl2.glTranslated(body.pos.x + 7.5f * body.up.x,
                body.pos.y + 7.5f * body.up.y,
                body.pos.z + 7.5f * body.up.z);
        gl2.glMultMatrixd(m);
        gl2.glRotatef(180, 0, 1, 0);
        modelBody.render(gl2);

        gl2.glPopMatrix();
    }


    void drawHead(GL2 gl2) {
        int i;

        matHead.render(gl2);

        gl2.glPushMatrix();
        // head
        Vector3d v = new Vector3d(body.forward);
        v.scale(10);
        v.add(body.pos);
        gl2.glTranslated(v.x, v.y, v.z);
        gl2.glBegin(GL2.GL_LINE_LOOP);
        for (i = 0; i < 32; ++i) {
            double x = i * (Math.PI * 2.0f) / 32.0f;
            gl2.glVertex3d(Math.sin(x) * 0.5f, Math.cos(x) * 0.5f, 0.0f);
        }
        gl2.glEnd();
        gl2.glBegin(GL2.GL_LINE_LOOP);
        for (i = 0; i < 32; ++i) {
            double x = i * (Math.PI * 2.0f) / 32.0f;
            gl2.glVertex3d(Math.sin(x) * 0.5f, 0.0f, Math.cos(x) * 0.5f);
        }
        gl2.glEnd();
        gl2.glBegin(GL2.GL_LINE_LOOP);
        for (i = 0; i < 32; ++i) {
            double x = i * (Math.PI * 2.0f) / 32.0f;
            gl2.glVertex3d(0.0f, Math.sin(x) * 0.5f, Math.cos(x) * 0.5f);
        }
        gl2.glEnd();
        gl2.glPopMatrix();
    }


    void drawLegs(GL2 gl2) {
        int i;
        DoubleBuffer m = DoubleBuffer.allocate(16);

        for (i = 0; i < 6; ++i) {
            SpideeLeg leg = legs[i];
            leg.render(gl2, i);

            matLeg1.render(gl2);

            gl2.glPushMatrix();
            gl2.glTranslated(leg.shoulderPan.pos.x,
                    leg.shoulderPan.pos.y,
                    leg.shoulderPan.pos.z);
            gl2.glTranslated(leg.shoulderPan.up.x * 2.5f,
                    leg.shoulderPan.up.y * 2.5f,
                    leg.shoulderPan.up.z * 2.5f);

            if (i < 3) {
                gl2.glTranslated(leg.shoulderPan.forward.x * -1.0f,
                        leg.shoulderPan.forward.y * -1.0f,
                        leg.shoulderPan.forward.z * -1.0f);
                gl2.glTranslated(leg.shoulderPan.left.x * -1.0f,
                        leg.shoulderPan.left.y * -1.0f,
                        leg.shoulderPan.left.z * -1.0f);
                m.put(0, -leg.shoulderPan.left.x);
                m.put(1, -leg.shoulderPan.left.y);
                m.put(2, -leg.shoulderPan.left.z);
                m.put(4, leg.shoulderPan.up.x);
                m.put(5, leg.shoulderPan.up.y);
                m.put(6, leg.shoulderPan.up.z);
                m.put(8, leg.shoulderPan.forward.x);
                m.put(9, leg.shoulderPan.forward.y);
                m.put(10, leg.shoulderPan.forward.z);
            } else {
                gl2.glTranslated(leg.shoulderPan.forward.x * 1.3f,
                        leg.shoulderPan.forward.y * 1.3f,
                        leg.shoulderPan.forward.z * 1.3f);
                gl2.glTranslated(leg.shoulderPan.left.x * 1.1f,
                        leg.shoulderPan.left.y * 1.1f,
                        leg.shoulderPan.left.z * 1.1f);

                m.put(0, leg.shoulderPan.left.x);
                m.put(1, leg.shoulderPan.left.y);
                m.put(2, leg.shoulderPan.left.z);
                m.put(4, leg.shoulderPan.up.x);
                m.put(5, leg.shoulderPan.up.y);
                m.put(6, leg.shoulderPan.up.z);
                m.put(8, -leg.shoulderPan.forward.x);
                m.put(9, -leg.shoulderPan.forward.y);
                m.put(10, -leg.shoulderPan.forward.z);
            }
            m.put(3, 0);
            m.put(7, 0);
            m.put(11, 0);
            m.put(12, 0);
            m.put(13, 0);
            m.put(14, 0);
            m.put(15, 1);
            gl2.glMultMatrixd(m);
            if (i < 3) modelShoulderLeft.render(gl2);
            else modelShoulderRight.render(gl2);
            gl2.glPopMatrix();

            // thigh
            matThigh.render(gl2);
            gl2.glPushMatrix();
            gl2.glTranslated(leg.shoulderTilt.pos.x,
                    leg.shoulderTilt.pos.y,
                    leg.shoulderTilt.pos.z);
            gl2.glTranslated(leg.shoulderPan.left.x * 2.0f,
                    leg.shoulderPan.left.y * 2.0f,
                    leg.shoulderPan.left.z * 2.0f);
            gl2.glTranslated(leg.shoulderPan.forward.x * 1.0f,
                    leg.shoulderPan.forward.y * 1.0f,
                    leg.shoulderPan.forward.z * 1.0f);
            gl2.glTranslated(leg.shoulderPan.up.x * 0.5f,
                    leg.shoulderPan.up.y * 0.5f,
                    leg.shoulderPan.up.z * 0.5f);

            m.put(0, leg.shoulderTilt.up.x);
            m.put(1, leg.shoulderTilt.up.y);
            m.put(2, leg.shoulderTilt.up.z);
            m.put(4, leg.shoulderTilt.left.x);
            m.put(5, leg.shoulderTilt.left.y);
            m.put(6, leg.shoulderTilt.left.z);
            m.put(8, -leg.shoulderTilt.forward.x);
            m.put(9, -leg.shoulderTilt.forward.y);
            m.put(10, -leg.shoulderTilt.forward.z);
            m.put(3, 0);
            m.put(7, 0);
            m.put(11, 0);
            m.put(12, 0);
            m.put(13, 0);
            m.put(14, 0);
            m.put(15, 1);

            gl2.glMultMatrixd(m);
            modelThigh.render(gl2);
            gl2.glPopMatrix();

            gl2.glPushMatrix();
            gl2.glTranslated(leg.shoulderTilt.pos.x,
                    leg.shoulderTilt.pos.y,
                    leg.shoulderTilt.pos.z);
            gl2.glTranslated(leg.shoulderTilt.left.x * -2.0f,
                    leg.shoulderTilt.left.y * -2.0f,
                    leg.shoulderTilt.left.z * -2.0f);
            gl2.glTranslated(leg.shoulderTilt.forward.x * 0.8f,
                    leg.shoulderTilt.forward.y * 0.8f,
                    leg.shoulderTilt.forward.z * 0.8f);
            gl2.glTranslated(leg.shoulderTilt.up.x * 0.5f,
                    leg.shoulderTilt.up.y * 0.5f,
                    leg.shoulderTilt.up.z * 0.5f);

            m.put(0, leg.shoulderTilt.up.x);
            m.put(1, leg.shoulderTilt.up.y);
            m.put(2, leg.shoulderTilt.up.z);
            m.put(4, -leg.shoulderTilt.left.x);
            m.put(5, -leg.shoulderTilt.left.y);
            m.put(6, -leg.shoulderTilt.left.z);
            m.put(8, -leg.shoulderTilt.forward.x);
            m.put(9, -leg.shoulderTilt.forward.y);
            m.put(10, -leg.shoulderTilt.forward.z);
            m.put(3, 0);
            m.put(7, 0);
            m.put(11, 0);
            m.put(12, 0);
            m.put(13, 0);
            m.put(14, 0);
            m.put(15, 1);

            gl2.glMultMatrixd(m);
            modelThigh.render(gl2);
            gl2.glPopMatrix();

            // shin
            matShin.render(gl2);
            gl2.glPushMatrix();
            gl2.glTranslated(leg.knee.pos.x,
                    leg.knee.pos.y,
                    leg.knee.pos.z);

            if (i < 3) {
                gl2.glTranslated(leg.knee.forward.x * -0.75f,
                        leg.knee.forward.y * -0.75f,
                        leg.knee.forward.z * -0.75f);
                m.put(0, -leg.knee.forward.x);
                m.put(1, -leg.knee.forward.y);
                m.put(2, -leg.knee.forward.z);
                m.put(4, leg.knee.left.x);
                m.put(5, leg.knee.left.y);
                m.put(6, leg.knee.left.z);
                m.put(8, leg.knee.up.x);
                m.put(9, leg.knee.up.y);
                m.put(10, leg.knee.up.z);
            } else {
                gl2.glTranslated(leg.knee.up.x * 2.0f,
                        leg.knee.up.y * 2.0f,
                        leg.knee.up.z * 2.0f);
                gl2.glTranslated(leg.knee.forward.x * -0.75f,
                        leg.knee.forward.y * -0.75f,
                        leg.knee.forward.z * -0.75f);
                m.put(0, -leg.knee.forward.x);
                m.put(1, -leg.knee.forward.y);
                m.put(2, -leg.knee.forward.z);
                m.put(4, -leg.knee.left.x);
                m.put(5, -leg.knee.left.y);
                m.put(6, -leg.knee.left.z);
                m.put(8, -leg.knee.up.x);
                m.put(9, -leg.knee.up.y);
                m.put(10, -leg.knee.up.z);
            }
            m.put(3, 0);
            m.put(7, 0);
            m.put(11, 0);
            m.put(12, 0);
            m.put(13, 0);
            m.put(14, 0);
            m.put(15, 1);

            gl2.glMultMatrixd(m);
            if (i < 3) modelShinLeft.render(gl2);
            else modelShinRight.render(gl2);
            gl2.glPopMatrix();
        }
    }


    void sendCalibrationPositions() {
        Log.message("Sending Zeros...\n");
        ByteBuffer message = ByteBuffer.allocate(18 * 2);
        int j = 0;
        for (int i = 0; i < 6; ++i) {
            message.put(j++, (byte) legs[i].shoulderPan.servoAddress);
            message.put(j++, (byte) legs[i].shoulderPan.zero);
            message.put(j++, (byte) legs[i].shoulderTilt.servoAddress);
            message.put(j++, (byte) legs[i].shoulderTilt.zero);
            message.put(j++, (byte) legs[i].knee.servoAddress);
            message.put(j++, (byte) legs[i].knee.zero);
            Log.message("\t" + legs[i].shoulderPan.servoAddress + ":" + legs[i].shoulderPan.zero);
            Log.message("\t" + legs[i].shoulderTilt.servoAddress + ":" + legs[i].shoulderTilt.zero);
            Log.message("\t" + legs[i].knee.servoAddress + ":" + legs[i].knee.zero);
        }

        message.rewind();
        sendToRobot('W', message);
    }


    public void resetPosition() {
        ByteBuffer buffer = ByteBuffer.allocate(6 * 6);

        gaitCycleTime = 0;

        // Reset the bot position
        int j = 0;
		for(SpideeLeg leg : legs) {
            buffer.put(j++, (byte)  leg.shoulderPan.servoAddress);
            buffer.put(j++, (byte) (leg.shoulderPan.zero));
            buffer.put(j++, (byte)  leg.shoulderTilt.servoAddress);
            buffer.put(j++, (byte) (leg.shoulderTilt.zero + 33));
            buffer.put(j++, (byte)  leg.knee.servoAddress);
            buffer.put(j++, (byte) (leg.knee.zero + 120));
        }

        buffer.rewind();
        sendToRobot('U', buffer);
    }


    void sendToRobot(char code, ByteBuffer buf) {
        if (connection == null || !isPortConfirmed) return;
        String line = code + buf.toString();

        try {
            connection.sendMessage(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void recordCalibration() {
        for (int i = 0; i < 6; ++i) {
            legs[i].shoulderTilt.zero -= 33;
            legs[i].knee.zero -= 120;
        }
    }


    void moveCalibrate(double dt) {
        // turn active legs on and off.
        float a = 0, b = 0, c = 0;
        if (a != 0 || b != 0 || c != 0) {
            int i;
            for (i = 0; i < 6; ++i) {
                SpideeLeg leg = legs[i];
                if (leg.active) {
                    // limit the max vel
			        /*
			        leg.ankleJoint.pos += leg.shoulderPan.forward * ( a * dt );
			        leg.ankleJoint.pos += leg.tiltJoint.left * ( c * dt );
			        leg.ankleJoint.pos.z += ( b * dt );
			        */
                    leg.shoulderPan.angle += c;
                    leg.shoulderTilt.angle += a;
                    leg.knee.angle += b;
                }
            }
        }
    }


    void updateGaitTarget(double dt, double moveBodyScale) {
        int turnDirection   = buttons[BUTTONS_Z_ROT_POS] - buttons[BUTTONS_Z_ROT_NEG];
        int walkDirection   = buttons[BUTTONS_Y_NEG    ] - buttons[BUTTONS_Y_POS    ];
        int strafeDirection = buttons[BUTTONS_X_POS    ] - buttons[BUTTONS_X_NEG    ];

        boolean updateNow = (turnDirection != 0 || walkDirection != 0 || strafeDirection != 0);

        // zero stance width
        if (buttons[BUTTONS_1] > 0) {  // widen stance
            standingRadius += dt * 4;
            updateNow = true;
        }
        if (buttons[BUTTONS_2] > 0) {  // narrow stance
            standingRadius -= dt * 4;
            if (standingRadius < bodyRadius + 1) {
                standingRadius = bodyRadius + 1;
            }
            updateNow = true;
        }

        // zero body standing height
        if (buttons[BUTTONS_Z_POS] > 0) {  // raise body
            standingHeight += dt * 4;
            updateNow = true;
        }
        if (buttons[BUTTONS_Z_NEG] > 0) {  // lower body
            standingHeight -= dt * 4;
            if (standingHeight < 1.5f) {
                standingHeight = 1.5f;
            }
            updateNow = true;
        }

        if (!updateNow) return;

		for( SpideeLeg leg : legs) {
            Vector3d ds = new Vector3d(leg.shoulderPan.pos);
            ds.sub(body.pos);
            ds.normalize();
            ds.scale(standingRadius);
            leg.nextPointOfContact.set(body.pos);
            leg.nextPointOfContact.add(ds);
            leg.nextPointOfContact.z = 0;
        }

        // turn
        if (turnDirection != 0) {
            turnDirection = (int) Math.max(Math.min((float) turnDirection, 180 * dt), -180 * dt);
            double turn = Math.toRadians(turnDirection * turnStrideLength) * dt * moveBodyScale / 6.0f;
            double c = (Math.cos(turn));
            double s = (Math.sin(turn));

			for( SpideeLeg leg : legs) {
                Vector3d df = new Vector3d(leg.nextPointOfContact);
                df.sub(body.pos);
                df.z = 0;
                leg.nextPointOfContact.x = df.x * c + df.y * -s;
                leg.nextPointOfContact.y = df.x * s + df.y *  c;
                leg.nextPointOfContact.add(body.pos);
            }

            Vector3d df = body.forward;
            target.forward.x = df.x * c + df.y * -s;
            target.forward.y = df.x * s + df.y *  c;
            target.forward.normalize();
            target.left.cross(body.up, target.forward);
        }

        // translate
        Vector3d dir = new Vector3d();
		dir.scaleAdd(walkDirection,body.forward,dir);
		dir.scaleAdd(strafeDirection,body.left,dir);
        dir.z = 0;
        if(dir.lengthSquared() > 0.001f) {
            dir.normalize();
			dir.scale(strideLength*dt);
			for( SpideeLeg leg : legs) {
				leg.nextPointOfContact.x += dir.x;
				leg.nextPointOfContact.y += dir.y;
				leg.nextPointOfContact.z = 0;
			}

			dir.scale(moveBodyScale / 6.0f);
			body.pos.add(dir);
			body.pos.z = standingHeight;
        }
    }


    void calculateAllNextPointOfContact(Vector3d destination, float dt, float moveBodyScale) {
        Vector3d dp = new Vector3d(destination);
        dp.sub(body.pos);
        double turnDirection = dp.dot(body.left);
        double walkDirection = dp.dot(body.forward);
        double strafeDirection = dp.dot(body.left);

		for( SpideeLeg leg : legs) {
            Vector3d ds = new Vector3d(leg.shoulderPan.pos);
            ds.sub(body.pos);
            ds.normalize();
            ds.scale(standingRadius);
            leg.nextPointOfContact.set(body.pos);
            leg.nextPointOfContact.add(ds);
            leg.nextPointOfContact.z = 0;
        }

        // turn
        if (turnDirection != 0) {
            turnDirection = Math.max(Math.min(turnDirection, 180 * dt), -180 * dt);
            double turn = Math.toRadians(turnDirection * turnStrideLength) * dt * moveBodyScale / 6.0f;

            double c = Math.cos(turn);
            double s = Math.sin(turn);

			for( SpideeLeg leg : legs) {
                Vector3d df = new Vector3d(leg.nextPointOfContact);
                df.sub(body.pos);
                df.z = 0;
                leg.nextPointOfContact.x = df.x * c + df.y * -s;
                leg.nextPointOfContact.y = df.x * s + df.y * c;
                leg.nextPointOfContact.add(body.pos);
            }

            Vector3d df = new Vector3d(body.forward);
            df.z = 0;
            target.forward.x = df.x * c + df.y * -s;
            target.forward.y = df.x * s + df.y * c;
            target.forward.normalize();
            target.left.cross(body.up, target.forward);
        }

        // translate
        Vector3d dir = new Vector3d(0, 0, 0);

        if (walkDirection > 0) dir.add(body.forward);  // forward
        if (walkDirection < 0) dir.sub(body.forward);  // backward
        if (strafeDirection > 0) dir.add(body.left);  // strafe left
        if (strafeDirection < 0) dir.sub(body.left);  // strafe right

        dir.z = 0;
        dir.normalize();
        Vector3d p = new Vector3d(0, 0, 0);
        float zi = 0;

		for( SpideeLeg leg : legs) {
            Vector3d t = new Vector3d(dir);
            t.scale(strideLength * dt);
            leg.nextPointOfContact.add(t);
            leg.nextPointOfContact.z = 0;

            Vector3d ptemp = new Vector3d(leg.ankle.pos);
            if (leg.onGround) ++zi;
            else ptemp.z = 0;
            p.add(ptemp);
        }

        //body.pos += dir * ( strideLength*dt * moveBodyScale / 6.0f );
        double z = p.z;
        p.scale(1.0f / 6.0f);
        if (zi > 0) p.z = z / zi;

        Vector3d t = new Vector3d(body.up);
        t.scale(standingHeight);
        body.pos.set(p);
        body.pos.add(t);
    }

	/**
	 * Snap feet to the floor.
	 */
    void plantFeet() {
		for( SpideeLeg leg : legs) {
            leg.ankle.pos.z = 0;
        }
    }


	/**
	 * Raise and lower the foot of a given leg over time.
	 * @param legIndex index of leg to update
	 * @param step 0 to 1, 0 is start of step, 1 is end of step
	 */
    void updateOneLegGait(int legIndex, double step) {
        SpideeLeg leg = legs[legIndex];

        double stepAdj = (step <= 0.5f) ? step : 1 - step;
        stepAdj = Math.sin(stepAdj * Math.PI);

		// horizontal distance from foot to next point of contact
        Vector3d dp = new Vector3d(leg.nextPointOfContact);
        dp.sub(leg.lastPointOfContact);
        dp.z = 0;
		dp.normalize();
        dp.scale(step);
        // add in the height of the step
        leg.ankle.pos.add(dp);
        leg.ankle.pos.z = stepAdj * strideHeight;
    }

	/**
	 * Move one leg at a time.
	 * @param dt time since last update
	 */
    void rippleGait(double dt) {
        gaitCycleTime += dt;

        updateGaitTarget(dt, 1.0f / 6.0f);

        double step = (gaitCycleTime - Math.floor(gaitCycleTime));
        int legToMove = ((int) Math.floor(gaitCycleTime) % 6);

        for(int i = 0; i < 6; ++i) {
            if (i != legToMove) {
                legs[i].ankle.pos.z = 0;
                continue;
            }
            updateOneLegGait(i, step);
        }
    }


	/**
	 * Move two legs at a time.  one is rising while one is falling.
	 * @param dt time since last update
	 */
    void waveGait(double dt) {
        gaitCycleTime += dt;

        updateGaitTarget(dt, 2.0f / 6.0f);

        double gc1 = gaitCycleTime + 0.5f;
        double gc2 = gaitCycleTime;

        double x1 = gc1 - Math.floor(gc1);
        double x2 = gc2 - Math.floor(gc2);
        double step1 = Math.max(0, x1);
        double step2 = Math.max(0, x2);
        int leg1 = (int) Math.floor(gc1) % 3;
        int leg2 = (int) Math.floor(gc2) % 3;

        // Put all feet down except the "active" leg(s).
        int i;
        for (i = 0; i < 6; ++i) {
            if (i != leg1 && i != leg2) {
                legs[i].ankle.pos.z = 0;
            }
        }

        // 0   5
        // 1 x 4
        // 2   3
        // order should be 0,3,1,5,2,4
        int o1, o2;
		o1 = switch (leg1) {
			case 0 -> 0;
			case 1 -> 1;
			case 2 -> 2;
			default -> 0;
		};
		o2 = switch (leg2) {
			case 0 -> 3;
			case 1 -> 5;
			case 2 -> 4;
			default -> 0;
		};

        updateOneLegGait(o1, step1);
        updateOneLegGait(o2, step2);
    }


	/**
	 * Move three legs at a time.
	 * @param dt time since last update
	 */
    void tripodGait(double dt) {
        gaitCycleTime += dt;

        updateGaitTarget(dt, 0.5f);

        double step = (gaitCycleTime - Math.floor(gaitCycleTime));
        int legToMove = ((int) Math.floor(gaitCycleTime) % 2);

        // put all feet down except the active leg(s).
        for (int i = 0; i < 6; ++i) {
            if ((i % 2) != legToMove) {
                legs[i].ankle.pos.z = 0;
            } else {
				updateOneLegGait(i, step);
			}
        }
    }


	/**
	 * Apply gravity, then keep the feet out of the floor. (above z=0)
	 * TODO: rewrite this code when the feet get pressure sensors.
	 * @param dt time step.
	 */
	void applyPhysics(double dt) {
		for(SpideeLeg leg : legs) {
            if (leg.shoulderPan.pos.z <= 0) leg.shoulderPan.pos.z = 0;
            if (leg.knee.pos.z <= 0) leg.knee.pos.z = 0;
            if (leg.ankle.pos.z <= 0) {
                leg.ankle.pos.z = 0;
                leg.lastPointOfContact = leg.ankle.pos;
                leg.onGround = true;
            } else {
                leg.onGround = false;
            }
        }
    }


    void applyConstraints(double dt) {
        int i;
        float scale = 0.5f;

        // adjust body orientation
        body.forward.set(target.forward);
        body.up.set(target.up);
		body.left.set(target.left);

        for (i = 0; i < 6; ++i) {
            SpideeLeg leg = legs[i];
            // keep shoulders locked in relative position
            leg.shoulderPan.pos.set(body.pos);
            Vector3d F = new Vector3d(body.forward);
            Vector3d L = new Vector3d(body.left);
            Vector3d U = new Vector3d(body.up);
            F.scale(leg.shoulderPan.relative.y);
            L.scale(leg.shoulderPan.relative.x);
            U.scale(leg.shoulderPan.relative.z);
            leg.shoulderPan.pos.add(F);
            leg.shoulderPan.pos.sub(L);
            leg.shoulderPan.pos.add(U);

            // make sure feet can not come under the body or get too far from the shoulder
            Vector3d ds = new Vector3d(leg.shoulderPan.pos);
            ds.sub(body.pos);
            Vector3d df = new Vector3d(leg.ankle.pos);
            df.sub(body.pos);
            double dfl = (df.length());
            double dsl = (ds.length());

            ds.z = 0;
            ds.normalize();
            if (dfl < dsl) {
                ds.scale(dsl - dfl);
                leg.ankle.pos.add(ds);
            } else if (dfl - dsl > maxLegLength) {
                // @TODO: should this test should be ankle - pan > maxLegLength ?
                ds.scale(dfl - dsl - maxLegLength);
                leg.ankle.pos.sub(ds);
            }

            // calculate the pan joint matrix
            leg.shoulderPan.up.set(body.up);
            leg.shoulderPan.forward.set(leg.ankle.pos);
            leg.shoulderPan.forward.sub(leg.shoulderPan.pos);

            df.set(body.up);
            df.scale(leg.shoulderPan.forward.dot(body.up));
            leg.shoulderPan.forward.sub(df);

            if (leg.shoulderPan.forward.length() < 0.01f) {
                leg.shoulderPan.forward.set(leg.shoulderTilt.pos);
                leg.shoulderPan.forward.sub(leg.shoulderPan.pos);
            }
            leg.shoulderPan.forward.normalize();
            leg.shoulderPan.left.cross(leg.shoulderPan.up, leg.shoulderPan.forward);

            // zero the distance between the pan joint and the tilt joint
            df.set(leg.shoulderPan.forward);
            df.scale(leg.shoulderTilt.relative.length());
            leg.shoulderTilt.pos.set(leg.shoulderPan.pos);
            leg.shoulderTilt.pos.add(df);

            // zero the knee/foot distance
            Vector3d a = new Vector3d(leg.knee.pos);
            a.sub(leg.ankle.pos);
            double kf = a.length() - leg.ankle.relative.length();
            if (Math.abs(kf) > 0.001) {
                a.normalize();
                a.scale(kf * scale);
                leg.knee.pos.sub(a);
            }

            // validate the tilt/knee plane
            a.set(leg.knee.pos);
            a.sub(leg.shoulderPan.pos);
            df.set(leg.shoulderPan.left);
            df.scale(a.dot(leg.shoulderPan.left));
            leg.knee.pos.sub(df);
            leg.knee.left.set(leg.shoulderPan.left);

            // zero the tilt/knee distance
            a.set(leg.knee.pos);
            a.sub(leg.shoulderTilt.pos);
            double kt = a.length() - leg.knee.relative.length();
            if (Math.abs(kt) > 0.001) {
                a.normalize();
                a.scale(kt);
                // don't push back on the tilt joint, it makes the simulation too unstable.
                leg.knee.pos.sub(a);
            }


            // calculate the tilt joint matrix
            leg.shoulderTilt.left.set(leg.shoulderPan.left);
            a.set(leg.knee.pos);
            a.sub(leg.shoulderTilt.pos);
            Vector3d b = new Vector3d();
            b.cross(a, leg.shoulderTilt.left);
            leg.shoulderTilt.forward.set(a);
            leg.shoulderTilt.forward.sub(b);
            leg.shoulderTilt.forward.normalize();
            leg.shoulderTilt.up.cross(leg.shoulderTilt.forward, leg.shoulderTilt.left);

            // calculate the knee matrix
            leg.knee.forward.set(leg.ankle.pos);
            leg.knee.forward.sub(leg.knee.pos);
            leg.knee.forward.normalize();
            leg.knee.up.cross(leg.knee.forward, leg.knee.left);
            //leg.kneeJoint.forward = leg.kneeJoint.left ^ leg.kneeJoint.up;

            // calculate the ankle matrix
            leg.ankle.forward.set(leg.knee.forward);
            leg.ankle.left.set(leg.knee.left);
            leg.ankle.up.set(leg.knee.up);
        }
    }


	/**
	 * Use the dimensions of the simulation to calculate the angles of the leg joints.
	 */
    void calculateJointAngles() {
        int i, j;
        double x, y;
        for (i = 0; i < 6; ++i) {
            SpideeLeg leg = legs[i];

            // find the pan angle
            Vector3d sf = new Vector3d(leg.shoulderPan.pos);
            sf.sub(body.pos);
            sf.normalize();
            Vector3d sl = new Vector3d();
            sl.cross(body.up, sf);

            x = leg.shoulderPan.forward.dot(sf);
            y = leg.shoulderPan.forward.dot(sl);
            double panAngle = Math.atan2(y, x);

            // find the tilt angle
            x = leg.shoulderTilt.forward.dot(leg.shoulderPan.forward);
            y = leg.shoulderTilt.forward.dot(leg.shoulderPan.up);
            double tiltAngle = Math.atan2(y, x);

            // find the knee angle
            x = leg.knee.forward.dot(leg.shoulderTilt.forward);
            y = leg.knee.forward.dot(leg.shoulderTilt.up);
            double kneeAngle = Math.atan2(y, x);

            // translate the angles into the servo range, 0...255 over 0...PI.
            final double scale = (255.0f / Math.PI);
            if (i < 3) panAngle = -panAngle;
            double p = leg.shoulderPan.zero - panAngle * leg.shoulderPan.scale * scale;
            double t = leg.shoulderTilt.zero + tiltAngle * leg.shoulderTilt.scale * scale;
            double k = leg.knee.zero - kneeAngle * leg.knee.scale * scale;
            leg.shoulderPan.angle = p;
            leg.shoulderTilt.angle = t;
            leg.knee.angle = k;

            // record the history for the graphs
            for (j = 0; j < SpideeJoint.ANGLE_HISTORY_LENGTH - 1; ++j) {
                leg.shoulderPan.angleHistory[j] = leg.shoulderPan.angleHistory[j + 1];
                leg.shoulderTilt.angleHistory[j] = leg.shoulderTilt.angleHistory[j + 1];
                leg.knee.angleHistory[j] = leg.knee.angleHistory[j + 1];
            }
            //memcpy( leg.shoulderPan .angleHistory, leg.shoulderPan .angleHistory + sizeof(float), ( Joint.ANGLE_HISTORY_LENGTH - 1 ) * sizeof(float) );
            //memcpy( leg.tiltJoint.angleHistory, leg.tiltJoint.angleHistory + sizeof(float), ( Joint.ANGLE_HISTORY_LENGTH - 1 ) * sizeof(float) );
            //memcpy( leg.kneeJoint.angleHistory, leg.kneeJoint.angleHistory + sizeof(float), ( Joint.ANGLE_HISTORY_LENGTH - 1 ) * sizeof(float) );
            leg.shoulderPan.angleHistory[SpideeJoint.ANGLE_HISTORY_LENGTH - 1] = p - leg.shoulderPan.zero;
            leg.shoulderTilt.angleHistory[SpideeJoint.ANGLE_HISTORY_LENGTH - 1] = t - leg.shoulderTilt.zero;
            leg.knee.angleHistory[SpideeJoint.ANGLE_HISTORY_LENGTH - 1] = k - leg.knee.zero;

            // @TODO: contrain angles in the model to the limits set in joint::angleMax and joint::angleMin
        }
    }


    /**
	 * post to a URL and get a new robot UID.
	 */
    private long getNewRobotUID() {
        long newUid = 0;

        try {
            // Send data
            URL url = new URL("https://marginallyclever.com/evilMinionGetuid.php");
            URLConnection conn = url.openConnection();
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line = rd.readLine();
                newUid = Long.parseLong(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        // did read go ok?
        if (newUid != 0) {
            // make sure a topLevelMachinesPreferenceNode node is created
            // tell the robot it's new UID.
            this.sendCommand("UID " + newUid);
        }
        return newUid;
    }

    // override this method to check that the software is connected to the right type of robot.
    public void dataAvailable(SessionLayer arg0, String line) {
        if (line.contains(hello)) {
            isPortConfirmed = true;
            //finalizeMove();
            this.sendCommand("R1");

            String uidString = line.substring(hello.length()).trim();
            Log.message(">>> UID=" + uidString);
            try {
                long uid = Long.parseLong(uidString);
                if (uid == 0) {
                    robotUID = getNewRobotUID();
                } else {
                    robotUID = uid;
                }
                // TODO set UID
                //arm5Panel.setUID(robotUID);
            } catch (Exception e) {
                e.printStackTrace();
            }

            setName("Evil Minion #" + robotUID);
        }
    }

    @Override
    public Memento createKeyframe() {
        return new SpideeMemento();
    }
}

