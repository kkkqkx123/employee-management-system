package com.example.demo.communication.announcement.service;

import com.example.demo.common.exception.ValidationException;
import com.example.demo.communication.announcement.dto.AnnouncementCreateRequest;
import com.example.demo.communication.announcement.entity.AnnouncementTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AnnouncementValidationServiceTest {

    @InjectMocks
    private AnnouncementValidationService announcementValidationService;

    @Test
    void validateCreateRequest_withValidRequest_shouldNotThrowException() {
        AnnouncementCreateRequest request = new AnnouncementCreateRequest();
        request.setTitle("Valid Title");
        request.setContent("Valid Content");
        request.setTargetAudience(AnnouncementTarget.ALL);

        assertDoesNotThrow(() -> announcementValidationService.validateCreateRequest(request));
    }

    @Test
    void validateCreateRequest_withDepartmentTargetAndNullDepartmentId_shouldThrowException() {
        AnnouncementCreateRequest request = new AnnouncementCreateRequest();
        request.setTitle("Test Title");
        request.setContent("Test Content");
        request.setTargetAudience(AnnouncementTarget.DEPARTMENT);
        request.setDepartmentId(null);

        assertThrows(ValidationException.class, () -> announcementValidationService.validateCreateRequest(request));
    }

    @Test
    void validateCreateRequest_withRoleTargetAndNullRoleName_shouldThrowException() {
        AnnouncementCreateRequest request = new AnnouncementCreateRequest();
        request.setTitle("Test Title");
        request.setContent("Test Content");
        request.setTargetAudience(AnnouncementTarget.ROLE);
        request.setRoleName(null);

        assertThrows(ValidationException.class, () -> announcementValidationService.validateCreateRequest(request));
    }
}