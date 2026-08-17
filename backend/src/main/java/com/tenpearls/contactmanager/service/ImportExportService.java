package com.tenpearls.contactmanager.service;

import com.tenpearls.contactmanager.model.Contact;
import com.tenpearls.contactmanager.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import com.opencsv.exceptions.CsvException;

/**
 * Service interface for importing and exporting contacts
 * in CSV and vCard (.vcf) formats.
 */
public interface ImportExportService {

    /**
     * Imports contacts from a CSV file and saves them for the specified user.
     *
     * @param file the uploaded CSV file
     * @param user the authenticated user who will own the imported contacts
     * @return a list of the newly created Contact entities
     * @throws IOException if the file cannot be read
     * @throws CsvException if the CSV parsing fails
     */
    List<Contact> importCsv(MultipartFile file, User user) throws IOException, CsvException;

    /**
     * Exports a list of contacts to CSV format.
     *
     * @param contacts the contacts to export
     * @return the CSV file content as a byte array
     * @throws IOException if writing fails
     */
    byte[] exportCsv(List<Contact> contacts) throws IOException;

    /**
     * Imports contacts from a vCard (.vcf) file and saves them for the specified user.
     *
     * @param file the uploaded vCard file
     * @param user the authenticated user who will own the imported contacts
     * @return a list of the newly created Contact entities
     * @throws IOException if parsing or persistence fails
     */
    List<Contact> importVcf(MultipartFile file, User user) throws IOException;

    /**
     * Exports a list of contacts to vCard (.vcf) format.
     *
     * @param contacts the contacts to export
     * @return the vCard file content as a byte array
     * @throws IOException if writing fails
     */
    byte[] exportVcf(List<Contact> contacts) throws IOException;
}
