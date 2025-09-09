package com.fkhrayef.motor.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;

@Service
public class S3Service {
    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    // upload from Postman
    public void uploadFile(MultipartFile file) throws IOException {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(file.getOriginalFilename()) // TODO: make it unique (id)
                        .build(),
                RequestBody.fromBytes(file.getBytes()));
    }

    // upload catalog file with structured naming
    public String uploadCatalogFile(MultipartFile file, String catalogPath) throws IOException {
        String key = "catalogs/" + catalogPath;
        
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));
        
        return generateS3Url(key);
    }

    // generate S3 URL for a given key
    public String generateS3Url(String key) {
        return String.format("https://%s.s3.eu-central-1.amazonaws.com/%s", bucketName, key);
    }

    // check if catalog file exists
    public boolean catalogFileExists(String catalogPath) {
        try {
            s3Client.headObject(software.amazon.awssdk.services.s3.model.HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key("catalogs/" + catalogPath)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // upload license file with unique naming using user ID and phone
    public String uploadLicenseFile(MultipartFile file, String userId, String phone) throws IOException {
        // Generate unique filename: user-{userId}-{phone}-license.pdf
        String fileName = String.format("user-%s-%s-license.pdf", userId, phone);
        String key = "licenses/" + fileName;
        
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));
        
        return generateS3Url(key);
    }

    // upload car registration file with unique naming using car ID and make-model
    public String uploadRegistrationFile(MultipartFile file, String carId, String make, String model) throws IOException {
        // Generate unique filename: car-{carId}-{make}-{model}-registration.pdf
        String fileName = String.format("car-%s-%s-%s-registration.pdf", 
                carId, 
                make.toLowerCase().replace(" ", "-"), 
                model.toLowerCase().replace(" ", "-"));
        String key = "registrations/" + fileName;
        
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));
        
        return generateS3Url(key);
    }

    // upload car insurance file with unique naming using car ID and make-model
    public String uploadInsuranceFile(MultipartFile file, String carId, String make, String model) throws IOException {
        // Generate unique filename: car-{carId}-{make}-{model}-insurance.pdf
        String fileName = String.format("car-%s-%s-%s-insurance.pdf", 
                carId, 
                make.toLowerCase().replace(" ", "-"), 
                model.toLowerCase().replace(" ", "-"));
        String key = "insurances/" + fileName;
        
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));
        
        return generateS3Url(key);
    }

    // upload maintenance invoice file with unique naming using maintenance ID and car info
    public String uploadMaintenanceInvoiceFile(MultipartFile file, String maintenanceId, String carMake, String carModel) throws IOException {
        // Generate unique filename: maintenance-{maintenanceId}-{carMake}-{carModel}-invoice.pdf
        String fileName = String.format("maintenance-%s-%s-%s-invoice.pdf", 
                maintenanceId, 
                carMake.toLowerCase().replace(" ", "-"), 
                carModel.toLowerCase().replace(" ", "-"));
        String key = "maintenance-invoices/" + fileName;
        
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));
        
        return generateS3Url(key);
    }

    // delete file from S3
    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    public byte[] downloadFile(String key) {
        ResponseBytes<GetObjectResponse> objectAsBytes =
                s3Client.getObjectAsBytes(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build());
        return objectAsBytes.asByteArray();
    }

    // upload from Byte data (if we wanted to do it in code but most likely we'll not need it)
    public void uploadByte(String key, byte[] data, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(data)
        );
    }
}
