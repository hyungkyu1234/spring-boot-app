package io.hyungkyu.app.modules.event.application;

import io.hyungkyu.app.modules.account.domain.entity.Account;
import io.hyungkyu.app.modules.event.domain.entity.Enrollment;
import io.hyungkyu.app.modules.event.domain.entity.Event;
import io.hyungkyu.app.modules.event.endpoint.form.EventForm;
import io.hyungkyu.app.modules.event.infra.repository.EnrollmentRepository;
import io.hyungkyu.app.modules.event.infra.repository.EventRepository;
import io.hyungkyu.app.modules.study.domain.entity.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;

    public Event createEvent(Study study, EventForm eventForm, Account account) {
        Event event = Event.from(eventForm, account, study);
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        event.updateFrom(eventForm);
        event.acceptWaitingList();
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }

    public void enroll(Event event, Account account) {
        if (!enrollmentRepository.existsByEventAndAccount(event, account)) {
            Enrollment enrollment = Enrollment.of(LocalDateTime.now(), event.isAbleToAcceptWaitingEnrollment(), account);
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    public void leave(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if (!enrollment.isAttended()) {
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);
            event.acceptNextIfAvailable();
        }
    }

    public void acceptEnrollment(Event event, Enrollment enrollment) {
        event.accept(enrollment);
    }

    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
    }

    public void checkInEnrollment(Event event, Enrollment enrollment) {
        enrollment.attend();
    }

    public void CancelCheckInEnrollment(Event event, Enrollment enrollment) {
        enrollment.absent();
    }
}