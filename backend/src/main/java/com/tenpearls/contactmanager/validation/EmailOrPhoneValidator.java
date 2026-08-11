package com.tenpearls.contactmanager.validation;

import com.tenpearls.contactmanager.dto.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

/**
 * Validator logic to check if at least one of email or phone is provided.
 */
public class EmailOrPhoneValidator implements ConstraintValidator<EmailOrPhoneRequired, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        boolean hasEmail = StringUtils.hasText(request.getEmail());
        boolean hasPhone = StringUtils.hasText(request.getPhone());
        
        return hasEmail || hasPhone;
    }
}
