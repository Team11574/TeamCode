package org.firstinspires.ftc.teamcode;

/*

Created by FTC Team 11574 on 1/28/2017.

This is the autonomous program which aims to find and press the beacons to the alliance
color. It uses encoder-based distance driving, range sensor, tape-finding color sensor,
and beacon color sensor to do so.

*/

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

// The program name to show on the driver station phone.
@Autonomous(name="Drive and Push Beacons", group="Autonomous")
@SuppressWarnings("unused")
public class Drive_and_Push_Beacons extends Mecanum_Wheels_Generic {
    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize the robot; this comes from Mecanum_Wheels_Generic.
        robotInit();

        while(!isStarted() && !isStopRequested()) {
            // Send some basic sensor data telemetry for confirmation and testing.
            telemetry.addData("1. alliance", COLOR_NAMES[check_alliance()]);
            telemetry.addData("2. range", range.cmUltrasonic());
            telemetry.addData("3. tape_alpha", Tape_color.alpha());
            telemetry.addData("4. beacon_blue", Beacon_color.blue());
            telemetry.addData("5. beacon_red", Beacon_color.red());
            telemetry.update();
        }

        // Exit immediately if stop was pressed, otherwise continue.
        if (!isStarted() || isStopRequested())
            return;

        // Read the alliance color from the alliance switch.
        int color_alliance = check_alliance();

        // Set up strafe_away and strafe_back variables to use the alliance color to decide which
        // way we will need to strafe (left or right). We can then use these consistently in the
        // rest of the code without worrying about which alliance we're on.
        int strafe_away, strafe_back;
        if (color_alliance == COLOR_RED) {
            strafe_away = STRAFE_RIGHT;
            strafe_back = STRAFE_LEFT;
        } else if(color_alliance == COLOR_BLUE) {
            strafe_away = STRAFE_LEFT;
            strafe_back = STRAFE_RIGHT;
        } else {
            // There's nothing we can do here.
            return;
        }

        /*
            Calibrate the Tape_color sensor to the alpha value of the floor in the starting
            position, so that if the floor is a different reflectivity it won't matter. We're
            just looking for tape that is *more* reflective by TAPE_ALPHA_DIFFERENCE than the
            calibrated value of the floor at the starting position.
         */
        double floor_alpha = Tape_color.alpha();
        double tape_alpha = floor_alpha + TAPE_ALPHA_DIFFERENCE;

        /*
            The strategy to align to the first beacon here is:
              1. Strafe away from the wall towards the middle of the arena, but remain clear
                 of the cap ball.
              2. Drive forwards until we're within range of the color sensor being able to
                 find the tape line while strafing.
              3. Strafe towards the tape line until we find it.
              4. Drive forwards quickly and then slowly towards the wall until within range of
                 the button pushing code to push the button.
         */
        drive_distance(strafe_away, 34.0, 1.0);
        drive_distance(DRIVE_FORWARD, 24.0, 1.0);
        drive_until_gt_alpha(strafe_away, tape_alpha, 28.0, 0.6);
        drive_until_lt_range(DRIVE_FORWARD, 10.0, 24.0, 0.6);
        drive_until_lt_range(DRIVE_FORWARD, 5.0, 10.0, 0.2);

        // Check the colors of each side of the beacon and then push the appropriate one.
        check_beacons_and_push_button(color_alliance, strafe_away, strafe_back);

        // Back up a bit to avoid hitting anything while driving quickly to the next beacon.
        drive_until_gt_range(DRIVE_BACKWARD, 10.0, 10.0, 0.6);

        /*
            The strategy to align to the second beacon here is:
              1. Strafe towards the second beacon quickly around 70% of the total distance.
              2. Strafe towards the tape line until we find it.
              3. Drive forwards quickly and then slowly as above.
         */
        drive_distance(strafe_away, 36.0, 1.0);
        drive_until_gt_alpha(strafe_away, tape_alpha, 24.0, 0.6);
        drive_until_lt_range(DRIVE_FORWARD, 10.0, 24.0, 0.6);
        drive_until_lt_range(DRIVE_FORWARD, 5.0, 10.0, 0.2);

        // Check the colors of each side of the beacon and then push the appropriate one.
        check_beacons_and_push_button(color_alliance, strafe_away, strafe_back);

        // Back up a bit to avoid hitting anything while driving quickly cap ball.
        drive_until_gt_range(DRIVE_BACKWARD, 10.0, 10.0, 0.6);

        // Strafe back to the first beacon's tape line, then back up to bump the cap ball and
        // park on the center.
        drive_distance(strafe_back, 36.0, 1.0);
        drive_until_gt_alpha(strafe_back, tape_alpha, 24.0, 0.6);
        drive_distance(DRIVE_BACKWARD, 36.0, 1.0);

        idle();
    }
}


