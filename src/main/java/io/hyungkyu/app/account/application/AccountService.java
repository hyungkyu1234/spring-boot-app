package io.hyungkyu.app.account.application;

import io.hyungkyu.app.account.domain.UserAccount;
import io.hyungkyu.app.account.domain.entity.Account;
import io.hyungkyu.app.account.domain.entity.Zone;
import io.hyungkyu.app.account.endpoint.controller.SignUpForm;
import io.hyungkyu.app.account.infra.repository.AccountRepository;
import io.hyungkyu.app.mail.EmailMessage;
import io.hyungkyu.app.mail.EmailService;
import io.hyungkyu.app.settings.controller.NotificationForm;
import io.hyungkyu.app.settings.controller.Profile;
import io.hyungkyu.app.tag.domain.entity.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public Account signUp(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendVerificationEmail(newAccount);
        return newAccount;
    }

    public Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.with(signUpForm.getEmail(), signUpForm.getNickname(), passwordEncoder.encode(signUpForm.getPassword()));
        // static 생성자를 이용해 객체를 생성함. builder를 사용할 경우 클래스 내에서 설정한 기본 값이 동작하지 않으므로 객체를 생성하는 방식으로 수정하였음.
        account.generateToken(); // 이메일 인증용 토큰 생성
        return accountRepository.save(account);
    }

    public void sendVerificationEmail(Account newAccount) {
        emailService.sendEmail(EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("Webluxible 회원 가입 인증")
                .message(String.format("/check-email-token?token=%s&email=%s", newAccount.getEmailToken(),
                        newAccount.getEmail()))
                .build());
    }

    public Account findAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(new UserAccount(account),
                account.getPassword(), Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token); // AuthenticationManager를 쓰는 방법이 정석적인 방법
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = Optional.ofNullable(accountRepository.findByEmail(username))
                .orElse(accountRepository.findByNickname(username));
        if (account == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UserAccount(account);
    }

    public void verify(Account account) {
        account.verified();
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {
        account.updateProfile(profile);
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.updatePassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotification(Account account, NotificationForm notificationForm) {
        account.updateNotification(notificationForm);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.updateNickname(nickname);
        accountRepository.save(account);
        login(account); // 중요! 로그인을 다시 호출해서 인증정보를 갱신하여 내비게이션 바에 변경된 닉네임을 표시하도록 해줌.
    }

    public void sendLoginLink(Account account) {
        account.generateToken();
        emailService.sendEmail(EmailMessage.builder()
                .to(account.getEmail())
                .subject("[Webluxible] 로그인 링크")
                .message("/login-by-email?token=" + account.getEmailToken() + "&email=" + account.getEmail())
                .build());
    }

    public Set<Tag> getTags(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getTags();
    }

    public void addTag(Account account, Tag tag) {
        accountRepository.findById(account.getId())
                .ifPresent(a -> a.getTags().add(tag));
    }

    public void removeTag(Account account, Tag tag) {
        accountRepository.findById(account.getId())
                .map(Account::getTags)
                .ifPresent(tags -> tags.remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        return accountRepository.findById(account.getId())
                .orElseThrow()
                .getZones();
    }

    public void addZone(Account account, Zone zone) {
        accountRepository.findById(account.getId())
                .ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        accountRepository.findById(account.getId())
                .ifPresent(a -> a.getZones().remove(zone));
    }
}