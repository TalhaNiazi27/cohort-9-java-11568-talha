package com.tenpearls.contactmanager.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.tenpearls.contactmanager.model.Contact;
import com.tenpearls.contactmanager.model.Email;
import com.tenpearls.contactmanager.model.Phone;
import com.tenpearls.contactmanager.model.User;
import com.tenpearls.contactmanager.repository.ContactRepository;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.StructuredName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for importing and exporting contacts
 * in CSV and vCard (.vcf) formats using OpenCSV and ez-vcard libraries.
 */
@Service
@Slf4j
public class ImportExportServiceImpl implements ImportExportService {

    private final ContactRepository contactRepository;

    /**
     * Constructs ImportExportServiceImpl with the required ContactRepository.
     *
     * @param contactRepository repository for persisting imported contacts
     */
    public ImportExportServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Contact> importCsv(MultipartFile file, User user) throws Exception {
        List<Contact> importedContacts = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {
             
            List<String[]> records = csvReader.readAll();
            boolean isFirst = true;
            for (String[] record : records) {
                if (isFirst) { // Skip header row
                    isFirst = false;
                    continue;
                }
                
                if (record.length < 2) {
                    continue; // At least first name and last name required
                }

                Contact contact = Contact.builder()
                        .firstName(record[0].trim())
                        .lastName(record[1].trim())
                        .title(record.length > 2 ? record[2].trim() : "")
                        .user(user)
                        .build();

                // Add Email if present
                if (record.length > 3 && record[3] != null && !record[3].trim().isEmpty()) {
                    Email email = Email.builder()
                            .emailAddress(record[3].trim())
                            .label("Work")
                            .contact(contact)
                            .build();
                    contact.getEmails().add(email);
                }

                // Add Phone if present
                if (record.length > 4 && record[4] != null && !record[4].trim().isEmpty()) {
                    Phone phone = Phone.builder()
                            .phoneNumber(record[4].trim())
                            .label("Mobile")
                            .contact(contact)
                            .build();
                    contact.getPhones().add(phone);
                }

                importedContacts.add(contact);
            }
        }

        log.info("Importing {} contacts from CSV for user ID: {}", importedContacts.size(), user.getId());
        return contactRepository.saveAll(importedContacts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] exportCsv(List<Contact> contacts) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(baos);
             CSVWriter csvWriter = new CSVWriter(writer)) {
             
            String[] header = {"FirstName", "LastName", "Title", "Email", "Phone"};
            csvWriter.writeNext(header);

            for (Contact contact : contacts) {
                String email = contact.getEmails().isEmpty() ? "" : contact.getEmails().get(0).getEmailAddress();
                String phone = contact.getPhones().isEmpty() ? "" : contact.getPhones().get(0).getPhoneNumber();
                String[] record = {
                        contact.getFirstName(),
                        contact.getLastName(),
                        contact.getTitle() != null ? contact.getTitle() : "",
                        email,
                        phone
                };
                csvWriter.writeNext(record);
            }
        }

        log.info("Exported {} contacts to CSV", contacts.size());
        return baos.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Contact> importVcf(MultipartFile file, User user) throws Exception {
        List<Contact> importedContacts = new ArrayList<>();
        List<VCard> vcards = Ezvcard.parse(file.getInputStream()).all();

        for (VCard vcard : vcards) {
            String firstName = "";
            String lastName = "";
            
            StructuredName structuredName = vcard.getStructuredName();
            if (structuredName != null) {
                if (structuredName.getGiven() != null) {
                    firstName = structuredName.getGiven();
                }
                if (structuredName.getFamily() != null) {
                    lastName = structuredName.getFamily();
                }
            } else if (vcard.getFormattedName() != null) {
                String[] parts = vcard.getFormattedName().getValue().split(" ", 2);
                firstName = parts[0];
                if (parts.length > 1) {
                    lastName = parts[1];
                }
            }
            
            if (firstName.isEmpty() && lastName.isEmpty()) {
                continue; // Skip contacts with no name
            }

            String title = "";
            if (!vcard.getTitles().isEmpty()) {
                title = vcard.getTitles().get(0).getValue();
            }

            Contact contact = Contact.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .title(title)
                    .user(user)
                    .build();

            for (ezvcard.property.Email vcardEmail : vcard.getEmails()) {
                Email email = Email.builder()
                        .emailAddress(vcardEmail.getValue())
                        .label(vcardEmail.getTypes().isEmpty() ? "Other" : vcardEmail.getTypes().get(0).getValue())
                        .contact(contact)
                        .build();
                contact.getEmails().add(email);
            }

            for (ezvcard.property.Telephone vcardPhone : vcard.getTelephoneNumbers()) {
                Phone phone = Phone.builder()
                        .phoneNumber(vcardPhone.getText())
                        .label(vcardPhone.getTypes().isEmpty() ? "Other" : vcardPhone.getTypes().get(0).getValue())
                        .contact(contact)
                        .build();
                contact.getPhones().add(phone);
            }

            importedContacts.add(contact);
        }

        log.info("Importing {} contacts from vCard for user ID: {}", importedContacts.size(), user.getId());
        return contactRepository.saveAll(importedContacts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] exportVcf(List<Contact> contacts) throws Exception {
        List<VCard> vcards = new ArrayList<>();
        for (Contact contact : contacts) {
            VCard vcard = new VCard();
            
            StructuredName structuredName = new StructuredName();
            structuredName.setGiven(contact.getFirstName());
            structuredName.setFamily(contact.getLastName());
            vcard.setStructuredName(structuredName);
            vcard.setFormattedName(contact.getFirstName() + " " + contact.getLastName());
            
            if (contact.getTitle() != null && !contact.getTitle().isEmpty()) {
                vcard.addTitle(contact.getTitle());
            }

            for (Email email : contact.getEmails()) {
                vcard.addEmail(email.getEmailAddress());
            }

            for (Phone phone : contact.getPhones()) {
                vcard.addTelephoneNumber(phone.getPhoneNumber());
            }
            
            vcards.add(vcard);
        }
        
        log.info("Exported {} contacts to vCard", contacts.size());
        return Ezvcard.write(vcards).version(VCardVersion.V4_0).go().getBytes();
    }
}
