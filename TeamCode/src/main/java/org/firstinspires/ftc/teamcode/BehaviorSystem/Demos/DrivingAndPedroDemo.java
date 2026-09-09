package org.firstinspires.ftc.teamcode.BehaviorSystem.Demos;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.teamcode.BehaviorSystem.GroupBuilder;
import org.firstinspires.ftc.teamcode.BehaviorSystem.StateMachine.BaseState;
import org.firstinspires.ftc.teamcode.BehaviorSystem.StateMachine.InterruptableTaskState;
import org.firstinspires.ftc.teamcode.BehaviorSystem.StateMachine.State;
import org.firstinspires.ftc.teamcode.BehaviorSystem.StateMachine.StateMachine;
import org.firstinspires.ftc.teamcode.BehaviorSystem.UserBehaviors.FollowPath;
import org.firstinspires.ftc.teamcode.BehaviorSystem.UserBehaviors.GamepadDrive;
import org.firstinspires.ftc.teamcode.Blackboard;
import org.firstinspires.ftc.teamcode.Chassis;
import org.firstinspires.ftc.teamcode.ControlHub;
import org.firstinspires.ftc.teamcode.Pedro.PedroUtility;
import org.firstinspires.ftc.teamcode.Pedro.UserPoses;

/**
 * OpMode to demonstrate basic driving and pedroPathing capabilities using the
 * BehaviorSystem.
 * @author Edson James
 */
@TeleOp(name="Behavior System Driving Demo", group="Demos")
public class DrivingAndPedroDemo extends OpMode {

    // General setup

    ControlHub controlHub;
    Chassis chassis;

    StateMachine mainStateMachine; // This state machine controls everything
    State gamepadDrivingState, followPathState; // These are the states the robot will be in

    // Variables we'll need for pedroPathing:

    IMU imu;
    Follower follower;
    PedroUtility pedroUtility; // Used for more easily creating pathChains

    // These are all the paths pedroPathing will use in this OpMode.
    // They are declared as entire FollowPath behaviors so they can be more easily
    // dropped into a sequence (see followPathState's definition).
    FollowPath goBottomLeftToBottomRight,
            goBottomRightToTopRight,
            goTopRightToTopLeft,
            goTopLeftToBottomLeft;

    @Override
    public void init() {

        // General setup

        controlHub = new ControlHub();
        chassis = new Chassis(hardwareMap);

        // Pedro setup

        imu = hardwareMap.get(IMU.class, "imu");
        follower = controlHub.createFollower(hardwareMap);
        pedroUtility = new PedroUtility(follower);

        imu.resetYaw();

        // Define all of our paths using the pedroUtility

        goBottomLeftToBottomRight = new FollowPath(
                follower,
                // UserPoses.java is where every pedro pose for the season should be saved
                pedroUtility.fromTo(UserPoses.bottomLeft, UserPoses.bottomRight),
                "Go from bottom left to bottom right"
        );
        goBottomRightToTopRight = new FollowPath(
                follower,
                pedroUtility.fromTo(UserPoses.bottomLeft, UserPoses.bottomRight),
                "Go from bottom right to top right"
        );
        goTopRightToTopLeft = new FollowPath(
                follower,
                pedroUtility.fromTo(UserPoses.bottomLeft, UserPoses.bottomRight),
                "Go from top right to top left"
        );
        goTopLeftToBottomLeft = new FollowPath(
                follower,
                pedroUtility.fromTo(UserPoses.bottomLeft, UserPoses.bottomRight),
                "Go from top left to bottom left"
        );

        // Set up the state machine

        mainStateMachine = new StateMachine("Main"); // Give the state machine a label

        // Defining our first state: driving
        gamepadDrivingState = new BaseState(
                new GamepadDrive(chassis, gamepad1),
                () -> {
                    if (gamepad1.b) return followPathState;
                    return gamepadDrivingState;
                },
                () -> "[Press B: follow path]"
        );

        // Defining our second state: following some paths with pedropathing
        followPathState = new InterruptableTaskState(
                GroupBuilder.create()
                        .sequential("Follow paths in a square")
                            .add(goBottomLeftToBottomRight)
                            .add(goBottomRightToTopRight)
                            .add(goTopRightToTopLeft)
                            .add(goTopLeftToBottomLeft)
                        .end()
                        .build(),
                () -> gamepadDrivingState,
                () -> {
                    if (gamepad1.left_bumper) return gamepadDrivingState;
                    return followPathState;
                },
                () -> "[Left bumper: cancel]"
        );
    }

    @Override
    public void init_loop() {
        // In here we're just printing some information about which robot is being used
        controlHub.processBotIdentificationTelemetry(telemetry);
        // ...and this lets us set the alliance, etc. to the Blackboard
        Blackboard.initLoopProcess(telemetry, gamepad1);
        telemetry.update();
    }

    @Override
    public void start() {
        // Start our state machine!
        mainStateMachine.setInitialState(gamepadDrivingState);
        mainStateMachine.enter();
    }

    @Override
    public void loop() {
        // Let the state machine do its thing!
        mainStateMachine.update();
        mainStateMachine.processTelemetry(telemetry, "");

        telemetry.update();
    }
}
