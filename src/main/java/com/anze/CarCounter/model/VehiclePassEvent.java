package com.anze.CarCounter.model;
import java.util.Objects;

public class VehiclePassEvent {
    
    private Long tollStationId;
    private String vehicleId;
    private VehicleBrand vehicleBrand;
    private Long timestamp; // Unix timestamp in milliseconds

    public enum VehicleBrand {
        VOLKSWAGEN,
        BMW,
        NISSAN,
        TOYOTA,
        FORD,
        HONDA,
        BYD,
        TESLA,
        HYUNDAI,
        OTHER
    }

    public VehiclePassEvent() {}

    public VehiclePassEvent(Long tollStationId, String vehicleId, 
                        VehicleBrand vehicleBrand, Long timestamp) {
        this.tollStationId = tollStationId;
        this.vehicleId = vehicleId;
        this.vehicleBrand = vehicleBrand;
        this.timestamp = timestamp;
    }

    public Long getTollStationId() {
        return tollStationId;
    }

    public void setTollStationId(Long tollStationId) {
        this.tollStationId = tollStationId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public VehicleBrand getVehicleBrand() {
        return vehicleBrand;
    }

    public void setVehicleBrand(VehicleBrand vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tollStationId, vehicleId, vehicleBrand, timestamp);
    }

    @Override
    public String toString() {
        return "VehiclePassEvent{" +
                "tollStationId=" + tollStationId +
                ", vehicleId='" + vehicleId + '\'' +
                ", vehicleBrand=" + vehicleBrand +
                ", timestamp=" + timestamp +
                '}';
    }
}