package io.hyungkyu.app.event.infra.repository;

import io.hyungkyu.app.account.domain.entity.Account;
import io.hyungkyu.app.event.domain.entity.Enrollment;
import io.hyungkyu.app.event.domain.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);
}
