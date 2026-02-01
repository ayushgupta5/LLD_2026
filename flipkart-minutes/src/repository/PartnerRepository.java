package src.repository;


import src.models.DeliveryPartner;
import src.enums.PartnerStatus;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

public class PartnerRepository extends InMemoryRepository<DeliveryPartner> {

    @Override
    protected String getEntityId(DeliveryPartner entity) {
        return String.valueOf(entity.getPartnerId());
    }

    // Custom query methods
    public List<DeliveryPartner> findByStatus(PartnerStatus status) {
        return findAll().stream()
                .filter(partner -> partner.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<DeliveryPartner> findAvailablePartners() {
        return findByStatus(PartnerStatus.AVAILABLE);
    }

    public DeliveryPartner findByPhone(String phone) {
        return findAll().stream()
                .filter(partner -> phone.equals(partner.getPhone()))
                .findFirst()
                .orElse(null);
    }

    public DeliveryPartner findByVehicleNumber(String vehicleNumber) {
        return findAll().stream()
                .filter(partner -> vehicleNumber.equals(partner.getVehicleNumber()))
                .findFirst()
                .orElse(null);
    }

    // Get top performers
    public List<DeliveryPartner> getTopPartnersByDeliveries(int limit) {
        return findAll().stream()
                .sorted(Comparator.comparingInt(DeliveryPartner::getTotalDeliveries).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<DeliveryPartner> getTopPartnersByRating(int limit) {
        return findAll().stream()
                .filter(partner -> partner.getRatingCount() > 0)
                .sorted(Comparator.comparingDouble(DeliveryPartner::getAverageRating).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Dashboard data
    public List<DeliveryPartner> getTopPartners(int limit) {
        return findAll().stream()
                .sorted((p1, p2) -> {
                    // Sort by deliveries first, then by rating
                    int deliveryCompare = Integer.compare(
                            p2.getTotalDeliveries(), p1.getTotalDeliveries());
                    if (deliveryCompare != 0) return deliveryCompare;
                    return Double.compare(p2.getAverageRating(), p1.getAverageRating());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
}