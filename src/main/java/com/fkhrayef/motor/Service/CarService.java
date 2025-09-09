package com.fkhrayef.motor.Service;

import com.fkhrayef.motor.Api.ApiException;
import com.fkhrayef.motor.DTOin.CarDTO;
import com.fkhrayef.motor.Model.Car;
import com.fkhrayef.motor.Model.Subscription;
import com.fkhrayef.motor.Model.User;
import com.fkhrayef.motor.Repository.CarRepository;
import com.fkhrayef.motor.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarService {
    private void ensureAccessible(Car car) {
        if (car != null && Boolean.FALSE.equals(car.getIsAccessible())) {
            throw new ApiException("This car is not accessible on your current plan.");
        }
    }

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public void addCar(Integer userId, CarDTO carDTO) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        // enforce subscription rules
        enforceCarLimit(user);

        validateCarMakeAndModel(carDTO);

        Car car = new Car();
        car.setMake(carDTO.getMake());
        car.setModel(carDTO.getModel());
        car.setYear(carDTO.getYear());
        car.setNickname(carDTO.getNickname());
        car.setMileage(carDTO.getMileage());
        car.setVin(carDTO.getVin());
        car.setPurchaseDate(carDTO.getPurchaseDate());
        car.setIsAccessible(true);
        car.setUser(user);


        carRepository.save(car);
    }

    public void updateCar(Integer userId, Integer id, CarDTO carDTO) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        Car car = carRepository.findCarById(id);
        if (car == null) {
            throw new ApiException("Car not found");
        }

        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        ensureAccessible(car);

        validateCarMakeAndModel(carDTO);

        car.setMake(carDTO.getMake());
        car.setModel(carDTO.getModel());
        car.setYear(carDTO.getYear());
        car.setNickname(carDTO.getNickname());
        car.setMileage(carDTO.getMileage());
        car.setVin(carDTO.getVin());
        car.setPurchaseDate(carDTO.getPurchaseDate());

        carRepository.save(car);
    }

    public void updateMileage(Integer userId, Integer carId, Integer newMileage) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        Car car = carRepository.findCarByIdAndUserId(carId, userId);
        if (car == null) {
            throw new ApiException("Car not found or does not belong to this user");
        }

        ensureAccessible(car);

        if (newMileage == null) {
            throw new ApiException("New mileage is required");
        }

        Integer current = car.getMileage();
        if (current != null && newMileage < current) {
            throw new ApiException("New mileage cannot be less than current mileage (" + current + ")");
        }

        car.setMileage(newMileage);
        carRepository.save(car);
    }

    public void deleteCar(Integer userId, Integer id) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        Car car = carRepository.findCarById(id);
        if (car == null) {
            throw new ApiException("Car not found");
        }

        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        // delete insurance if exists
        if (car.getInsuranceFileUrl() != null) {
            deleteInsurance(userId, id);
        }

        // delete registration if exists
        if (car.getRegistrationFileUrl() != null) {
            deleteRegistration(userId, id);
        }


        carRepository.delete(car);
    }

    public List<Car> getCarsByUserId(Integer userId){
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("UNAUTHENTICATED USER");
        }

        return carRepository.findCarsByUserId(user.getId());
    }

    // Registration file management
    public void uploadRegistration(Integer userId, Integer carId, MultipartFile file, LocalDate registrationExpiry) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        // Get car details from database
        Car car = carRepository.findCarById(carId);
        if (car == null) {
            throw new ApiException("Car not found with id: " + carId);
        }

        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        ensureAccessible(car);

        // Validate file presence
        if (file == null || file.isEmpty()) {
            throw new ApiException("Registration file is required");
        }
        // Validate file type
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new ApiException("Only PDF files are allowed for registration upload");
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ApiException("Registration file size cannot exceed 10MB");
        }

        // Validate registration expiry date
        if (registrationExpiry.isBefore(LocalDate.now())) {
            throw new ApiException("Registration expiry date must be in the future");
        }

        // Upload to S3 with unique naming
        String s3Url;
        try {
            s3Url = s3Service.uploadRegistrationFile(file, carId.toString(), car.getMake(), car.getModel());
        } catch (Exception e) {
            throw new ApiException("Failed to upload registration file: " + e.getMessage());
        }

        // Update car record with registration information
        car.setRegistrationFileUrl(s3Url);
        car.setRegistrationExpiry(registrationExpiry);
        carRepository.save(car);
    }

    public byte[] downloadRegistration(Integer userId, Integer carId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        // Get car details from database
        Car car = carRepository.findCarById(carId);
        if (car == null) {
            throw new ApiException("Car not found with id: " + carId);
        }

        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        if (car.getRegistrationFileUrl() == null) {
            throw new ApiException("No registration file found for this car");
        }

        // Extract the S3 key from the URL
        String s3Url = car.getRegistrationFileUrl();
        String key = s3Url.substring(s3Url.indexOf("/registrations/") + 1); // Extract "registrations/car-123-toyota-camry-registration.pdf"

        // Download file from S3
        return s3Service.downloadFile(key);
    }

    public void deleteRegistration(Integer userId, Integer carId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        // Get car details from database
        Car car = carRepository.findCarById(carId);
        if (car == null) {
            throw new ApiException("Car not found with id: " + carId);
        }

        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        if (car.getRegistrationFileUrl() == null) {
            throw new ApiException("No registration file found for this car");
        }

        // Extract the S3 key from the URL
        String s3Url = car.getRegistrationFileUrl();
        String key = s3Url.substring(s3Url.indexOf("/registrations/") + 1); // Extract "registrations/car-123-toyota-camry-registration.pdf"

        // Delete file from S3
        try {
            s3Service.deleteFile(key);
        } catch (Exception e) {
            throw new ApiException("Failed to delete registration file from S3: " + e.getMessage());
        }

        // Clear registration information from car record
        car.setRegistrationFileUrl(null);
        car.setRegistrationExpiry(null);
        carRepository.save(car);
    }

    // Insurance file management
    public void uploadInsurance(Integer userId, Integer carId, MultipartFile file, LocalDate insuranceEndDate) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        // Get car details from database
        Car car = carRepository.findCarById(carId);
        if (car == null) {
            throw new ApiException("Car not found with id: " + carId);
        }

        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        ensureAccessible(car);

        // Validate file type
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new ApiException("Only PDF files are allowed for insurance upload");
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ApiException("Insurance file size cannot exceed 10MB");
        }

        // Validate insurance end date
        if (insuranceEndDate.isBefore(LocalDate.now())) {
            throw new ApiException("Insurance end date must be in the future");
        }

        // Upload to S3 with unique naming
        String s3Url;
        try {
            s3Url = s3Service.uploadInsuranceFile(file, carId.toString(), car.getMake(), car.getModel());
        } catch (Exception e) {
            throw new ApiException("Failed to upload insurance file: " + e.getMessage());
        }

        // Update car record with insurance information
        car.setInsuranceFileUrl(s3Url);
        car.setInsuranceEndDate(insuranceEndDate);
        carRepository.save(car);
    }

    public byte[] downloadInsurance(Integer userId, Integer carId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        // Get car details from database
        Car car = carRepository.findCarById(carId);
        if (car == null) {
            throw new ApiException("Car not found with id: " + carId);
        }

        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        if (car.getInsuranceFileUrl() == null) {
            throw new ApiException("No insurance file found for this car");
        }

        // Extract the S3 key from the URL
        String s3Url = car.getInsuranceFileUrl();
        String key = s3Url.substring(s3Url.indexOf("/insurances/") + 1); // Extract "insurances/car-123-toyota-camry-insurance.pdf"

        // Download file from S3
        return s3Service.downloadFile(key);
    }

    public void deleteInsurance(Integer userId, Integer carId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        // Get car details from database
        Car car = carRepository.findCarById(carId);
        if (car == null) {
            throw new ApiException("Car not found with id: " + carId);
        }

        if (!car.getUser().getId().equals(userId)) {
            throw new ApiException("UNAUTHORIZED USER");
        }

        if (car.getInsuranceFileUrl() == null) {
            throw new ApiException("No insurance file found for this car");
        }

        // Extract the S3 key from the URL
        String s3Url = car.getInsuranceFileUrl();
        String key = s3Url.substring(s3Url.indexOf("/insurances/") + 1); // Extract "insurances/car-123-toyota-camry-insurance.pdf"

        // Delete file from S3
        try {
            s3Service.deleteFile(key);
        } catch (Exception e) {
            throw new ApiException("Failed to delete insurance file from S3: " + e.getMessage());
        }

        // Clear insurance information from car record
        car.setInsuranceFileUrl(null);
        car.setInsuranceEndDate(null);
        carRepository.save(car);
    }

    private static final Map<String, Set<String>> MAKE_MODELS = Map.ofEntries(
            Map.entry("Toyota", Set.of("Land Cruiser","Prado","Camry","Corolla","Hilux","Yaris","Avalon")),
            Map.entry("Lexus", Set.of("LX570","ES350")),
            Map.entry("Hyundai", Set.of("Sonata","Elantra","Tucson","Palisade")),
            Map.entry("Nissan", Set.of("Altima","Patrol","X-Trail","Sentra")),
            Map.entry("Kia", Set.of("Sportage","Sorento")),
            Map.entry("Chevrolet", Set.of("Tahoe","Suburban","Silverado")),
            Map.entry("GMC", Set.of("Yukon","Sierra")),
            Map.entry("Ford", Set.of("F-150","Explorer")),
            Map.entry("Mazda", Set.of("CX-5")),
            Map.entry("Mitsubishi", Set.of("L200")),
            Map.entry("Isuzu", Set.of("D-Max"))
    );


    public void validateCarMakeAndModel(CarDTO carDTO) {
        String make = carDTO.getMake();
        String model = carDTO.getModel();

        if (!MAKE_MODELS.containsKey(make)) {
            throw new ApiException("Unsupported make: " + make);
        }
        if (!MAKE_MODELS.get(make).contains(model)) {
            throw new ApiException("Model " + model + " does not belong to " + make);
        }
    }

    public String getMaintenanceCostOneYear(Integer userId, String make, String model, Integer minMileage, Integer maxMileage) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        if (!MAKE_MODELS.containsKey(make) || !MAKE_MODELS.get(make).contains(model)) {
            throw new ApiException("Unsupported make/model");
        }

        if (minMileage != null && maxMileage != null && minMileage > maxMileage) {
                    throw new ApiException("minMileage cannot be greater than maxMileage");
        }

        LocalDate oneYearAgo = LocalDate.now().minusYears(1);

        List<Car> cars = carRepository.findByMakeAndModel(make, model).stream()
                .filter(c -> minMileage == null || (c.getMileage() != null && c.getMileage() >= minMileage))
                .filter(c -> maxMileage == null || (c.getMileage() != null && c.getMileage() <= maxMileage))
                .toList();


        List<Double> costs = new ArrayList<>();
        for (Car c : cars) {
            double spend = c.getMaintenances() == null ? 0.0 :
                    c.getMaintenances().stream()
                            .filter(m -> "MAINTENANCE".equalsIgnoreCase(m.getRecordType()))
                            .filter(m -> m.getServiceDate() != null && !m.getServiceDate().isBefore(oneYearAgo))
                            .mapToDouble(m -> m.getInvoiceAmount() == null ? 0.0 : m.getInvoiceAmount())
                            .sum();
            costs.add(spend);
        }

        if (costs.isEmpty()) return "Maintenance cost in last year: 0 SAR (0 cars)";

        double avg = costs.stream().mapToDouble(d -> d).average().orElse(0.0);
        long count = costs.size();

        return String.format("Maintenance cost in last year: %.2f SAR (based on %d cars)", avg, count);
    }

    public String getVisitFrequency(Integer userId, String make, String model, Integer minAge, Integer maxAge) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        if (!MAKE_MODELS.containsKey(make) || !MAKE_MODELS.get(make).contains(model)) {
            throw new ApiException("Unsupported make/model");
        }

        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusYears(1);

        List<Car> cars = carRepository.findAll().stream()
                .filter(c -> make.equals(c.getMake()) && model.equals(c.getModel()))
                .filter(c -> {
                    if (c.getPurchaseDate() == null) return true;
                    int age = today.getYear() - c.getPurchaseDate().getYear();
                    return (minAge == null || age >= minAge) && (maxAge == null || age <= maxAge);
                })
                .toList();

        List<Long> yearlyVisits = new ArrayList<>();
        for (Car c : cars) {
            if (c.getMaintenances() == null) continue;

            long visits = c.getMaintenances().stream()
                    .filter(m -> "MAINTENANCE".equalsIgnoreCase(m.getRecordType()))
                    .filter(m -> m.getServiceDate() != null && !m.getServiceDate().isBefore(oneYearAgo))
                    .count();

            yearlyVisits.add(visits);
        }

        if (yearlyVisits.isEmpty()) return "No data for this car";

        double avg = yearlyVisits.stream().mapToLong(v -> v).average().orElse(0);
        long count = yearlyVisits.size();

        if (avg == 0) {
            return String.format("On average, cars had no maintenance visits in the last year (based on %d cars)", count);
        }

        double yearsBetween = 1 / avg;
        return String.format("On average, cars are serviced once every ~%.0f years (based on %d cars)", yearsBetween, count);
    }

    public String getTypicalMileagePerYear(Integer userId, String make, String model, String city) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("UNAUTHENTICATED USER");
        }

        if (!MAKE_MODELS.containsKey(make) || !MAKE_MODELS.get(make).contains(model)) {
            throw new ApiException("Unsupported make/model");
        }

        LocalDate today = LocalDate.now();

        List<Car> cars = carRepository.findByMakeAndModel(make, model).stream()
                .filter(c -> city == null ||
                        (c.getUser() != null && c.getUser().getCity() != null && city.equalsIgnoreCase(c.getUser().getCity())))
                .toList();

        List<Double> mileagePerYear = new ArrayList<>();
        for (Car c : cars) {
            if (c.getMileage() == null || c.getPurchaseDate() == null) continue;

            long daysOwned = java.time.temporal.ChronoUnit.DAYS.between(c.getPurchaseDate(), today);
            double yearsOwned = Math.max(1.0, daysOwned / 365.25);

            mileagePerYear.add(c.getMileage() / yearsOwned);
        }

        if (mileagePerYear.isEmpty()) return "No mileage data for this car";

        double avg = mileagePerYear.stream().mapToDouble(d -> d).average().orElse(0);
        long count = mileagePerYear.size();

        return String.format("Typical mileage per year: %.0f km (based on %d cars)", avg, count);
    }

    public long getCarsNumbers(Integer userId){
        return carRepository.countByUserId(userId);
    }

    public void enforceCarLimit(User user) {
        long existing = getCarsNumbers(user.getId());
        Subscription sub = user.getSubscription();

        // Treat missing or non-active subscriptions as FREE
        if (sub == null || sub.getStatus() == null || !"active".equalsIgnoreCase(sub.getStatus())) {
            if (existing >= 1) throw new ApiException("Free plan allows only 1 car. Upgrade to Pro to add up to 5 cars.");
            return;
        }

        String plan = sub.getPlanType() == null ? "" : sub.getPlanType().toLowerCase();
        switch (plan) {
            case "pro":
                if (existing >= 5) throw new ApiException("Pro plan allows up to 5 cars. Upgrade to Enterprise to add more than 5 cars.");
                break;
            case "enterprise":
                // unlimited → no check
                break;
            default:
                if (existing >= 1) throw new ApiException("This plan allows only 1 car.");
        }
    }

    public void enforceCarAccess(Integer userId) {
        User u = userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found"));
        Subscription s = u.getSubscription();

        boolean active = s != null
                && "active".equalsIgnoreCase(s.getStatus())
                && (s.getEndDate() == null || s.getEndDate().isAfter(LocalDateTime.now()));

        int limit = Integer.MAX_VALUE;
        if (!active) limit = 1;
        else {
            String plan = s.getPlanType() == null ? "" : s.getPlanType().toLowerCase();
            if ("pro".equals(plan)) limit = 5;
            else if ("enterprise".equals(plan)) limit = Integer.MAX_VALUE;
            else limit = 1;
        }

        List<Car> cars = carRepository.findByUserIdOrderByCreatedAtAsc(userId);
        for (int i = 0; i < cars.size(); i++) cars.get(i).setIsAccessible(i < limit);
        carRepository.saveAll(cars);
    }

    //  @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Riyadh")
    @Scheduled(cron = "0 * * * * *") // Every minute (for testing)
    public void enforceAllUsersAccessPaged() {
        log.info("[Scheduler] Starting enforceAllUsersAccessPaged...");
        int page = 0;
        Page<User> slice;
        do {
            slice = userRepository.findAll(PageRequest.of(page, 500));
            log.info("[Scheduler] Enforcing access for {} users (page {})", slice.getNumberOfElements(), page);
            for (User u : slice) {
                enforceCarAccess(u.getId());
            }
            page++;
        } while (slice.hasNext());
        log.info("[Scheduler] Completed enforceAllUsersAccessPaged.");
    }



}
