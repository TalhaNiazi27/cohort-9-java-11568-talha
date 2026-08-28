package com.tenpearls.contactmanager.controller;

import com.tenpearls.contactmanager.model.Contact;
import com.tenpearls.contactmanager.model.User;
import com.tenpearls.contactmanager.repository.ContactRepository;
import com.tenpearls.contactmanager.repository.UserRepository;
import com.tenpearls.contactmanager.service.ImportExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.opencsv.exceptions.CsvException;
import java.util.List;

/**
 * Controller handling import and export of contacts in CSV and vCard formats.
 */
@RestController
@RequestMapping("/api/contacts")
@Slf4j
public class ImportExportController {

    private final ImportExportService importExportService;
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;

    /**
     * Constructs ImportExportController with required dependencies.
     *
     * @param importExportService service for parsing and generating import/export files
     * @param userRepository      repository for user lookups
     * @param contactRepository   repository for contact queries
     */
    public ImportExportController(ImportExportService importExportService, UserRepository userRepository, ContactRepository contactRepository) {
        this.importExportService = importExportService;
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
    }

    /**
     * Imports contacts from a CSV or vCard file uploaded by the authenticated user.
     * The file format is auto-detected from the file extension.
     *
     * @param file           the uploaded file (must be .csv or .vcf)
     * @param authentication the Spring Security authentication context
     * @return a success message with the number of imported contacts
     */
    @PostMapping("/import")
    public ResponseEntity<?> importContacts(@RequestParam("file") MultipartFile file, Authentication authentication) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            User user = fetchAuthenticatedUser(authentication);

            String filename = file.getOriginalFilename();
            List<Contact> imported;
            if (filename != null && filename.toLowerCase().endsWith(".vcf")) {
                imported = importExportService.importVcf(file, user);
            } else if (filename != null && filename.toLowerCase().endsWith(".csv")) {
                imported = importExportService.importCsv(file, user);
            } else {
                return ResponseEntity.badRequest().body("Unsupported file format. Please upload a .csv or .vcf file.");
            }
            
            log.info("Successfully imported {} contacts for user: {}", imported.size(), authentication.getName());
            return ResponseEntity.ok().body("Successfully imported " + imported.size() + " contacts.");
        } catch (IOException | CsvException e) {
            log.warn("Rejected malformed import file", e);
            return ResponseEntity.badRequest().body("The file could not be parsed. Check the file format.");
        } catch (RuntimeException e) {
            log.error("Failed to import contacts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to import contacts.");
        }
    }

    /**
     * Exports all contacts for the authenticated user as a CSV file download.
     *
     * @param authentication the Spring Security authentication context
     * @return a CSV file as a byte array with appropriate download headers
     */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(Authentication authentication) {
        try {
            User user = fetchAuthenticatedUser(authentication);
            List<Contact> contacts = contactRepository.findAllByUser(user);
            byte[] csvBytes = importExportService.exportCsv(contacts);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDisposition(ContentDisposition.attachment().filename("contacts.csv").build());
            headers.setCacheControl("private, no-store");

            log.info("Exported {} contacts to CSV for user: {}", contacts.size(), authentication.getName());
            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to export CSV: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Exports all contacts for the authenticated user as a vCard (.vcf) file download.
     *
     * @param authentication the Spring Security authentication context
     * @return a vCard file as a byte array with appropriate download headers
     */
    @GetMapping("/export/vcf")
    public ResponseEntity<byte[]> exportVcf(Authentication authentication) {
        try {
            User user = fetchAuthenticatedUser(authentication);
            List<Contact> contacts = contactRepository.findAllByUser(user);
            byte[] vcfBytes = importExportService.exportVcf(contacts);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/vcard"));
            headers.setContentDisposition(ContentDisposition.attachment().filename("contacts.vcf").build());
            headers.setCacheControl("private, no-store");

            log.info("Exported {} contacts to vCard for user: {}", contacts.size(), authentication.getName());
            return new ResponseEntity<>(vcfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to export vCard: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Fetches the authenticated User entity from the database.
     *
     * @param authentication the Spring Security authentication context
     * @return the User entity
     * @throws UsernameNotFoundException if the user is not found
     */
    private User fetchAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmailOrPhone(authentication.getName(), authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found in database"));
    }
}
