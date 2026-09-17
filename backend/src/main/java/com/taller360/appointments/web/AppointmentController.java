package com.taller360.appointments.web;

import com.taller360.appointments.dto.AppointmentDtos.*;
import com.taller360.appointments.service.AppointmentService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
  private final AppointmentService appointments;

  public AppointmentController(AppointmentService appointments) {
    this.appointments = appointments;
  }

  @GetMapping
  @PreAuthorize("@perm.has(authentication, 'CITAS:VER')")
  CalendarResponse calendar(@RequestParam Instant start, @RequestParam Instant end,
      @RequestParam(required = false) UUID branchId, @RequestParam(defaultValue = "SEMANA") String view) {
    return appointments.calendar(start, end, branchId, view);
  }

  @GetMapping("/capacity")
  @PreAuthorize("@perm.has(authentication, 'CITAS:VER')")
  CapacityResponse capacity(@RequestParam UUID branchId, @RequestParam Instant start, @RequestParam Instant end) {
    return appointments.capacity(branchId, start, end);
  }

  @PostMapping
  @PreAuthorize("@perm.has(authentication, 'CITAS:CREAR')")
  AppointmentResponse create(@RequestBody AppointmentRequest request) {
    return appointments.create(request);
  }

  @PutMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'CITAS:EDITAR')")
  AppointmentResponse update(@PathVariable UUID id, @RequestBody AppointmentRequest request) {
    return appointments.update(id, request);
  }

  @PatchMapping("/{id}/state")
  @PreAuthorize("@perm.has(authentication, 'CITAS:EDITAR')")
  AppointmentResponse changeState(@PathVariable UUID id, @RequestParam String state) {
    return appointments.changeState(id, state);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'CITAS:ELIMINAR')")
  ResponseEntity<Void> cancel(@PathVariable UUID id) {
    appointments.cancel(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/reminders")
  @PreAuthorize("@perm.has(authentication, 'CITAS:VER')")
  List<AppointmentReminderResponse> reminders(@PathVariable UUID id) {
    return appointments.reminders(id);
  }

  @PostMapping("/{id}/reminders")
  @PreAuthorize("@perm.has(authentication, 'CITAS:EDITAR')")
  AppointmentReminderResponse addReminder(@PathVariable UUID id, @RequestBody AppointmentReminderRequest request) {
    return appointments.addReminder(id, request);
  }

  @PostMapping("/{id}/convert-to-reception")
  @PreAuthorize("@perm.has(authentication, 'CITAS:APROBAR')")
  ReceptionConversionResponse convertToReception(@PathVariable UUID id) {
    return appointments.convertToReception(id);
  }

  @GetMapping("/technicians")
  @PreAuthorize("@perm.has(authentication, 'CITAS:VER')")
  List<TechnicianResponse> technicians() {
    return appointments.technicians();
  }

  @GetMapping("/bays")
  @PreAuthorize("@perm.has(authentication, 'CITAS:VER')")
  List<ServiceBayResponse> bays(@RequestParam UUID branchId) {
    return appointments.bays(branchId);
  }

  @GetMapping("/capacity-rules")
  @PreAuthorize("@perm.has(authentication, 'CITAS:VER')")
  List<ScheduleCapacityResponse> capacityRules(@RequestParam UUID branchId) {
    return appointments.capacityRules(branchId);
  }
}
