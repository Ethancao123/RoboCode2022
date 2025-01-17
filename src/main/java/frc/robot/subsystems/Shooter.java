package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.sensors.SensorVelocityMeasPeriod;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotMap;
import frc.robot.Units;
import frc.robot.util.SimpleVelocitySystem;
import harkerrobolib.wrappers.HSFalcon;

/**
 * Specifies a two-motor shooter with a linear quadratic regulator and a kalman filter
 */
public class Shooter extends SubsystemBase {
    private static Shooter shooter;

    private static final boolean MASTER_INVERTED = true;
    private static final boolean FOLLOWER_INVERTED = false;
    
    
    public static final double kS = 0.70457;    
    public static final double kV = 0.33337;
    public static final double kA = 0.060663;
    
    private static final double MAX_CONTROL_EFFORT = 10; // volts 
    private static final double MODEL_STANDARD_DEVIATION = 3;
    private static final double ENCODER_STANDARD_DEVIATION = 0.1;

    public static final double SHOOTER_REV_TIME = 1.0;

    private SimpleVelocitySystem velocitySystem;
    
    private HSFalcon master;
    private HSFalcon follower;
    
    private Shooter() {
        master = new HSFalcon(RobotMap.SHOOTER_MASTER);
        follower = new HSFalcon(RobotMap.SHOOTER_FOLLOWER);
    
        initMotors();
        velocitySystem = new SimpleVelocitySystem(kS, kV, kA, MAX_CONTROL_EFFORT, MODEL_STANDARD_DEVIATION, ENCODER_STANDARD_DEVIATION, RobotMap.LOOP_TIME);
    }

    public void initMotors() {
        follower.follow(master);

        master.setInverted(MASTER_INVERTED);
        follower.setInverted(FOLLOWER_INVERTED);

        master.configVelocityMeasurementWindow(1);
        master.configVelocityMeasurementPeriod(SensorVelocityMeasPeriod.Period_10Ms);

        master.configVoltageCompSaturation(MAX_CONTROL_EFFORT);
        follower.configVoltageCompSaturation(MAX_CONTROL_EFFORT);
    }

    public void setPercentOutput(double speed) {
        master.set(ControlMode.PercentOutput, speed);
    }

    // m/s, raw encoder value
    public double getRawVelocity() {
        return Shooter.getInstance().getMaster().getSelectedSensorVelocity() * 10 / Units.TICKS_PER_REVOLUTION * Units.FLYWHEEL_ROT_TO_METER;
    }

    public SimpleVelocitySystem getVelocitySystem() {
        return velocitySystem;
    }

    public void setVelocity(double vel){
        velocitySystem.set(vel);
        velocitySystem.update(getRawVelocity());
        // setPercentOutput(velocitySystem.getOutput());
        setPercentOutput(vel);
    }

    public HSFalcon getMaster() {
        return master;
    }

    public static Shooter getInstance() {
        if (shooter == null) {
            shooter = new Shooter();
        }
        return shooter;
    }
}
