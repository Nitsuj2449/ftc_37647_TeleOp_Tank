// Le package FTC

package org.firstinspires.ftc.teamcode;

// imports requis 

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;


@TeleOp(name = "TeleOp Tank", group = "TeleOp")
public class TankDriveTeleOp extends LinearOpMode {

    private DcMotor right_Motor;
    private DcMotor left_Motor;
    private DcMotor intake;
    private DcMotorEx launcher;
    private CRServo servo_droit;
    private CRServo servo_gauche;
    private CRServo helice;
    private Servo rgb;
    private IMU imu;

    @Override
        public void runOpMode() throws InterruptedException {

        right_Motor = hardwareMap.get(DcMotor.class, "Right Motor");
        left_Motor = hardwareMap.get(DcMotor.class, "Left Motor");
        launcher = hardwareMap.get(DcMotorEx.class, "Launcher");
        intake = hardwareMap.get(DcMotor.class, "Intake");
        servo_droit = hardwareMap.get(CRServo.class, "Servo Droit");
        servo_gauche = hardwareMap.get(CRServo.class, "Servo Gauche");
        helice = hardwareMap.get(CRServo.class, "Helice");
        rgb = hardwareMap.get(Servo.class, "rgb");
        imu = hardwareMap.get(IMU.class, "imu");


        right_Motor.setDirection(DcMotorSimple.Direction.REVERSE);// À vérifier
        left_Motor.setDirection(DcMotorSimple.Direction.FORWARD);// À vérifier
        intake.setDirection(DcMotorSimple.Direction.REVERSE);// À vérifier

        // PIDF Coefficients
        launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        PIDFCoefficients CoefficientsTest = new PIDFCoefficients(40.0, 0.0, 0.0, 12.5);
        launcher.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, CoefficientsTest);

        /* Je veux utiliser l'IMU pour voir sur la Driver Station
        quand admettons le yaw parfait pour tirer est 90 et bien
        on regarde sur la Driver Station pour se placer à 90. 
        */
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
        RevHubOrientationOnRobot.LogoFacingDirection.RIGHT, // À ajuster selon la config
        RevHubOrientationOnRobot.UsbFacingDirection.FORWARD)); // Également à ajuster
        imu.initialize(parameters);

        waitForStart();

        if (isStopRequested()) return;

        servo_droit.setPower(0);
        servo_gauche.setPower(0);
        intake.setPower(0);
        left_Motor.setPower(0);
        right_Motor.setPower(0);
        launcher.setVelocity(0);

        while (opModeIsActive()) {
            double y = -gamepad1.left_stick_y;
            double x = gamepad1.right_stick_x;


            double denominator = Math.max(Math.abs(y) + Math.abs(x), 1);
            double left_Power = (y + x) / denominator;
            double right_Power = (y - x) / denominator;

            left_Motor.setPower(left_Power);
            right_Motor.setPower(right_Power);

            // TEST Launcher
            int velocity = 0;
            if (gamepad2.right_trigger > 0.1) {
                velocity = 2400;
                launcher.setVelocity(velocity); // 2400 ticks = environ 5140 RPM
            } else {
                velocity = 0;
                launcher.setVelocity(velocity);
            }

            // RGB

            String rgbCurrentColor = "Inconnu";
            if (velocity == 0) {
                rgb.setPosition(0.27);
                rgbCurrentColor = "Rouge";
            } else if (launcher.getVelocity() >= (velocity - 50)) {
                rgb.setPosition(0.45);
                rgbCurrentColor = "Vert";
            } else {
                rgb.setPosition(0.27);
                rgbCurrentColor = "Rouge";
            }


            double botheading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
            telemetry.addData("Vitesse Cible launcher", velocity);
            telemetry.addData("Vitesse Reelle launcher", launcher.getVelocity());
            telemetry.addData("Power moteur gauche", left_Power);
            telemetry.addData("Power moteur droit", right_Power);
            telemetry.addData("Yaw", botheading);
            telemetry.addData("RGB Color", rgbCurrentColor);
            telemetry.update();

            // Intake
            if (gamepad1.right_trigger > 0.1) {
                intake.setPower(1);
            } else if (gamepad1.left_trigger > 0.1) {
                intake.setPower(-0.5);
            } else {
                intake.setPower(0);
            } 

            // Servo côtés
            if (gamepad2.right_bumper) {
                servo_droit.setPower(1);
                servo_gauche.setPower(1);
            } else if (gamepad2.left_bumper) {
                servo_droit.setPower(-1);
                servo_gauche.setPower(-1);
            } else {
                servo_droit.setPower(0);
                servo_gauche.setPower(0);
            }

            // Hélice
            if (gamepad2.a) {
                helice.setPower(1);
            } else if (gamepad2.b) { 
                helice.setPower(-1); // Si jamais une balle est bloquée on peut l'enlever avec l'hélice
            } else {
                helice.setPower(0);
            }
        }
    }
}