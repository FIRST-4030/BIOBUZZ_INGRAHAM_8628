package org.firstinspires.ftc.teamcode.BehaviorSystem.Demos;

import com.pedropathing.follower.Follower;
import com.pedropathing.paths.PathChain;
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

@TeleOp(name="Behavior System Driving Demo", group="Demos")
public class DrivingDemo extends OpMode {
    ControlHub controlHub;
    Chassis chassis;

    StateMachine mainStateMachine;
    State gamepadDrivingState, followPathState;

    IMU imu;
    Follower follower;
    PedroUtility pedroUtility;

    PathChain fromBottomLeftToBottomRight,
            fromBottomRightToTopRight,
            fromTopRightToTopLeft,
            fromTopLeftToBottomLeft;

    @Override
    public void init() {
        controlHub = new ControlHub();
        chassis = new Chassis(hardwareMap);

        imu = hardwareMap.get(IMU.class, "imu");
        follower = controlHub.createFollower(hardwareMap);
        pedroUtility = new PedroUtility(follower);

        imu.resetYaw();

        // PathChains
        fromBottomLeftToBottomRight = pedroUtility.fromTo(
                UserPoses.bottomLeft,
                UserPoses.bottomRight
        );
        fromBottomRightToTopRight = pedroUtility.fromTo(
                UserPoses.bottomRight,
                UserPoses.topRight
        );
        fromTopRightToTopLeft = pedroUtility.fromTo(
                UserPoses.topRight,
                UserPoses.topLeft
        );
        fromTopLeftToBottomLeft = pedroUtility.fromTo(
                UserPoses.topLeft,
                UserPoses.bottomLeft
        );

        // --- MAIN STATE MACHINE ---

        mainStateMachine = new StateMachine("Main");

        gamepadDrivingState = new BaseState(
                new GamepadDrive(chassis, gamepad1),
                () -> {
                    if (gamepad1.b) return followPathState;
                    return gamepadDrivingState;
                },
                () -> "[Press B: follow path]"
        );

        followPathState = new InterruptableTaskState(
                GroupBuilder.create()
                        .sequential("Follow paths in a square")
                            .add(new FollowPath(follower, fromBottomLeftToBottomRight))
                            .add(new FollowPath(follower, fromBottomRightToTopRight))
                            .add(new FollowPath(follower, fromTopRightToTopLeft))
                            .add(new FollowPath(follower, fromTopLeftToBottomLeft))
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
        controlHub.processBotIdentificationTelemetry(telemetry);
        Blackboard.initLoopProcess(telemetry, gamepad1);
        telemetry.update();
    }

    @Override
    public void start() {
        mainStateMachine.setInitialState(gamepadDrivingState);
        mainStateMachine.enter();
    }

    @Override
    public void loop() {
        telemetry.addData("Heading", follower.getHeading());

        mainStateMachine.update();
        mainStateMachine.processTelemetry(telemetry, "");

        telemetry.update();
    }
}
