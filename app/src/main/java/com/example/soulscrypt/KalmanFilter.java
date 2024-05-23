package com.example.soulscrypt;


public class KalmanFilter {
    // Time of the last measurement
    private long timeStamp = 0;

    // State vector [position, velocity]
    private double[] X = {0, 0};

    // Uncertainty covariance matrix
    private double[][] P = {{1, 0}, {0, 1}};

    // Process noise covariance matrix (adjust Q based on expected movement variability)
    private double[][] Q = {{0.000001, 0}, {0, 0.000001}}; // Adjusted Q matrix

    // Measurement noise covariance (adjust R based on GPS measurement noise)
    private double R = 0.001; // Adjusted R value

    // Kalman gain
    private double[][] K = new double[2][2];

    // Indicates if the filter is initialized with its first measurement
    private boolean isInitialized = false;

    public void processMeasurement(double measurement, double accuracy, long timeStamp) {
        // Convert accuracy to measurement noise
        R = accuracy * accuracy;

        // On the first measurement, initialize the state vector and timestamp
        if (!isInitialized) {
            this.timeStamp = timeStamp;
            X[0] = measurement;
            X[1] = 0; // Initial velocity
            P[0][0] = accuracy * accuracy;
            P[1][1] = 1;
            isInitialized = true;
            return;
        }

        // Time difference in seconds
        double dt = (timeStamp - this.timeStamp) / 1000.0;
        this.timeStamp = timeStamp;

        // State transition matrix
        double[][] F = {{1, dt}, {0, 1}};

        // Predict
        X[0] += dt * X[1];
        P[0][0] += dt * (2 * P[0][1] + dt * P[1][1]);
        P[0][1] += dt * P[1][1];
        P[1][1] += Q[1][1];

        // Measurement update
        double S = P[0][0] + R;
        K[0][0] = P[0][0] / S;
        K[0][1] = P[0][1] / S;
        K[1][0] = P[1][0] / S;
        K[1][1] = P[1][1] / S;

        double Y = measurement - X[0]; // Measurement residual
        X[0] += K[0][0] * Y;
        X[1] += K[1][0] * Y;

        double P00_temp = P[0][0];
        double P01_temp = P[0][1];

        P[0][0] -= K[0][0] * P00_temp;
        P[0][1] -= K[0][0] * P01_temp;
        P[1][0] -= K[1][0] * P00_temp;
        P[1][1] -= K[1][0] * P01_temp;
    }

    // Returns the current position
    public double getPosition() {
        return X[0];
    }

    // Returns the current velocity
    public double getVelocity() {
        return X[1];
    }
}
