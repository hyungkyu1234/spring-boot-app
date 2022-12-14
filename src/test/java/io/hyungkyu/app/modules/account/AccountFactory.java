package io.hyungkyu.app.modules.account;

import io.hyungkyu.app.modules.account.domain.entity.Account;
import io.hyungkyu.app.modules.account.infra.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountFactory {
    @Autowired AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        return accountRepository.save(Account.with(nickname + "@example.com", nickname, "password"));
    }
}
