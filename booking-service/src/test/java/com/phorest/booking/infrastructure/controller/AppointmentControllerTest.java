package com.phorest.booking.infrastructure.controller;

import com.phorest.booking.application.dto.AppointmentResponse;
import com.phorest.booking.application.service.AppointmentCommandService;
import com.phorest.booking.application.service.AppointmentReadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BookingErrorHandler.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentReadService readService;

    @MockBean
    private AppointmentCommandService commandService;

    @Test
    void listsAppointments() throws Exception {
        when(readService.getAppointments(any())).thenReturn(new PageImpl<>(List.of(
                new AppointmentResponse("app-1", "client-1", Instant.parse("2024-01-01T10:00:00Z"), Instant.parse("2024-01-01T11:00:00Z"))
        ), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("app-1"));
    }

    @Test
    void updatesAppointment() throws Exception {
        when(commandService.updateAppointment(any(), any())).thenReturn(Optional.of(
                new AppointmentResponse("app-1", "client-1", Instant.parse("2024-01-01T10:00:00Z"), Instant.parse("2024-01-01T11:00:00Z"))
        ));

        mockMvc.perform(put("/api/v1/appointments/app-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "client-1",
                                  "startTime": "2024-01-01T10:00:00Z",
                                  "endTime": "2024-01-01T11:00:00Z"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("app-1"));
    }

    @Test
    void deletesAppointment() throws Exception {
        when(commandService.deleteAppointment("app-1")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/appointments/app-1"))
                .andExpect(status().isNoContent());
    }
}
