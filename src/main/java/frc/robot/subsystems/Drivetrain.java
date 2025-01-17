package frc.robot.subsystems;

import com.ctre.phoenix.sensors.Pigeon2;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.ctre.phoenix.sensors.WPI_Pigeon2;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotMap;
import frc.robot.util.SwerveModule;

import harkerrobolib.wrappers.HSPigeon;

/**
 * Specifies a drivetrain with 4 swerve modules
 */
public class Drivetrain extends SubsystemBase {
    private static Drivetrain drivetrain;

    private SwerveModule[] modules;

    private static final boolean[] ROTATION_SENSOR_PHASE = {false, false, false, false};
    private static final boolean[] TRANSLATION_SENSOR_PHASE = {false, false, false, false};
    private static final boolean[] ROTATION_INVERT = {false, false, false, false};
    private static final boolean[] TRANSLATION_INVERT = {false, false, false, false};

    public static final double[] OFFSETS = {263.935547, 109.951172, 178.330078, 32.255859};

    public static final double DT_WIDTH = 0.5461; // 0.93345 bumper to bumper
    public static final double DT_LENGTH = 0.5969; // 0.88265

    public static final double VOLTAGE_COMP = 10;

    public static final double TRANSLATION_GEAR_RATIO = 6.75;
    public static final double ROTATION_GEAR_RATIO = 12.8;//12.75;//12.8; // 12.8 rotations of motor = 1 rotation of wheel
    public static final double WHEEL_DIAMETER = 4; //inches
    public static final double FEET_TO_METER = 0.3048;

    public static final double MIN_OUTPUT = 0.01;
    public static final double MAX_DRIVE_VEL = 3; // theoretical 4.1148 m / s
    public static final double MAX_ANGULAR_VEL = 2*Math.PI; // 

    public static final double MP_X_KP = 8;
    public static final double MP_X_KI = 0;
    public static final double MP_X_KD = 0;

    public static final double MP_Y_KP = 8;
    public static final double MP_Y_KI = 0;
    public static final double MP_Y_KD = 0;

    public static final double MP_THETA_KP = 4;
    public static final double MP_THETA_KI = 0;
    public static final double MP_THETA_KD = 7;

    public static final double MP_MAX_DRIVE_VELOCITY = 3;
    public static final double MP_MAX_DRIVE_ACCELERATION = 2;

    private boolean fieldCentric = true;

    private WPI_Pigeon2 pigeon;

    private SwerveDriveKinematics kinematics;
    private SwerveDriveOdometry odometry;

    private Drivetrain() {
        modules = new SwerveModule[4];
        for(int i = 0; i < 4; i++){
            modules[i] = new SwerveModule(ROTATION_SENSOR_PHASE[i], TRANSLATION_SENSOR_PHASE[i], 
                RobotMap.ROTATION_IDS[i], RobotMap.ROTATION_CANCODER_IDS[i], RobotMap.TRANSLATION_IDS[i], 
                ROTATION_INVERT[i], TRANSLATION_INVERT[i]);
        }
        
        pigeon = new WPI_Pigeon2(RobotMap.PIGEON_ID);
        pigeon.zeroGyroBiasNow();
        // pigeon.setFusedHeading(0);//-RobotMap.PIGEON_CONSTANT * pigeon.getAngle());
        
        kinematics = new SwerveDriveKinematics(new Translation2d(DT_LENGTH / 2, DT_WIDTH / 2), new Translation2d(DT_LENGTH / 2, -DT_WIDTH / 2), 
        new Translation2d(-DT_LENGTH / 2, DT_WIDTH / 2), new Translation2d(-DT_LENGTH / 2, -DT_WIDTH / 2));
        odometry = new SwerveDriveOdometry(kinematics, Rotation2d.fromDegrees(pigeon.getAngle()), new Pose2d(0, 0, new Rotation2d()));
    }

    /**
     * Set angle to given angle and set velocity to given velocity
     */
    public void setAngleAndDriveVelocity(SwerveModuleState[] states, boolean isPercentOutput){
        for(int i = 0; i < 4; i++){
            SmartDashboard.putNumber("Desired angle module " + i, states[i].angle.getDegrees());
            modules[i].setSwerveManual(states[i], isPercentOutput);
        }
    }

    public void setAngleAndDriveVelocity(SwerveModuleState[] states) {
        setAngleAndDriveVelocity(states, false);
    }

    public void toggleFieldCentric() {
        fieldCentric = fieldCentric == false;
    }

    public boolean isFieldCentric() {
        return fieldCentric;
    }

    public WPI_Pigeon2 getPigeon(){
        return pigeon;
    }

    public SwerveDriveOdometry getOdometry() {
        return odometry;
    }

    public SwerveDriveKinematics getKinematics(){
        return kinematics;
    }

    public SwerveModule getTopLeft() {
        return modules[0];
    }

    public SwerveModule getTopRight() {
        return modules[1];
    }

    public SwerveModule getBottomLeft() {
        return modules[2];
    }

    public SwerveModule getBottomRight() {
        return modules[3];
    }

    public static Drivetrain getInstance() {
        if (drivetrain == null) {
            drivetrain = new Drivetrain();
        }
        return drivetrain;
    }

    /**
     * we need to know where 0 is after turning off the robot --> set encoder position for rotation gear from
     * Can Coder (absolute encoder) to offset
     */
    public void readCANCoders() {
        for(int i = 0; i < 4; i++){
            modules[i].getRotationMotor().setSelectedSensorPosition(
                (modules[i].getCanCoder().getAbsolutePosition() - OFFSETS[i]) * SwerveModule.ENCODER_TICKS / 360 * ROTATION_GEAR_RATIO);
        }
    }
}
