package com.phorest.booking.infrastructure.controller;

import com.phorest.booking.application.dto.ImportSummary;
import com.phorest.booking.application.service.AppointmentImportService;
import com.phorest.booking.application.service.PurchaseImportService;
import com.phorest.booking.application.service.ServiceImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingImportController.class)
@Import(BookingErrorHandler.class)
class BookingImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentImportService appointmentImportService;

    @MockBean
    private ServiceImportService serviceImportService;

    @MockBean
    private PurchaseImportService purchaseImportService;

    @Test
    void importsAppointments() throws Exception {
        when(appointmentImportService.importAppointments(any())).thenReturn(new ImportSummary(1, 1, 0, 0, 0));
        MockMultipartFile file = new MockMultipartFile("file", "appointments.csv", MediaType.TEXT_PLAIN_VALUE, "id".getBytes());

        mockMvc.perform(multipart("/api/v1/import/appointments").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(1));

        verify(appointmentImportService).importAppointments(any());
    }
}
